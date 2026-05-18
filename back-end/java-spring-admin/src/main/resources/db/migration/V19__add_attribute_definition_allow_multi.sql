ALTER TABLE admin.attribute_definition
    ADD COLUMN IF NOT EXISTS allow_multi BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN admin.attribute_definition.allow_multi IS '다중값 허용 여부 (TRUE = 사용자 attributes에 배열로 여러 값 set 가능)';
