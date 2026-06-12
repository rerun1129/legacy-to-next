package com.freightos.fms.application.attachment.port.in;

import com.freightos.fms.application.attachment.command.UploadBlAttachmentCommand;
import com.freightos.fms.application.attachment.projection.BlAttachmentContent;
import com.freightos.fms.domain.attachment.entity.BlAttachment;
import com.freightos.fms.domain.attachment.enums.AttachmentBlKind;

import java.util.List;

/**
 * B/L 첨부파일 인바운드 포트.
 * upload는 SRP 원칙에 따라 저장된 id만 반환한다.
 */
public interface BlAttachmentUseCase {

    List<BlAttachment> findAttachmentsByBl(AttachmentBlKind blKind, Long blId);

    /** 업로드 후 생성된 bl_attachment_id 반환. */
    Long upload(UploadBlAttachmentCommand command);

    BlAttachmentContent download(Long attachmentId);

    void deleteAttachmentById(Long attachmentId);

    /** B/L 삭제 cascade — B/L 삭제 트랜잭션과 동일 tx에서 호출한다. */
    void deleteAttachmentsByBl(AttachmentBlKind blKind, Long blId);
}
