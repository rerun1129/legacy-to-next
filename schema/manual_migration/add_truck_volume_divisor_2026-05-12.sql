-- =============================================================================
-- Truck B/L volume_divisor 컬럼 추가 (이슈 #truck-dim)
-- 작성일: 2026-05-12
-- 실행 환경: fms 스키마, PostgreSQL
-- 실행 방법: 수동 실행 (Flyway 자동 적용 영역 외)
--
-- 목적:
--   house_bl_truck 테이블에 volume_divisor 컬럼 신규 추가.
--   Truck B/L Dimension 그리드의 Volume Divisor ComboBox 선택값 저장용.
--   기존 row는 NULL 유지 (FE에서 ?? "CM6000" 기본값 표시).
-- =============================================================================
SET search_path TO fms;

-- ============================================================
-- 1. volume_divisor 컬럼 추가
-- ============================================================
ALTER TABLE house_bl_truck ADD COLUMN IF NOT EXISTS volume_divisor VARCHAR(10);

-- ============================================================
-- 2. COMMENT 추가
-- ============================================================
COMMENT ON COLUMN house_bl_truck.volume_divisor IS 'Truck Dimension 그리드 단위 선택자 (CM/6000 등)';
