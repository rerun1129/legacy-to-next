-- =============================================================================
-- PMS hbl_no/mbl_no 전방일치(prefix) 검색 인덱스 — 기존 DB 수동 적용용
--
-- 배경:
--   PMS 실적 조회의 B/L 번호 필터를 부분일치(ILIKE '%x%')에서 전방일치(LIKE 'x%')로
--   전환한다. 본 DB 의 collate 가 en_US.utf8 (C 로캘 아님)이므로 일반 btree 인덱스로는
--   LIKE 'x%' 가 range scan 으로 동작하지 않는다. text_pattern_ops 연산자 클래스 인덱스가
--   있어야 prefix range scan 이 가능하다.
--   입력값은 애플리케이션(java-spring-pms)에서 대문자 정규화하며, SQL 은 case-sensitive
--   LIKE 를 사용한다(B/L 번호는 100% 대문자 저장: HBL.../MBL...).
--
-- 적용:
--   docker exec -e PGPASSWORD=fms_local fms-postgres \
--     psql -U fms -d fms -f /path/pms_blno_prefix_index_2026-06-09.sql
--   (schema/V1 init 파일에도 동일 DDL 반영됨 — 신규 환경은 자동 생성)
-- =============================================================================

CREATE INDEX IF NOT EXISTS ix_house_bl_hbl_no_pattern  ON fms.house_bl  (hbl_no text_pattern_ops);
CREATE INDEX IF NOT EXISTS ix_house_bl_mbl_no_pattern  ON fms.house_bl  (mbl_no text_pattern_ops);
CREATE INDEX IF NOT EXISTS ix_master_bl_mbl_no_pattern ON fms.master_bl (mbl_no text_pattern_ops);
