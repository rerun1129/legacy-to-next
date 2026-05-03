-- house_bl_non_bl 테이블에 Schedule 신규 필드 추가 (Non B/L 전용)
ALTER TABLE fms.house_bl_non_bl
    ADD COLUMN IF NOT EXISTS liner_code       VARCHAR(10),
    ADD COLUMN IF NOT EXISTS liner_name       VARCHAR(100),
    ADD COLUMN IF NOT EXISTS vessel_name      VARCHAR(100),
    ADD COLUMN IF NOT EXISTS voyage_no        VARCHAR(20),
    ADD COLUMN IF NOT EXISTS final_dest_code  VARCHAR(5),
    ADD COLUMN IF NOT EXISTS final_dest_name  VARCHAR(100),
    ADD COLUMN IF NOT EXISTS final_eta        VARCHAR(8),
    ADD COLUMN IF NOT EXISTS rton             NUMERIC(10,3),
    ADD COLUMN IF NOT EXISTS volume_wt_kg     NUMERIC(12,3);
