package com.freightos.fms.adapter.in.web.attachment;

import com.freightos.fms.adapter.in.web.attachment.dto.BlAttachmentResponse;
import com.freightos.fms.application.attachment.command.UploadBlAttachmentCommand;
import com.freightos.fms.domain.attachment.entity.BlAttachment;
import com.freightos.fms.domain.attachment.enums.AttachmentBlKind;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * Adapter(in) — Request → Command 변환, Domain → Response DTO 변환 담당.
 * Controller에서 domain.* 직접 참조를 차단하기 위한 경유 계층.
 */
@Component
public class BlAttachmentAssembler {

    /**
     * blKind 문자열을 도메인 enum으로 변환한다.
     * 잘못된 값이면 IllegalArgumentException — GlobalExceptionHandler가 400으로 처리.
     */
    public AttachmentBlKind toBlKind(String blKind) {
        return AttachmentBlKind.valueOf(blKind);
    }

    public UploadBlAttachmentCommand toUploadCommand(
            MultipartFile file,
            String blKind,
            Long blId,
            String uploadedBy) {
        try {
            return new UploadBlAttachmentCommand(
                    AttachmentBlKind.valueOf(blKind),
                    blId,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    file.getInputStream(),
                    uploadedBy
            );
        } catch (IOException e) {
            throw new UncheckedIOException("첨부파일 스트림 읽기 실패: " + file.getOriginalFilename(), e);
        }
    }

    public BlAttachmentResponse toResponse(BlAttachment attachment) {
        return new BlAttachmentResponse(
                attachment.getId(),
                attachment.getBlKind().name(),
                attachment.getBlId(),
                attachment.getOriginalFilename(),
                attachment.getContentType(),
                attachment.getFileSize(),
                attachment.getUploadedBy(),
                attachment.getCreatedAt()
        );
    }

    public List<BlAttachmentResponse> toResponseList(List<BlAttachment> attachments) {
        return attachments.stream().map(this::toResponse).toList();
    }
}
