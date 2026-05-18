-- Admin: ABAC 속성 정의 카탈로그.
-- attribute_definition: 속성 키 및 타입 정의 (surrogate PK + UNIQUE attribute_key).
-- attribute_value: ENUM 타입 속성의 허용 값 목록.
CREATE TABLE admin.attribute_definition (
    attribute_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    attribute_key VARCHAR(80)  NOT NULL,
    name          VARCHAR(200) NOT NULL,
    description   VARCHAR(500),
    value_type    VARCHAR(20)  NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by    VARCHAR(50),
    updated_by    VARCHAR(50),
    CONSTRAINT uq_admin_attribute_definition_key UNIQUE (attribute_key),
    CONSTRAINT ck_admin_attribute_definition_value_type
        CHECK (value_type IN ('STRING', 'NUMBER', 'BOOLEAN', 'ENUM'))
);

COMMENT ON TABLE  admin.attribute_definition               IS 'Admin: ABAC 속성 정의 카탈로그';
COMMENT ON COLUMN admin.attribute_definition.attribute_id  IS '속성 PK (IDENTITY)';
COMMENT ON COLUMN admin.attribute_definition.attribute_key IS '속성 키 (UNIQUE, 예: role · department)';
COMMENT ON COLUMN admin.attribute_definition.name          IS '속성 표시명';
COMMENT ON COLUMN admin.attribute_definition.description   IS '속성 설명';
COMMENT ON COLUMN admin.attribute_definition.value_type    IS '값 타입: STRING | NUMBER | BOOLEAN | ENUM';
COMMENT ON COLUMN admin.attribute_definition.active        IS '활성 여부';
COMMENT ON COLUMN admin.attribute_definition.created_at    IS '생성 일시';
COMMENT ON COLUMN admin.attribute_definition.updated_at    IS '수정 일시';
COMMENT ON COLUMN admin.attribute_definition.created_by    IS '생성자';
COMMENT ON COLUMN admin.attribute_definition.updated_by    IS '수정자';

CREATE TABLE admin.attribute_value (
    attribute_key VARCHAR(80)  NOT NULL,
    value         VARCHAR(100) NOT NULL,
    label         VARCHAR(200) NOT NULL,
    sort_order    INT,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by    VARCHAR(50),
    updated_by    VARCHAR(50),
    CONSTRAINT pk_admin_attribute_value
        PRIMARY KEY (attribute_key, value),
    CONSTRAINT fk_admin_attribute_value_key
        FOREIGN KEY (attribute_key) REFERENCES admin.attribute_definition(attribute_key)
);

COMMENT ON TABLE  admin.attribute_value               IS 'Admin: ENUM 타입 속성의 허용 값 목록';
COMMENT ON COLUMN admin.attribute_value.attribute_key IS '속성 키 (FK → attribute_definition.attribute_key)';
COMMENT ON COLUMN admin.attribute_value.value         IS '속성 값 (예: ADMIN · USER)';
COMMENT ON COLUMN admin.attribute_value.label         IS '속성 값 표시명';
COMMENT ON COLUMN admin.attribute_value.sort_order    IS '정렬 순서';
COMMENT ON COLUMN admin.attribute_value.active        IS '활성 여부';
COMMENT ON COLUMN admin.attribute_value.created_at    IS '생성 일시';
COMMENT ON COLUMN admin.attribute_value.updated_at    IS '수정 일시';
COMMENT ON COLUMN admin.attribute_value.created_by    IS '생성자';
COMMENT ON COLUMN admin.attribute_value.updated_by    IS '수정자';

INSERT INTO admin.attribute_definition (attribute_key, name, value_type, active) VALUES
    ('role',       '역할', 'ENUM',   TRUE),
    ('department', '부서', 'STRING', TRUE),
    ('region',     '지역', 'STRING', TRUE),
    ('level',      '레벨', 'NUMBER', TRUE)
ON CONFLICT (attribute_key) DO NOTHING;

INSERT INTO admin.attribute_value (attribute_key, value, label, sort_order, active) VALUES
    ('role', 'ADMIN', '관리자',      1, TRUE),
    ('role', 'USER',  '일반 사용자', 2, TRUE)
ON CONFLICT (attribute_key, value) DO NOTHING;
