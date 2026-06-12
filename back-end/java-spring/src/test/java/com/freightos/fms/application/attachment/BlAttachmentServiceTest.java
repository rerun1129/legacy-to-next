package com.freightos.fms.application.attachment;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.application.attachment.command.UploadBlAttachmentCommand;
import com.freightos.fms.application.attachment.port.out.BlAttachmentPort;
import com.freightos.fms.application.attachment.port.out.StoragePort;
import com.freightos.fms.application.attachment.projection.BlAttachmentContent;
import com.freightos.fms.domain.attachment.entity.BlAttachment;
import com.freightos.fms.domain.attachment.enums.AttachmentBlKind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BlAttachmentServiceTest {

    @Mock private BlAttachmentPort blAttachmentPort;
    @Mock private StoragePort storagePort;

    // Clock.fixed으로 결정적 시간 보장 (ARCH5 · T1 준수)
    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneId.of("UTC"));

    // ── upload ────────────────────────────────────────────────────

    @Test
    @DisplayName("upload: 빈 파일(size=0) → IllegalArgumentException")
    void upload_emptyFile_throwsIllegalArgument() {
        BlAttachmentService svc = new BlAttachmentService(blAttachmentPort, storagePort, fixedClock);
        UploadBlAttachmentCommand cmd = new UploadBlAttachmentCommand(
                AttachmentBlKind.HOUSE, 1L, "empty.txt", "text/plain",
                0L, new ByteArrayInputStream(new byte[0]), "user1");

        assertThatThrownBy(() -> svc.upload(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("0");

        then(storagePort).should(never()).store(any(), any(), any(Long.class));
    }

    @Test
    @DisplayName("upload: 정상 파일 → storage store → metaSave → id 반환")
    void upload_validFile_storesAndSavesMeta() {
        BlAttachmentService svc = new BlAttachmentService(blAttachmentPort, storagePort, fixedClock);
        byte[] bytes = "pdf-content".getBytes();
        UploadBlAttachmentCommand cmd = new UploadBlAttachmentCommand(
                AttachmentBlKind.HOUSE, 1L, "test.pdf", "application/pdf",
                bytes.length, new ByteArrayInputStream(bytes), "user1");
        given(blAttachmentPort.saveAttachment(any())).willReturn(99L);

        Long id = svc.upload(cmd);

        assertThat(id).isEqualTo(99L);
        then(storagePort).should().store(anyString(), any(), any(Long.class));
        then(blAttachmentPort).should().saveAttachment(any());
    }

    @Test
    @DisplayName("upload: 메타 저장 실패 시 보상 삭제 후 예외 rethrow")
    void upload_metaSaveFails_compensatesStorageDelete() {
        BlAttachmentService svc = new BlAttachmentService(blAttachmentPort, storagePort, fixedClock);
        byte[] bytes = "pdf-content".getBytes();
        UploadBlAttachmentCommand cmd = new UploadBlAttachmentCommand(
                AttachmentBlKind.HOUSE, 1L, "test.pdf", "application/pdf",
                bytes.length, new ByteArrayInputStream(bytes), "user1");
        given(blAttachmentPort.saveAttachment(any())).willThrow(new RuntimeException("DB 오류"));
        given(storagePort.delete(anyString())).willReturn(true);

        assertThatThrownBy(() -> svc.upload(cmd))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB 오류");

        then(storagePort).should().delete(anyString());
    }

    // ── download ─────────────────────────────────────────────────

    @Test
    @DisplayName("download: 메타 없음 → ResourceNotFoundException(404)")
    void download_notFound_throwsResourceNotFoundException() {
        BlAttachmentService svc = new BlAttachmentService(blAttachmentPort, storagePort, fixedClock);
        given(blAttachmentPort.findAttachmentById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> svc.download(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("download: 파일 유실(storage.load 예외) → ResourceNotFoundException(404)")
    void download_fileNotFound_throwsResourceNotFoundException() {
        BlAttachmentService svc = new BlAttachmentService(blAttachmentPort, storagePort, fixedClock);
        BlAttachment meta = new BlAttachment(1L, AttachmentBlKind.HOUSE, 1L, "test.pdf",
                "HOUSE/1/uuid", "application/pdf", 1024L, "user1", LocalDateTime.now(fixedClock));
        given(blAttachmentPort.findAttachmentById(1L)).willReturn(Optional.of(meta));
        given(storagePort.load("HOUSE/1/uuid"))
                .willThrow(new ResourceNotFoundException("첨부파일", "HOUSE/1/uuid"));

        assertThatThrownBy(() -> svc.download(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("download: 정상 → BlAttachmentContent 반환")
    void download_happyPath_returnsContent() {
        BlAttachmentService svc = new BlAttachmentService(blAttachmentPort, storagePort, fixedClock);
        BlAttachment meta = new BlAttachment(1L, AttachmentBlKind.HOUSE, 1L, "test.pdf",
                "HOUSE/1/uuid", "application/pdf", 1024L, "user1", LocalDateTime.now(fixedClock));
        given(blAttachmentPort.findAttachmentById(1L)).willReturn(Optional.of(meta));
        given(storagePort.load("HOUSE/1/uuid")).willReturn(new ByteArrayInputStream("content".getBytes()));

        BlAttachmentContent content = svc.download(1L);

        assertThat(content.originalFilename()).isEqualTo("test.pdf");
        assertThat(content.contentType()).isEqualTo("application/pdf");
        assertThat(content.fileSize()).isEqualTo(1024L);
    }

    // ── deleteByBl ────────────────────────────────────────────────

    @Test
    @DisplayName("deleteAttachmentsByBl: 메타 삭제 후 storage best-effort 삭제")
    void deleteAttachmentsByBl_deletesMetaAndStorageBestEffort() {
        BlAttachmentService svc = new BlAttachmentService(blAttachmentPort, storagePort, fixedClock);
        given(blAttachmentPort.deleteAttachmentsByBlAndReturnKeys(AttachmentBlKind.HOUSE, 1L))
                .willReturn(List.of("HOUSE/1/uuid1", "HOUSE/1/uuid2"));
        given(storagePort.delete(anyString())).willReturn(true);

        svc.deleteAttachmentsByBl(AttachmentBlKind.HOUSE, 1L);

        then(storagePort).should().delete("HOUSE/1/uuid1");
        then(storagePort).should().delete("HOUSE/1/uuid2");
    }

    @Test
    @DisplayName("deleteAttachmentsByBl: storage 삭제 실패(false) → 예외 미발생, WARN만")
    void deleteAttachmentsByBl_storageFails_swallows() {
        BlAttachmentService svc = new BlAttachmentService(blAttachmentPort, storagePort, fixedClock);
        given(blAttachmentPort.deleteAttachmentsByBlAndReturnKeys(AttachmentBlKind.HOUSE, 1L))
                .willReturn(List.of("HOUSE/1/uuid1"));
        given(storagePort.delete(anyString())).willReturn(false);

        // 예외 미발생 확인
        svc.deleteAttachmentsByBl(AttachmentBlKind.HOUSE, 1L);
    }
}
