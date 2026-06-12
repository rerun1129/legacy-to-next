package com.freightos.fms.adapter.out.storage;

import com.freightos.common.exception.ResourceNotFoundException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * 로컬 스필오버 파일을 S3로 자동 승격하는 컴포넌트.
 * AttachmentCleanupScheduler.run() 선두에서 호출되어
 * S3 장애 회복 후 스필오버된 파일을 S3로 이관한다.
 *
 * 초기 마이그레이션 경로: 기존 로컬 파일(메타 보유)을 첫 정상 주기에 자동 S3 승격한다.
 *
 * ⚠️ cleanup.enabled=false 면 승격도 정지(스케줄러 게이트 동승 — 스케줄러가 실행되지 않음).
 */
@Slf4j
public class AttachmentS3Promoter {

    private final CircuitBreaker breaker;
    private final S3StorageAdapter s3;
    private final LocalFileSystemStorageAdapter local;

    public AttachmentS3Promoter(
            CircuitBreakerRegistry registry,
            S3StorageAdapter s3,
            LocalFileSystemStorageAdapter local) {
        this.s3 = s3;
        this.local = local;
        this.breaker = registry.circuitBreaker(FailoverStoragePort.CB_NAME);
    }

    /**
     * 로컬 스토리지에 있고 metaKeys에 포함된 파일을 S3로 승격한다.
     * 로컬 고아(메타 없는 파일)는 sweep 몫이므로 건너뛴다.
     *
     * @param metaKeys 메타DB에 등록된 storageKey 집합
     * @return 이번 주기에 성공 승격된 파일 수
     */
    public int promote(Set<String> metaKeys) {
        CircuitBreaker.State state = breaker.getState();
        if (state == CircuitBreaker.State.OPEN || state == CircuitBreaker.State.FORCED_OPEN) {
            log.warn("fmsS3 CB {} — 승격 주기 건너뜀", state);
            return 0;
        }

        int promoted = 0;
        for (StoragePort.StoredObject obj : local.list()) {
            if (!metaKeys.contains(obj.key())) {
                continue; // 로컬 고아는 sweep 몫
            }
            try {
                long size = local.sizeOf(obj.key());
                try (InputStream in = local.load(obj.key())) {
                    final String key = obj.key();
                    breaker.executeRunnable(() -> s3.store(key, in, size));
                }
                if (!local.delete(obj.key())) {
                    log.warn("승격 후 로컬 삭제 실패(다음 주기 멱등 재승격): storageKey={}", obj.key());
                }
                promoted++;
            } catch (CallNotPermittedException e) {
                log.warn("승격 중 fmsS3 CB OPEN — 잔여 중단: storageKey={}", obj.key());
                break;
            } catch (ResourceNotFoundException e) {
                // 승격 중 사용자 삭제 경합 — 무해, 다음 항목 계속
                continue;
            } catch (RuntimeException | IOException e) {
                log.warn("승격 실패(다음 주기 재시도): storageKey={}, error={}", obj.key(), e.toString());
            }
        }
        if (promoted > 0) {
            log.info("S3 승격 완료: promoted={}건", promoted);
        }
        return promoted;
    }
}
