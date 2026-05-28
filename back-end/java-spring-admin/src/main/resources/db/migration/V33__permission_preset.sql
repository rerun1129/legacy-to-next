-- permission_preset: 재활용 가능한 권한 묶음 템플릿
CREATE TABLE admin.permission_preset (
    id          BIGSERIAL    PRIMARY KEY,
    code        VARCHAR(64)  NOT NULL,
    name        VARCHAR(128) NOT NULL,
    description VARCHAR(512),
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by  VARCHAR(50),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_by  VARCHAR(50),
    CONSTRAINT uq_permission_preset_code UNIQUE (code),
    CONSTRAINT chk_permission_preset_code CHECK (code LIKE 'PRESET\_%')
);

-- permission_preset_attribute_value: 프리셋이 보유한 attribute_value 묶음 (N:M)
-- attribute_value 의 PK는 (attribute_key, value) 복합키이므로 두 컬럼 모두 FK 참조
CREATE TABLE admin.permission_preset_attribute_value (
    id             BIGSERIAL    PRIMARY KEY,
    preset_id      BIGINT       NOT NULL,
    attribute_key  VARCHAR(80)  NOT NULL,
    av_value       VARCHAR(100) NOT NULL,
    CONSTRAINT fk_ppav_preset         FOREIGN KEY (preset_id)                       REFERENCES admin.permission_preset(id)             ON DELETE CASCADE,
    CONSTRAINT fk_ppav_attr_value     FOREIGN KEY (attribute_key, av_value)         REFERENCES admin.attribute_value(attribute_key, value) ON DELETE CASCADE,
    CONSTRAINT uq_ppav_preset_av      UNIQUE (preset_id, attribute_key, av_value)
);

-- user_permission_preset: 사용자별 프리셋 부여 (N:M)
CREATE TABLE admin.user_permission_preset (
    id        BIGSERIAL PRIMARY KEY,
    user_id   BIGINT    NOT NULL,
    preset_id BIGINT    NOT NULL,
    CONSTRAINT fk_upp_user         FOREIGN KEY (user_id)   REFERENCES admin.admin_user(user_id)         ON DELETE CASCADE,
    CONSTRAINT fk_upp_preset       FOREIGN KEY (preset_id) REFERENCES admin.permission_preset(id)  ON DELETE RESTRICT,
    CONSTRAINT uq_upp_user_preset  UNIQUE (user_id, preset_id)
);

-- 보조 인덱스
CREATE INDEX idx_permission_preset_code   ON admin.permission_preset (code);
CREATE INDEX idx_permission_preset_active ON admin.permission_preset (active);

CREATE INDEX idx_ppav_preset_id           ON admin.permission_preset_attribute_value (preset_id);
CREATE INDEX idx_ppav_attribute_key_value ON admin.permission_preset_attribute_value (attribute_key, av_value);

CREATE INDEX idx_upp_user_id   ON admin.user_permission_preset (user_id);
CREATE INDEX idx_upp_preset_id ON admin.user_permission_preset (preset_id);
