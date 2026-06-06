-- BMS PMS: 서류 생성 기준 조회(document_dt 범위) 가속 인덱스.
--   PMS DOCUMENT_CREATED basis 가 financial_document.document_dt 범위로 모수를 좁히는데
--   기존 인덱스(V7: document_type, document_no)는 일자 범위에 부적합 → 전용 인덱스 추가.
--   freight_line.financial_document_id 는 V8(ix_freight_line_document_id)로 이미 커버됨.
--
-- 주의(운영): 대형 테이블 일반 CREATE INDEX 는 ACCESS EXCLUSIVE 락 동반.
--   무중단 필요 시 운영 DB 에는 CREATE INDEX CONCURRENTLY 로 수동 선적용 후 baseline 처리.
-- ──────────────────────────────────────────────────────────────────────────────

CREATE INDEX IF NOT EXISTS ix_financial_document_document_dt
    ON bms.financial_document (document_dt);

COMMENT ON INDEX bms.ix_financial_document_document_dt
    IS 'PMS 서류 생성 기준: document_dt 범위 필터 가속';
