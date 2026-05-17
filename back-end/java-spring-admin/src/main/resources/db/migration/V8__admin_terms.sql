CREATE TABLE IF NOT EXISTS admin.terms (
    terms_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type         VARCHAR(30)  NOT NULL,
    version      INTEGER      NOT NULL,
    effective_at TIMESTAMPTZ  NOT NULL,
    content      TEXT         NOT NULL,
    summary      VARCHAR(500),
    deleted_at   TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by   VARCHAR(50),
    updated_by   VARCHAR(50),
    CONSTRAINT uq_admin_terms_type_version UNIQUE (type, version)
);
CREATE INDEX IF NOT EXISTS ix_admin_terms_type_effective
    ON admin.terms(type, effective_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS ix_admin_terms_type_version
    ON admin.terms(type ASC, version DESC);

COMMENT ON TABLE  admin.terms              IS '약관·정책';
COMMENT ON COLUMN admin.terms.terms_id     IS '약관 식별자 (IDENTITY PK)';
COMMENT ON COLUMN admin.terms.type         IS '약관 종류 (TOS/PRIVACY/MARKETING)';
COMMENT ON COLUMN admin.terms.version      IS '버전 번호 (type 내 단조 증가)';
COMMENT ON COLUMN admin.terms.effective_at IS '발효 시각';
COMMENT ON COLUMN admin.terms.content      IS '본문 (plain text)';
COMMENT ON COLUMN admin.terms.summary      IS '요약 (nullable)';
COMMENT ON COLUMN admin.terms.deleted_at   IS 'soft delete 시각';
COMMENT ON COLUMN admin.terms.created_at   IS '생성 시각';
COMMENT ON COLUMN admin.terms.updated_at   IS '최종 수정 시각';
COMMENT ON COLUMN admin.terms.created_by   IS '생성자';
COMMENT ON COLUMN admin.terms.updated_by   IS '최종 수정자';
