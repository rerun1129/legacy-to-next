-- V35: Permission Preset 관리 메뉴·버튼·정책 시드
-- menu: 1건 (MENU_ADMIN_ACCESS_PERMISSION_PRESET)
-- button: 3건 (CREATE / UPDATE / DELETE)
-- menu_policy: 1건 (admin_scope=ACCESS)
-- button_policy: 3건 (admin_scope=ACCESS)

-- 1. leaf 메뉴 1건 (ADMIN_ACCESS 그룹 하위, sort_order=5)
INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, module_code)
VALUES (
    'MENU_ADMIN_ACCESS_PERMISSION_PRESET',
    (SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS'),
    '/admin/access/permission-preset',
    'Permission Preset',
    'List',
    5,
    'ADMIN'
)
ON CONFLICT (menu_code) DO NOTHING;

-- 2. 버튼 3건 (CREATE / UPDATE / DELETE)
INSERT INTO admin.button (menu_id, button_code, label, action_type, sort_order)
VALUES
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'MENU_ADMIN_ACCESS_PERMISSION_PRESET'),
     'BTN_ADMIN_ACCESS_PERMISSION_PRESET_CREATE', '신규', 'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'MENU_ADMIN_ACCESS_PERMISSION_PRESET'),
     'BTN_ADMIN_ACCESS_PERMISSION_PRESET_UPDATE', '수정', 'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'MENU_ADMIN_ACCESS_PERMISSION_PRESET'),
     'BTN_ADMIN_ACCESS_PERMISSION_PRESET_DELETE', '삭제', 'DELETE', 3)
ON CONFLICT (button_code) DO NOTHING;

-- 3. 메뉴 정책 1건 (admin_scope=ACCESS) — V27 access 메뉴 정책 패턴
INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'admin_scope', 'ACCESS'
FROM admin.menu m
WHERE m.menu_code = 'MENU_ADMIN_ACCESS_PERMISSION_PRESET'
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

-- 4. 버튼 정책 3건 (admin_scope=ACCESS)
INSERT INTO admin.button_policy (button_id, attribute_key, required_value)
SELECT b.button_id, 'admin_scope', 'ACCESS'
FROM admin.button b
WHERE b.button_code IN (
    'BTN_ADMIN_ACCESS_PERMISSION_PRESET_CREATE',
    'BTN_ADMIN_ACCESS_PERMISSION_PRESET_UPDATE',
    'BTN_ADMIN_ACCESS_PERMISSION_PRESET_DELETE'
)
ON CONFLICT (button_id, attribute_key, required_value) DO NOTHING;
