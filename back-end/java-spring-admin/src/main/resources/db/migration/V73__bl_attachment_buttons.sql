-- EDMS B/L 첨부 모달 버튼(Attach) 권한 시드.
-- BTN_ prefix는 AuthService가 런타임에 자동 부착하므로 DB에는 prefix 없이 저장한다.

-- 1. 버튼 등록 — 10개 B/L Entry 화면 각각에 Attach 버튼 추가
INSERT INTO admin.button (menu_id, button_code, label, action_type, sort_order) VALUES
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_SEA_EXP_ENTRY'), 'FMS_HOUSE_BL_SEA_EXP_ENTRY_ATTACH', 'Attach', 'CUSTOM', 8),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_SEA_IMP_ENTRY'), 'FMS_HOUSE_BL_SEA_IMP_ENTRY_ATTACH', 'Attach', 'CUSTOM', 8),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_AIR_EXP_ENTRY'), 'FMS_HOUSE_BL_AIR_EXP_ENTRY_ATTACH', 'Attach', 'CUSTOM', 8),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_AIR_IMP_ENTRY'), 'FMS_HOUSE_BL_AIR_IMP_ENTRY_ATTACH', 'Attach', 'CUSTOM', 8),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_SEA_EXP_ENTRY'), 'FMS_MASTER_BL_SEA_EXP_ENTRY_ATTACH', 'Attach', 'CUSTOM', 8),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_SEA_IMP_ENTRY'), 'FMS_MASTER_BL_SEA_IMP_ENTRY_ATTACH', 'Attach', 'CUSTOM', 8),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_AIR_EXP_ENTRY'), 'FMS_MASTER_BL_AIR_EXP_ENTRY_ATTACH', 'Attach', 'CUSTOM', 8),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_AIR_IMP_ENTRY'), 'FMS_MASTER_BL_AIR_IMP_ENTRY_ATTACH', 'Attach', 'CUSTOM', 8),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_TRUCK_BL_ENTRY'),           'FMS_TRUCK_BL_ENTRY_ATTACH',           'Attach', 'CUSTOM', 8),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_NON_BL_ENTRY'),             'FMS_NON_BL_ENTRY_ATTACH',             'Attach', 'CUSTOM', 8)
ON CONFLICT (button_code) DO NOTHING;

-- 2. button_policy — 위 10개 버튼에 module=FMS 정책 부여
INSERT INTO admin.button_policy (button_id, attribute_key, required_value)
SELECT b.button_id, 'module', 'FMS'
FROM admin.button b
WHERE b.button_code IN (
    'FMS_HOUSE_BL_SEA_EXP_ENTRY_ATTACH',
    'FMS_HOUSE_BL_SEA_IMP_ENTRY_ATTACH',
    'FMS_HOUSE_BL_AIR_EXP_ENTRY_ATTACH',
    'FMS_HOUSE_BL_AIR_IMP_ENTRY_ATTACH',
    'FMS_MASTER_BL_SEA_EXP_ENTRY_ATTACH',
    'FMS_MASTER_BL_SEA_IMP_ENTRY_ATTACH',
    'FMS_MASTER_BL_AIR_EXP_ENTRY_ATTACH',
    'FMS_MASTER_BL_AIR_IMP_ENTRY_ATTACH',
    'FMS_TRUCK_BL_ENTRY_ATTACH',
    'FMS_NON_BL_ENTRY_ATTACH'
)
ON CONFLICT (button_id, attribute_key, required_value) DO NOTHING;
