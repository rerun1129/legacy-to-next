-- =============================================================================
-- master_bl.mbl_no 검색 인덱스 추가 (Quick Search 통합 자동완성)
-- 작성일: 2026-06-01
-- 실행 환경: fms 스키마, PostgreSQL
-- 실행 방법: 수동 실행 (Flyway 자동 적용 영역 외)
--
-- 배경: Quick Search(House+Master 통합 B/L 번호 자동완성)는 mbl_no LIKE 검색을
--       수행한다. house_bl 은 idx_house_bl_hbl_no 보유하나 master_bl 은 mbl_no
--       인덱스가 없어 풀스캔이 발생하므로 동등한 인덱스를 추가한다.
--       (신규 셋업은 V1__fms_initial_schema.sql 에 동일 인덱스 반영됨)
-- =============================================================================
SET search_path TO fms;

CREATE INDEX IF NOT EXISTS idx_master_bl_mbl_no ON master_bl(mbl_no);
