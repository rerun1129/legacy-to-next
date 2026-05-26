-- Phase 2: FMS 하위 메뉴 정책 + fms_scope 속성 + deny-by-default 준비

-- 1. 8개 leaf 메뉴에 module=FMS 정책 부여
INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'module', 'FMS'
FROM admin.menu m
WHERE m.menu_code IN (
  'FMS_HOUSE_BL_SEA_EXP','FMS_HOUSE_BL_SEA_IMP','FMS_HOUSE_BL_AIR_EXP','FMS_HOUSE_BL_AIR_IMP',
  'FMS_MASTER_BL_SEA_EXP','FMS_MASTER_BL_SEA_IMP','FMS_MASTER_BL_AIR_EXP','FMS_MASTER_BL_AIR_IMP'
)
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

-- 2. fms_scope 속성 정의 + 값
INSERT INTO admin.attribute_definition (attribute_key, name, value_type, allow_multi, active)
VALUES ('fms_scope', 'FMS 접근 범위', 'ENUM', TRUE, TRUE)
ON CONFLICT (attribute_key) DO NOTHING;

INSERT INTO admin.attribute_value (attribute_key, value, label, sort_order, active)
VALUES ('fms_scope', 'SEA',    'Sea Operations',    1, TRUE),
       ('fms_scope', 'AIR',    'Air Operations',    2, TRUE),
       ('fms_scope', 'TRUCK',  'Truck Operations',  3, TRUE),
       ('fms_scope', 'NON_BL', 'Non-B/L Operations',4, TRUE)
ON CONFLICT (attribute_key, value) DO NOTHING;

-- 3. fms_scope 정책 부여 (leaf + truck/non-bl root)

-- Sea 계열 leaf → fms_scope=SEA
INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'fms_scope', 'SEA'
FROM admin.menu m
WHERE m.menu_code IN ('FMS_HOUSE_BL_SEA_EXP','FMS_HOUSE_BL_SEA_IMP','FMS_MASTER_BL_SEA_EXP','FMS_MASTER_BL_SEA_IMP')
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

-- Air 계열 leaf → fms_scope=AIR
INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'fms_scope', 'AIR'
FROM admin.menu m
WHERE m.menu_code IN ('FMS_HOUSE_BL_AIR_EXP','FMS_HOUSE_BL_AIR_IMP','FMS_MASTER_BL_AIR_EXP','FMS_MASTER_BL_AIR_IMP')
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

-- Truck root → fms_scope=TRUCK
INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'fms_scope', 'TRUCK'
FROM admin.menu m WHERE m.menu_code = 'FMS_TRUCK_BL'
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

-- Non B/L root → fms_scope=NON_BL
INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'fms_scope', 'NON_BL'
FROM admin.menu m WHERE m.menu_code = 'FMS_NON_BL'
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

-- 4. fms 사용자에 fms_scope 전체 부여
UPDATE admin.admin_user
SET attributes = attributes || '{"fms_scope":["SEA","AIR","TRUCK","NON_BL"]}'::jsonb,
    updated_at = now()
WHERE username = 'fms';
