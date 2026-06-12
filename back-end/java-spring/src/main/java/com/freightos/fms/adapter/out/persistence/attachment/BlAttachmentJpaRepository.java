package com.freightos.fms.adapter.out.persistence.attachment;

import com.freightos.fms.adapter.out.persistence.attachment.entity.BlAttachmentJpaEntity;
import com.freightos.fms.domain.attachment.enums.AttachmentBlKind;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BlAttachmentJpaRepository extends JpaRepository<BlAttachmentJpaEntity, Long> {

    /** bl_attachment_id DESC — 최신 등록 순 목록. */
    List<BlAttachmentJpaEntity> findByBlKindAndBlIdOrderByBlAttachmentIdDesc(AttachmentBlKind blKind, Long blId);

    void deleteByBlKindAndBlId(AttachmentBlKind blKind, Long blId);

    @Query("SELECT e.storageKey FROM BlAttachmentJpaEntity e WHERE e.blKind = :blKind AND e.blId = :blId")
    List<String> findStorageKeysByBlKindAndBlId(AttachmentBlKind blKind, Long blId);

    @Query("SELECT e.storageKey FROM BlAttachmentJpaEntity e")
    List<String> findAllStorageKeys();
}
