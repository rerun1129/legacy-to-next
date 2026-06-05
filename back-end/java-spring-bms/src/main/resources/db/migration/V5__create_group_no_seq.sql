-- BMS: 금융 서류 그룹 채번 카운터 테이블 (단계 D 그룹화)
--   · 그룹 카테고리(group_category) · 월(yymm) 단위로 시퀀스를 원자적으로 증가시킨다.
--   · 그룹 부여 시 INSERT ... ON CONFLICT (group_category, yymm) DO UPDATE SET last_seq = last_seq + 1
--     RETURNING last_seq 로 동시성 안전하게 다음 번호를 채번한다(재시도·락 불필요, document_no_seq 패턴 복제).
--   · D/C Note 화면은 DEBIT+CREDIT 혼합 그룹을 허용하므로 카테고리를 DCNOTE 하나로 통합한다.
--   · 그룹 번호 형식: 'G' + 카테고리이니셜(I/P/D) + YYMM + 시퀀스5 (예: GI260600001).
--   · 단일 BIGINT PK + 업무키 UNIQUE(group_category, yymm) (신규 테이블 단일 PK 원칙).
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bms.group_no_seq (
    group_no_seq_id BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    group_category  VARCHAR(10) NOT NULL
                        CHECK (group_category IN ('INVOICE', 'PAYMENT', 'DCNOTE')),
    yymm            VARCHAR(4)  NOT NULL,
    last_seq        INTEGER     NOT NULL DEFAULT 0,
    CONSTRAINT uq_group_no_seq UNIQUE (group_category, yymm)
);

COMMENT ON TABLE  bms.group_no_seq IS 'BMS: 금융 서류 그룹 채번 카운터 — 카테고리·월(YYMM) 단위 시퀀스(단계 D)';
COMMENT ON COLUMN bms.group_no_seq.group_no_seq_id IS '그룹 채번 카운터 PK';
COMMENT ON COLUMN bms.group_no_seq.group_category  IS '그룹 카테고리: INVOICE | PAYMENT | DCNOTE(DEBIT+CREDIT 통합)';
COMMENT ON COLUMN bms.group_no_seq.yymm            IS '그룹 부여 연월 YYMM(2+2). 대표 서류 발급일(document_dt) 기준';
COMMENT ON COLUMN bms.group_no_seq.last_seq        IS '해당 카테고리·월의 마지막 발번 시퀀스(원자적 UPSERT로 증가, group_financial_no 끝 5자리)';

-- group_financial_no 검색 필터(BS-01/02/03) + 합류 판단 조회용 부분 인덱스.
-- 미그룹(NULL) 서류 비중이 높을 수 있어 NOT NULL 행만 색인한다.
CREATE INDEX IF NOT EXISTS ix_financial_document_group_no
    ON bms.financial_document (group_financial_no)
    WHERE group_financial_no IS NOT NULL;
