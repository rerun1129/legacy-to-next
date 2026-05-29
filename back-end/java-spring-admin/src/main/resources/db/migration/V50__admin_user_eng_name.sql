ALTER TABLE admin.admin_user ADD COLUMN user_eng_name VARCHAR(100);
COMMENT ON COLUMN admin.admin_user.user_eng_name IS '사용자 영문명 (autocomplete 표시명)';
