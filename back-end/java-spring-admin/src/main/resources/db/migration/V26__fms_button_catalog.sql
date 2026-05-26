-- FMS 모듈 버튼 카탈로그: LIST 20건 + ENTRY 50건 + 특수 4건 = 74건.

-- ─────────────────────────────────────────────────────────────────────────────
-- 1. LIST 메뉴 10개 × RESET + SEARCH = 20건
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO admin.button (menu_id, button_code, label, action_type, sort_order) VALUES
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_SEA_EXP_LIST'),  'FMS_HOUSE_BL_SEA_EXP_LIST_RESET',  'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_SEA_EXP_LIST'),  'FMS_HOUSE_BL_SEA_EXP_LIST_SEARCH', 'Search', 'CUSTOM', 11),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_SEA_IMP_LIST'),  'FMS_HOUSE_BL_SEA_IMP_LIST_RESET',  'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_SEA_IMP_LIST'),  'FMS_HOUSE_BL_SEA_IMP_LIST_SEARCH', 'Search', 'CUSTOM', 11),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_AIR_EXP_LIST'),  'FMS_HOUSE_BL_AIR_EXP_LIST_RESET',  'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_AIR_EXP_LIST'),  'FMS_HOUSE_BL_AIR_EXP_LIST_SEARCH', 'Search', 'CUSTOM', 11),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_AIR_IMP_LIST'),  'FMS_HOUSE_BL_AIR_IMP_LIST_RESET',  'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_AIR_IMP_LIST'),  'FMS_HOUSE_BL_AIR_IMP_LIST_SEARCH', 'Search', 'CUSTOM', 11),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_SEA_EXP_LIST'), 'FMS_MASTER_BL_SEA_EXP_LIST_RESET',  'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_SEA_EXP_LIST'), 'FMS_MASTER_BL_SEA_EXP_LIST_SEARCH', 'Search', 'CUSTOM', 11),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_SEA_IMP_LIST'), 'FMS_MASTER_BL_SEA_IMP_LIST_RESET',  'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_SEA_IMP_LIST'), 'FMS_MASTER_BL_SEA_IMP_LIST_SEARCH', 'Search', 'CUSTOM', 11),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_AIR_EXP_LIST'), 'FMS_MASTER_BL_AIR_EXP_LIST_RESET',  'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_AIR_EXP_LIST'), 'FMS_MASTER_BL_AIR_EXP_LIST_SEARCH', 'Search', 'CUSTOM', 11),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_AIR_IMP_LIST'), 'FMS_MASTER_BL_AIR_IMP_LIST_RESET',  'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_AIR_IMP_LIST'), 'FMS_MASTER_BL_AIR_IMP_LIST_SEARCH', 'Search', 'CUSTOM', 11),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_TRUCK_BL_LIST'),           'FMS_TRUCK_BL_LIST_RESET',           'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_TRUCK_BL_LIST'),           'FMS_TRUCK_BL_LIST_SEARCH',          'Search', 'CUSTOM', 11),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_NON_BL_LIST'),             'FMS_NON_BL_LIST_RESET',             'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_NON_BL_LIST'),             'FMS_NON_BL_LIST_SEARCH',            'Search', 'CUSTOM', 11)
ON CONFLICT (button_code) DO NOTHING;

-- ─────────────────────────────────────────────────────────────────────────────
-- 2. ENTRY 메뉴 10개 × 5종(기본) = 50건
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO admin.button (menu_id, button_code, label, action_type, sort_order) VALUES
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_SEA_EXP_ENTRY'),  'FMS_HOUSE_BL_SEA_EXP_ENTRY_CREATE',    'New',          'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_SEA_EXP_ENTRY'),  'FMS_HOUSE_BL_SEA_EXP_ENTRY_UPDATE',    'Save',         'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_SEA_EXP_ENTRY'),  'FMS_HOUSE_BL_SEA_EXP_ENTRY_DELETE',    'Delete',       'DELETE', 3),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_SEA_EXP_ENTRY'),  'FMS_HOUSE_BL_SEA_EXP_ENTRY_SEARCH_BL', 'Search B/L',   'CUSTOM', 4),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_SEA_EXP_ENTRY'),  'FMS_HOUSE_BL_SEA_EXP_ENTRY_CHANGE_BL_NO', 'Change B/L No.', 'CUSTOM', 5),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_SEA_IMP_ENTRY'),  'FMS_HOUSE_BL_SEA_IMP_ENTRY_CREATE',    'New',          'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_SEA_IMP_ENTRY'),  'FMS_HOUSE_BL_SEA_IMP_ENTRY_UPDATE',    'Save',         'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_SEA_IMP_ENTRY'),  'FMS_HOUSE_BL_SEA_IMP_ENTRY_DELETE',    'Delete',       'DELETE', 3),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_SEA_IMP_ENTRY'),  'FMS_HOUSE_BL_SEA_IMP_ENTRY_SEARCH_BL', 'Search B/L',   'CUSTOM', 4),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_SEA_IMP_ENTRY'),  'FMS_HOUSE_BL_SEA_IMP_ENTRY_CHANGE_BL_NO', 'Change B/L No.', 'CUSTOM', 5),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_AIR_EXP_ENTRY'),  'FMS_HOUSE_BL_AIR_EXP_ENTRY_CREATE',    'New',          'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_AIR_EXP_ENTRY'),  'FMS_HOUSE_BL_AIR_EXP_ENTRY_UPDATE',    'Save',         'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_AIR_EXP_ENTRY'),  'FMS_HOUSE_BL_AIR_EXP_ENTRY_DELETE',    'Delete',       'DELETE', 3),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_AIR_EXP_ENTRY'),  'FMS_HOUSE_BL_AIR_EXP_ENTRY_SEARCH_BL', 'Search B/L',   'CUSTOM', 4),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_AIR_EXP_ENTRY'),  'FMS_HOUSE_BL_AIR_EXP_ENTRY_CHANGE_BL_NO', 'Change B/L No.', 'CUSTOM', 5),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_AIR_IMP_ENTRY'),  'FMS_HOUSE_BL_AIR_IMP_ENTRY_CREATE',    'New',          'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_AIR_IMP_ENTRY'),  'FMS_HOUSE_BL_AIR_IMP_ENTRY_UPDATE',    'Save',         'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_AIR_IMP_ENTRY'),  'FMS_HOUSE_BL_AIR_IMP_ENTRY_DELETE',    'Delete',       'DELETE', 3),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_AIR_IMP_ENTRY'),  'FMS_HOUSE_BL_AIR_IMP_ENTRY_SEARCH_BL', 'Search B/L',   'CUSTOM', 4),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_AIR_IMP_ENTRY'),  'FMS_HOUSE_BL_AIR_IMP_ENTRY_CHANGE_BL_NO', 'Change B/L No.', 'CUSTOM', 5),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_SEA_EXP_ENTRY'), 'FMS_MASTER_BL_SEA_EXP_ENTRY_CREATE',    'New',          'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_SEA_EXP_ENTRY'), 'FMS_MASTER_BL_SEA_EXP_ENTRY_UPDATE',    'Save',         'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_SEA_EXP_ENTRY'), 'FMS_MASTER_BL_SEA_EXP_ENTRY_DELETE',    'Delete',       'DELETE', 3),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_SEA_EXP_ENTRY'), 'FMS_MASTER_BL_SEA_EXP_ENTRY_SEARCH_BL', 'Search B/L',   'CUSTOM', 4),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_SEA_EXP_ENTRY'), 'FMS_MASTER_BL_SEA_EXP_ENTRY_CHANGE_BL_NO', 'Change B/L No.', 'CUSTOM', 5),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_SEA_IMP_ENTRY'), 'FMS_MASTER_BL_SEA_IMP_ENTRY_CREATE',    'New',          'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_SEA_IMP_ENTRY'), 'FMS_MASTER_BL_SEA_IMP_ENTRY_UPDATE',    'Save',         'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_SEA_IMP_ENTRY'), 'FMS_MASTER_BL_SEA_IMP_ENTRY_DELETE',    'Delete',       'DELETE', 3),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_SEA_IMP_ENTRY'), 'FMS_MASTER_BL_SEA_IMP_ENTRY_SEARCH_BL', 'Search B/L',   'CUSTOM', 4),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_SEA_IMP_ENTRY'), 'FMS_MASTER_BL_SEA_IMP_ENTRY_CHANGE_BL_NO', 'Change B/L No.', 'CUSTOM', 5),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_AIR_EXP_ENTRY'), 'FMS_MASTER_BL_AIR_EXP_ENTRY_CREATE',    'New',          'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_AIR_EXP_ENTRY'), 'FMS_MASTER_BL_AIR_EXP_ENTRY_UPDATE',    'Save',         'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_AIR_EXP_ENTRY'), 'FMS_MASTER_BL_AIR_EXP_ENTRY_DELETE',    'Delete',       'DELETE', 3),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_AIR_EXP_ENTRY'), 'FMS_MASTER_BL_AIR_EXP_ENTRY_SEARCH_BL', 'Search B/L',   'CUSTOM', 4),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_AIR_EXP_ENTRY'), 'FMS_MASTER_BL_AIR_EXP_ENTRY_CHANGE_BL_NO', 'Change B/L No.', 'CUSTOM', 5),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_AIR_IMP_ENTRY'), 'FMS_MASTER_BL_AIR_IMP_ENTRY_CREATE',    'New',          'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_AIR_IMP_ENTRY'), 'FMS_MASTER_BL_AIR_IMP_ENTRY_UPDATE',    'Save',         'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_AIR_IMP_ENTRY'), 'FMS_MASTER_BL_AIR_IMP_ENTRY_DELETE',    'Delete',       'DELETE', 3),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_AIR_IMP_ENTRY'), 'FMS_MASTER_BL_AIR_IMP_ENTRY_SEARCH_BL', 'Search B/L',   'CUSTOM', 4),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_MASTER_BL_AIR_IMP_ENTRY'), 'FMS_MASTER_BL_AIR_IMP_ENTRY_CHANGE_BL_NO', 'Change B/L No.', 'CUSTOM', 5),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_TRUCK_BL_ENTRY'),           'FMS_TRUCK_BL_ENTRY_CREATE',    'New',          'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_TRUCK_BL_ENTRY'),           'FMS_TRUCK_BL_ENTRY_UPDATE',    'Save',         'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_TRUCK_BL_ENTRY'),           'FMS_TRUCK_BL_ENTRY_DELETE',    'Delete',       'DELETE', 3),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_TRUCK_BL_ENTRY'),           'FMS_TRUCK_BL_ENTRY_SEARCH_BL', 'Search B/L',   'CUSTOM', 4),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_TRUCK_BL_ENTRY'),           'FMS_TRUCK_BL_ENTRY_CHANGE_BL_NO', 'Change B/L No.', 'CUSTOM', 5),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_NON_BL_ENTRY'),             'FMS_NON_BL_ENTRY_CREATE',    'New',          'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_NON_BL_ENTRY'),             'FMS_NON_BL_ENTRY_UPDATE',    'Save',         'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_NON_BL_ENTRY'),             'FMS_NON_BL_ENTRY_DELETE',    'Delete',       'DELETE', 3),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_NON_BL_ENTRY'),             'FMS_NON_BL_ENTRY_SEARCH_BL', 'Search B/L',   'CUSTOM', 4),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_NON_BL_ENTRY'),             'FMS_NON_BL_ENTRY_CHANGE_BL_NO', 'Change B/L No.', 'CUSTOM', 5)
ON CONFLICT (button_code) DO NOTHING;

-- ─────────────────────────────────────────────────────────────────────────────
-- 3. House Air Exp/Imp ENTRY 특수 2메뉴 × 2종 = 4건
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO admin.button (menu_id, button_code, label, action_type, sort_order) VALUES
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_AIR_EXP_ENTRY'), 'FMS_HOUSE_BL_AIR_EXP_ENTRY_PRINT',     'Print',       'CUSTOM', 6),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_AIR_EXP_ENTRY'), 'FMS_HOUSE_BL_AIR_EXP_ENTRY_SWITCH_BL', 'Switch B/L',  'CUSTOM', 7),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_AIR_IMP_ENTRY'), 'FMS_HOUSE_BL_AIR_IMP_ENTRY_PRINT',     'Print',       'CUSTOM', 6),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_AIR_IMP_ENTRY'), 'FMS_HOUSE_BL_AIR_IMP_ENTRY_SWITCH_BL', 'Switch B/L',  'CUSTOM', 7)
ON CONFLICT (button_code) DO NOTHING;

-- ─────────────────────────────────────────────────────────────────────────────
-- 4. button_policy INSERT 74건 — 전체 FMS 버튼에 module=FMS 정책 부여
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO admin.button_policy (button_id, attribute_key, required_value)
SELECT b.button_id, 'module', 'FMS'
FROM admin.button b
WHERE b.button_code LIKE 'FMS_%'
ON CONFLICT (button_id, attribute_key, required_value) DO NOTHING;
