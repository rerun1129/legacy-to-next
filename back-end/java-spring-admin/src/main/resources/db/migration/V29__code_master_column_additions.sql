-- V29: Code Master 컬럼 추가 + ExchangeRate 구조 변경 + Customer rename + Country 신설

-- ========== 1. ALTER TABLE — 단순 컬럼 추가 ==========

ALTER TABLE admin.carrier
    ADD COLUMN IF NOT EXISTS carrier_address VARCHAR(4000),
    ADD COLUMN IF NOT EXISTS edi_code        VARCHAR(2);

ALTER TABLE admin.currency
    ADD COLUMN IF NOT EXISTS currency_unit INTEGER;

ALTER TABLE admin.freight
    ADD COLUMN IF NOT EXISTS freight_unit  VARCHAR(10),
    ADD COLUMN IF NOT EXISTS freight_group VARCHAR(50);

ALTER TABLE admin.hs_code
    ADD COLUMN IF NOT EXISTS country_code VARCHAR(5);

-- ========== 2. ALTER TABLE — Port country_code 크기 확장 ==========

ALTER TABLE admin.port
    ALTER COLUMN country_code TYPE VARCHAR(5);

-- ========== 3. ALTER TABLE — ExchangeRate 구조 변경 ==========

ALTER TABLE admin.exchange_rate
    RENAME COLUMN base_currency   TO from_currency_code;
ALTER TABLE admin.exchange_rate
    RENAME COLUMN target_currency TO to_currency_code;
ALTER TABLE admin.exchange_rate
    DROP COLUMN IF EXISTS rate;

ALTER TABLE admin.exchange_rate
    ADD COLUMN IF NOT EXISTS exchange_date             VARCHAR(8),
    ADD COLUMN IF NOT EXISTS cash_sell_exchange_rate    DECIMAL(10,4),
    ADD COLUMN IF NOT EXISTS cash_buy_exchange_rate     DECIMAL(10,4),
    ADD COLUMN IF NOT EXISTS wire_send_exchange_rate    DECIMAL(10,4),
    ADD COLUMN IF NOT EXISTS wire_receive_exchange_rate DECIMAL(10,4),
    ADD COLUMN IF NOT EXISTS standard_exchange_rate     DECIMAL(10,4);

ALTER TABLE admin.exchange_rate
    DROP CONSTRAINT IF EXISTS uq_admin_exchange_rate;
ALTER TABLE admin.exchange_rate
    ADD CONSTRAINT uq_admin_exchange_rate UNIQUE (from_currency_code, to_currency_code, exchange_date);

DROP INDEX IF EXISTS ix_admin_exchange_rate_base;
CREATE INDEX IF NOT EXISTS ix_admin_exchange_rate_from ON admin.exchange_rate(from_currency_code, active);

-- ========== 4. ALTER TABLE — Customer address rename ==========

ALTER TABLE admin.customer
    RENAME COLUMN address    TO customer_local_address;
ALTER TABLE admin.customer
    RENAME COLUMN address_en TO customer_english_address;

-- ========== 5. CREATE TABLE — Country ==========

CREATE TABLE IF NOT EXISTS admin.country (
    country_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    country_code VARCHAR(20)  NOT NULL,
    name         VARCHAR(100) NOT NULL,
    name_en      VARCHAR(100),
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted_at   TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by   VARCHAR(50),
    updated_by   VARCHAR(50),
    CONSTRAINT uq_admin_country_code UNIQUE (country_code)
);
CREATE INDEX IF NOT EXISTS ix_admin_country_code_active ON admin.country(country_code, active);

-- ========== 6. Menu — Country 1건 ==========

INSERT INTO admin.menu (menu_code, parent_id, path, label, label_en, icon, sort_order, module_code) VALUES
    ('ADMIN_CODE_COUNTRY',
        (SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE'),
        '/admin/code/country/list', '국가', 'Country', 'Globe', 9, 'ADMIN')
ON CONFLICT (menu_code) DO NOTHING;

-- ========== 7. Button — Country 5건 ==========

INSERT INTO admin.button (menu_id, button_code, label, action_type, sort_order) VALUES
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_COUNTRY'), 'ADMIN_CODE_COUNTRY_CREATE', '신규',   'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_COUNTRY'), 'ADMIN_CODE_COUNTRY_UPDATE', '수정',   'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_COUNTRY'), 'ADMIN_CODE_COUNTRY_DELETE', '삭제',   'DELETE', 3),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_COUNTRY'), 'ADMIN_CODE_COUNTRY_RESET',  'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_COUNTRY'), 'ADMIN_CODE_COUNTRY_SEARCH', 'Search', 'CUSTOM', 11)
ON CONFLICT (button_code) DO NOTHING;

-- ========== 8. Menu Policy (module=ADMIN + admin_scope=CODE) ==========

INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'module', 'ADMIN'
FROM admin.menu m WHERE m.menu_code = 'ADMIN_CODE_COUNTRY'
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'admin_scope', 'CODE'
FROM admin.menu m WHERE m.menu_code = 'ADMIN_CODE_COUNTRY'
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

-- ========== 9. Button Policy (module=ADMIN) ==========

INSERT INTO admin.button_policy (button_id, attribute_key, required_value)
SELECT b.button_id, 'module', 'ADMIN'
FROM admin.button b
WHERE b.button_code LIKE 'ADMIN_CODE_COUNTRY_%'
ON CONFLICT (button_id, attribute_key, required_value) DO NOTHING;
