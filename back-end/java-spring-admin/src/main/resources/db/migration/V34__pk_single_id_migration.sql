-- V34: admin.attribute_value + admin.admin_user_permission 단일 Auto-inc PK 전환
--      + permission_preset_attribute_value FK 재매핑 (attribute_key/av_value → attribute_value_id)

-- =============================================================================
-- 1. 데이터 백업 (영구 보존 — DROP 금지)
-- =============================================================================

CREATE TABLE admin.attribute_value_backup
    AS SELECT * FROM admin.attribute_value;

-- admin_user_permission 은 V16 에서 DROP 되었으므로 빈 백업 테이블만 생성한다.
-- V4 DDL 시그니처 기준: (user_id BIGINT, permission VARCHAR(30))
CREATE TABLE admin.admin_user_permission_backup (
    user_id    BIGINT      NOT NULL,
    permission VARCHAR(30) NOT NULL
);

-- =============================================================================
-- 2. attribute_value 단일 PK 전환 + permission_preset_attribute_value FK 재매핑 (인터리브)
--    PK DROP 전에 의존 FK / UNIQUE / INDEX 를 먼저 정리해야 PostgreSQL 이 허용
-- =============================================================================

-- 2-1. attribute_value 에 단일 id 컬럼 추가 (GENERATED ALWAYS AS IDENTITY, V11 attribute_definition 과 동일 컨벤션)
ALTER TABLE admin.attribute_value
    ADD COLUMN id BIGINT GENERATED ALWAYS AS IDENTITY;

-- 2-2. permission_preset_attribute_value 에 attribute_value_id 컬럼 추가 (NULL 허용으로 시작 — 후속 UPDATE 로 채움)
ALTER TABLE admin.permission_preset_attribute_value
    ADD COLUMN attribute_value_id BIGINT;

-- 2-3. 기존 (attribute_key, av_value) → attribute_value.id 매핑
UPDATE admin.permission_preset_attribute_value ppav
SET attribute_value_id = av.id
FROM admin.attribute_value av
WHERE av.attribute_key = ppav.attribute_key
  AND av.value         = ppav.av_value;

-- 2-4. attribute_value_id NOT NULL 강제
ALTER TABLE admin.permission_preset_attribute_value
    ALTER COLUMN attribute_value_id SET NOT NULL;

-- 2-5. ★ 의존 FK / UNIQUE / INDEX 먼저 DROP (PK DROP 의 사전조건)
ALTER TABLE admin.permission_preset_attribute_value
    DROP CONSTRAINT IF EXISTS fk_ppav_attr_value;

ALTER TABLE admin.permission_preset_attribute_value
    DROP CONSTRAINT IF EXISTS uq_ppav_preset_av;

DROP INDEX IF EXISTS admin.idx_ppav_attribute_key_value;

-- 2-6. 구 컬럼 DROP
ALTER TABLE admin.permission_preset_attribute_value
    DROP COLUMN attribute_key,
    DROP COLUMN av_value;

-- 2-7. ★ 이제 attribute_value 복합 PK 안전하게 DROP (의존 FK 가 사라졌으므로)
ALTER TABLE admin.attribute_value
    DROP CONSTRAINT pk_admin_attribute_value;

-- 2-8. attribute_value 새 PK (단일 id)
ALTER TABLE admin.attribute_value
    ADD CONSTRAINT pk_admin_attribute_value PRIMARY KEY (id);

-- 2-9. (attribute_key, value) 보조 UNIQUE 제약 — 의미(무결성) 유지
ALTER TABLE admin.attribute_value
    ADD CONSTRAINT uq_admin_attribute_value_key_value UNIQUE (attribute_key, value);

-- fk_admin_attribute_value_key 는 기존 그대로 유지 (attribute_key → attribute_definition.attribute_key, V11 정의)

-- 2-10. permission_preset_attribute_value 새 FK / UNIQUE / INDEX
ALTER TABLE admin.permission_preset_attribute_value
    ADD CONSTRAINT fk_ppav_attribute_value
        FOREIGN KEY (attribute_value_id) REFERENCES admin.attribute_value(id) ON DELETE CASCADE;

ALTER TABLE admin.permission_preset_attribute_value
    ADD CONSTRAINT uq_ppav_preset_av UNIQUE (preset_id, attribute_value_id);

CREATE INDEX idx_ppav_attribute_value_id
    ON admin.permission_preset_attribute_value (attribute_value_id);

-- =============================================================================
-- 3. 검증 (DO 블록 — 불일치 시 마이그레이션 롤백)
-- =============================================================================

DO $$
DECLARE
    v_backup_count     BIGINT;
    v_live_count       BIGINT;
    v_null_av_count    BIGINT;
    v_aup_backup_count BIGINT;
BEGIN
    SELECT COUNT(*) INTO v_backup_count FROM admin.attribute_value_backup;
    SELECT COUNT(*) INTO v_live_count   FROM admin.attribute_value;
    IF v_backup_count <> v_live_count THEN
        RAISE EXCEPTION 'attribute_value row count mismatch: backup=% live=%', v_backup_count, v_live_count;
    END IF;

    -- admin_user_permission_backup 는 V16 에서 테이블이 DROP 되어 0건이 정상
    SELECT COUNT(*) INTO v_aup_backup_count FROM admin.admin_user_permission_backup;
    IF v_aup_backup_count <> 0 THEN
        RAISE EXCEPTION 'admin_user_permission_backup should be empty (table was dropped in V16), found % rows', v_aup_backup_count;
    END IF;

    SELECT COUNT(*) INTO v_null_av_count
    FROM admin.permission_preset_attribute_value
    WHERE attribute_value_id IS NULL;
    IF v_null_av_count <> 0 THEN
        RAISE EXCEPTION 'permission_preset_attribute_value has % rows with NULL attribute_value_id', v_null_av_count;
    END IF;
END $$;
