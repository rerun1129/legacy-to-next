-- BMS PMS: 실적 집계 조회 성능 인덱스
--   · ix_freight_line_header_id      — PmsFreightLineAggregateQueryRepository
--       freight_line → freight_header JOIN 시 freight_header_id 필터(10M 행 seq-scan 방지).
--   · ix_freight_line_document_id    — freight_line → financial_document JOIN 가속.
--   · ix_freight_line_tax_no_partial — 세금계산서 발행 기준 partial index(tax_no IS NOT NULL 행만).
--   · ix_freight_line_slip_no_partial — 전표 발행 기준 partial index(slip_no IS NOT NULL 행만).
--   · ix_freight_line_performance_dt — 실적 일자 범위 필터(performance_dt BETWEEN).
--   · ix_freight_header_bl_type_id   — (bl_type, bl_id) 복합 인덱스.
--       GROUP BY header.bl_type, header.bl_id 및 B/L 멤버십 조회 가속.
--
-- 주의(운영): 대형 테이블(freight_line 10M) 일반 CREATE INDEX 는
--   ACCESS EXCLUSIVE 락을 동반한다. 무중단 적용이 필요하면 운영 DB 에는
--   CREATE INDEX CONCURRENTLY 로 수동 선적용 후 본 마이그레이션을 baseline 처리할 것.
--   (Flyway 는 마이그레이션을 단일 트랜잭션으로 실행하므로 CONCURRENTLY 불가 → 여기서는 일반 생성.)
-- ──────────────────────────────────────────────────────────────────────────────

CREATE INDEX IF NOT EXISTS ix_freight_line_header_id
    ON bms.freight_line (freight_header_id);

CREATE INDEX IF NOT EXISTS ix_freight_line_document_id
    ON bms.freight_line (financial_document_id);

CREATE INDEX IF NOT EXISTS ix_freight_line_tax_no_partial
    ON bms.freight_line (freight_header_id)
    WHERE tax_no IS NOT NULL;

CREATE INDEX IF NOT EXISTS ix_freight_line_slip_no_partial
    ON bms.freight_line (freight_header_id)
    WHERE slip_no IS NOT NULL;

CREATE INDEX IF NOT EXISTS ix_freight_line_performance_dt
    ON bms.freight_line (performance_dt);

CREATE INDEX IF NOT EXISTS ix_freight_header_bl_type_id
    ON bms.freight_header (bl_type, bl_id);

COMMENT ON INDEX bms.ix_freight_line_header_id
    IS 'PMS 집계: freight_line→freight_header JOIN 가속';
COMMENT ON INDEX bms.ix_freight_line_document_id
    IS 'PMS 집계: freight_line→financial_document JOIN 가속';
COMMENT ON INDEX bms.ix_freight_line_tax_no_partial
    IS 'PMS 세금계산서 발행 기준: tax_no IS NOT NULL partial index';
COMMENT ON INDEX bms.ix_freight_line_slip_no_partial
    IS 'PMS 전표 발행 기준: slip_no IS NOT NULL partial index';
COMMENT ON INDEX bms.ix_freight_line_performance_dt
    IS 'PMS 실적 일자 범위 필터 가속';
COMMENT ON INDEX bms.ix_freight_header_bl_type_id
    IS 'PMS GROUP BY (bl_type, bl_id) 및 B/L 멤버십 조회 가속';
