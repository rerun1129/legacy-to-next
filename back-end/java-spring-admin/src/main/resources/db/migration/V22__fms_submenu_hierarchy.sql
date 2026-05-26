-- FMS 하위 메뉴 등록: FMS_HOUSE_BL·FMS_MASTER_BL root 아래 각 4개의 leaf 메뉴를 추가한다.
-- FMS_TRUCK_BL·FMS_NON_BL은 단일 화면(list+entry 공유)이므로 하위 메뉴 불필요.
-- parent_id는 menu_code subquery로 참조하여 하드코딩 ID를 배제한다.

-- FMS_HOUSE_BL 하위 4개
INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_HOUSE_BL_SEA_EXP', m.menu_id, '/fms/house-bl/sea-exp/list', 'Sea Export', 'Ship', 1, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_HOUSE_BL'
ON CONFLICT (menu_code) DO NOTHING;

INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_HOUSE_BL_SEA_IMP', m.menu_id, '/fms/house-bl/sea-imp/list', 'Sea Import', 'Ship', 2, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_HOUSE_BL'
ON CONFLICT (menu_code) DO NOTHING;

INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_HOUSE_BL_AIR_EXP', m.menu_id, '/fms/house-bl/air-exp/list', 'Air Export', 'Plane', 3, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_HOUSE_BL'
ON CONFLICT (menu_code) DO NOTHING;

INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_HOUSE_BL_AIR_IMP', m.menu_id, '/fms/house-bl/air-imp/list', 'Air Import', 'Plane', 4, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_HOUSE_BL'
ON CONFLICT (menu_code) DO NOTHING;

-- FMS_MASTER_BL 하위 4개
INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_MASTER_BL_SEA_EXP', m.menu_id, '/fms/master-bl/sea-exp/list', 'Sea Export', 'Ship', 1, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_MASTER_BL'
ON CONFLICT (menu_code) DO NOTHING;

INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_MASTER_BL_SEA_IMP', m.menu_id, '/fms/master-bl/sea-imp/list', 'Sea Import', 'Ship', 2, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_MASTER_BL'
ON CONFLICT (menu_code) DO NOTHING;

INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_MASTER_BL_AIR_EXP', m.menu_id, '/fms/master-bl/air-exp/list', 'Air Export', 'Plane', 3, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_MASTER_BL'
ON CONFLICT (menu_code) DO NOTHING;

INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, active, module_code)
SELECT 'FMS_MASTER_BL_AIR_IMP', m.menu_id, '/fms/master-bl/air-imp/list', 'Air Import', 'Plane', 4, TRUE, 'FMS'
FROM admin.menu m WHERE m.menu_code = 'FMS_MASTER_BL'
ON CONFLICT (menu_code) DO NOTHING;
