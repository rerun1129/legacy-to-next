-- FMS 메뉴 계층 완성: variant 8개를 intermediate node로 전환하고 leaf 20개를 추가한다.
-- 1단계: variant 메뉴 path NULL 전환 (leaf→parent)
-- 2단계: leaf 20개 INSERT
-- 3단계: leaf 20개에 module=FMS + fms_scope 정책 복사

-- ===========================================================================
-- 1단계: 기존 variant 메뉴 8개를 intermediate node로 전환 (path 제거)
-- ===========================================================================
UPDATE admin.menu SET path = NULL
WHERE menu_code IN (
  'FMS_HOUSE_BL_SEA_EXP','FMS_HOUSE_BL_SEA_IMP','FMS_HOUSE_BL_AIR_EXP','FMS_HOUSE_BL_AIR_IMP',
  'FMS_MASTER_BL_SEA_EXP','FMS_MASTER_BL_SEA_IMP','FMS_MASTER_BL_AIR_EXP','FMS_MASTER_BL_AIR_IMP'
);

-- ===========================================================================
-- 2단계: leaf 메뉴 20개 INSERT
-- ===========================================================================

-- House B/L Sea Export 하위
INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_HOUSE_BL_SEA_EXP_LIST', m.menu_id, '/fms/house-bl/sea-exp/list', 'List', 'List', 1, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_HOUSE_BL_SEA_EXP'
ON CONFLICT (menu_code) DO NOTHING;

INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_HOUSE_BL_SEA_EXP_ENTRY', m.menu_id, '/fms/house-bl/sea-exp/entry', 'Entry', 'FilePlus', 2, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_HOUSE_BL_SEA_EXP'
ON CONFLICT (menu_code) DO NOTHING;

-- House B/L Sea Import 하위
INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_HOUSE_BL_SEA_IMP_LIST', m.menu_id, '/fms/house-bl/sea-imp/list', 'List', 'List', 1, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_HOUSE_BL_SEA_IMP'
ON CONFLICT (menu_code) DO NOTHING;

INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_HOUSE_BL_SEA_IMP_ENTRY', m.menu_id, '/fms/house-bl/sea-imp/entry', 'Entry', 'FilePlus', 2, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_HOUSE_BL_SEA_IMP'
ON CONFLICT (menu_code) DO NOTHING;

-- House B/L Air Export 하위
INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_HOUSE_BL_AIR_EXP_LIST', m.menu_id, '/fms/house-bl/air-exp/list', 'List', 'List', 1, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_HOUSE_BL_AIR_EXP'
ON CONFLICT (menu_code) DO NOTHING;

INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_HOUSE_BL_AIR_EXP_ENTRY', m.menu_id, '/fms/house-bl/air-exp/entry', 'Entry', 'FilePlus', 2, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_HOUSE_BL_AIR_EXP'
ON CONFLICT (menu_code) DO NOTHING;

-- House B/L Air Import 하위
INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_HOUSE_BL_AIR_IMP_LIST', m.menu_id, '/fms/house-bl/air-imp/list', 'List', 'List', 1, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_HOUSE_BL_AIR_IMP'
ON CONFLICT (menu_code) DO NOTHING;

INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_HOUSE_BL_AIR_IMP_ENTRY', m.menu_id, '/fms/house-bl/air-imp/entry', 'Entry', 'FilePlus', 2, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_HOUSE_BL_AIR_IMP'
ON CONFLICT (menu_code) DO NOTHING;

-- Master B/L Sea Export 하위
INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_MASTER_BL_SEA_EXP_LIST', m.menu_id, '/fms/master-bl/sea-exp/list', 'List', 'List', 1, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_MASTER_BL_SEA_EXP'
ON CONFLICT (menu_code) DO NOTHING;

INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_MASTER_BL_SEA_EXP_ENTRY', m.menu_id, '/fms/master-bl/sea-exp/entry', 'Entry', 'FilePlus', 2, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_MASTER_BL_SEA_EXP'
ON CONFLICT (menu_code) DO NOTHING;

-- Master B/L Sea Import 하위
INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_MASTER_BL_SEA_IMP_LIST', m.menu_id, '/fms/master-bl/sea-imp/list', 'List', 'List', 1, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_MASTER_BL_SEA_IMP'
ON CONFLICT (menu_code) DO NOTHING;

INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_MASTER_BL_SEA_IMP_ENTRY', m.menu_id, '/fms/master-bl/sea-imp/entry', 'Entry', 'FilePlus', 2, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_MASTER_BL_SEA_IMP'
ON CONFLICT (menu_code) DO NOTHING;

-- Master B/L Air Export 하위
INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_MASTER_BL_AIR_EXP_LIST', m.menu_id, '/fms/master-bl/air-exp/list', 'List', 'List', 1, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_MASTER_BL_AIR_EXP'
ON CONFLICT (menu_code) DO NOTHING;

INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_MASTER_BL_AIR_EXP_ENTRY', m.menu_id, '/fms/master-bl/air-exp/entry', 'Entry', 'FilePlus', 2, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_MASTER_BL_AIR_EXP'
ON CONFLICT (menu_code) DO NOTHING;

-- Master B/L Air Import 하위
INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_MASTER_BL_AIR_IMP_LIST', m.menu_id, '/fms/master-bl/air-imp/list', 'List', 'List', 1, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_MASTER_BL_AIR_IMP'
ON CONFLICT (menu_code) DO NOTHING;

INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_MASTER_BL_AIR_IMP_ENTRY', m.menu_id, '/fms/master-bl/air-imp/entry', 'Entry', 'FilePlus', 2, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_MASTER_BL_AIR_IMP'
ON CONFLICT (menu_code) DO NOTHING;

-- Truck B/L 하위
INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_TRUCK_BL_LIST', m.menu_id, '/fms/truck-bl/list', 'List', 'List', 1, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_TRUCK_BL'
ON CONFLICT (menu_code) DO NOTHING;

INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_TRUCK_BL_ENTRY', m.menu_id, '/fms/truck-bl/entry', 'Entry', 'FilePlus', 2, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_TRUCK_BL'
ON CONFLICT (menu_code) DO NOTHING;

-- Non B/L 하위
INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_NON_BL_LIST', m.menu_id, '/fms/non-bl/list', 'List', 'List', 1, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_NON_BL'
ON CONFLICT (menu_code) DO NOTHING;

INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_NON_BL_ENTRY', m.menu_id, '/fms/non-bl/entry', 'Entry', 'FilePlus', 2, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_NON_BL'
ON CONFLICT (menu_code) DO NOTHING;

-- ===========================================================================
-- 3단계: leaf 20개에 정책 복사
-- ===========================================================================

-- module=FMS 정책 일괄 부여 (20개 전체)
INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'module', 'FMS'
FROM admin.menu m
WHERE m.menu_code IN (
  'FMS_HOUSE_BL_SEA_EXP_LIST','FMS_HOUSE_BL_SEA_EXP_ENTRY',
  'FMS_HOUSE_BL_SEA_IMP_LIST','FMS_HOUSE_BL_SEA_IMP_ENTRY',
  'FMS_HOUSE_BL_AIR_EXP_LIST','FMS_HOUSE_BL_AIR_EXP_ENTRY',
  'FMS_HOUSE_BL_AIR_IMP_LIST','FMS_HOUSE_BL_AIR_IMP_ENTRY',
  'FMS_MASTER_BL_SEA_EXP_LIST','FMS_MASTER_BL_SEA_EXP_ENTRY',
  'FMS_MASTER_BL_SEA_IMP_LIST','FMS_MASTER_BL_SEA_IMP_ENTRY',
  'FMS_MASTER_BL_AIR_EXP_LIST','FMS_MASTER_BL_AIR_EXP_ENTRY',
  'FMS_MASTER_BL_AIR_IMP_LIST','FMS_MASTER_BL_AIR_IMP_ENTRY',
  'FMS_TRUCK_BL_LIST','FMS_TRUCK_BL_ENTRY',
  'FMS_NON_BL_LIST','FMS_NON_BL_ENTRY'
)
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

-- fms_scope=SEA: Sea 계열 leaf 8개
INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'fms_scope', 'SEA'
FROM admin.menu m
WHERE m.menu_code IN (
  'FMS_HOUSE_BL_SEA_EXP_LIST','FMS_HOUSE_BL_SEA_EXP_ENTRY',
  'FMS_HOUSE_BL_SEA_IMP_LIST','FMS_HOUSE_BL_SEA_IMP_ENTRY',
  'FMS_MASTER_BL_SEA_EXP_LIST','FMS_MASTER_BL_SEA_EXP_ENTRY',
  'FMS_MASTER_BL_SEA_IMP_LIST','FMS_MASTER_BL_SEA_IMP_ENTRY'
)
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

-- fms_scope=AIR: Air 계열 leaf 8개
INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'fms_scope', 'AIR'
FROM admin.menu m
WHERE m.menu_code IN (
  'FMS_HOUSE_BL_AIR_EXP_LIST','FMS_HOUSE_BL_AIR_EXP_ENTRY',
  'FMS_HOUSE_BL_AIR_IMP_LIST','FMS_HOUSE_BL_AIR_IMP_ENTRY',
  'FMS_MASTER_BL_AIR_EXP_LIST','FMS_MASTER_BL_AIR_EXP_ENTRY',
  'FMS_MASTER_BL_AIR_IMP_LIST','FMS_MASTER_BL_AIR_IMP_ENTRY'
)
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

-- fms_scope=TRUCK: Truck B/L leaf 2개
INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'fms_scope', 'TRUCK'
FROM admin.menu m
WHERE m.menu_code IN ('FMS_TRUCK_BL_LIST','FMS_TRUCK_BL_ENTRY')
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

-- fms_scope=NON_BL: Non B/L leaf 2개
INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'fms_scope', 'NON_BL'
FROM admin.menu m
WHERE m.menu_code IN ('FMS_NON_BL_LIST','FMS_NON_BL_ENTRY')
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;
