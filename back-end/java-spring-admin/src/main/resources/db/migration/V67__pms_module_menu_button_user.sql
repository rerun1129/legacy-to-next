-- PMS(실적) 모듈 권한 인프라: attribute_value(module), SELF 구독, 메뉴, 메뉴 정책, 버튼, 버튼 정책 + pms 테스트 계정 시드.
-- 멱등(ON CONFLICT DO NOTHING) 작성 — bootRun 반복 기동 안전. V63(BMS) 미러.

-- ==================== 1. module attribute_value 'PMS' 등록 ====================
-- ADMIN=1, FMS=2(V20), BMS=3(V63) 이후 sort_order=4 으로 추가.
INSERT INTO admin.attribute_value (attribute_key, value, label, sort_order, active)
VALUES ('module', 'PMS', 'Performance Management', 4, TRUE)
ON CONFLICT (attribute_key, value) DO NOTHING;

-- ==================== 2. SELF 고객사 PMS 구독 시드 ====================
-- V57이 PMS 출현 전(ADMIN/FMS) 실행됐고 V63이 BMS만 추가했으므로 PMS 별도 추가.
-- AuthService 구독 게이트 통과에 필수.
INSERT INTO admin.subscription (subscriber_id, module_code, start_date, end_date, active)
SELECT s.subscriber_id, 'PMS', DATE '2000-01-01', DATE '9999-12-31', TRUE
FROM admin.subscriber s
WHERE s.subscriber_code = 'SELF'
ON CONFLICT (subscriber_id, module_code) DO NOTHING;

-- ==================== 3. 메뉴 — 단일 leaf (단일 화면 모듈) ====================
-- 사이드바는 정적(sidebar.tsx NAV_MODULE) — 본 메뉴/정책은 권한 게이트(hasMenuAccess)용.
INSERT INTO admin.menu (menu_code, parent_id, path, label, label_en, icon, sort_order, active, module_code)
VALUES ('PMS_PERFORMANCE', NULL, '/pms/performance', '실적 조회', 'Performance', 'BarChart3', 1, TRUE, 'PMS')
ON CONFLICT (menu_code) DO NOTHING;

-- ==================== 4. menu_policy — PMS_PERFORMANCE module=PMS ====================
INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'module', 'PMS'
FROM admin.menu m
WHERE m.menu_code = 'PMS_PERFORMANCE'
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

-- ==================== 5. 버튼 — Reset/Search 2건 (읽기 전용 화면) ====================
-- button 테이블 컬럼: menu_id, button_code, label, label_en, action_type, sort_order.
-- V42/V25/V63 패턴: CUSTOM, sort_order 10(Reset)/11(Search). 엑셀 등 보조 액션은 후속.
INSERT INTO admin.button (menu_id, button_code, label, label_en, action_type, sort_order)
VALUES
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'PMS_PERFORMANCE'),
     'PMS_PERFORMANCE_RESET',  'Reset',  'Reset',  'CUSTOM', 10),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'PMS_PERFORMANCE'),
     'PMS_PERFORMANCE_SEARCH', 'Search', 'Search', 'CUSTOM', 11)
ON CONFLICT (button_code) DO NOTHING;

-- ==================== 6. button_policy — 2개 버튼 전부 module=PMS ====================
INSERT INTO admin.button_policy (button_id, attribute_key, required_value)
SELECT b.button_id, 'module', 'PMS'
FROM admin.button b
WHERE b.button_code IN ('PMS_PERFORMANCE_RESET', 'PMS_PERFORMANCE_SEARCH')
ON CONFLICT (button_id, attribute_key, required_value) DO NOTHING;

-- ==================== 7. pms 테스트 계정 ====================
-- password 컨벤션: raw password = username (V61/V41 동일). fms.crypt(bf,10) → $2a$ 해시(Spring BCrypt 호환).
-- 신규 계정이므로 subscriber_id=SELF 직접 지정 필수(V58 NULL→SELF 백필은 1회성).
-- PMS는 scope 개념 없음 → module=PMS 만으로 menu_policy/구독 게이트 통과.
INSERT INTO admin.admin_user (username, email, password_hash, active, attributes, subscriber_id)
SELECT 'pms', NULL, fms.crypt('pms', fms.gen_salt('bf', 10)), TRUE,
       '{"role":["MANAGER"],"module":["PMS"]}'::jsonb,
       s.subscriber_id
FROM admin.subscriber s
WHERE s.subscriber_code = 'SELF'
ON CONFLICT (username) DO NOTHING;
