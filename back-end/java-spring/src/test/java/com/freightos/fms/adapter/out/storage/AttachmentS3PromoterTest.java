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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AttachmentS3PromoterTest {

    @Mock private S3StorageAdapter s3;
    @Mock private LocalFileSystemStorageAdapter local;

    private CircuitBreaker breaker;
    private AttachmentS3Promoter promoter;

    @BeforeEach
    void setUp() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(4)
                .minimumNumberOfCalls(2)
                .failureRateThreshold(50)
                .build();
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        breaker = registry.circuitBreaker(FailoverStoragePort.CB_NAME);
        promoter = new AttachmentS3Promoter(registry, s3, local);
    }

    // ── 메타 교집합만 승격 ────────────────────────────────────────────

    @Test
    @DisplayName("promote: 메타에 있는 파일만 승격, 고아는 건너뜀")
    void promote_onlyMetaIntersection() {
        given(local.list()).willReturn(List.of(
                new StoredObject("HOUSE/1/meta.pdf", Instant.now()),
                new StoredObject("HOUSE/2/orphan.pdf", Instant.now())));
        given(local.sizeOf("HOUSE/1/meta.pdf")).willReturn(10L);
        given(local.load("HOUSE/1/meta.pdf")).willReturn(new ByteArrayInputStream(new byte[10]));
        willDoNothing().given(s3).store(anyString(), any(), anyLong());
        given(local.delete("HOUSE/1/meta.pdf")).willReturn(true);

        int count = promoter.promote(Set.of("HOUSE/1/meta.pdf"));

        assertThat(count).isEqualTo(1);
        then(s3).should().store(anyString(), any(InputStream.class), anyLong());
        then(local).should(never()).sizeOf("HOUSE/2/orphan.pdf");
    }

    // ── 성공 후 local.delete 호출 ────────────────────────────────────

    @Test
    @DisplayName("promote: 승격 성공 후 로컬 삭제 호출")
    void promote_success_deletesLocal() {
        given(local.list()).willReturn(List.of(
                new StoredObject("HOUSE/1/f.pdf", Instant.now())));
        given(local.sizeOf("HOUSE/1/f.pdf")).willReturn(5L);
        given(local.load("HOUSE/1/f.pdf")).willReturn(new ByteArrayInputStream(new byte[5]));
        willDoNothing().given(s3).store(anyString(), any(), anyLong());
        given(local.delete("HOUSE/1/f.pdf")).willReturn(true);

        int count = promoter.promote(Set.of("HOUSE/1/f.pdf"));

        assertThat(count).isEqualTo(1);
        then(local).should().delete("HOUSE/1/f.pdf");
    }

    // ── local.delete false → WARN, 카운트 유지 ──────────────────────

    @Test
    @DisplayName("promote: 로컬 삭제 실패(false) → WARN만, 카운트 유지(다음 주기 멱등 재승격)")
    void promote_localDeleteFails_countKept() {
        given(local.list()).willReturn(List.of(
                new StoredObject("HOUSE/1/f.pdf", Instant.now())));
        given(local.sizeOf("HOUSE/1/f.pdf")).willReturn(5L);
        given(local.load("HOUSE/1/f.pdf")).willReturn(new ByteArrayInputStream(new byte[5]));
        willDoNothing().given(s3).store(anyString(), any(), anyLong());
        given(local.delete("HOUSE/1/f.pdf")).willReturn(false);

        int count = promoter.promote(Set.of("HOUSE/1/f.pdf"));

        // S3 store는 성공했으므로 promoted++ → 카운트 유지
        assertThat(count).isEqualTo(1);
    }

    // ── CB OPEN → 전체 skip ──────────────────────────────────────────

    @Test
    @DisplayName("promote: CB OPEN 상태에서 전체 건너뜀(promoted=0)")
    void promote_cbOpen_skipsAll() {
        breaker.transitionToOpenState();

        int count = promoter.promote(Set.of("HOUSE/1/f.pdf"));

        assertThat(count).isEqualTo(0);
        then(local).should(never()).list();
        then(s3).should(never()).store(anyString(), any(), anyLong());
    }

    // ── 도중 CallNotPermittedException → 중단 ──────────────────────

    @Test
    @DisplayName("promote: 승격 도중 CallNotPermittedException → 1건 완료 후 잔여 중단")
    void promote_callNotPermittedDuringLoop_breaksLoop() {
        // CB는 CLOSED 유지(선두 가드 통과), 2개 파일 중 1번째는 성공, 2번째 s3.store에서 CallNotPermitted
        given(local.list()).willReturn(List.of(
                new StoredObject("HOUSE/1/first.pdf", Instant.parse("2026-01-01T00:00:00Z")),
                new StoredObject("HOUSE/2/second.pdf", Instant.parse("2026-01-02T00:00:00Z"))));
        given(local.sizeOf("HOUSE/1/first.pdf")).willReturn(5L);
        given(local.load("HOUSE/1/first.pdf")).willReturn(new ByteArrayInputStream(new byte[5]));
        given(local.delete("HOUSE/1/first.pdf")).willReturn(true);
        given(local.sizeOf("HOUSE/2/second.pdf")).willReturn(5L);
        given(local.load("HOUSE/2/second.pdf")).willReturn(new ByteArrayInputStream(new byte[5]));

        // 1번째 호출 성공, 2번째 호출 시 CallNotPermittedException — 루프 break 검증
        willAnswer(invocation -> null)
                .willThrow(CallNotPermittedException.createCallNotPermittedException(breaker))
                .given(s3).store(anyString(), any(), anyLong());

        int count = promoter.promote(Set.of("HOUSE/1/first.pdf", "HOUSE/2/second.pdf"));

        assertThat(count).isEqualTo(1);
        then(local).should(never()).delete("HOUSE/2/second.pdf");
    }

    // ── 개별 실패 → WARN 후 계속 ─────────────────────────────────────

    @Test
    @DisplayName("promote: 개별 파일 S3 실패 → WARN 후 다음 파일 계속")
    void promote_individualFailure_continuesLoop() {
        given(local.list()).willReturn(List.of(
                new StoredObject("HOUSE/1/fail.pdf", Instant.now()),
                new StoredObject("HOUSE/2/ok.pdf", Instant.now())));
        // 첫 번째 파일: sizeOf 성공, load 성공, s3.store 실패
        given(local.sizeOf("HOUSE/1/fail.pdf")).willReturn(5L);
        given(local.load("HOUSE/1/fail.pdf")).willReturn(new ByteArrayInputStream(new byte[5]));
        willThrow(new RuntimeException("s3 error")).given(s3).store(
                anyString(), any(), anyLong());

        // minimumNumberOfCalls=2이고 두 번 실패여야 OPEN이므로, 하나 실패 후에도 두 번째 파일 시도
        // 두 번째 파일: 실패로 인해 sizeOf 전에 CB가 OPEN되지 않으므로 계속 진행
        // 두 번째 파일은 sizeOf에서 멈추므로 willReturn 추가
        given(local.sizeOf("HOUSE/2/ok.pdf")).willReturn(5L);
        given(local.load("HOUSE/2/ok.pdf")).willReturn(new ByteArrayInputStream(new byte[5]));
        // 두 번째 파일 s3.store도 실패 (minimumNumberOfCalls=2, 2번 실패 → OPEN)
        // 두 번 모두 RuntimeException 이므로 willThrow(any RuntimeException) 이미 설정됨

        int count = promoter.promote(Set.of("HOUSE/1/fail.pdf", "HOUSE/2/ok.pdf"));

        // 모두 실패했으므로 promoted=0
        assertThat(count).isEqualTo(0);
        // 두 파일 모두 시도했는지 확인(loop 계속)
        then(local).should().sizeOf("HOUSE/2/ok.pdf");
    }

    // ── ResourceNotFoundException → continue ─────────────────────────

    @Test
    @DisplayName("promote: 승격 중 ResourceNotFoundException → continue(경합 삭제 무해)")
    void promote_resourceNotFoundDuringPromotion_continues() {
        given(local.list()).willReturn(List.of(
                new StoredObject("HOUSE/1/deleted.pdf", Instant.now()),
                new StoredObject("HOUSE/2/ok.pdf", Instant.now())));
        given(local.sizeOf("HOUSE/1/deleted.pdf")).willReturn(5L);
        given(local.load("HOUSE/1/deleted.pdf"))
                .willThrow(new ResourceNotFoundException("첨부파일", "HOUSE/1/deleted.pdf"));
        given(local.sizeOf("HOUSE/2/ok.pdf")).willReturn(3L);
        given(local.load("HOUSE/2/ok.pdf")).willReturn(new ByteArrayInputStream(new byte[3]));
        willDoNothing().given(s3).store(anyString(), any(), anyLong());
        given(local.delete("HOUSE/2/ok.pdf")).willReturn(true);

        int count = promoter.promote(Set.of("HOUSE/1/deleted.pdf", "HOUSE/2/ok.pdf"));

        assertThat(count).isEqualTo(1);
    }
}
