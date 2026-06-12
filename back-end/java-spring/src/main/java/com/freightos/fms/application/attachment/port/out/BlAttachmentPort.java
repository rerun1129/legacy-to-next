package com.freightos.fms.application.attachment.port.out;

import com.freightos.fms.domain.attachment.entity.BlAttachment;
import com.freightos.fms.domain.attachment.enums.AttachmentBlKind;

import java.util.List;
import java.util.Optional;

/**
 * B/L 첨부파일 메타데이터 아웃바운드 포트 (JPA 영속성 어댑터가 구현).
 */
public interface BlAttachmentPort {

    /** 메타데이터를 저장하고 생성된 id를 반환한다. */
    Long saveAttachment(BlAttachment attachment);

    Optional<BlAttachment> findAttachmentById(Long id);

    /** bl_attachment_id DESC — 최신 등록 순. */
    List<BlAttachment> findAttachmentsByBl(AttachmentBlKind blKind, Long blId);

    void deleteAttachmentById(Long id);

    /**
     * 해당 B/L의 첨부파일 메타데이터를 전부 삭제하고,
     * 삭제된 행들의 storage_key 목록을 반환한다 (파일 후처리용).
     */
    List<String> deleteAttachmentsByBlAndReturnKeys(AttachmentBlKind blKind, Long blId);

    /** 고아 파일 정리 배치용 — 현재 메타에 존재하는 모든 storage_key 반환. */
    List<String> findAllStorageKeys();
}
