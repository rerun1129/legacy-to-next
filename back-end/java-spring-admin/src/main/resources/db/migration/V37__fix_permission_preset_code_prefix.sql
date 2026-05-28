-- V37: Permission Preset menu_code / button_code prefix 제거 + module 정책 보완
--
-- AuthService 가 LoginResult 응답에 MENU_/BTN_ prefix 를 자동 부착함.
-- V35 가 prefix 이미 포함한 코드로 시드해서 더블 prefix 발생, FE 매칭 실패.
-- 다른 access 메뉴 컨벤션 (prefix 없음, AuthService 가 부착) 에 맞춰 정정.
--
-- 또한 V35 가 module=ADMIN 정책 누락 (다른 access 메뉴들과 비일관). 동시 보완.

-- 1. menu_code prefix 제거
UPDATE admin.menu
SET menu_code = 'ADMIN_ACCESS_PERMISSION_PRESET',
    updated_at = now()
WHERE menu_code = 'MENU_ADMIN_ACCESS_PERMISSION_PRESET';

-- 2. button_code prefix 제거 (3건)
UPDATE admin.button
SET button_code = 'ADMIN_ACCESS_PERMISSION_PRESET_CREATE',
    updated_at = now()
WHERE button_code = 'BTN_ADMIN_ACCESS_PERMISSION_PRESET_CREATE';

UPDATE admin.button
SET button_code = 'ADMIN_ACCESS_PERMISSION_PRESET_UPDATE',
    updated_at = now()
WHERE button_code = 'BTN_ADMIN_ACCESS_PERMISSION_PRESET_UPDATE';

UPDATE admin.button
SET button_code = 'ADMIN_ACCESS_PERMISSION_PRESET_DELETE',
    updated_at = now()
WHERE button_code = 'BTN_ADMIN_ACCESS_PERMISSION_PRESET_DELETE';

-- 3. menu_policy: module=ADMIN 정책 추가 (다른 access 메뉴 일관성)
INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'module', 'ADMIN'
FROM admin.menu m
WHERE m.menu_code = 'ADMIN_ACCESS_PERMISSION_PRESET'
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

-- 4. button_policy: module=ADMIN 정책 추가 (3건)
INSERT INTO admin.button_policy (button_id, attribute_key, required_value)
SELECT b.button_id, 'module', 'ADMIN'
FROM admin.button b
WHERE b.button_code IN (
    'ADMIN_ACCESS_PERMISSION_PRESET_CREATE',
    'ADMIN_ACCESS_PERMISSION_PRESET_UPDATE',
    'ADMIN_ACCESS_PERMISSION_PRESET_DELETE'
)
ON CONFLICT (button_id, attribute_key, required_value) DO NOTHING;
