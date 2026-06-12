package com.freightos.fms.adapter.out.storage;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.application.attachment.port.out.StoragePort;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * S3 우선 + 로컬 폴백을 합성하는 StoragePort 구현체.
 * 프로그래매틱 CB(annotation 미사용) — AOP self-invocation 문제를 회피하고
 * ignoreExceptions 등 폴백 예외 집합을 정밀 제어한다.
 *
 * <pre>
 * catch 순서 절대 유지(전부 RuntimeException 하위이므로 순서가 분기 의미를 결정):
 *   CallNotPermittedException → ResourceNotFoundException → RuntimeException
 * </pre>
 *
 * 선례: PmsMartCircuitGuard(java-spring-pms 모듈)
 */
@Slf4j
public class FailoverStoragePort implements StoragePort {

    static final String CB_NAME = "fmsS3";

    private final CircuitBreaker breaker;
    private final S3StorageAdapter s3;
    private final LocalFileSystemStorageAdapter local;

    public FailoverStoragePort(
            CircuitBreakerRegistry registry,
            S3StorageAdapter s3,
            LocalFileSystemStorageAdapter local) {
        this.s3 = s3;
        this.local = local;
        this.breaker = registry.circuitBreaker(CB_NAME);

        // yml 인스턴스명 오타 시 디폴트 설정으로 조용히 생성되는 함정 탐지
        log.info("FailoverStoragePort 초기화 — slidingWindowSize={}, minimumNumberOfCalls={}, failureRateThreshold={}",
                breaker.getCircuitBreakerConfig().getSlidingWindowSize(),
                breaker.getCircuitBreakerConfig().getMinimumNumberOfCalls(),
                breaker.getCircuitBreakerConfig().getFailureRateThreshold());

        breaker.getEventPublisher().onStateTransition(event ->
                log.warn("fmsS3 CB 상태 전이: {}→{}",
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState()));
    }

    /**
     * S3 저장 시도 → 실패 시 로컬 스필오버.
     *
     * 부분소비 가드: S3가 스트림 일부를 읽고 실패하면 같은 스트림으로 폴백 시 잘린 파일이 저장된다.
     * bytesRead > 0 이면 스필오버를 거부하고 예외를 전파한다.
     * 주요 장애 모드(connect 거부·DNS 실패)는 0바이트 실패이므로 폴백 가능하며,
     * CB OPEN은 호출 전에 단락되어 tracked 스트림이 0바이트 상태임이 보장된다.
     * 반복 실패는 CB 트립 후 CallNotPermittedException 경로로 수렴해 전량 0바이트 스필오버가 된다.
     */
    @Override
    public void store(String storageKey, InputStream content, long contentLength) {
        ConsumptionTrackingInputStream tracked = new ConsumptionTrackingInputStream(content);
        try {
            breaker.executeRunnable(() -> s3.store(storageKey, tracked, contentLength));
        } catch (CallNotPermittedException e) {
            log.warn("fmsS3 차단기 OPEN — 로컬 스필오버: storageKey={}", storageKey);
            local.store(storageKey, tracked, contentLength);
        } catch (RuntimeException fault) {
            if (tracked.bytesRead() > 0) {
                // 스트림 일부 소비 후 실패 — 잘린 파일 폴백 금지, 예외 전파
                log.error("S3 저장 중 부분소비 실패 — 스필오버 불가(잘린 파일 방지): storageKey={}, bytesRead={}",
                        storageKey, tracked.bytesRead());
                throw fault;
            }
            log.warn("S3 저장 실패 — 로컬 스필오버: storageKey={}, error={}", storageKey, fault.toString());
            local.store(storageKey, tracked, contentLength);
        }
    }

    /**
     * S3 로드 시도 → 실패 시 로컬 체인.
     *
     * ResourceNotFoundException은 CB ignoreExceptions에 등록되어 failure 카운트에 집계되지 않는다.
     * not-found는 정상 흐름(사용자 삭제, 미업로드 등)으로 CB 트립 방지가 목적이다.
     * 양측에 모두 없으면 local.load()가 동일 ResourceNotFoundException을 던져 404로 전파된다.
     */
    @Override
    public InputStream load(String storageKey) {
        try {
            return breaker.executeSupplier(() -> s3.load(storageKey));
        } catch (ResourceNotFoundException e) {
            // S3에 없음 → 로컬 체인 시도(CB 미집계)
            return local.load(storageKey);
        } catch (CallNotPermittedException e) {
            log.warn("fmsS3 차단기 OPEN — 로컬 폴백 로드: storageKey={}", storageKey);
            return local.load(storageKey);
        } catch (RuntimeException e) {
            log.warn("S3 로드 실패 — 로컬 폴백: storageKey={}, error={}", storageKey, e.toString());
            return local.load(storageKey);
        }
    }

    /**
     * S3와 로컬 양측에서 삭제를 시도하고 어느 한 쪽이라도 성공하면 true를 반환한다.
     * CB 단락 또는 S3 예외 시 s3Ok=false로 처리하며 예외를 전파하지 않는다(포트 계약).
     * 삭제 실패분은 정리 배치가 다음 주기에 회수한다.
     */
    @Override
    public boolean delete(String storageKey) {
        boolean s3Ok = deleteFromS3(storageKey);
        boolean localOk = local.delete(storageKey);
        return s3Ok || localOk;
    }

    /**
     * S3와 로컬 목록의 합집합을 반환한다.
     * 동일 key가 양측에 있으면 lastModified가 큰(더 최신) StoredObject를 채택한다.
     *
     * S3 list 실패 시 부분 목록으로 고아를 오판하는 사고를 방지하기 위해 예외를 그대로 단락한다.
     * 선례: feedback_partial_index_ready_gate — 부분 인덱스가 정답 행세하면 실 데이터 손실 위험.
     */
    @Override
    public List<StoredObject> list() {
        // S3 list 실패 또는 OPEN이면 예외 그대로 단락(부분 목록으로 고아 오판 금지)
        List<StoredObject> s3Objects = breaker.executeSupplier(s3::list);
        List<StoredObject> localObjects = local.list();

        // key 기준 dedupe 합집합 — lastModified 큰 쪽 채택
        Map<String, StoredObject> merged = new HashMap<>();
        for (StoredObject obj : s3Objects) {
            merged.put(obj.key(), obj);
        }
        for (StoredObject obj : localObjects) {
            merged.merge(obj.key(), obj, (existing, incoming) ->
                    incoming.lastModified().isAfter(existing.lastModified()) ? incoming : existing);
        }
        return new ArrayList<>(merged.values());
    }

    private boolean deleteFromS3(String storageKey) {
        try {
            return breaker.executeSupplier(() -> s3.delete(storageKey));
        } catch (CallNotPermittedException e) {
            log.warn("fmsS3 차단기 OPEN — S3 삭제 건너뜀(로컬만 시도): storageKey={}", storageKey);
            return false;
        } catch (RuntimeException e) {
            log.warn("S3 삭제 실패: storageKey={}, error={}", storageKey, e.toString());
            return false;
        }
    }
}
