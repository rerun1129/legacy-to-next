-- V36: admin_user attributes 명시적 reseed
-- V27 의 `attributes || '...'` merge 연산이 NULL attributes 인 user 에서
-- NULL 결과를 만들어 시드 값이 정확히 들어가지 않던 문제 보완.
-- 명시적 SET 으로 idempotent reseed (운영/dev 환경 일관성 보장).

UPDATE admin.admin_user
SET attributes = '{"role":["ADMIN"],"module":["ADMIN"],"admin_scope":["CODE","USER","CUSTOMER","CMS_NOTICE","ACCESS"]}'::jsonb,
    updated_at = now()
WHERE username = 'admin';

UPDATE admin.admin_user
SET attributes = '{"role":["MANAGER"],"module":["ADMIN"],"admin_scope":["ACCESS"]}'::jsonb,
    updated_at = now()
WHERE username = 'access_manager';

UPDATE admin.admin_user
SET attributes = '{"role":["MANAGER"],"module":["ADMIN"],"admin_scope":["CODE"]}'::jsonb,
    updated_at = now()
WHERE username = 'code_manager';

UPDATE admin.admin_user
SET attributes = '{"role":["MANAGER"],"module":["ADMIN"],"admin_scope":["USER"]}'::jsonb,
    updated_at = now()
WHERE username = 'user_manager';

UPDATE admin.admin_user
SET attributes = '{"role":["MANAGER"],"module":["ADMIN"],"admin_scope":["CUSTOMER"]}'::jsonb,
    updated_at = now()
WHERE username = 'customer_manager';

UPDATE admin.admin_user
SET attributes = '{"role":["MANAGER"],"module":["FMS"],"fms_scope":["SEA"]}'::jsonb,
    updated_at = now()
WHERE username = 'sea_manager';

UPDATE admin.admin_user
SET attributes = '{"role":["MANAGER"],"module":["FMS"],"fms_scope":["AIR"]}'::jsonb,
    updated_at = now()
WHERE username = 'air_manager';

UPDATE admin.admin_user
SET attributes = '{"role":["MANAGER"],"module":["FMS"],"fms_scope":["TRUCK"]}'::jsonb,
    updated_at = now()
WHERE username = 'truck_manager';

UPDATE admin.admin_user
SET attributes = '{"role":["MANAGER"],"module":["FMS"],"fms_scope":["NON_BL"]}'::jsonb,
    updated_at = now()
WHERE username = 'non_manager';
