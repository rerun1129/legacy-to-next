-- 모듈 기반 접근 제어: admin→ADMIN, fms→FMS 분리.
-- 기존 role=ADMIN 메뉴 정책을 module=ADMIN으로 전환하고 FMS 메뉴·정책·사용자 시드.

-- 1. module attribute 정의 + 값
INSERT INTO admin.attribute_definition (attribute_key, name, value_type, allow_multi, active)
VALUES ('module', '접근 모듈', 'ENUM', TRUE, TRUE)
ON CONFLICT (attribute_key) DO NOTHING;

INSERT INTO admin.attribute_value (attribute_key, value, label, sort_order, active)
VALUES ('module', 'ADMIN', 'Admin Console', 1, TRUE),
       ('module', 'FMS',   'Freight Management', 2, TRUE)
ON CONFLICT (attribute_key, value) DO NOTHING;

-- 2. FMS 그룹 메뉴 등록 (사이드바 requiredMenuCode 매칭용)
INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
VALUES ('FMS_HOUSE_BL',  NULL, NULL, 'House B/L',  'FileStack', 1, TRUE, 'FMS'),
       ('FMS_MASTER_BL', NULL, NULL, 'Master B/L', 'Layers',    2, TRUE, 'FMS'),
       ('FMS_TRUCK_BL',  NULL, NULL, 'Truck B/L',  'Truck',     3, TRUE, 'FMS'),
       ('FMS_NON_BL',    NULL, NULL, 'Non B/L',    'Package',   4, TRUE, 'FMS')
ON CONFLICT (menu_code) DO NOTHING;

-- 3. 기존 ADMIN 메뉴 정책: role=ADMIN → module=ADMIN 전환
UPDATE admin.menu_policy
SET attribute_key = 'module', required_value = 'ADMIN'
WHERE attribute_key = 'role' AND required_value = 'ADMIN';

-- 4. FMS 메뉴 정책: module=FMS
INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'module', 'FMS'
FROM admin.menu m
WHERE m.menu_code IN ('FMS_HOUSE_BL', 'FMS_MASTER_BL', 'FMS_TRUCK_BL', 'FMS_NON_BL')
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

-- 5. 기존 role=ADMIN 사용자에 module=["ADMIN"] 추가 (하위 호환)
UPDATE admin.admin_user
SET attributes = COALESCE(attributes, '{}'::jsonb) || '{"module":["ADMIN"]}'::jsonb,
    updated_at = now()
WHERE attributes @> '{"role":["ADMIN"]}'::jsonb;

-- 6. fms 사용자 UPSERT (password: fms)
INSERT INTO admin.admin_user (username, email, password_hash, active, attributes)
VALUES ('fms', NULL,
        '$2b$10$/z9myifLW7XKUNrINovDr.30/W.TYBHKsAbMhvwLEP7uPGFIuok4O',
        TRUE,
        '{"role":["USER"],"module":["FMS"]}'::jsonb)
ON CONFLICT (username) DO UPDATE SET
    attributes = EXCLUDED.attributes,
    updated_at = now();
