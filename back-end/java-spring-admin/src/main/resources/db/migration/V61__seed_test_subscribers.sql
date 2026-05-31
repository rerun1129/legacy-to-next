-- V61: 구독 기능 테스트용 고객사 A~E + FMS 모듈 구독 시드
--   진행중 (A, B, C): start_date=2025-01-01 / end_date=2099-12-31
--   만   료 (D, E):   start_date=2023-01-01 / end_date=2024-12-31

-- ── 고객사 시드 ──────────────────────────────────────────────────────────────
INSERT INTO admin.subscriber (subscriber_code, name, name_en, active)
VALUES
    ('A', '고객사 A', 'Company A', TRUE),
    ('B', '고객사 B', 'Company B', TRUE),
    ('C', '고객사 C', 'Company C', TRUE),
    ('D', '고객사 D', 'Company D', TRUE),
    ('E', '고객사 E', 'Company E', TRUE)
ON CONFLICT (subscriber_code) DO NOTHING;

-- ── 구독 시드 (진행중: A, B, C) ──────────────────────────────────────────────
-- 유효 판정: active=true AND start_date <= 오늘 <= end_date
-- 2025-01-01 ~ 2099-12-31 → 오늘(2026-06-01) 기준 진행중
INSERT INTO admin.subscription (subscriber_id, module_code, start_date, end_date, active)
SELECT s.subscriber_id, 'FMS', DATE '2025-01-01', DATE '2099-12-31', TRUE
FROM admin.subscriber s
WHERE s.subscriber_code IN ('A', 'B', 'C')
ON CONFLICT (subscriber_id, module_code) DO NOTHING;

-- ── 구독 시드 (만료: D, E) ────────────────────────────────────────────────────
-- 2023-01-01 ~ 2024-12-31 → 오늘(2026-06-01) 기준 end_date 이미 경과하여 만료
INSERT INTO admin.subscription (subscriber_id, module_code, start_date, end_date, active)
SELECT s.subscriber_id, 'FMS', DATE '2023-01-01', DATE '2024-12-31', TRUE
FROM admin.subscriber s
WHERE s.subscriber_code IN ('D', 'E')
ON CONFLICT (subscriber_id, module_code) DO NOTHING;

-- ── 소속 테스트 유저 ──────────────────────────────────────────────────────────
-- 로그인 차단 검증: module ∩ 소속 고객사 유효 구독 교집합이 없으면 차단
-- attributes: role=USER, module=FMS, fms_scope 전체 (SEA/AIR/TRUCK/NON_BL)
-- password 컨벤션: raw password = username (V38/V41 동일)

-- 진행중 대조 (고객사 A — 구독 유효, 로그인 정상)
INSERT INTO admin.admin_user (username, email, password_hash, active, attributes, subscriber_id)
SELECT 'user_a', NULL, fms.crypt('user_a', fms.gen_salt('bf', 10)), TRUE,
       '{"role":["USER"],"module":["FMS"],"fms_scope":["SEA","AIR","TRUCK","NON_BL"]}'::jsonb,
       s.subscriber_id
FROM admin.subscriber s
WHERE s.subscriber_code = 'A'
ON CONFLICT (username) DO NOTHING;

-- 만료 차단 (고객사 D — 구독 만료, 로그인 차단)
INSERT INTO admin.admin_user (username, email, password_hash, active, attributes, subscriber_id)
SELECT 'user_d', NULL, fms.crypt('user_d', fms.gen_salt('bf', 10)), TRUE,
       '{"role":["USER"],"module":["FMS"],"fms_scope":["SEA","AIR","TRUCK","NON_BL"]}'::jsonb,
       s.subscriber_id
FROM admin.subscriber s
WHERE s.subscriber_code = 'D'
ON CONFLICT (username) DO NOTHING;

-- 만료 차단 (고객사 E — 구독 만료, 로그인 차단)
INSERT INTO admin.admin_user (username, email, password_hash, active, attributes, subscriber_id)
SELECT 'user_e', NULL, fms.crypt('user_e', fms.gen_salt('bf', 10)), TRUE,
       '{"role":["USER"],"module":["FMS"],"fms_scope":["SEA","AIR","TRUCK","NON_BL"]}'::jsonb,
       s.subscriber_id
FROM admin.subscriber s
WHERE s.subscriber_code = 'E'
ON CONFLICT (username) DO NOTHING;
