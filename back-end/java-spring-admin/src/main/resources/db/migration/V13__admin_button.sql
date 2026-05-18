-- Admin: 버튼 카탈로그.
-- 각 leaf 메뉴에 속한 액션 버튼 정의 (CREATE/UPDATE/DELETE 등).
CREATE TABLE admin.button (
    button_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    menu_id     BIGINT       NOT NULL,
    button_code VARCHAR(100) NOT NULL,
    label       VARCHAR(100) NOT NULL,
    action_type VARCHAR(20)  NOT NULL,
    api_method  VARCHAR(10),
    api_path    VARCHAR(200),
    sort_order  INT,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  VARCHAR(50),
    updated_by  VARCHAR(50),
    CONSTRAINT uq_admin_button_code   UNIQUE (button_code),
    CONSTRAINT ck_admin_button_action_type
        CHECK (action_type IN ('CREATE', 'UPDATE', 'DELETE', 'EXPORT', 'CUSTOM')),
    CONSTRAINT fk_admin_button_menu
        FOREIGN KEY (menu_id) REFERENCES admin.menu(menu_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_admin_button_menu_active ON admin.button(menu_id, active);

COMMENT ON TABLE  admin.button             IS 'Admin: 버튼 카탈로그 — 메뉴별 액션 버튼 정의';
COMMENT ON COLUMN admin.button.button_id   IS '버튼 PK (IDENTITY)';
COMMENT ON COLUMN admin.button.menu_id     IS '소속 메뉴 PK (FK → menu, CASCADE DELETE)';
COMMENT ON COLUMN admin.button.button_code IS '버튼 코드 (UNIQUE, 예: ADMIN_CODE_LIST_CREATE)';
COMMENT ON COLUMN admin.button.label       IS '버튼 표시명';
COMMENT ON COLUMN admin.button.action_type IS '액션 타입: CREATE | UPDATE | DELETE | EXPORT | CUSTOM';
COMMENT ON COLUMN admin.button.api_method  IS 'API HTTP 메서드 (예: POST · PUT · DELETE, 옵셔널)';
COMMENT ON COLUMN admin.button.api_path    IS 'API 경로 (옵셔널)';
COMMENT ON COLUMN admin.button.sort_order  IS '정렬 순서';
COMMENT ON COLUMN admin.button.active      IS '활성 여부';
COMMENT ON COLUMN admin.button.created_at  IS '생성 일시';
COMMENT ON COLUMN admin.button.updated_at  IS '수정 일시';
COMMENT ON COLUMN admin.button.created_by  IS '생성자';
COMMENT ON COLUMN admin.button.updated_by  IS '수정자';

INSERT INTO admin.button (menu_id, button_code, label, action_type, sort_order) VALUES
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_LIST'),       'ADMIN_CODE_LIST_CREATE',       '신규', 'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_LIST'),       'ADMIN_CODE_LIST_UPDATE',       '수정', 'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE_LIST'),       'ADMIN_CODE_LIST_DELETE',       '삭제', 'DELETE', 3),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_USER_LIST'),       'ADMIN_USER_LIST_CREATE',       '신규', 'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_USER_LIST'),       'ADMIN_USER_LIST_UPDATE',       '수정', 'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_USER_LIST'),       'ADMIN_USER_LIST_DELETE',       '삭제', 'DELETE', 3),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CUSTOMER_LIST'),   'ADMIN_CUSTOMER_LIST_CREATE',   '신규', 'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CUSTOMER_LIST'),   'ADMIN_CUSTOMER_LIST_UPDATE',   '수정', 'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CUSTOMER_LIST'),   'ADMIN_CUSTOMER_LIST_DELETE',   '삭제', 'DELETE', 3),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CMS_NOTICE_LIST'), 'ADMIN_CMS_NOTICE_LIST_CREATE', '신규', 'CREATE', 1),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CMS_NOTICE_LIST'), 'ADMIN_CMS_NOTICE_LIST_UPDATE', '수정', 'UPDATE', 2),
    ((SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CMS_NOTICE_LIST'), 'ADMIN_CMS_NOTICE_LIST_DELETE', '삭제', 'DELETE', 3)
ON CONFLICT (button_code) DO NOTHING;
