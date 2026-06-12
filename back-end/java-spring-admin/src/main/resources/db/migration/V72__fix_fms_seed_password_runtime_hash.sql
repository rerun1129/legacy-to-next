-- V21이 미리 계산된 해시 리터럴을 시드했으나 그 리터럴이 주석 주장(fms12345)과 다른 평문의 해시여서 fresh DB에서 fms 로그인 불가였음 (pgcrypto 판정 실증).
-- 실행 시점 fms.crypt() 생성으로 교체해 평문↔해시 드리프트를 원천 차단한다 (비밀번호: fms12345).
UPDATE admin.admin_user
SET password_hash = fms.crypt('fms12345', fms.gen_salt('bf', 10)),
    updated_at = now()
WHERE username = 'fms';
