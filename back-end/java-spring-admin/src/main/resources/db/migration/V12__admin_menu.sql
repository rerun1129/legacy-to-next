-- Admin: 메뉴 카탈로그.
-- 트리 구조(self-referencing parent_id), module_code로 모듈 소속 표시.
CREATE TABLE admin.menu (
    menu_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    menu_code   VARCHAR(80)  NOT NULL,
    parent_id   BIGINT,
    path        VARCHAR(200),
    label       VARCHAR(200) NOT NULL,
    label_en    VARCHAR(200),
    icon        VARCHAR(100),
    sort_order  INT,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    module_code VARCHAR(40)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  VARCHAR(50),
    updated_by  VARCHAR(50),
    CONSTRAINT uq_admin_menu_code   UNIQUE (menu_code),
    CONSTRAINT fk_admin_menu_parent FOREIGN KEY (parent_id)    REFERENCES admin.menu(menu_id),
    CONSTRAINT fk_admin_menu_module FOREIGN KEY (module_code)  REFERENCES admin.module(module_code)
);

CREATE INDEX IF NOT EXISTS ix_admin_menu_module_active ON admin.menu(module_code, active);
CREATE INDEX IF NOT EXISTS ix_admin_menu_parent        ON admin.menu(parent_id);

COMMENT ON TABLE  admin.menu             IS 'Admin: 메뉴 카탈로그 — 트리 구조 (자기 참조)';
COMMENT ON COLUMN admin.menu.menu_id     IS '메뉴 PK (IDENTITY)';
COMMENT ON COLUMN admin.menu.menu_code   IS '메뉴 코드 (UNIQUE, 예: ADMIN_CODE_LIST)';
COMMENT ON COLUMN admin.menu.parent_id   IS '부모 메뉴 PK (NULL = 루트)';
COMMENT ON COLUMN admin.menu.path        IS '프론트엔드 라우트 경로 (예: /admin/code/list)';
COMMENT ON COLUMN admin.menu.label       IS '메뉴 표시명 (한국어)';
COMMENT ON COLUMN admin.menu.label_en    IS '메뉴 표시명 (영어, 옵셔널)';
COMMENT ON COLUMN admin.menu.icon        IS '아이콘 식별자 (Lucide 등)';
COMMENT ON COLUMN admin.menu.sort_order  IS '정렬 순서';
COMMENT ON COLUMN admin.menu.active      IS '활성 여부';
COMMENT ON COLUMN admin.menu.module_code IS '소속 모듈 코드 (FK → module)';
COMMENT ON COLUMN admin.menu.created_at  IS '생성 일시';
COMMENT ON COLUMN admin.menu.updated_at  IS '수정 일시';
COMMENT ON COLUMN admin.menu.created_by  IS '생성자';
COMMENT ON COLUMN admin.menu.updated_by  IS '수정자';

-- 부모 메뉴 4개 먼저 삽입
INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, module_code) VALUES
    ('ADMIN_CODE',       NULL, NULL, 'Code Master', 'KeyRound',  1, 'ADMIN'),
    ('ADMIN_USER',       NULL, NULL, '사용자 관리', 'UserCog',   2, 'ADMIN'),
    ('ADMIN_CUSTOMER',   NULL, NULL, 'Customer',    'Building2', 3, 'ADMIN'),
    ('ADMIN_CMS_NOTICE', NULL, NULL, '공지사항',    'Megaphone', 4, 'ADMIN')
ON CONFLICT (menu_code) DO NOTHING;

-- 자식 메뉴 4개 (parent_id 서브쿼리로 참조)
INSERT INTO admin.menu (menu_code, parent_id, path, label, icon, sort_order, module_code) VALUES
    ('ADMIN_CODE_LIST',
        (SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CODE'),
        '/admin/code/list', 'List', 'List', 1, 'ADMIN'),
    ('ADMIN_USER_LIST',
        (SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_USER'),
        '/admin/user/list', 'List', 'List', 1, 'ADMIN'),
    ('ADMIN_CUSTOMER_LIST',
        (SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CUSTOMER'),
        '/admin/customer/list', 'List', 'List', 1, 'ADMIN'),
    ('ADMIN_CMS_NOTICE_LIST',
        (SELECT menu_id FROM admin.menu WHERE menu_code = 'ADMIN_CMS_NOTICE'),
        '/admin/cms/notice/list', 'List', 'List', 1, 'ADMIN')
ON CONFLICT (menu_code) DO NOTHING;
