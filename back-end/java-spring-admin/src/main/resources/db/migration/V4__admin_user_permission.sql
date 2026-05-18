-- Admin: 사용자 권한. user_id + permission 복합 PK.
-- permission은 Permission.name() 저장 (CODE_MANAGE / USER_MANAGE / CUSTOMER_MANAGE / CMS_MANAGE).
-- Flyway 활성화는 후속 Goal(G7). 본 plan(G6) 로컬은 ddl-auto=create-drop으로 자동 생성.
CREATE TABLE IF NOT EXISTS admin.admin_user_permission (
    user_id    BIGINT      NOT NULL,
    permission VARCHAR(30) NOT NULL,
    CONSTRAINT pk_admin_user_permission PRIMARY KEY (user_id, permission),
    CONSTRAINT fk_aup_user FOREIGN KEY (user_id) REFERENCES admin.admin_user (user_id)
);

COMMENT ON TABLE  admin.admin_user_permission IS 'Admin: 사용자 권한 목록';
COMMENT ON COLUMN admin.admin_user_permission.user_id    IS 'admin_user.user_id FK';
COMMENT ON COLUMN admin.admin_user_permission.permission IS 'Permission.name(): CODE_MANAGE | USER_MANAGE | CUSTOMER_MANAGE | CMS_MANAGE';
