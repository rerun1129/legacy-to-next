-- =============================================================================
-- 2026-05-17: switch_bl.switch_bl_no NOT NULL 제약 추가
-- 사유: Switch B/L 화면 토대로 Switch B/L No 를 필수 입력으로 격상.
--      DTO @NotBlank + JPA nullable=false 와 함께 DB 무결성 확보.
-- 적용 순서:
--   1) 기존 NULL 행은 placeholder('UNSET-' || PK)로 채워 제약 위반 회피
--   2) NOT NULL 제약 부여
-- =============================================================================

UPDATE fms.switch_bl
   SET switch_bl_no = 'UNSET-' || switch_bl_id
 WHERE switch_bl_no IS NULL;

ALTER TABLE fms.switch_bl
    ALTER COLUMN switch_bl_no SET NOT NULL;
