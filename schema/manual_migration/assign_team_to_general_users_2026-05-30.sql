-- =============================================================================
-- 일반 유저(시드 계정 외) team_id 임의 분산 배정
-- 작성일: 2026-05-30 (2026-05-31 수정: user_id→username 정렬)
-- 실행 환경: admin 스키마, PostgreSQL
-- 실행 방법: 수동 실행 (각 PC에서 동일 실행 → 동일 결과, 추출/전달 불필요)
--
-- 전제:
--   - admin.team 6팀 존재(V52), admin_user.team_id 컬럼 존재(V53, FK → admin.team)
--   - 시드 유저 11명은 Flyway/Seeder 가 이미 배정 → team_id IS NOT NULL
--   - 일반 유저(admin_user_staff_seed.sql 로 INSERT)는 team_id IS NULL
--   - staff seed 는 모든 PC 에 동일 username 을 넣음(user_id 는 IDENTITY 라 PC마다 다를 수 있음).
--     team_id 자체도 IDENTITY 라 PC마다 다를 수 있으므로, team 테이블을 join 해
--     username 정렬 순서로 team_id 를 가져온다 → 각 PC 에서 같은 SQL 만 돌려도 배정 일치.
--
-- 목적: 일반 유저를 username(COLLATE "C" = 바이트순, DB collation 무관) 순서로
--       6팀에 라운드로빈 균등 분산(team 테이블 sort_order 순 team_id 매핑).
-- 멱등성: team_id IS NULL 대상만 갱신 → 재실행 시 신규 미배정 유저만 추가 배정.
--
-- 실행 순서(각 PC 동일): ① admin-api 부팅(시드 11명 자동) ② admin_user_staff_seed.sql ③ 본 스크립트
-- =============================================================================
SET search_path TO admin;

WITH ranked AS (
    SELECT user_id,
           (row_number() OVER (ORDER BY username COLLATE "C") - 1)
               % (SELECT count(*) FROM team) AS slot
    FROM admin_user
    WHERE team_id IS NULL
),
team_slot AS (
    SELECT team_id,
           (row_number() OVER (ORDER BY sort_order, team_id) - 1) AS slot
    FROM team
)
UPDATE admin_user u
SET team_id    = ts.team_id,
    updated_at = now()
FROM ranked r
JOIN team_slot ts ON r.slot = ts.slot
WHERE u.user_id = r.user_id;

-- 검증:
--   SELECT t.team_code, count(*) AS cnt
--     FROM admin_user u JOIN team t ON u.team_id = t.team_id
--    GROUP BY t.team_code ORDER BY t.team_code;
--   SELECT count(*) FROM admin_user WHERE team_id IS NULL;   -- 0 기대(시드+staff 모두 배정 시)
