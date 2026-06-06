-- BMS 발급 메뉴·버튼 권한 시드 (단계 E 세금계산서·전표 발급 화면 2개).
-- 멱등(ON CONFLICT DO NOTHING) 작성 — bootRun 반복 기동 안전.
-- V63 BMS_FINANCIAL 구조 패턴 준용. prefix 없이 시드(AuthService 가 BTN_/MENU_ 자동 부착).
-- bms 운영 계정(module=BMS)은 menu_policy로 자동 노출 — 추가 scope 매핑 불필요.

-- ==================== 1. 발급 parent 메뉴 BMS_ISSUE ====================
-- BMS_FINANCIAL 하위 4번째 섹션으로 추가(sort_order=4).
INSERT INTO admin.menu (menu_code, parent_id, path, label, label_en, icon, sort_order, active, module_code)
SELECT 'BMS_ISSUE',
       m.menu_id,
       NULL,
       '발급', 'Issue',
       'Stamp',
       4, TRUE, 'BMS'
FROM admin.menu m
WHERE m.menu_code = 'BMS_FINANCIAL'
ON CONFLICT (menu_code) DO NOTHING;

-- ==================== 2. leaf 메뉴 2개 ====================

INSERT INTO admin.menu (menu_code, parent_id, path, label, label_en, icon, sort_order, active, module_code)
SELECT 'BMS_TAX_INVOICE',
       m.menu_id,
       '/bms/tax-invoice/issue',
       '세금계산서', 'Tax Invoice',
       'Receipt',
       1, TRUE, 'BMS'
FROM admin.menu m
WHERE m.menu_code = 'BMS_ISSUE'
ON CONFLICT (menu_code) DO NOTHING;

INSERT INTO admin.menu (menu_code, parent_id, path, label, label_en, icon, sort_order, active, module_code)
SELECT 'BMS_SLIP',
       m.menu_id,
       '/bms/slip/issue',
       '전표', 'Slip',
       'FileSpreadsheet',
       2, TRUE, 'BMS'
FROM admin.menu m
WHERE m.menu_code = 'BMS_ISSUE'
ON CONFLICT (menu_code) DO NOTHING;

-- ==================== 3. menu_policy — BMS_ISSUE + leaf 2개 module=BMS ====================
INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'module', 'BMS'
FROM admin.menu m
WHERE m.menu_code IN ('BMS_ISSUE', 'BMS_TAX_INVOICE', 'BMS_SLIP')
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

-- ==================== 4. 버튼 — 각 화면 RESET/SEARCH/ISSUE = 6건 ====================
-- V63 Reset(10)/Search(11), 발급=12. prefix 없이 저장(AuthService 가 BTN_ 부착).
INSERT INTO admin.button (menu_id, button_code, label, label_en, action_type, sort_order)
VALUES
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'BMS_TAX_INVOICE'),
     'BMS_TAX_INVOICE_RESET',  'Reset',  'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'BMS_TAX_INVOICE'),
     'BMS_TAX_INVOICE_SEARCH', 'Search', 'Search', 'CUSTOM', 11),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'BMS_TAX_INVOICE'),
     'BMS_TAX_INVOICE_ISSUE',  'Issue',  'Issue',  'CUSTOM', 12),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'BMS_SLIP'),
     'BMS_SLIP_RESET',  'Reset',  'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'BMS_SLIP'),
     'BMS_SLIP_SEARCH', 'Search', 'Search', 'CUSTOM', 11),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'BMS_SLIP'),
     'BMS_SLIP_ISSUE',  'Issue',  'Issue',  'CUSTOM', 12)
ON CONFLICT (button_code) DO NOTHING;

-- ==================== 5. button_policy — 6개 버튼 전부 module=BMS ====================
INSERT INTO admin.button_policy (button_id, attribute_key, required_value)
SELECT b.button_id, 'module', 'BMS'
FROM admin.button b
WHERE b.button_code IN (
    'BMS_TAX_INVOICE_RESET',  'BMS_TAX_INVOICE_SEARCH',  'BMS_TAX_INVOICE_ISSUE',
    'BMS_SLIP_RESET',         'BMS_SLIP_SEARCH',          'BMS_SLIP_ISSUE'
)
ON CONFLICT (button_id, attribute_key, required_value) DO NOTHING;
