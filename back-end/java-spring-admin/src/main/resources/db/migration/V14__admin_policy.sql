-- Admin: ABAC 접근 정책 테이블.
-- menu_policy: 메뉴 접근 조건 (attribute_key + required_value 조합).
-- button_policy: 버튼 노출/실행 조건.
CREATE TABLE admin.menu_policy (
    policy_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    menu_id        BIGINT       NOT NULL,
    attribute_key  VARCHAR(80)  NOT NULL,
    required_value VARCHAR(100) NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by     VARCHAR(50),
    updated_by     VARCHAR(50),
    CONSTRAINT uq_admin_menu_policy
        UNIQUE (menu_id, attribute_key, required_value),
    CONSTRAINT fk_admin_menu_policy_menu
        FOREIGN KEY (menu_id) REFERENCES admin.menu(menu_id) ON DELETE CASCADE,
    CONSTRAINT fk_admin_menu_policy_attr
        FOREIGN KEY (attribute_key) REFERENCES admin.attribute_definition(attribute_key)
);

CREATE INDEX IF NOT EXISTS ix_admin_menu_policy_menu ON admin.menu_policy(menu_id);
CREATE INDEX IF NOT EXISTS ix_admin_menu_policy_attr ON admin.menu_policy(attribute_key, required_value);

COMMENT ON TABLE  admin.menu_policy                IS 'Admin: 메뉴 접근 정책 — 속성 키/값 조건 매핑';
COMMENT ON COLUMN admin.menu_policy.policy_id      IS '정책 PK (IDENTITY)';
COMMENT ON COLUMN admin.menu_policy.menu_id        IS '대상 메뉴 PK (FK → menu, CASCADE DELETE)';
COMMENT ON COLUMN admin.menu_policy.attribute_key  IS '속성 키 (FK → attribute_definition)';
COMMENT ON COLUMN admin.menu_policy.required_value IS '접근 허용에 필요한 속성 값 (예: ADMIN)';
COMMENT ON COLUMN admin.menu_policy.created_at     IS '생성 일시';
COMMENT ON COLUMN admin.menu_policy.updated_at     IS '수정 일시';
COMMENT ON COLUMN admin.menu_policy.created_by     IS '생성자';
COMMENT ON COLUMN admin.menu_policy.updated_by     IS '수정자';

CREATE TABLE admin.button_policy (
    policy_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    button_id      BIGINT       NOT NULL,
    attribute_key  VARCHAR(80)  NOT NULL,
    required_value VARCHAR(100) NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by     VARCHAR(50),
    updated_by     VARCHAR(50),
    CONSTRAINT uq_admin_button_policy
        UNIQUE (button_id, attribute_key, required_value),
    CONSTRAINT fk_admin_button_policy_btn
        FOREIGN KEY (button_id) REFERENCES admin.button(button_id) ON DELETE CASCADE,
    CONSTRAINT fk_admin_button_policy_attr
        FOREIGN KEY (attribute_key) REFERENCES admin.attribute_definition(attribute_key)
);

CREATE INDEX IF NOT EXISTS ix_admin_button_policy_btn  ON admin.button_policy(button_id);
CREATE INDEX IF NOT EXISTS ix_admin_button_policy_attr ON admin.button_policy(attribute_key, required_value);

COMMENT ON TABLE  admin.button_policy                IS 'Admin: 버튼 노출/실행 정책 — 속성 키/값 조건 매핑';
COMMENT ON COLUMN admin.button_policy.policy_id      IS '정책 PK (IDENTITY)';
COMMENT ON COLUMN admin.button_policy.button_id      IS '대상 버튼 PK (FK → button, CASCADE DELETE)';
COMMENT ON COLUMN admin.button_policy.attribute_key  IS '속성 키 (FK → attribute_definition)';
COMMENT ON COLUMN admin.button_policy.required_value IS '접근 허용에 필요한 속성 값 (예: ADMIN)';
COMMENT ON COLUMN admin.button_policy.created_at     IS '생성 일시';
COMMENT ON COLUMN admin.button_policy.updated_at     IS '수정 일시';
COMMENT ON COLUMN admin.button_policy.created_by     IS '생성자';
COMMENT ON COLUMN admin.button_policy.updated_by     IS '수정자';

-- leaf 메뉴 4개에 role=ADMIN 정책 시드
INSERT INTO admin.menu_policy (menu_id, attribute_key, required_value)
SELECT m.menu_id, 'role', 'ADMIN'
FROM admin.menu m
WHERE m.menu_code IN ('ADMIN_CODE_LIST', 'ADMIN_USER_LIST', 'ADMIN_CUSTOMER_LIST', 'ADMIN_CMS_NOTICE_LIST')
ON CONFLICT (menu_id, attribute_key, required_value) DO NOTHING;

-- 12개 버튼 전체에 role=ADMIN 정책 시드
INSERT INTO admin.button_policy (button_id, attribute_key, required_value)
SELECT b.button_id, 'role', 'ADMIN'
FROM admin.button b
WHERE b.button_code LIKE 'ADMIN_%'
ON CONFLICT (button_id, attribute_key, required_value) DO NOTHING;
