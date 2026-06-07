-- BMS PMS Mart: 증분 동기화 변경 탐지(updated_at 워터마크) 인덱스
--   java-spring-pms 의 MongoDB Mart 증분 스케줄러는 "updated_at > 마지막동기화시각" 으로
--   바뀐 B/L 을 탐지한다(PmsMartChangeDetector). 인덱스가 없으면 매 틱마다
--   freight_line(10M)·financial_document(4M) 풀스캔 → 운영 OLTP 부하. 최근 슬라이스만
--   레인지 스캔하도록 updated_at 인덱스를 추가한다.
--   · ix_freight_header_updated_at       — 헤더(B/L 1:1) 변경 탐지
--   · ix_freight_line_updated_at         — 운임 라인 변경 탐지(추가/수정)
--   · ix_financial_document_updated_at   — 금융 서류 변경 탐지(서류→라인→헤더 fan-out 기점)
--   fms 측(house_bl/master_bl) updated_at 인덱스는 schema/V1 참조.
--
-- 주의(운영): 대형 테이블(freight_line 10M) 일반 CREATE INDEX 는 ACCESS EXCLUSIVE 락 동반.
--   무중단 적용이 필요하면 운영 DB 에는 CREATE INDEX CONCURRENTLY 로 수동 선적용 후 baseline 처리.
--   (Flyway 는 마이그레이션을 단일 트랜잭션으로 실행하므로 CONCURRENTLY 불가 → 여기서는 일반 생성.)
-- ──────────────────────────────────────────────────────────────────────────────

CREATE INDEX IF NOT EXISTS ix_freight_header_updated_at
    ON bms.freight_header (updated_at);

CREATE INDEX IF NOT EXISTS ix_freight_line_updated_at
    ON bms.freight_line (updated_at);

CREATE INDEX IF NOT EXISTS ix_financial_document_updated_at
    ON bms.financial_document (updated_at);

COMMENT ON INDEX bms.ix_freight_header_updated_at
    IS 'PMS Mart 증분: freight_header.updated_at 워터마크 변경 탐지';
COMMENT ON INDEX bms.ix_freight_line_updated_at
    IS 'PMS Mart 증분: freight_line.updated_at 워터마크 변경 탐지';
COMMENT ON INDEX bms.ix_financial_document_updated_at
    IS 'PMS Mart 증분: financial_document.updated_at 워터마크 변경 탐지(서류 fan-out 기점)';
