CREATE TABLE admin.code_master (
    master_id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    master_code  VARCHAR(40)  NOT NULL,
    master_name  VARCHAR(200) NOT NULL,
    description  VARCHAR(500),
    sort_order   INT,
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by   VARCHAR(50),
    updated_by   VARCHAR(50),
    CONSTRAINT uq_admin_code_master_code UNIQUE (master_code)
);
CREATE INDEX ix_admin_code_master_active ON admin.code_master (active);
COMMENT ON TABLE  admin.code_master IS '공통 코드 부모 그룹';
COMMENT ON COLUMN admin.code_master.master_code IS '비즈니스 키 (대문자/언더스코어, 예: USER_STATUS)';
COMMENT ON COLUMN admin.code_master.master_name IS '그룹 표시명';
