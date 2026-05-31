-- V59: Subscriber 메뉴·버튼·정책 시드
-- Customer(V12/V25/V27/V47) 패턴을 그대로 미러.
-- admin_scope=SUBSCRIBER 를 신규 추가하여 customer_manager 와 독립적으로 게이팅한다.

-- ==================== 1. admin_scope 신규 값 ====================

INSERT INTO admin.attribute_value (attribute_key, value, label, sort_order, active)
VALUES ('admin_scope', 'SUBSCRIBER', 'Subscriber', 6, TRUE)
ON CONFLICT (attribute_key, value) DO NOTHING;

-- ==================== 2. 메뉴 ====================

-- 부모 메뉴 (루트, path=NULL)
INSERT INTO admin.menu (menu_code, parent_id, path, label, label_en, icon, sort_order, module_code)
VALUES ('ADMIN_SUBSCRIBER', NULL, NULL, 'Subscriber', 'Subscriber', 'Building', 5, 'ADMIN')
ON CONFLICT (menu_code) DO NOTHING;

-- 자식 메뉴 (leaf)
INSERT INTO admin.menu (menu_code, parent_id, path, label, label_en, icon, sort_order, module_code)
VALUES ('ADMIN_SUBSCRIBER_LIST',
    (SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_SUBSCRIBER'),
    '/admin/subscriber/list', 'List', 'List', 'List', 1, 'ADMIN')
ON CONFLICT (menu_code) DO NOTHING;

-- ==================== 3. 버튼 (label_en 포함 — V55 이후 형태) ====================

INSERT INTO admin.button (menu_id, button_code, label, label_en, action_type, sort_order)
VALUES
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_SUBSCRIBER_LIST'), 'ADMIN_SUBSCRIBER_LIST_RESET',          'Reset',  'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_SUBSCRIBER_LIST'), 'ADMIN_SUBSCRIBER_LIST_SEARCH',         'Search', 'Search', 'CUSTOM', 11),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_SUBSCRIBER_LIST'), 'ADMIN_SUBSCRIBER_LIST_SAVE',           'Save',   'Save',   'CUSTOM', 12),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_SUBSCRIBER_LIST'), 'ADMIN_SUBSCRIBER_SUBSCRIPTION_SAVE',   'Subscription Save', 'Subscription Save', 'CUSTOM', 13)
ON CONFLICT (button_code) DO NOTHING;

-- ==================== 4. menu_policy ====================

-- 4-1. leaf 메뉴에 module=ADMIN (Customer와 동일)
INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'module', 'ADMIN'
FROM admin.menu m WHERE m.menu_code = 'ADMIN_SUBSCRIBER_LIST'
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

-- 4-2. leaf 메뉴에 admin_scope=SUBSCRIBER (Customer의 admin_scope=CUSTOMER 패턴 미러)
INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'admin_scope', 'SUBSCRIBER'
FROM admin.menu m WHERE m.menu_code = 'ADMIN_SUBSCRIBER_LIST'
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

-- ==================== 5. button_policy (module=ADMIN — V25/V47 패턴) ====================

INSERT INTO admin.button_policy (button_id, attribute_key, required_value)
SELECT b.button_id, 'module', 'ADMIN'
FROM admin.button b
WHERE b.button_code IN (
    'ADMIN_SUBSCRIBER_LIST_RESET',
    'ADMIN_SUBSCRIBER_LIST_SEARCH',
    'ADMIN_SUBSCRIBER_LIST_SAVE',
    'ADMIN_SUBSCRIBER_SUBSCRIPTION_SAVE'
)
ON CONFLICT (button_id, attribute_key, required_value) DO NOTHING;
