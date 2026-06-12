package com.freightos.fms.application.attachment;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.application.attachment.command.UploadBlAttachmentCommand;
import com.freightos.fms.application.attachment.port.in.BlAttachmentUseCase;
import com.freightos.fms.application.attachment.port.out.BlAttachmentPort;
import com.freightos.fms.application.attachment.port.out.StoragePort;
import com.freightos.fms.application.attachment.projection.BlAttachmentContent;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.attachment.entity.BlAttachment;
import com.freightos.fms.domain.attachment.enums.AttachmentBlKind;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlAttachmentService implements BlAttachmentUseCase {

    private final BlAttachmentPort blAttachmentPort;
    private final StoragePort storagePort;
    private final Clock clock;

    @Override
    public List<BlAttachment> findAttachmentsByBl(AttachmentBlKind blKind, Long blId) {
        return blAttachmentPort.findAttachmentsByBl(blKind, blId);
    }

    @Override
    @Transactional
    public Long upload(UploadBlAttachmentCommand command) {
        if (command.size() == 0) {
            throw new IllegalArgumentException("업로드 파일 크기가 0입니다. 비어 있는 파일은 업로드할 수 없습니다.");
        }

        String storageKey = command.blKind().name() + "/" + command.blId() + "/" + UUID.randomUUID();

        // 스토리지 먼저 저장 — 메타 저장 실패 시 보상 삭제
        storagePort.store(storageKey, command.content(), command.size());

        BlAttachment attachment = new BlAttachment(
                null,
                command.blKind(),
                command.blId(),
                command.originalFilename(),
                storageKey,
                command.contentType(),
                command.size(),
                command.uploadedBy(),
                LocalDateTime.now(clock)
        );

        try {
            return blAttachmentPort.saveAttachment(attachment);
        } catch (Exception e) {
            // 메타 저장 실패 시 스토리지 보상 삭제
            boolean deleted = storagePort.delete(storageKey);
            log.warn("메타 저장 실패로 스토리지 보상 삭제: storageKey={}, deleted={}", storageKey, deleted);
            throw e;
        }
    }

    @Override
    public BlAttachmentContent download(Long attachmentId) {
        BlAttachment meta = blAttachmentPort.findAttachmentById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.BL_ATTACHMENT_NOT_FOUND));

        InputStream stream;
        try {
            stream = storagePort.load(meta.getStorageKey());
        } catch (ResourceNotFoundException e) {
            log.warn("첨부파일 메타는 존재하나 파일 유실: attachmentId={}, storageKey={}", attachmentId, meta.getStorageKey());
            throw new ResourceNotFoundException(MessageCode.BL_ATTACHMENT_NOT_FOUND);
        }

        return new BlAttachmentContent(
                meta.getOriginalFilename(),
                meta.getContentType(),
                meta.getFileSize(),
                stream
        );
    }

    @Override
    @Transactional
    public void deleteAttachmentById(Long attachmentId) {
        BlAttachment meta = blAttachmentPort.findAttachmentById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.BL_ATTACHMENT_NOT_FOUND));

        blAttachmentPort.deleteAttachmentById(attachmentId);

        // 메타 트랜잭션 삭제 성공 후 스토리지 삭제 — 실패는 WARN 로그만(배치 회수)
        boolean deleted = storagePort.delete(meta.getStorageKey());
        if (!deleted) {
            log.warn("첨부파일 스토리지 삭제 실패 (고아 파일 — 배치가 회수): storageKey={}", meta.getStorageKey());
        }
    }

    @Override
    @Transactional
    public void deleteAttachmentsByBl(AttachmentBlKind blKind, Long blId) {
        List<String> keys = blAttachmentPort.deleteAttachmentsByBlAndReturnKeys(blKind, blId);

        // best-effort 스토리지 삭제 — 실패는 WARN 로그만
        for (String key : keys) {
            boolean deleted = storagePort.delete(key);
            if (!deleted) {
                log.warn("B/L cascade 삭제 중 스토리지 삭제 실패 (고아 파일 — 배치가 회수): storageKey={}", key);
            }
        }
    }
}
