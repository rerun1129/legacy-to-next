-- BMS: 발급 번호 채번 카운터 테이블 (단계 E 세금계산서·전표 발급)
--   · 발급 종류(issue_type) · 월(yymm) 단위로 시퀀스를 원자적으로 증가시킨다.
--   · 발급 시 INSERT ... ON CONFLICT (issue_type, yymm) DO UPDATE SET last_seq = last_seq + 1
--     RETURNING last_seq 로 동시성 안전하게 다음 번호를 채번한다(재시도·락 불필요).
--   · 종류·월이 바뀌면 행이 새로 생기므로 시퀀스가 자동 리셋된다.
--   · 단일 BIGINT PK + 업무키 UNIQUE(issue_type, yymm) (신규 테이블 단일 PK 원칙).
--   · issue_type: TAX=세금계산서(번호 T+YYMM+seq5) / SLIP=전표(S+YYMM+seq5).
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bms.issue_no_seq (
    issue_no_seq_id BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    issue_type      VARCHAR(4)  NOT NULL
                        CHECK (issue_type IN ('TAX', 'SLIP')),
    yymm            VARCHAR(4)  NOT NULL,
    last_seq        INTEGER     NOT NULL DEFAULT 0,
    CONSTRAINT uq_issue_no_seq UNIQUE (issue_type, yymm)
);

COMMENT ON TABLE  bms.issue_no_seq IS 'BMS: 발급 번호 채번 카운터 — 종류·월(YYMM) 단위 시퀀스(단계 E)';
COMMENT ON COLUMN bms.issue_no_seq.issue_no_seq_id IS '발급 채번 카운터 PK';
COMMENT ON COLUMN bms.issue_no_seq.issue_type      IS '발급 종류: TAX(세금계산서) | SLIP(전표)';
COMMENT ON COLUMN bms.issue_no_seq.yymm            IS '발급 연월 YYMM(2+2). 발급일(issue_dt) 기준';
COMMENT ON COLUMN bms.issue_no_seq.last_seq        IS '해당 종류·월의 마지막 발번 시퀀스(원자적 UPSERT로 증가)';
