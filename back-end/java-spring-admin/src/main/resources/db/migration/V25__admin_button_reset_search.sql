-- 4개 leaf 메뉴에 Reset/Search CUSTOM 버튼 추가 + button_policy role→module 전환 (V20 누락분).

-- 1. Reset/Search 버튼 8개 INSERT
INSERT INTO admin.button (menu_id, button_code, label, action_type, sort_order) VALUES
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_LIST'),       'ADMIN_CODE_LIST_RESET',       'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_LIST'),       'ADMIN_CODE_LIST_SEARCH',      'Search', 'CUSTOM', 11),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_USER_LIST'),       'ADMIN_USER_LIST_RESET',       'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_USER_LIST'),       'ADMIN_USER_LIST_SEARCH',      'Search', 'CUSTOM', 11),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CUSTOMER_LIST'),   'ADMIN_CUSTOMER_LIST_RESET',   'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CUSTOMER_LIST'),   'ADMIN_CUSTOMER_LIST_SEARCH',  'Search', 'CUSTOM', 11),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CMS_NOTICE_LIST'), 'ADMIN_CMS_NOTICE_LIST_RESET', 'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CMS_NOTICE_LIST'), 'ADMIN_CMS_NOTICE_LIST_SEARCH','Search', 'CUSTOM', 11)
ON CONFLICT (button_code) DO NOTHING;

-- 2. 신규 8개 버튼에 button_policy INSERT (module=ADMIN)
INSERT INTO admin.button_policy (button_id, attribute_key, required_value)
SELECT b.button_id, 'module', 'ADMIN'
FROM admin.button b
WHERE b.button_code IN (
    'ADMIN_CODE_LIST_RESET','ADMIN_CODE_LIST_SEARCH',
    'ADMIN_USER_LIST_RESET','ADMIN_USER_LIST_SEARCH',
    'ADMIN_CUSTOMER_LIST_RESET','ADMIN_CUSTOMER_LIST_SEARCH',
    'ADMIN_CMS_NOTICE_LIST_RESET','ADMIN_CMS_NOTICE_LIST_SEARCH'
)
ON CONFLICT (button_id, attribute_key, required_value) DO NOTHING;

-- 3. 기존 button_policy role=ADMIN → module=ADMIN 전환 (V20에서 menu_policy만 전환되고 button_policy 누락)
UPDATE admin.button_policy
SET attribute_key = 'module', required_value = 'ADMIN'
WHERE attribute_key = 'role' AND required_value = 'ADMIN';
