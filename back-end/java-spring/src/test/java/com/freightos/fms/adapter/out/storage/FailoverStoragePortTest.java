package com.freightos.fms.adapter.out.storage;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.application.attachment.port.out.StoragePort.StoredObject;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.exception.SdkClientException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

/**
 * FailoverStoragePort 단위 테스트.
 *
 * mock 어댑터 + 소형 윈도우 실 CB(slidingWindowSize=4, minimumNumberOfCalls=2)를 사용한다.
 * CB 상태는 transitionToOpenState() 등 결정적 API로 제어한다(sleep·시간 의존 금지).
 */
@ExtendWith(MockitoExtension.class)
class FailoverStoragePortTest {

    @Mock private S3StorageAdapter s3;
    @Mock private LocalFileSystemStorageAdapter local;

    private CircuitBreaker breaker;
    private FailoverStoragePort port;

    @BeforeEach
    void setUp() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(4)
                .minimumNumberOfCalls(2)
                .failureRateThreshold(50)
                .ignoreExceptions(ResourceNotFoundException.class)
                .build();
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        breaker = registry.circuitBreaker(FailoverStoragePort.CB_NAME);
        port = new FailoverStoragePort(registry, s3, local);
    }

    // ── store: S3 성공 ───────────────────────────────────────────────

    @Test
    @DisplayName("store: S3 성공 → local 미호출")
    void store_s3Success_localNotCalled() {
        willDoNothing().given(s3).store(anyString(), any(), anyLong());

        port.store("HOUSE/1/f.pdf", new ByteArrayInputStream(new byte[0]), 0);

        then(local).should(never()).store(anyString(), any(), anyLong());
    }

    // ── store: 0바이트 실패 → 스필오버 ──────────────────────────────

    @Test
    @DisplayName("store: S3 0바이트 실패 → 로컬 스필오버")
    void store_s3ZeroBytesFailure_spillsOverToLocal() {
        willThrow(SdkClientException.builder().message("connect refused").build())
                .given(s3).store(anyString(), any(), anyLong());
        willDoNothing().given(local).store(anyString(), any(), anyLong());

        port.store("HOUSE/1/f.pdf", new ByteArrayInputStream(new byte[0]), 0);

        then(local).should().store(anyString(), any(), anyLong());
    }

    // ── store: 부분소비 실패 → 예외 전파, local 미호출 ────────────────

    @Test
    @DisplayName("store: 부분소비 실패 → 예외 전파·local 미호출")
    void store_partialConsumptionFailure_throwsWithoutLocalFallback() {
        // S3가 스트림 일부를 읽고 실패하는 상황을 시뮬레이션하는 fake stream
        InputStream partialStream = new InputStream() {
            private int callCount = 0;

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                if (callCount++ == 0) {
                    b[off] = 'X'; // 1바이트 반환 — 부분소비
                    return 1;
                }
                throw new IOException("mid-stream failure");
            }

            @Override
            public int read() throws IOException {
                return read(new byte[1], 0, 1) == -1 ? -1 : 0;
            }
        };

        // S3 store가 스트림에서 읽다가 실패하도록 구성
        willThrow(SdkClientException.builder().message("mid-transfer error").build())
                .given(s3).store(anyString(), any(), anyLong());

        // ConsumptionTrackingInputStream이 래핑되므로, s3.store 호출 시 실제 스트림 읽기 발생을
        // 시뮬레이션하기 위해 doAnswer로 처리
        org.mockito.BDDMockito.willAnswer(invocation -> {
            InputStream in = invocation.getArgument(1);
            in.read(new byte[1], 0, 1); // 1바이트 읽어 부분소비 표시
            throw SdkClientException.builder().message("mid-transfer error").build();
        }).given(s3).store(anyString(), any(), anyLong());

        assertThatThrownBy(() -> port.store("HOUSE/1/f.pdf", partialStream, 100))
                .isInstanceOf(SdkClientException.class);
        then(local).should(never()).store(anyString(), any(), anyLong());
    }

    // ── store: 반복 실패 → CB OPEN → CallNotPermitted 스필오버 ─────────

    @Test
    @DisplayName("store: 반복 실패→OPEN→CallNotPermitted 시 로컬 스필오버")
    void store_repeatedFailure_opensCircuitAndSpillsOver() {
        willThrow(SdkClientException.builder().message("connection refused").build())
                .given(s3).store(anyString(), any(), anyLong());
        willDoNothing().given(local).store(anyString(), any(), anyLong());

        // minimumNumberOfCalls=2, failureRate=50% → 2회 실패로 OPEN
        port.store("HOUSE/1/f1.pdf", new ByteArrayInputStream(new byte[0]), 0);
        port.store("HOUSE/1/f2.pdf", new ByteArrayInputStream(new byte[0]), 0);

        // CB가 OPEN 상태로 전이됐는지 확인
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // OPEN 상태에서 스필오버 확인
        port.store("HOUSE/1/f3.pdf", new ByteArrayInputStream(new byte[0]), 0);
        then(local).should(org.mockito.Mockito.atLeast(1)).store(anyString(), any(), anyLong());
    }

    // ── load: not-found → local 체인 + CB failure 카운트 0 ─────────────

    @Test
    @DisplayName("load: S3 not-found → local 체인 + CB failure 카운트 0")
    void load_s3NotFound_chainsToLocalWithoutCbFailure() {
        given(s3.load("HOUSE/1/f.pdf")).willThrow(new ResourceNotFoundException("첨부파일", "HOUSE/1/f.pdf"));
        InputStream expected = new ByteArrayInputStream("data".getBytes());
        given(local.load("HOUSE/1/f.pdf")).willReturn(expected);

        InputStream result = port.load("HOUSE/1/f.pdf");

        assertThat(result).isSameAs(expected);
        // ResourceNotFoundException은 ignoreExceptions → CB failure 미집계
        assertThat(breaker.getMetrics().getNumberOfFailedCalls()).isEqualTo(0);
    }

    // ── load: S3 장애 → local 폴백 ──────────────────────────────────

    @Test
    @DisplayName("load: S3 장애 → local 폴백")
    void load_s3Failure_fallsBackToLocal() {
        given(s3.load("HOUSE/1/f.pdf"))
                .willThrow(SdkClientException.builder().message("io error").build());
        InputStream expected = new ByteArrayInputStream("data".getBytes());
        given(local.load("HOUSE/1/f.pdf")).willReturn(expected);

        InputStream result = port.load("HOUSE/1/f.pdf");

        assertThat(result).isSameAs(expected);
    }

    // ── load: 양측 없음 → ResourceNotFoundException 전파 ─────────────

    @Test
    @DisplayName("load: S3·local 양측 없음 → ResourceNotFoundException 전파")
    void load_bothMissing_propagates404() {
        given(s3.load(anyString())).willThrow(new ResourceNotFoundException("첨부파일", "key"));
        given(local.load(anyString())).willThrow(new ResourceNotFoundException("첨부파일", "key"));

        assertThatThrownBy(() -> port.load("HOUSE/1/missing.pdf"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── load: CB OPEN → local 폴백 ───────────────────────────────────

    @Test
    @DisplayName("load: CB OPEN → local 폴백")
    void load_cbOpen_fallsBackToLocal() {
        breaker.transitionToOpenState();
        InputStream expected = new ByteArrayInputStream("data".getBytes());
        given(local.load("HOUSE/1/f.pdf")).willReturn(expected);

        InputStream result = port.load("HOUSE/1/f.pdf");

        assertThat(result).isSameAs(expected);
        then(s3).should(never()).load(anyString());
    }

    // ── delete: OR 3조합 + OPEN ──────────────────────────────────────

    @Test
    @DisplayName("delete: S3 성공·local 성공 → true")
    void delete_bothSuccess_returnsTrue() {
        given(s3.delete("k")).willReturn(true);
        given(local.delete("k")).willReturn(true);
        assertThat(port.delete("k")).isTrue();
    }

    @Test
    @DisplayName("delete: S3 실패·local 성공 → true")
    void delete_s3FailureLocalSuccess_returnsTrue() {
        given(s3.delete("k")).willReturn(false);
        given(local.delete("k")).willReturn(true);
        assertThat(port.delete("k")).isTrue();
    }

    @Test
    @DisplayName("delete: S3 성공·local 없음 → true")
    void delete_s3SuccessLocalMissing_returnsTrue() {
        given(s3.delete("k")).willReturn(true);
        given(local.delete("k")).willReturn(false);
        assertThat(port.delete("k")).isTrue();
    }

    @Test
    @DisplayName("delete: CB OPEN → s3Ok=false, local 시도, OR 결과 반환")
    void delete_cbOpen_skipsS3UsesLocalResult() {
        breaker.transitionToOpenState();
        given(local.delete("k")).willReturn(true);

        assertThat(port.delete("k")).isTrue();
        then(s3).should(never()).delete(anyString());
    }

    // ── list: 합집합 dedupe ──────────────────────────────────────────

    @Test
    @DisplayName("list: S3+local 합집합 — 동일 key는 lastModified 큰 쪽 채택")
    void list_mergesWithDeduplication() {
        Instant t1 = Instant.parse("2026-01-01T00:00:00Z");
        Instant t2 = Instant.parse("2026-01-02T00:00:00Z"); // t2 > t1
        given(s3.list()).willReturn(List.of(
                new StoredObject("HOUSE/1/a.pdf", t1),
                new StoredObject("HOUSE/2/b.pdf", t1)));
        given(local.list()).willReturn(List.of(
                new StoredObject("HOUSE/1/a.pdf", t2), // 더 최신 — 채택돼야 함
                new StoredObject("HOUSE/3/c.pdf", t1)));

        List<StoredObject> result = port.list();

        assertThat(result).hasSize(3);
        StoredObject forKey1 = result.stream()
                .filter(o -> o.key().equals("HOUSE/1/a.pdf"))
                .findFirst().orElseThrow();
        assertThat(forKey1.lastModified()).isEqualTo(t2);
    }

    // ── list: S3 장애 → 예외 단락 ────────────────────────────────────

    @Test
    @DisplayName("list: S3 장애 → 예외 단락(부분 목록 고아 오판 방지)")
    void list_s3Failure_propagatesException() {
        given(s3.list()).willThrow(SdkClientException.builder().message("io error").build());

        assertThatThrownBy(() -> port.list())
                .isInstanceOf(RuntimeException.class);
        // local.list()는 호출하지 않음
        then(local).should(never()).list();
    }

    // ── list: CB OPEN → CallNotPermittedException 단락 ──────────────

    @Test
    @DisplayName("list: CB OPEN → CallNotPermittedException 단락")
    void list_cbOpen_propagatesCallNotPermitted() {
        breaker.transitionToOpenState();

        assertThatThrownBy(() -> port.list())
                .isInstanceOf(CallNotPermittedException.class);
        then(local).should(never()).list();
    }
}
