-- fms 사용자 패스워드를 fms12345로 변경 + $2b$→$2a$ 호환 수정
UPDATE admin.admin_user
SET password_hash = '$2a$10$LmTy6a77YGx3aHPyKpJnE.1ETuS4wAVAQgZPdH1LCKEnN1boYMlZ6',
    updated_at = now()
WHERE username = 'fms';
