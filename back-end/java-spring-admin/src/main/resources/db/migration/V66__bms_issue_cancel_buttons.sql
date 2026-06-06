-- BMS 발급취소 버튼 권한 시드 (단계 E 후속 — 세금계산서·전표 발급취소).
-- 멱등(ON CONFLICT DO NOTHING) 작성 — bootRun 반복 기동 안전.
-- V65 발급 버튼 시드 패턴 준용. prefix 없이 시드(AuthService 가 BTN_ 자동 부착).
-- 메뉴(BMS_TAX_INVOICE / BMS_SLIP)는 V65에서 이미 생성 — 버튼·정책만 추가.

-- ==================== 1. 버튼 — 각 화면 CANCEL = 2건 ====================
-- V65 Reset(10)/Search(11)/Issue(12) 다음 발급취소=13. prefix 없이 저장.
INSERT INTO admin.button (menu_id, button_code, label, label_en, action_type, sort_order)
VALUES
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'BMS_TAX_INVOICE'),
     'BMS_TAX_INVOICE_CANCEL', 'Cancel Issue', 'Cancel Issue', 'CUSTOM', 13),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'BMS_SLIP'),
     'BMS_SLIP_CANCEL', 'Cancel Issue', 'Cancel Issue', 'CUSTOM', 13)
ON CONFLICT (button_code) DO NOTHING;

-- ==================== 2. button_policy — 2개 버튼 module=BMS ====================
INSERT INTO admin.button_policy (button_id, attribute_key, required_value)
SELECT b.button_id, 'module', 'BMS'
FROM admin.button b
WHERE b.button_code IN (
    'BMS_TAX_INVOICE_CANCEL',
    'BMS_SLIP_CANCEL'
)
ON CONFLICT (button_id, attribute_key, required_value) DO NOTHING;
