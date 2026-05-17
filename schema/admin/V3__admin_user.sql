-- Admin: 사용자 마스터.
-- username UNIQUE, role enum.name() 저장.
-- soft delete (deleted_at IS NULL → 활성 row).
-- Flyway 활성화는 후속 Goal(G7). 본 plan(G1) 로컬은 application.yml의
-- ddl-auto=create-drop + db/init/01-create-schema.sql + AdminUserSeeder
-- ApplicationRunner로 시드. 운영 마이그레이션 활성화 시 본 파일을 사용한다.
CREATE TABLE IF NOT EXISTS admin.admin_user (
    user_id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL,
    email         VARCHAR(200),
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'ADMIN',
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted_at    TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by    VARCHAR(50),
    updated_by    VARCHAR(50),
    CONSTRAINT uq_admin_user_username UNIQUE (username)
);

CREATE INDEX IF NOT EXISTS ix_admin_user_active
    ON admin.admin_user(active);

COMMENT ON TABLE  admin.admin_user IS 'Admin: 사용자 마스터';
COMMENT ON COLUMN admin.admin_user.username IS '로그인 ID (UNIQUE, 불변)';
COMMENT ON COLUMN admin.admin_user.email    IS '이메일 (옵셔널)';
COMMENT ON COLUMN admin.admin_user.password_hash IS 'BCrypt hash';
COMMENT ON COLUMN admin.admin_user.role     IS 'UserRole.name(): ADMIN | USER';
COMMENT ON COLUMN admin.admin_user.active   IS '활성 여부';
COMMENT ON COLUMN admin.admin_user.deleted_at IS 'soft delete 시각 (NULL = 미삭제)';
