-- BMS: 조회 성능 인덱스 (대량 데이터 EXPLAIN ANALYZE 실측 기반 추가)
--   · ix_financial_document_type_no — BS-01/02/03 서류 리스트 기본 조회 가속.
--       FinancialDocumentSearchQueryRepository.search() 는
--       WHERE document_type IN (...) ... ORDER BY document_no DESC LIMIT n 형태인데,
--       이를 받칠 인덱스가 없으면 document_no 유니크 인덱스를 역방향 풀스캔하며
--       타 타입(I/P/D/C) 서류 수십만 건을 스킵 → 실측 ~1.8s(100페이지 ~2.5s).
--       (document_type, document_no) 복합 인덱스로 타입 내 document_no desc 정렬 스캔 → limit 조기 종료.
--   · ix_freight_line_customer — 운임 발급/조회 리스트의 customer_code 필터 시
--       freight_line(10M) seq-scan 방지(기존 무인덱스).
--
-- 주의(운영): 대형 테이블(financial_document 4M / freight_line 10M) 일반 CREATE INDEX 는
--   ACCESS EXCLUSIVE 락을 동반한다. 무중단 적용이 필요하면 운영 DB 에는
--   CREATE INDEX CONCURRENTLY 로 수동 선적용 후 본 마이그레이션을 baseline 처리할 것.
--   (Flyway 는 마이그레이션을 단일 트랜잭션으로 실행하므로 CONCURRENTLY 불가 → 여기서는 일반 생성.)
-- ──────────────────────────────────────────────────────────────────────────────

CREATE INDEX IF NOT EXISTS ix_financial_document_type_no
    ON bms.financial_document (document_type, document_no);

CREATE INDEX IF NOT EXISTS ix_freight_line_customer
    ON bms.freight_line (customer_code);

COMMENT ON INDEX bms.ix_financial_document_type_no
    IS 'BS 서류 리스트: document_type 필터 + document_no desc 정렬 조회용';
COMMENT ON INDEX bms.ix_freight_line_customer
    IS '운임 라인 customer_code 필터 조회용';
