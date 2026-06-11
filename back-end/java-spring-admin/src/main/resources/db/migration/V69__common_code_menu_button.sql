-- V69: 공통코드(common_code) admin 화면 메뉴/버튼/정책 시드.
-- 인라인 저장 모델: SAVE 1개 (V47~V49 패턴 미러).
-- AuthService가 prefix 자동 부착 — DB에는 prefix 없이 저장.

-- ==================== 1. 메뉴 — ADMIN_CODE 하위 leaf ====================
-- ADMIN_CODE 그룹 기존 sort_order 최대값 확인 후 9로 배치(기존 8개: V28 기준 Port=8).
INSERT INTO admin.menu (menu_code, parent_id, path, label, label_en, icon, sort_order, active, module_code)
VALUES ('ADMIN_COMMON_CODE',
        (SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE'),
        '/admin/code/common-code',
        '공통코드', 'Common Code',
        'Tags',
        9, TRUE, 'ADMIN')
ON CONFLICT (menu_code) DO NOTHING;

-- ==================== 2. 버튼 — Reset/Search/Save 3건 ====================
-- 인라인 저장 모델(V46~V49): SAVE 단일 버튼 + Reset/Search 보조.
INSERT INTO admin.button (menu_id, button_code, label, label_en, action_type, sort_order)
VALUES
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_COMMON_CODE'),
     'ADMIN_COMMON_CODE_RESET',  'Reset',  'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_COMMON_CODE'),
     'ADMIN_COMMON_CODE_SEARCH', 'Search', 'Search', 'CUSTOM', 11),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_COMMON_CODE'),
     'COMMON_CODE_SAVE',         'Save',   'Save',   'CUSTOM', 12)
ON CONFLICT (button_code) DO NOTHING;

-- ==================== 3. menu_policy — module=ADMIN ====================
INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'module', 'ADMIN'
FROM admin.menu m
WHERE m.menu_code = 'ADMIN_COMMON_CODE'
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

-- ==================== 4. button_policy — 3개 버튼 module=ADMIN ====================
INSERT INTO admin.button_policy (button_id, attribute_key, required_value)
SELECT b.button_id, 'module', 'ADMIN'
FROM admin.button b
WHERE b.button_code IN (
    'ADMIN_COMMON_CODE_RESET',
    'ADMIN_COMMON_CODE_SEARCH',
    'COMMON_CODE_SAVE'
)
ON CONFLICT (button_id, attribute_key, required_value) DO NOTHING;
