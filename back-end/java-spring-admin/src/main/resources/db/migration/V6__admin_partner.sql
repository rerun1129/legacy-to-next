CREATE TABLE IF NOT EXISTS admin.partner (
    partner_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    partner_code    VARCHAR(40)  NOT NULL,
    partner_type    VARCHAR(30)  NOT NULL,
    name            VARCHAR(200) NOT NULL,
    name_en         VARCHAR(200),
    business_no     VARCHAR(50),
    representative  VARCHAR(100),
    phone           VARCHAR(50),
    email           VARCHAR(200),
    address         VARCHAR(500),
    memo            VARCHAR(1000),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by      VARCHAR(50),
    updated_by      VARCHAR(50),
    CONSTRAINT uq_admin_partner_code UNIQUE (partner_code)
);
CREATE INDEX IF NOT EXISTS ix_admin_partner_code_active ON admin.partner(partner_code, active);
CREATE INDEX IF NOT EXISTS ix_admin_partner_type ON admin.partner(partner_type);
