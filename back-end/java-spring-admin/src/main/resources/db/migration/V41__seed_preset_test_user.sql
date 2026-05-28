-- V41: Permission Preset 테스트용 user 시드
--
-- 초기 attributes 가 거의 비어있는 user (role=MANAGER 만 보유) 를 추가하여
-- Permission Preset 부여 전후의 effective attributes 확장 동작을 검증.
-- preset 부여 전: 메뉴 가시성 거의 없음
-- preset 부여 후: 다음 로그인 시 preset 의 attribute_value 합집합 적용
--
-- BCrypt 해시 표준 (V38 동일): pgcrypto schema-qualified, $2a$ 접두사
-- password 컨벤션: password = username (V27/V36 manager 8명 동일)

INSERT INTO admin.admin_user (username, email, password_hash, active, attributes) VALUES
    ('preset_test', NULL, fms.crypt('preset_test', fms.gen_salt('bf', 10)), TRUE,
     '{"role":["MANAGER"]}'::jsonb)
ON CONFLICT (username) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    attributes = EXCLUDED.attributes,
    updated_at = now();
