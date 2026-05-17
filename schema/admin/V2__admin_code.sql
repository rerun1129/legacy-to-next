-- Admin: 공통 코드 마스터.
-- (code_group, code_value) UNIQUE.
CREATE TABLE IF NOT EXISTS admin.code (
    code_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code_group  VARCHAR(40)  NOT NULL,
    code_value  VARCHAR(40)  NOT NULL,
    code_label  VARCHAR(200) NOT NULL,
    sort_order  INT,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    remark      VARCHAR(500),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  VARCHAR(50),
    updated_by  VARCHAR(50),
    CONSTRAINT uq_admin_code_group_value UNIQUE (code_group, code_value)
);

CREATE INDEX IF NOT EXISTS ix_admin_code_group_active
    ON admin.code(code_group, active);

COMMENT ON TABLE  admin.code IS 'Admin: 공통 코드 마스터';
COMMENT ON COLUMN admin.code.code_group IS '코드 그룹 키';
COMMENT ON COLUMN admin.code.code_value IS '코드 값';
COMMENT ON COLUMN admin.code.code_label IS '코드 표시명';
COMMENT ON COLUMN admin.code.sort_order IS '정렬 순서 (NULL 허용)';
COMMENT ON COLUMN admin.code.active     IS '사용 여부: TRUE | FALSE';
COMMENT ON COLUMN admin.code.remark     IS '비고';
