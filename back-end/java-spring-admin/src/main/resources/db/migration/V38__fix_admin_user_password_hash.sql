-- V38: admin_user password_hash 표준화
--
-- V27 시드 manager 8명의 hash 가 $2b$ 접두사로 박혀 Spring BCryptPasswordEncoder 와
-- 매칭 실패(로그인 401). admin 도 $2a$ 였지만 raw 가 'admin1234' 와 매칭되지
-- 않는 상태였음. pgcrypto fms.crypt(..., fms.gen_salt('bf', 10)) 로 $2a$10$ 표준 BCrypt
-- 재생성하여 일괄 reseed.
--
-- raw password:
--   admin            -> 'admin1234'   (AdminUserSeeder.ADMIN_RAW_PASSWORD)
--   {role}_manager   -> 각 username   (V27 시드 컨벤션)
--
-- idempotent: 이미 raw 매칭이 되는 환경이라도 재시드는 무해.

UPDATE admin.admin_user
SET password_hash = fms.crypt('admin1234', fms.gen_salt('bf', 10)),
    updated_at = now()
WHERE username = 'admin';

UPDATE admin.admin_user
SET password_hash = fms.crypt(username, fms.gen_salt('bf', 10)),
    updated_at = now()
WHERE username IN (
    'access_manager',
    'code_manager',
    'user_manager',
    'customer_manager',
    'sea_manager',
    'air_manager',
    'truck_manager',
    'non_manager'
);
