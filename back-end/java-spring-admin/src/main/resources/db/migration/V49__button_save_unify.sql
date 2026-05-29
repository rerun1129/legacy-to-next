-- V49: Access Button 화면을 인라인 편집형 + Reset/Search/Save batch save 모델로 전환.
-- DB 에는 prefix 없이 저장 (AuthService 가 응답에 BTN_ 자동 부착, V42 패턴).

-- ========== 1. Reset/Search/Save 버튼 3건 INSERT ==========

INSERT INTO admin.button (menu_id, button_code, label, action_type, sort_order)
VALUES
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_BUTTON'), 'ADMIN_ACCESS_BUTTON_RESET',  'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_BUTTON'), 'ADMIN_ACCESS_BUTTON_SEARCH', 'Search', 'CUSTOM', 11),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_ACCESS_BUTTON'), 'ADMIN_ACCESS_BUTTON_SAVE',   'Save',   'CUSTOM', 12)
ON CONFLICT (button_code) DO NOTHING;

-- ========== 2. button_policy: admin_scope=ACCESS (V42 형제 패턴) ==========

INSERT INTO admin.button_policy (button_id, attribute_key, required_value)
SELECT b.button_id, 'admin_scope', 'ACCESS'
FROM admin.button b
WHERE b.button_code IN (
    'ADMIN_ACCESS_BUTTON_RESET',
    'ADMIN_ACCESS_BUTTON_SEARCH',
    'ADMIN_ACCESS_BUTTON_SAVE'
)
ON CONFLICT (button_id, attribute_key, required_value) DO NOTHING;

-- ========== 3. button_policy: module=ADMIN (V42 형제 패턴) ==========

INSERT INTO admin.button_policy (button_id, attribute_key, required_value)
SELECT b.button_id, 'module', 'ADMIN'
FROM admin.button b
WHERE b.button_code IN (
    'ADMIN_ACCESS_BUTTON_RESET',
    'ADMIN_ACCESS_BUTTON_SEARCH',
    'ADMIN_ACCESS_BUTTON_SAVE'
)
ON CONFLICT (button_id, attribute_key, required_value) DO NOTHING;

-- ========== 4. 구 UPDATE/DELETE 액션버튼 정책 삭제 (FK 선행) ==========
-- 액션버튼 카탈로그만 하드삭제, 버튼(데이터) 행은 화면에서 active 토글로만 비활성.
-- ADMIN_ACCESS_BUTTON_CREATE는 V45에서 이미 삭제됨(무동작).

DELETE FROM admin.button_policy
WHERE button_id IN (
    SELECT button_id FROM admin.button
    WHERE button_code IN ('ADMIN_ACCESS_BUTTON_UPDATE', 'ADMIN_ACCESS_BUTTON_DELETE')
);

-- ========== 5. 구 UPDATE/DELETE 액션버튼 카탈로그 삭제 ==========

DELETE FROM admin.button
WHERE button_code IN ('ADMIN_ACCESS_BUTTON_UPDATE', 'ADMIN_ACCESS_BUTTON_DELETE');
