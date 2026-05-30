-- Admin: admin_user에 team_id BIGINT FK 컬럼 추가 (admin.team 참조)
ALTER TABLE admin.admin_user ADD COLUMN team_id BIGINT;

ALTER TABLE admin.admin_user
    ADD CONSTRAINT fk_admin_user_team
    FOREIGN KEY (team_id) REFERENCES admin.team(team_id);

CREATE INDEX ix_admin_user_team_id ON admin.admin_user(team_id);

COMMENT ON COLUMN admin.admin_user.team_id IS '소속 팀 (FK → admin.team.team_id, NULL=미배정)';

-- 해상팀 (SEA): sea_manager, truck_manager
UPDATE admin.admin_user u SET team_id = t.team_id, updated_at = now()
FROM admin.team t
WHERE t.team_code = 'SEA' AND u.username IN ('sea_manager', 'truck_manager');

-- 항공팀 (AIR): air_manager
UPDATE admin.admin_user u SET team_id = t.team_id, updated_at = now()
FROM admin.team t
WHERE t.team_code = 'AIR' AND u.username IN ('air_manager');

-- 영업팀 (SALES): customer_manager, non_manager
UPDATE admin.admin_user u SET team_id = t.team_id, updated_at = now()
FROM admin.team t
WHERE t.team_code = 'SALES' AND u.username IN ('customer_manager', 'non_manager');

-- 재무팀 (FINANCE): preset_test
UPDATE admin.admin_user u SET team_id = t.team_id, updated_at = now()
FROM admin.team t
WHERE t.team_code = 'FINANCE' AND u.username IN ('preset_test');

-- 인사팀 (HR): user_manager
UPDATE admin.admin_user u SET team_id = t.team_id, updated_at = now()
FROM admin.team t
WHERE t.team_code = 'HR' AND u.username IN ('user_manager');

-- 전략팀 (STRATEGY): code_manager, access_manager
UPDATE admin.admin_user u SET team_id = t.team_id, updated_at = now()
FROM admin.team t
WHERE t.team_code = 'STRATEGY' AND u.username IN ('code_manager', 'access_manager');
