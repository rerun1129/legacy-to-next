-- BMS: 금융 서류 채번 카운터 테이블 (§6.11)
--   · 종류(document_type) · 월(yymm) 단위로 시퀀스를 원자적으로 증가시킨다.
--   · 발행 시 INSERT ... ON CONFLICT (document_type, yymm) DO UPDATE SET last_seq = last_seq + 1
--     RETURNING last_seq 로 동시성 안전하게 다음 번호를 채번한다(재시도·락 불필요).
--   · 종류·월이 바뀌면 행이 새로 생기므로 시퀀스가 자동 리셋된다.
--   · 단일 BIGINT PK + 업무키 UNIQUE(document_type, yymm) (신규 테이블 단일 PK 원칙).
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bms.document_no_seq (
    document_no_seq_id BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    document_type      VARCHAR(10) NOT NULL
                           CHECK (document_type IN ('INVOICE', 'PAYMENT', 'DEBIT', 'CREDIT')),
    yymm               VARCHAR(4)  NOT NULL,
    last_seq           INTEGER     NOT NULL DEFAULT 0,
    CONSTRAINT uq_document_no_seq UNIQUE (document_type, yymm)
);

COMMENT ON TABLE  bms.document_no_seq IS 'BMS: 금융 서류 채번 카운터 — 종류·월(YYMM) 단위 시퀀스(§6.11)';
COMMENT ON COLUMN bms.document_no_seq.document_no_seq_id IS '채번 카운터 PK';
COMMENT ON COLUMN bms.document_no_seq.document_type      IS '서류 종류: INVOICE | PAYMENT | DEBIT | CREDIT';
COMMENT ON COLUMN bms.document_no_seq.yymm               IS '발급 연월 YYMM(2+2). 서류 발급일(document_dt) 기준';
COMMENT ON COLUMN bms.document_no_seq.last_seq           IS '해당 종류·월의 마지막 발번 시퀀스(원자적 UPSERT로 증가, document_no 끝 5자리)';
