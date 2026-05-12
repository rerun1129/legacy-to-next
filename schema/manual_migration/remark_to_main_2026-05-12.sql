-- =============================================================================
-- remark 컬럼 desc 자식 → 본체 ext 이전 (이슈 #12)
-- 작성일: 2026-05-12
-- 실행 환경: fms 스키마, PostgreSQL
-- 실행 방법: 수동 실행 (Flyway 자동 적용 영역 외)
--
-- 영향 5쌍:
--   house_bl_sea_desc.remark   → house_bl_sea.remark
--   house_bl_air_desc.remark   → house_bl_air.remark
--   house_bl_truck_desc.remark → house_bl_truck.remark
--   master_bl_sea_desc.remark  → master_bl_sea.remark
--   master_bl_air_desc.remark  → master_bl_air.remark
--
-- 타입 변경: TEXT → VARCHAR(1000)
-- (메인 엔티티 컬럼 메모리 점유 제한 — 무제한 TEXT 회피)
-- =============================================================================
SET search_path TO fms;

-- ============================================================
-- 1. 본체에 remark 컬럼 추가 (VARCHAR(1000))
-- ============================================================
ALTER TABLE house_bl_sea   ADD COLUMN IF NOT EXISTS remark VARCHAR(1000);
ALTER TABLE house_bl_air   ADD COLUMN IF NOT EXISTS remark VARCHAR(1000);
ALTER TABLE house_bl_truck ADD COLUMN IF NOT EXISTS remark VARCHAR(1000);
ALTER TABLE master_bl_sea  ADD COLUMN IF NOT EXISTS remark VARCHAR(1000);
ALTER TABLE master_bl_air  ADD COLUMN IF NOT EXISTS remark VARCHAR(1000);

-- ============================================================
-- 2. desc 테이블 → 본체로 데이터 이관
-- ============================================================
UPDATE house_bl_sea s
   SET remark = d.remark
  FROM house_bl_sea_desc d
 WHERE d.house_bl_sea_id = s.house_bl_sea_id
   AND d.remark IS NOT NULL;

UPDATE house_bl_air a
   SET remark = d.remark
  FROM house_bl_air_desc d
 WHERE d.house_bl_air_id = a.house_bl_air_id
   AND d.remark IS NOT NULL;

UPDATE house_bl_truck t
   SET remark = d.remark
  FROM house_bl_truck_desc d
 WHERE d.house_bl_truck_id = t.house_bl_truck_id
   AND d.remark IS NOT NULL;

UPDATE master_bl_sea s
   SET remark = d.remark
  FROM master_bl_sea_desc d
 WHERE d.master_bl_sea_id = s.master_bl_sea_id
   AND d.remark IS NOT NULL;

UPDATE master_bl_air a
   SET remark = d.remark
  FROM master_bl_air_desc d
 WHERE d.master_bl_air_id = a.master_bl_air_id
   AND d.remark IS NOT NULL;

-- ============================================================
-- 3. desc 테이블에서 remark 컬럼 제거
-- ============================================================
ALTER TABLE house_bl_sea_desc   DROP COLUMN IF EXISTS remark;
ALTER TABLE house_bl_air_desc   DROP COLUMN IF EXISTS remark;
ALTER TABLE house_bl_truck_desc DROP COLUMN IF EXISTS remark;
ALTER TABLE master_bl_sea_desc  DROP COLUMN IF EXISTS remark;
ALTER TABLE master_bl_air_desc  DROP COLUMN IF EXISTS remark;

-- ============================================================
-- 4. 본체 remark COMMENT 추가
-- ============================================================
COMMENT ON COLUMN house_bl_sea.remark   IS '비고';
COMMENT ON COLUMN house_bl_air.remark   IS '비고';
COMMENT ON COLUMN house_bl_truck.remark IS '비고';
COMMENT ON COLUMN master_bl_sea.remark  IS '비고';
COMMENT ON COLUMN master_bl_air.remark  IS '비고';
