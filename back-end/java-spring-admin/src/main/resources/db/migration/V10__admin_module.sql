-- Admin: 모듈 카탈로그.
-- ABAC 정책의 최상위 그룹 단위 (ADMIN, FMS 등).
CREATE TABLE admin.module (
    module_code VARCHAR(30)  PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    sort_order  INT,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  VARCHAR(50),
    updated_by  VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS ix_admin_module_active ON admin.module(active);

COMMENT ON TABLE  admin.module             IS 'Admin: 모듈 카탈로그 — ABAC 정책의 최상위 그룹 단위';
COMMENT ON COLUMN admin.module.module_code IS '모듈 식별 코드 (PK, 예: ADMIN · FMS)';
COMMENT ON COLUMN admin.module.name        IS '모듈 표시명';
COMMENT ON COLUMN admin.module.description IS '모듈 설명';
COMMENT ON COLUMN admin.module.sort_order  IS '정렬 순서';
COMMENT ON COLUMN admin.module.active      IS '활성 여부';
COMMENT ON COLUMN admin.module.created_at  IS '생성 일시';
COMMENT ON COLUMN admin.module.updated_at  IS '수정 일시';
COMMENT ON COLUMN admin.module.created_by  IS '생성자';
COMMENT ON COLUMN admin.module.updated_by  IS '수정자';

INSERT INTO admin.module (module_code, name, description, sort_order) VALUES
    ('ADMIN', 'Admin Console',       '관리 모듈', 1),
    ('FMS',   'Freight Management',  '운영 모듈', 2)
ON CONFLICT (module_code) DO NOTHING;
