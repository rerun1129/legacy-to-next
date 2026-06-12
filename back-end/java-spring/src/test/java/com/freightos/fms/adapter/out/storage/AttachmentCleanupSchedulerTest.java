package com.freightos.fms.adapter.out.storage;

import com.freightos.fms.application.attachment.port.out.BlAttachmentPort;
import com.freightos.fms.application.attachment.port.out.StoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AttachmentCleanupSchedulerTest {

    @Mock private StoragePort storagePort;
    @Mock private BlAttachmentPort blAttachmentPort;

    private AttachmentCleanupProperties props;

    // 고정 시각: 2026-01-03T00:00:00Z (grace=24h 기준 48h 전 파일은 삭제 대상)
    private static final Instant NOW = Instant.parse("2026-01-03T00:00:00Z");
    private final Clock fixedClock = Clock.fixed(NOW, ZoneId.of("UTC"));

    @BeforeEach
    void setUp() {
        props = new AttachmentCleanupProperties();
        props.setGraceHours(24);
    }

    // ── 고아 파일 삭제 ──────────────────────────────────────────

    @Test
    @DisplayName("cleanOrphanFiles: 메타 없고 grace 경과 → 삭제")
    void cleanOrphan_graceExpired_deletesFile() {
        Instant orphanTime = NOW.minusSeconds(49 * 3600); // 49h 전 — grace 24h 경과
        given(blAttachmentPort.findAllStorageKeys()).willReturn(List.of());
        given(storagePort.list()).willReturn(List.of(
                new StoragePort.StoredObject("HOUSE/1/orphan", orphanTime)));
        given(storagePort.delete("HOUSE/1/orphan")).willReturn(true);

        AttachmentCleanupScheduler scheduler = new AttachmentCleanupScheduler(
                storagePort, blAttachmentPort, fixedClock, props);
        scheduler.cleanOrphanFiles();

        then(storagePort).should().delete("HOUSE/1/orphan");
    }

    @Test
    @DisplayName("cleanOrphanFiles: 메타 없지만 grace 미경과 → 삭제 안 함")
    void cleanOrphan_graceNotExpired_doesNotDelete() {
        Instant recentTime = NOW.minusSeconds(12 * 3600); // 12h 전 — grace 미경과
        given(blAttachmentPort.findAllStorageKeys()).willReturn(List.of());
        given(storagePort.list()).willReturn(List.of(
                new StoragePort.StoredObject("HOUSE/1/recent", recentTime)));

        AttachmentCleanupScheduler scheduler = new AttachmentCleanupScheduler(
                storagePort, blAttachmentPort, fixedClock, props);
        scheduler.cleanOrphanFiles();

        then(storagePort).should(never()).delete(anyString());
    }

    @Test
    @DisplayName("cleanOrphanFiles: 메타에 있는 파일 → 삭제 안 함")
    void cleanOrphan_fileInMeta_doesNotDelete() {
        Instant oldTime = NOW.minusSeconds(72 * 3600);
        given(blAttachmentPort.findAllStorageKeys()).willReturn(List.of("HOUSE/1/registered"));
        given(storagePort.list()).willReturn(List.of(
                new StoragePort.StoredObject("HOUSE/1/registered", oldTime)));

        AttachmentCleanupScheduler scheduler = new AttachmentCleanupScheduler(
                storagePort, blAttachmentPort, fixedClock, props);
        scheduler.cleanOrphanFiles();

        then(storagePort).should(never()).delete(anyString());
    }

    @Test
    @DisplayName("cleanOrphanFiles: dangling 메타(파일 없음) → WARN만, 삭제 미호출")
    void cleanOrphan_danglingMeta_noDelete() {
        // 메타에는 있지만 스토리지에는 없는 경우
        given(blAttachmentPort.findAllStorageKeys()).willReturn(List.of("HOUSE/1/dangling"));
        given(storagePort.list()).willReturn(List.of());

        AttachmentCleanupScheduler scheduler = new AttachmentCleanupScheduler(
                storagePort, blAttachmentPort, fixedClock, props);
        scheduler.cleanOrphanFiles();

        then(storagePort).should(never()).delete(anyString());
    }
}
