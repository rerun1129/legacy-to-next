-- V27: MANAGER 역할 + admin_scope 속성 + 시드 유저 8명

-- 1. MANAGER role 값 추가
INSERT INTO admin.attribute_value (attribute_key, value, label, sort_order, active)
VALUES ('role', 'MANAGER', '매니저', 3, TRUE)
ON CONFLICT (attribute_key, value) DO NOTHING;

-- 2. admin_scope 속성 정의 + 값 5건
INSERT INTO admin.attribute_definition (attribute_key, name, value_type, allow_multi, active)
VALUES ('admin_scope', 'Admin 접근 범위', 'ENUM', TRUE, TRUE)
ON CONFLICT (attribute_key) DO NOTHING;

INSERT INTO admin.attribute_value (attribute_key, value, label, sort_order, active)
VALUES ('admin_scope', 'CODE',       'Code Master',  1, TRUE),
       ('admin_scope', 'USER',       '사용자 관리',  2, TRUE),
       ('admin_scope', 'CUSTOMER',   'Customer',     3, TRUE),
       ('admin_scope', 'CMS_NOTICE', '공지사항',     4, TRUE),
       ('admin_scope', 'ACCESS',     'Access 관리',  5, TRUE)
ON CONFLICT (attribute_key, value) DO NOTHING;

-- 3. ADMIN leaf 메뉴에 admin_scope 정책 추가 (9건)
INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'admin_scope', 'CODE'
FROM admin.menu m WHERE m.menu_code = 'ADMIN_CODE_LIST'
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'admin_scope', 'USER'
FROM admin.menu m WHERE m.menu_code = 'ADMIN_USER_LIST'
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'admin_scope', 'CUSTOMER'
FROM admin.menu m WHERE m.menu_code = 'ADMIN_CUSTOMER_LIST'
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'admin_scope', 'CMS_NOTICE'
FROM admin.menu m WHERE m.menu_code = 'ADMIN_CMS_NOTICE_LIST'
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

-- Access 5개 메뉴 일괄
INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'admin_scope', 'ACCESS'
FROM admin.menu m
WHERE m.menu_code IN ('ADMIN_ACCESS_MENU','ADMIN_ACCESS_BUTTON','ADMIN_ACCESS_POLICY','ADMIN_ACCESS_ATTRIBUTE','ADMIN_ACCESS_MODULE')
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

-- 4. 기존 admin 유저에 admin_scope 전체 부여
UPDATE admin.admin_user
SET attributes = attributes || '{"admin_scope":["CODE","USER","CUSTOMER","CMS_NOTICE","ACCESS"]}'::jsonb,
    updated_at = now()
WHERE username = 'admin';

-- 5. fms 유저 role: USER → MANAGER
UPDATE admin.admin_user
SET attributes = jsonb_set(attributes, '{role}', '["MANAGER"]'::jsonb),
    updated_at = now()
WHERE username = 'fms';

-- 6. ADMIN 모듈 매니저 4명 (password: 각 계정명)
INSERT INTO admin.admin_user (username, email, password_hash, active, attributes) VALUES
('code_manager', NULL, '$2b$10$Ab1kaRBUH.mMSW0ZYIFA1ePjkGxeOTVYFj4lmbX6Q5O1U8Lws7ORq', TRUE,
 '{"role":["MANAGER"],"module":["ADMIN"],"admin_scope":["CODE"]}'::jsonb),
('user_manager', NULL, '$2b$10$BAKirs6L8bDUhJo0rrj9lurGmzu08DpikC1T.PAdgECXLsRYiQhvm', TRUE,
 '{"role":["MANAGER"],"module":["ADMIN"],"admin_scope":["USER"]}'::jsonb),
('customer_manager', NULL, '$2b$10$1VvBdJa7BJdwXM9Xvof86unL/HmIcEyHiqGEp5a/RRqd0rj7G1q4G', TRUE,
 '{"role":["MANAGER"],"module":["ADMIN"],"admin_scope":["CUSTOMER"]}'::jsonb),
('access_manager', NULL, '$2b$10$vLq8sVnD5Ggd9vPhOidbneSxuxipn1NhIgqotI18v3bjhyCHdTUdi', TRUE,
 '{"role":["MANAGER"],"module":["ADMIN"],"admin_scope":["ACCESS"]}'::jsonb)
ON CONFLICT (username) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    attributes = EXCLUDED.attributes,
    updated_at = now();

-- 7. FMS 모듈 매니저 4명 (password: 각 계정명)
INSERT INTO admin.admin_user (username, email, password_hash, active, attributes) VALUES
('sea_manager', NULL, '$2b$10$B8mlApBD6Jh/.uf7TksFI.L2i0MSUfPPTHYE81ZbA8Rq29FKl551a', TRUE,
 '{"role":["MANAGER"],"module":["FMS"],"fms_scope":["SEA"]}'::jsonb),
('air_manager', NULL, '$2b$10$hKelVhgAazkrOfnNLnMriOa7mrsuP4pfUPcR5CM9bZLZ2FooCULHK', TRUE,
 '{"role":["MANAGER"],"module":["FMS"],"fms_scope":["AIR"]}'::jsonb),
('truck_manager', NULL, '$2b$10$fxGBSzenJ11vEmhRoSIA.eHShwxnL7rcxs6kYpv.ZBTT7nV/3Z2Iu', TRUE,
 '{"role":["MANAGER"],"module":["FMS"],"fms_scope":["TRUCK"]}'::jsonb),
('non_manager', NULL, '$2b$10$C8Dn3cy4Q0AF6ZsvLGhyqe/.NQ5BImFhLLuIaj8iUhrUACbH5wqL6', TRUE,
 '{"role":["MANAGER"],"module":["FMS"],"fms_scope":["NON_BL"]}'::jsonb)
ON CONFLICT (username) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    attributes = EXCLUDED.attributes,
    updated_at = now();
