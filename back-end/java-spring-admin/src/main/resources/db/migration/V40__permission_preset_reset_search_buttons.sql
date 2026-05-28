-- V40: Permission Preset 메뉴에 Reset/Search CUSTOM 버튼 + 정책 시드
--
-- User List 화면 패턴(V25)을 답습한 PermissionPreset 인라인 그리드 토글바에
-- Reset/Search AccessibleButton 이 노출되도록 button + button_policy 시드.
-- DB 에는 prefix 없이 저장 (AuthService 가 응답에 BTN_ 자동 부착, V37 정정 패턴).

-- 1. Reset/Search 버튼 2건
INSERT INTO admin.button (menu_id, button_code, label, action_type, sort_order) VALUES
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_PERMISSION_PRESET'),
     'ADMIN_ACCESS_PERMISSION_PRESET_RESET',  'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_PERMISSION_PRESET'),
     'ADMIN_ACCESS_PERMISSION_PRESET_SEARCH', 'Search', 'CUSTOM', 11)
ON CONFLICT (button_code) DO NOTHING;

-- 2. button_policy: admin_scope=ACCESS (V35 형제 패턴)
INSERT INTO admin.button_policy (button_id, attribute_key, required_value)
SELECT b.button_id, 'admin_scope', 'ACCESS'
FROM admin.button b
WHERE b.button_code IN (
    'ADMIN_ACCESS_PERMISSION_PRESET_RESET',
    'ADMIN_ACCESS_PERMISSION_PRESET_SEARCH'
)
ON CONFLICT (button_id, attribute_key, required_value) DO NOTHING;

-- 3. button_policy: module=ADMIN (V37 형제 패턴)
INSERT INTO admin.button_policy (button_id, attribute_key, required_value)
SELECT b.button_id, 'module', 'ADMIN'
FROM admin.button b
WHERE b.button_code IN (
    'ADMIN_ACCESS_PERMISSION_PRESET_RESET',
    'ADMIN_ACCESS_PERMISSION_PRESET_SEARCH'
)
ON CONFLICT (button_id, attribute_key, required_value) DO NOTHING;
