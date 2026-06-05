-- BMS 모듈 권한 인프라: attribute_value(module), 구독, 메뉴 계층, 메뉴 정책, 버튼, 버튼 정책 시드.
-- 멱등(ON CONFLICT DO NOTHING) 작성 — bootRun 반복 기동 안전.

-- ==================== 1. module attribute_value 'BMS' 등록 ====================
-- ADMIN=1, FMS=2 기존 등록(V20) 이후 sort_order=3 으로 추가.
INSERT INTO admin.attribute_value (attribute_key, value, label, sort_order, active)
VALUES ('module', 'BMS', 'Billing Management', 3, TRUE)
ON CONFLICT (attribute_key, value) DO NOTHING;

-- ==================== 2. SELF 고객사 BMS 구독 시드 ====================
-- V57이 ADMIN/FMS만 시드했으므로 BMS 별도 추가.
-- AuthService.verifySubscriptionOrThrow 게이트 통과에 필수.
INSERT INTO admin.subscription (subscriber_id, module_code, start_date, end_date, active)
SELECT s.subscriber_id, 'BMS', DATE '2000-01-01', DATE '9999-12-31', TRUE
FROM admin.subscriber s
WHERE s.subscriber_code = 'SELF'
ON CONFLICT (subscriber_id, module_code) DO NOTHING;

-- ==================== 3. 메뉴 — 그룹(parent) + leaf 3개 ====================

-- 3-1. BMS_FINANCIAL parent 그룹 메뉴 (루트, path=NULL)
INSERT INTO admin.menu (menu_code, parent_id, path, label, label_en, icon, sort_order, active, module_code)
VALUES ('BMS_FINANCIAL', NULL, NULL, 'Financial Document', 'Financial Document', 'FileText', 1, TRUE, 'BMS')
ON CONFLICT (menu_code) DO NOTHING;

-- 3-2. leaf 메뉴 3개 (parent_id = BMS_FINANCIAL 서브쿼리)
INSERT INTO admin.menu (menu_code, parent_id, path, label, label_en, icon, sort_order, active, module_code)
SELECT 'BMS_INVOICE',
       m.menu_id,
       '/bms/invoice/list',
       'Invoice', 'Invoice',
       'Receipt',
       1, TRUE, 'BMS'
FROM admin.menu m
WHERE m.menu_code = 'BMS_FINANCIAL'
ON CONFLICT (menu_code) DO NOTHING;

INSERT INTO admin.menu (menu_code, parent_id, path, label, label_en, icon, sort_order, active, module_code)
SELECT 'BMS_PAYMENT',
       m.menu_id,
       '/bms/payment/list',
       'Payment', 'Payment',
       'CreditCard',
       2, TRUE, 'BMS'
FROM admin.menu m
WHERE m.menu_code = 'BMS_FINANCIAL'
ON CONFLICT (menu_code) DO NOTHING;

INSERT INTO admin.menu (menu_code, parent_id, path, label, label_en, icon, sort_order, active, module_code)
SELECT 'BMS_DC_NOTE',
       m.menu_id,
       '/bms/dc-note/list',
       'D/C Note', 'D/C Note',
       'ArrowLeftRight',
       3, TRUE, 'BMS'
FROM admin.menu m
WHERE m.menu_code = 'BMS_FINANCIAL'
ON CONFLICT (menu_code) DO NOTHING;

-- ==================== 4. menu_policy — 4개 메뉴 전부 module=BMS ====================
-- V20 패턴: 서브쿼리로 menu_id 참조.
INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'module', 'BMS'
FROM admin.menu m
WHERE m.menu_code IN ('BMS_FINANCIAL', 'BMS_INVOICE', 'BMS_PAYMENT', 'BMS_DC_NOTE')
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

-- ==================== 5. 버튼 — 3개 화면 × SEARCH/RESET = 6건 ====================
-- button 테이블 컬럼: menu_id, button_code, label, label_en, action_type, sort_order.
-- V42/V25 패턴: CUSTOM, sort_order 10(Reset)/11(Search).

INSERT INTO admin.button (menu_id, button_code, label, label_en, action_type, sort_order)
VALUES
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'BMS_INVOICE'),
     'BMS_INVOICE_RESET',  'Reset',  'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'BMS_INVOICE'),
     'BMS_INVOICE_SEARCH', 'Search', 'Search', 'CUSTOM', 11),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'BMS_PAYMENT'),
     'BMS_PAYMENT_RESET',  'Reset',  'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'BMS_PAYMENT'),
     'BMS_PAYMENT_SEARCH', 'Search', 'Search', 'CUSTOM', 11),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'BMS_DC_NOTE'),
     'BMS_DC_NOTE_RESET',  'Reset',  'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'BMS_DC_NOTE'),
     'BMS_DC_NOTE_SEARCH', 'Search', 'Search', 'CUSTOM', 11)
ON CONFLICT (button_code) DO NOTHING;

-- ==================== 6. button_policy — 6개 버튼 전부 module=BMS ====================
-- V26 패턴: LIKE 또는 IN으로 배치.
INSERT INTO admin.button_policy (button_id, attribute_key, required_value)
SELECT b.button_id, 'module', 'BMS'
FROM admin.button b
WHERE b.button_code IN (
    'BMS_INVOICE_RESET',  'BMS_INVOICE_SEARCH',
    'BMS_PAYMENT_RESET',  'BMS_PAYMENT_SEARCH',
    'BMS_DC_NOTE_RESET',  'BMS_DC_NOTE_SEARCH'
)
ON CONFLICT (button_id, attribute_key, required_value) DO NOTHING;
