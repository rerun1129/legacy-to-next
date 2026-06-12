package com.freightos.fms.domain.attachment.enums;

/**
 * 첨부파일이 종속되는 B/L 종류.
 * DB bl_kind 컬럼과 동일 값(name() 기준)으로 저장된다.
 */
public enum AttachmentBlKind {
    HOUSE,
    MASTER,
    TRUCK,
    NON_BL
}
