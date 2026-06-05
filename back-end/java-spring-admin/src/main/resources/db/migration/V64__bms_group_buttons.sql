-- BMS 그룹 버튼(단계 D 그룹화) 권한 시드: button 3건 + button_policy 3건.
-- 멱등(ON CONFLICT DO NOTHING) — bootRun 반복 기동 안전. V63 BMS 버튼 패턴 동일.
-- button_code 는 prefix 없이 저장(AuthService 가 응답 시 BTN_ 자동 부착 → FE config 의 BTN_BMS_*_GROUP 와 매칭).

-- ==================== 1. 버튼 — 3개 화면 × GROUP = 3건 ====================
-- V63 Reset(10)/Search(11) 이후 sort_order=12. action_type CUSTOM.
INSERT INTO admin.button (menu_id, button_code, label, label_en, action_type, sort_order)
VALUES
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'BMS_INVOICE'),
     'BMS_INVOICE_GROUP', 'Group', 'Group', 'CUSTOM', 12),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'BMS_PAYMENT'),
     'BMS_PAYMENT_GROUP', 'Group', 'Group', 'CUSTOM', 12),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'BMS_DC_NOTE'),
     'BMS_DC_NOTE_GROUP', 'Group', 'Group', 'CUSTOM', 12)
ON CONFLICT (button_code) DO NOTHING;

-- ==================== 2. button_policy — 3개 버튼 전부 module=BMS ====================
-- bms 운영 계정(module=["BMS"])이 PolicyEvaluator AND 검사를 통과해 자동 노출.
INSERT INTO admin.button_policy (button_id, attribute_key, required_value)
SELECT b.button_id, 'module', 'BMS'
FROM admin.button b
WHERE b.button_code IN (
    'BMS_INVOICE_GROUP',
    'BMS_PAYMENT_GROUP',
    'BMS_DC_NOTE_GROUP'
)
ON CONFLICT (button_id, attribute_key, required_value) DO NOTHING;
