CREATE TABLE admin.code_detail (
    detail_id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    master_id    BIGINT       NOT NULL,
    code_value   VARCHAR(40)  NOT NULL,
    code_label   VARCHAR(200) NOT NULL,
    sort_order   INT,
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    remark       VARCHAR(500),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by   VARCHAR(50),
    updated_by   VARCHAR(50),
    CONSTRAINT fk_code_detail_master FOREIGN KEY (master_id)
        REFERENCES admin.code_master(master_id) ON DELETE RESTRICT,
    CONSTRAINT uq_admin_code_detail_master_value UNIQUE (master_id, code_value)
);
CREATE INDEX ix_admin_code_detail_master_active ON admin.code_detail (master_id, active);
COMMENT ON TABLE admin.code_detail IS '공통 코드 자식 항목';
