-- Admin: admin_user 테이블에 ABAC 속성 JSONB 컬럼 추가.
-- Phase 1 병행 동작 — 기존 role 컬럼과 admin_user_permission 테이블은 유지.
ALTER TABLE admin.admin_user
    ADD COLUMN IF NOT EXISTS attributes JSONB NOT NULL DEFAULT '{}'::jsonb;

CREATE INDEX IF NOT EXISTS ix_admin_user_attributes_gin
    ON admin.admin_user USING GIN (attributes jsonb_path_ops);

COMMENT ON COLUMN admin.admin_user.attributes IS 'ABAC user attributes (JSONB). 예: {"role":["ADMIN"],"region":["ASIA"]}';

-- 기존 role 컬럼 + admin_user_permission 데이터를 attributes JSONB로 이행.
-- attributes가 아직 초기값인 row만 대상으로 하여 재실행 안전성 확보.
UPDATE admin.admin_user u
SET attributes = jsonb_build_object('role', jsonb_build_array(u.role))
                 || CASE
                        WHEN EXISTS (
                            SELECT 1
                            FROM admin.admin_user_permission p
                            WHERE p.user_id = u.user_id
                        )
                        THEN jsonb_build_object(
                                 'permissions',
                                 (SELECT jsonb_agg(p.permission ORDER BY p.permission)
                                  FROM admin.admin_user_permission p
                                  WHERE p.user_id = u.user_id)
                             )
                        ELSE '{}'::jsonb
                    END
WHERE u.attributes = '{}'::jsonb OR u.attributes IS NULL;
