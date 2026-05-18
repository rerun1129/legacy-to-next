-- Admin: ABAC 속성 정의 카탈로그.
-- attribute_definition: 속성 키 및 타입 정의.
-- attribute_value: ENUM 타입 속성의 허용 값 목록.
CREATE TABLE admin.attribute_definition (
    attribute_key VARCHAR(50) PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    value_type    VARCHAR(20)  NOT NULL,
    allow_multi   BOOLEAN      NOT NULL DEFAULT FALSE,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by    VARCHAR(50),
    updated_by    VARCHAR(50),
    CONSTRAINT ck_admin_attribute_definition_value_type
        CHECK (value_type IN ('STRING', 'NUMBER', 'BOOLEAN', 'ENUM'))
);

COMMENT ON TABLE  admin.attribute_definition              IS 'Admin: ABAC 속성 정의 카탈로그';
COMMENT ON COLUMN admin.attribute_definition.attribute_key IS '속성 키 (PK, 예: role · department)';
COMMENT ON COLUMN admin.attribute_definition.name          IS '속성 표시명';
COMMENT ON COLUMN admin.attribute_definition.value_type    IS '값 타입: STRING | NUMBER | BOOLEAN | ENUM';
COMMENT ON COLUMN admin.attribute_definition.allow_multi   IS '다중 값 허용 여부';
COMMENT ON COLUMN admin.attribute_definition.active        IS '활성 여부';
COMMENT ON COLUMN admin.attribute_definition.created_at    IS '생성 일시';
COMMENT ON COLUMN admin.attribute_definition.updated_at    IS '수정 일시';
COMMENT ON COLUMN admin.attribute_definition.created_by    IS '생성자';
COMMENT ON COLUMN admin.attribute_definition.updated_by    IS '수정자';

CREATE TABLE admin.attribute_value (
    attribute_key VARCHAR(50)  NOT NULL,
    value         VARCHAR(100) NOT NULL,
    label         VARCHAR(200),
    sort_order    INT,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_admin_attribute_value
        PRIMARY KEY (attribute_key, value),
    CONSTRAINT fk_admin_attribute_value_key
        FOREIGN KEY (attribute_key) REFERENCES admin.attribute_definition(attribute_key)
);

COMMENT ON TABLE  admin.attribute_value               IS 'Admin: ENUM 타입 속성의 허용 값 목록';
COMMENT ON COLUMN admin.attribute_value.attribute_key IS '속성 키 (FK → attribute_definition)';
COMMENT ON COLUMN admin.attribute_value.value         IS '속성 값 (예: ADMIN · USER)';
COMMENT ON COLUMN admin.attribute_value.label         IS '속성 값 표시명';
COMMENT ON COLUMN admin.attribute_value.sort_order    IS '정렬 순서';
COMMENT ON COLUMN admin.attribute_value.active        IS '활성 여부';

INSERT INTO admin.attribute_definition (attribute_key, name, value_type, allow_multi) VALUES
    ('role',       '역할', 'ENUM',   FALSE),
    ('department', '부서', 'STRING', FALSE),
    ('region',     '지역', 'STRING', TRUE),
    ('level',      '레벨', 'NUMBER', FALSE)
ON CONFLICT (attribute_key) DO NOTHING;

INSERT INTO admin.attribute_value (attribute_key, value, label, sort_order) VALUES
    ('role', 'ADMIN', '관리자',      1),
    ('role', 'USER',  '일반 사용자', 2)
ON CONFLICT (attribute_key, value) DO NOTHING;
