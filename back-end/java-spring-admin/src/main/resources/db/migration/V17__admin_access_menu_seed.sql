-- Access 관리 그룹(부모 1개) + leaf 5개 메뉴 시드.
-- Menu: ADMIN_ACCESS_MENU / BUTTON / POLICY / ATTRIBUTE / MODULE
-- Button: 각 leaf × 3 (CREATE/UPDATE/DELETE) = 15개
-- Policy: menu_policy 5개 + button_policy 15개 (role=ADMIN)

-- access 관리 그룹 (부모)
INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, module_code) VALUES
    ('ADMIN_ACCESS', NULL, NULL, 'Access 관리', 'ShieldCheck', 5, 'ADMIN')
ON CONFLICT (menu_code) DO NOTHING;

-- access leaf 5개 (부모 서브쿼리로 참조)
INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, module_code) VALUES
    ('ADMIN_ACCESS_MENU',
        (SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS'),
        '/admin/access/menu', 'Menu', 'List', 1, 'ADMIN'),
    ('ADMIN_ACCESS_BUTTON',
        (SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS'),
        '/admin/access/button', 'Button', 'List', 2, 'ADMIN'),
    ('ADMIN_ACCESS_POLICY',
        (SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS'),
        '/admin/access/policy', 'Policy', 'List', 3, 'ADMIN'),
    ('ADMIN_ACCESS_ATTRIBUTE',
        (SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS'),
        '/admin/access/attribute', 'Attribute', 'List', 4, 'ADMIN'),
    ('ADMIN_ACCESS_MODULE',
        (SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS'),
        '/admin/access/module', 'Module', 'List', 5, 'ADMIN')
ON CONFLICT (menu_code) DO NOTHING;

-- access leaf 5개 각각에 CREATE/UPDATE/DELETE 버튼 시드 (15개)
INSERT INTO admin.button (menu_id, button_code, label, action_type, sort_order) VALUES
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_MENU'),      'ADMIN_ACCESS_MENU_CREATE',      '신규', 'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_MENU'),      'ADMIN_ACCESS_MENU_UPDATE',      '수정', 'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_MENU'),      'ADMIN_ACCESS_MENU_DELETE',      '삭제', 'DELETE', 3),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_BUTTON'),    'ADMIN_ACCESS_BUTTON_CREATE',    '신규', 'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_BUTTON'),    'ADMIN_ACCESS_BUTTON_UPDATE',    '수정', 'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_BUTTON'),    'ADMIN_ACCESS_BUTTON_DELETE',    '삭제', 'DELETE', 3),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_POLICY'),    'ADMIN_ACCESS_POLICY_CREATE',    '신규', 'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_POLICY'),    'ADMIN_ACCESS_POLICY_UPDATE',    '수정', 'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_POLICY'),    'ADMIN_ACCESS_POLICY_DELETE',    '삭제', 'DELETE', 3),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_ATTRIBUTE'), 'ADMIN_ACCESS_ATTRIBUTE_CREATE', '신규', 'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_ATTRIBUTE'), 'ADMIN_ACCESS_ATTRIBUTE_UPDATE', '수정', 'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_ATTRIBUTE'), 'ADMIN_ACCESS_ATTRIBUTE_DELETE', '삭제', 'DELETE', 3),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_MODULE'),    'ADMIN_ACCESS_MODULE_CREATE',    '신규', 'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_MODULE'),    'ADMIN_ACCESS_MODULE_UPDATE',    '수정', 'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_MODULE'),    'ADMIN_ACCESS_MODULE_DELETE',    '삭제', 'DELETE', 3)
ON CONFLICT (button_code) DO NOTHING;

-- access leaf 5개 메뉴에 role=ADMIN 접근 정책 시드
INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'role', 'ADMIN'
FROM admin.menu m
WHERE m.menu_code IN (
    'ADMIN_ACCESS_MENU', 'ADMIN_ACCESS_BUTTON', 'ADMIN_ACCESS_POLICY',
    'ADMIN_ACCESS_ATTRIBUTE', 'ADMIN_ACCESS_MODULE'
)
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

-- access 버튼 15개에 role=ADMIN 실행 정책 시드
INSERT INTO admin.button_policy (button_id, attribute_key, required_value)
SELECT b.button_id, 'role', 'ADMIN'
FROM admin.button b
WHERE b.button_code LIKE 'ADMIN_ACCESS_%'
ON CONFLICT (button_id, attribute_key, required_value) DO NOTHING;
