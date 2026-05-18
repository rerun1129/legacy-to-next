CREATE TABLE IF NOT EXISTS admin.customer (
    customer_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_code   VARCHAR(40)  NOT NULL,
    customer_type   VARCHAR(30)  NOT NULL,
    name            VARCHAR(200) NOT NULL,
    name_en         VARCHAR(200),
    business_no     VARCHAR(50),
    representative  VARCHAR(100),
    phone           VARCHAR(50),
    email           VARCHAR(200),
    address         VARCHAR(500),
    address_en      VARCHAR(500),
    memo            VARCHAR(1000),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by      VARCHAR(50),
    updated_by      VARCHAR(50),
    CONSTRAINT uq_admin_customer_code UNIQUE (customer_code)
);
CREATE INDEX IF NOT EXISTS ix_admin_customer_code_active ON admin.customer(customer_code, active);
CREATE INDEX IF NOT EXISTS ix_admin_customer_type ON admin.customer(customer_type);
