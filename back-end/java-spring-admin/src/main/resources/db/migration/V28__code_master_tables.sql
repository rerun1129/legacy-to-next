-- V28: Code Master 7개 코드 관리 테이블 + 메뉴 7건 + 버튼 35건 + 정책

-- ========== 1. DDL (7 tables) ==========

CREATE TABLE IF NOT EXISTS admin.freight (
    freight_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    freight_code VARCHAR(20)  NOT NULL,
    name         VARCHAR(100) NOT NULL,
    name_en      VARCHAR(100),
    description  VARCHAR(500),
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted_at   TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by   VARCHAR(50),
    updated_by   VARCHAR(50),
    CONSTRAINT uq_admin_freight_code UNIQUE (freight_code)
);
CREATE INDEX IF NOT EXISTS ix_admin_freight_code_active ON admin.freight(freight_code, active);

CREATE TABLE IF NOT EXISTS admin.currency (
    currency_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    currency_code VARCHAR(3)   NOT NULL,
    name          VARCHAR(100) NOT NULL,
    name_en       VARCHAR(100),
    symbol        VARCHAR(10),
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted_at    TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by    VARCHAR(50),
    updated_by    VARCHAR(50),
    CONSTRAINT uq_admin_currency_code UNIQUE (currency_code)
);
CREATE INDEX IF NOT EXISTS ix_admin_currency_code_active ON admin.currency(currency_code, active);

CREATE TABLE IF NOT EXISTS admin.exchange_rate (
    exchange_rate_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    base_currency    VARCHAR(3)    NOT NULL,
    target_currency  VARCHAR(3)    NOT NULL,
    rate             DECIMAL(18,6) NOT NULL,
    name             VARCHAR(100)  NOT NULL,
    name_en          VARCHAR(100),
    active           BOOLEAN       NOT NULL DEFAULT TRUE,
    deleted_at       TIMESTAMPTZ,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    created_by       VARCHAR(50),
    updated_by       VARCHAR(50),
    CONSTRAINT uq_admin_exchange_rate UNIQUE (base_currency, target_currency)
);
CREATE INDEX IF NOT EXISTS ix_admin_exchange_rate_base ON admin.exchange_rate(base_currency, active);

CREATE TABLE IF NOT EXISTS admin.package_unit (
    package_unit_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    package_code    VARCHAR(20)  NOT NULL,
    name            VARCHAR(100) NOT NULL,
    name_en         VARCHAR(100),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by      VARCHAR(50),
    updated_by      VARCHAR(50),
    CONSTRAINT uq_admin_package_code UNIQUE (package_code)
);
CREATE INDEX IF NOT EXISTS ix_admin_package_code_active ON admin.package_unit(package_code, active);

CREATE TABLE IF NOT EXISTS admin.hs_code (
    hs_code_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    hs_code    VARCHAR(20)  NOT NULL,
    name       VARCHAR(200) NOT NULL,
    name_en    VARCHAR(200),
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    CONSTRAINT uq_admin_hs_code UNIQUE (hs_code)
);
CREATE INDEX IF NOT EXISTS ix_admin_hs_code_active ON admin.hs_code(hs_code, active);

CREATE TABLE IF NOT EXISTS admin.carrier (
    carrier_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    carrier_code VARCHAR(20)  NOT NULL,
    name         VARCHAR(200) NOT NULL,
    name_en      VARCHAR(200),
    carrier_type VARCHAR(20)  NOT NULL,
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted_at   TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by   VARCHAR(50),
    updated_by   VARCHAR(50),
    CONSTRAINT uq_admin_carrier_code UNIQUE (carrier_code)
);
CREATE INDEX IF NOT EXISTS ix_admin_carrier_code_active ON admin.carrier(carrier_code, active);
CREATE INDEX IF NOT EXISTS ix_admin_carrier_type ON admin.carrier(carrier_type);

CREATE TABLE IF NOT EXISTS admin.port (
    port_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    port_code    VARCHAR(10)  NOT NULL,
    name         VARCHAR(200) NOT NULL,
    name_en      VARCHAR(200),
    country_code VARCHAR(3),
    port_type    VARCHAR(20)  NOT NULL,
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted_at   TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by   VARCHAR(50),
    updated_by   VARCHAR(50),
    CONSTRAINT uq_admin_port_code UNIQUE (port_code)
);
CREATE INDEX IF NOT EXISTS ix_admin_port_code_active ON admin.port(port_code, active);
CREATE INDEX IF NOT EXISTS ix_admin_port_type ON admin.port(port_type);

-- ========== 2. Menu 7건 (ADMIN_CODE 하위) ==========

INSERT INTO admin.menu (menu_code, parent_id, path, label, label_en, icon, sort_order, module_code) VALUES
    ('ADMIN_CODE_FREIGHT',
        (SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE'),
        '/admin/code/freight/list', '운임', 'Freight', 'Package', 2, 'ADMIN'),
    ('ADMIN_CODE_CURRENCY',
        (SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE'),
        '/admin/code/currency/list', '통화', 'Currency', 'Coins', 3, 'ADMIN'),
    ('ADMIN_CODE_EXCHANGE_RATE',
        (SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE'),
        '/admin/code/exchange-rate/list', '환율', 'Exchange Rate', 'ArrowLeftRight', 4, 'ADMIN'),
    ('ADMIN_CODE_PACKAGE',
        (SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE'),
        '/admin/code/package/list', '포장단위', 'Package Unit', 'Box', 5, 'ADMIN'),
    ('ADMIN_CODE_HSCODE',
        (SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE'),
        '/admin/code/hs-code/list', 'HS Code', 'HS Code', 'Hash', 6, 'ADMIN'),
    ('ADMIN_CODE_CARRIER',
        (SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE'),
        '/admin/code/carrier/list', '선사', 'Carrier', 'Ship', 7, 'ADMIN'),
    ('ADMIN_CODE_PORT',
        (SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE'),
        '/admin/code/port/list', '항구', 'Port', 'Anchor', 8, 'ADMIN')
ON CONFLICT (menu_code) DO NOTHING;

-- ========== 3. Button 35건 (5 × 7) ==========

INSERT INTO admin.button (menu_id, button_code, label, action_type, sort_order) VALUES
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_FREIGHT'), 'ADMIN_CODE_FREIGHT_CREATE', '신규',   'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_FREIGHT'), 'ADMIN_CODE_FREIGHT_UPDATE', '수정',   'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_FREIGHT'), 'ADMIN_CODE_FREIGHT_DELETE', '삭제',   'DELETE', 3),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_FREIGHT'), 'ADMIN_CODE_FREIGHT_RESET',  'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_FREIGHT'), 'ADMIN_CODE_FREIGHT_SEARCH', 'Search', 'CUSTOM', 11),

    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_CURRENCY'), 'ADMIN_CODE_CURRENCY_CREATE', '신규',   'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_CURRENCY'), 'ADMIN_CODE_CURRENCY_UPDATE', '수정',   'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_CURRENCY'), 'ADMIN_CODE_CURRENCY_DELETE', '삭제',   'DELETE', 3),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_CURRENCY'), 'ADMIN_CODE_CURRENCY_RESET',  'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_CURRENCY'), 'ADMIN_CODE_CURRENCY_SEARCH', 'Search', 'CUSTOM', 11),

    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_EXCHANGE_RATE'), 'ADMIN_CODE_EXCHANGE_RATE_CREATE', '신규',   'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_EXCHANGE_RATE'), 'ADMIN_CODE_EXCHANGE_RATE_UPDATE', '수정',   'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_EXCHANGE_RATE'), 'ADMIN_CODE_EXCHANGE_RATE_DELETE', '삭제',   'DELETE', 3),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_EXCHANGE_RATE'), 'ADMIN_CODE_EXCHANGE_RATE_RESET',  'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_EXCHANGE_RATE'), 'ADMIN_CODE_EXCHANGE_RATE_SEARCH', 'Search', 'CUSTOM', 11),

    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_PACKAGE'), 'ADMIN_CODE_PACKAGE_CREATE', '신규',   'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_PACKAGE'), 'ADMIN_CODE_PACKAGE_UPDATE', '수정',   'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_PACKAGE'), 'ADMIN_CODE_PACKAGE_DELETE', '삭제',   'DELETE', 3),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_PACKAGE'), 'ADMIN_CODE_PACKAGE_RESET',  'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_PACKAGE'), 'ADMIN_CODE_PACKAGE_SEARCH', 'Search', 'CUSTOM', 11),

    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_HSCODE'), 'ADMIN_CODE_HSCODE_CREATE', '신규',   'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_HSCODE'), 'ADMIN_CODE_HSCODE_UPDATE', '수정',   'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_HSCODE'), 'ADMIN_CODE_HSCODE_DELETE', '삭제',   'DELETE', 3),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_HSCODE'), 'ADMIN_CODE_HSCODE_RESET',  'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_HSCODE'), 'ADMIN_CODE_HSCODE_SEARCH', 'Search', 'CUSTOM', 11),

    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_CARRIER'), 'ADMIN_CODE_CARRIER_CREATE', '신규',   'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_CARRIER'), 'ADMIN_CODE_CARRIER_UPDATE', '수정',   'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_CARRIER'), 'ADMIN_CODE_CARRIER_DELETE', '삭제',   'DELETE', 3),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_CARRIER'), 'ADMIN_CODE_CARRIER_RESET',  'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_CARRIER'), 'ADMIN_CODE_CARRIER_SEARCH', 'Search', 'CUSTOM', 11),

    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_PORT'), 'ADMIN_CODE_PORT_CREATE', '신규',   'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_PORT'), 'ADMIN_CODE_PORT_UPDATE', '수정',   'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_PORT'), 'ADMIN_CODE_PORT_DELETE', '삭제',   'DELETE', 3),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_PORT'), 'ADMIN_CODE_PORT_RESET',  'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_PORT'), 'ADMIN_CODE_PORT_SEARCH', 'Search', 'CUSTOM', 11)
ON CONFLICT (button_code) DO NOTHING;

-- ========== 4. Menu Policy (module=ADMIN + admin_scope=CODE) ==========

INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'module', 'ADMIN'
FROM admin.menu m
WHERE m.menu_code IN (
    'ADMIN_CODE_FREIGHT','ADMIN_CODE_CURRENCY','ADMIN_CODE_EXCHANGE_RATE',
    'ADMIN_CODE_PACKAGE','ADMIN_CODE_HSCODE','ADMIN_CODE_CARRIER','ADMIN_CODE_PORT'
)
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'admin_scope', 'CODE'
FROM admin.menu m
WHERE m.menu_code IN (
    'ADMIN_CODE_FREIGHT','ADMIN_CODE_CURRENCY','ADMIN_CODE_EXCHANGE_RATE',
    'ADMIN_CODE_PACKAGE','ADMIN_CODE_HSCODE','ADMIN_CODE_CARRIER','ADMIN_CODE_PORT'
)
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

-- ========== 5. Button Policy (module=ADMIN) ==========

INSERT INTO admin.button_policy (button_id, attribute_key, required_value)
SELECT b.button_id, 'module', 'ADMIN'
FROM admin.button b
WHERE b.button_code LIKE 'ADMIN_CODE_FREIGHT_%'
   OR b.button_code LIKE 'ADMIN_CODE_CURRENCY_%'
   OR b.button_code LIKE 'ADMIN_CODE_EXCHANGE_RATE_%'
   OR b.button_code LIKE 'ADMIN_CODE_PACKAGE_%'
   OR b.button_code LIKE 'ADMIN_CODE_HSCODE_%'
   OR b.button_code LIKE 'ADMIN_CODE_CARRIER_%'
   OR b.button_code LIKE 'ADMIN_CODE_PORT_%'
ON CONFLICT (button_id, attribute_key, required_value) DO NOTHING;
