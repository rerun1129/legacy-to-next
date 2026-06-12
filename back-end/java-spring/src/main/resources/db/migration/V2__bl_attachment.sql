-- =============================================================================
-- V2: bl_attachment — B/L 첨부파일 테이블
-- B/L 종류(bl_kind) + B/L PK(bl_id)로 4종 B/L에 대응.
-- bl_id는 각 B/L 테이블이 독립 IDENTITY 시퀀스를 사용하므로 FK 불가.
-- =============================================================================

CREATE TABLE IF NOT EXISTS bl_attachment (
    bl_attachment_id   BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    bl_kind            VARCHAR(10)  NOT NULL,
    bl_id              BIGINT       NOT NULL,
    original_filename  VARCHAR(255) NOT NULL,
    storage_key        VARCHAR(100) NOT NULL,
    content_type       VARCHAR(100),
    file_size          BIGINT       NOT NULL,
    uploaded_by        VARCHAR(50)  NOT NULL,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by         VARCHAR(50),
    updated_by         VARCHAR(50),
    CONSTRAINT uq_bl_attachment_storage_key UNIQUE (storage_key),
    CONSTRAINT chk_bl_attachment_bl_kind CHECK (bl_kind IN ('HOUSE', 'MASTER', 'TRUCK', 'NON_BL'))
);

COMMENT ON TABLE  bl_attachment                    IS 'B/L 첨부파일 메타데이터';
COMMENT ON COLUMN bl_attachment.bl_kind            IS 'B/L 종류: HOUSE | MASTER | TRUCK | NON_BL';
COMMENT ON COLUMN bl_attachment.bl_id              IS '각 B/L 테이블의 PK — 독립 시퀀스라 FK 불가';
COMMENT ON COLUMN bl_attachment.original_filename  IS '업로드 시 원본 파일명';
COMMENT ON COLUMN bl_attachment.storage_key        IS '스토리지 내 상대 경로 키 (bl_kind/bl_id/UUID)';
COMMENT ON COLUMN bl_attachment.content_type       IS 'MIME 타입';
COMMENT ON COLUMN bl_attachment.file_size          IS '파일 크기(bytes)';
COMMENT ON COLUMN bl_attachment.uploaded_by        IS '업로드한 사용자 (인증 사용자명)';

CREATE INDEX IF NOT EXISTS idx_bl_attachment_bl ON bl_attachment(bl_kind, bl_id);
