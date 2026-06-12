package com.freightos.fms.adapter.out.storage;

import com.freightos.fms.application.attachment.port.out.BlAttachmentPort;
import com.freightos.fms.application.attachment.port.out.StoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AttachmentCleanupSchedulerTest {

    @Mock private StoragePort storagePort;
    @Mock private BlAttachmentPort blAttachmentPort;
    @Mock private AttachmentS3Promoter promoter;
    @Mock private ObjectProvider<AttachmentS3Promoter> promoterProvider;

    private AttachmentCleanupProperties props;

    // 고정 시각: 2026-01-03T00:00:00Z (grace=24h 기준 48h 전 파일은 삭제 대상)
    private static final Instant NOW = Instant.parse("2026-01-03T00:00:00Z");
    private final Clock fixedClock = Clock.fixed(NOW, ZoneId.of("UTC"));

    @BeforeEach
    void setUp() {
        props = new AttachmentCleanupProperties();
        props.setGraceHours(24);
    }

    private AttachmentCleanupScheduler scheduler(boolean withPromoter) {
        if (withPromoter) {
            given(promoterProvider.getIfAvailable()).willReturn(promoter);
        } else {
            given(promoterProvider.getIfAvailable()).willReturn(null);
        }
        return new AttachmentCleanupScheduler(storagePort, blAttachmentPort, fixedClock, props, promoterProvider);
    }

    // ── 기존 케이스 (생성자 5번째 인자 추가 — 기존 검증 무영향) ──────────

    @Test
    @DisplayName("cleanOrphanFiles: 메타 없고 grace 경과 → 삭제")
    void cleanOrphan_graceExpired_deletesFile() {
        Instant orphanTime = NOW.minusSeconds(49 * 3600); // 49h 전 — grace 24h 경과
        given(blAttachmentPort.findAllStorageKeys()).willReturn(List.of());
        given(storagePort.list()).willReturn(List.of(
                new StoragePort.StoredObject("HOUSE/1/orphan", orphanTime)));
        given(storagePort.delete("HOUSE/1/orphan")).willReturn(true);

        scheduler(false).cleanOrphanFiles();

        then(storagePort).should().delete("HOUSE/1/orphan");
    }

    @Test
    @DisplayName("cleanOrphanFiles: 메타 없지만 grace 미경과 → 삭제 안 함")
    void cleanOrphan_graceNotExpired_doesNotDelete() {
        Instant recentTime = NOW.minusSeconds(12 * 3600); // 12h 전 — grace 미경과
        given(blAttachmentPort.findAllStorageKeys()).willReturn(List.of());
        given(storagePort.list()).willReturn(List.of(
                new StoragePort.StoredObject("HOUSE/1/recent", recentTime)));

        scheduler(false).cleanOrphanFiles();

        then(storagePort).should(never()).delete(anyString());
    }

    @Test
    @DisplayName("cleanOrphanFiles: 메타에 있는 파일 → 삭제 안 함")
    void cleanOrphan_fileInMeta_doesNotDelete() {
        Instant oldTime = NOW.minusSeconds(72 * 3600);
        given(blAttachmentPort.findAllStorageKeys()).willReturn(List.of("HOUSE/1/registered"));
        given(storagePort.list()).willReturn(List.of(
                new StoragePort.StoredObject("HOUSE/1/registered", oldTime)));

        scheduler(false).cleanOrphanFiles();

        then(storagePort).should(never()).delete(anyString());
    }

    @Test
    @DisplayName("cleanOrphanFiles: dangling 메타(파일 없음) → WARN만, 삭제 미호출")
    void cleanOrphan_danglingMeta_noDelete() {
        // 메타에는 있지만 스토리지에는 없는 경우
        given(blAttachmentPort.findAllStorageKeys()).willReturn(List.of("HOUSE/1/dangling"));
        given(storagePort.list()).willReturn(List.of());

        scheduler(false).cleanOrphanFiles();

        then(storagePort).should(never()).delete(anyString());
    }

    // ── 신규 케이스 ──────────────────────────────────────────────────

    @Test
    @DisplayName("cleanOrphanFiles: promote가 storagePort.list()보다 선행 호출")
    void cleanOrphanFiles_promote_beforeList() {
        given(blAttachmentPort.findAllStorageKeys()).willReturn(List.of());
        given(storagePort.list()).willReturn(List.of());
        given(promoter.promote(any(Set.class))).willReturn(0);

        scheduler(true).cleanOrphanFiles();

        InOrder order = inOrder(promoter, storagePort);
        order.verify(promoter).promote(any(Set.class));
        order.verify(storagePort).list();
    }

    @Test
    @DisplayName("cleanOrphanFiles: promote 예외 → sweep 계속(delete 호출됨)")
    void cleanOrphanFiles_promoteThrows_sweepContinues() {
        Instant orphanTime = NOW.minusSeconds(49 * 3600);
        given(blAttachmentPort.findAllStorageKeys()).willReturn(List.of());
        given(storagePort.list()).willReturn(List.of(
                new StoragePort.StoredObject("HOUSE/1/orphan", orphanTime)));
        given(storagePort.delete("HOUSE/1/orphan")).willReturn(true);
        willThrow(new RuntimeException("promote failed")).given(promoter).promote(any(Set.class));

        scheduler(true).cleanOrphanFiles();

        // promote 예외에도 불구하고 sweep은 계속됨
        then(storagePort).should().delete("HOUSE/1/orphan");
    }

    @Test
    @DisplayName("cleanOrphanFiles: storagePort.list() 예외 → run 전체 건너뜀(delete 미호출)")
    void cleanOrphanFiles_listThrows_skipsDeleteEntireRun() {
        given(blAttachmentPort.findAllStorageKeys()).willReturn(List.of());
        given(storagePort.list()).willThrow(new RuntimeException("list failed"));
        given(promoter.promote(any(Set.class))).willReturn(0);

        scheduler(true).cleanOrphanFiles();

        then(storagePort).should(never()).delete(anyString());
    }

    @Test
    @DisplayName("cleanOrphanFiles: promoter 부재(local 모드) → 기존 동작과 동일")
    void cleanOrphanFiles_noPromoter_sameAsOriginal() {
        Instant orphanTime = NOW.minusSeconds(49 * 3600);
        given(blAttachmentPort.findAllStorageKeys()).willReturn(List.of());
        given(storagePort.list()).willReturn(List.of(
                new StoragePort.StoredObject("HOUSE/1/orphan", orphanTime)));
        given(storagePort.delete("HOUSE/1/orphan")).willReturn(true);

        scheduler(false).cleanOrphanFiles();

        // promoter 없어도 sweep은 정상 동작
        then(storagePort).should().delete("HOUSE/1/orphan");
        then(promoter).should(never()).promote(any(Set.class));
    }
}
