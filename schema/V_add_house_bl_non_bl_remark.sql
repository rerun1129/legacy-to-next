-- house_bl_non_bl 테이블에 remark 컬럼 추가 (Non B/L 전용)
-- NON_BL의 remark는 house_bl_desc.remark 대신 house_bl_non_bl.remark로 관리한다.
ALTER TABLE fms.house_bl_non_bl
    ADD COLUMN IF NOT EXISTS remark TEXT;
