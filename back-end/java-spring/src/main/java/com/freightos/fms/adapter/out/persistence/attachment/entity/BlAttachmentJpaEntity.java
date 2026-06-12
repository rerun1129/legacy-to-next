package com.freightos.fms.adapter.out.persistence.attachment.entity;

import com.freightos.common.persistence.BaseJpaEntity;
import com.freightos.fms.domain.attachment.enums.AttachmentBlKind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "fms", name = "bl_attachment")
@Getter
@NoArgsConstructor
public class BlAttachmentJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bl_attachment_id", updatable = false, nullable = false)
    private Long blAttachmentId;

    @Column(name = "bl_kind", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private AttachmentBlKind blKind;

    @Column(name = "bl_id", nullable = false)
    private Long blId;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "storage_key", nullable = false, length = 100)
    private String storageKey;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "uploaded_by", nullable = false, length = 50)
    private String uploadedBy;

    public void setBlKind(AttachmentBlKind blKind) { this.blKind = blKind; }
    public void setBlId(Long blId) { this.blId = blId; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
}
