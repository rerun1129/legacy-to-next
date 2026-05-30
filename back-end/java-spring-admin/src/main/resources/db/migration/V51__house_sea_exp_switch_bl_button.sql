-- 해상 House 수출 Entry에 Switch B/L 버튼 추가.
-- V26 버튼 카탈로그에서 SWITCH_BL이 AIR Exp/Imp House 에만 등록되고 SEA House 에는 누락되어,
-- Sea Export House Entry 에서 Switch B/L 버튼이 권한 부재로 표시되지 않던 문제를 수정한다.
-- (FE canSwitchBl 조건도 sea/air 수출 모두 허용으로 함께 변경됨)

-- 1. 버튼 추가
INSERT INTO admin.button (menu_id, button_code, label, action_type, sort_order) VALUES
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'FMS_HOUSE_BL_SEA_EXP_ENTRY'), 'FMS_HOUSE_BL_SEA_EXP_ENTRY_SWITCH_BL', 'Switch B/L', 'CUSTOM', 6)
ON CONFLICT (button_code) DO NOTHING;

-- 2. button_policy module=FMS 부여 (다른 FMS 버튼과 동일 정책)
INSERT INTO admin.button_policy (button_id, attribute_key, required_value)
SELECT b.button_id, 'module', 'FMS'
FROM admin.button b
WHERE b.button_code = 'FMS_HOUSE_BL_SEA_EXP_ENTRY_SWITCH_BL'
ON CONFLICT (button_id, attribute_key, required_value) DO NOTHING;
