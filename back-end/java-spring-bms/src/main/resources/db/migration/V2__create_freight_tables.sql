-- BMS: 운임·정산 핵심 3테이블 생성.
-- 생성 순서: freight_header → financial_document → freight_line (FK 의존 순)
-- cross-schema(admin/fms) 하드 FK 없음 — 코드/식별자 컬럼으로만 참조(§6.4).
-- 감사 컬럼(created_at, updated_at, created_by, updated_by)은 BaseJpaEntity 컬럼명과 일치.
-- soft delete 패턴 미사용(BMS 엔티티는 소프트 삭제 대신 논리 상태 관리).
-- 날짜 컬럼(*_date, *_dt)은 VARCHAR(8) yyyyMMdd 저장 (FMS/ADMIN 컨벤션: house_bl.etd/eta·admin.exchange_date). 시각은 TIMESTAMP.

-- ──────────────────────────────────────────────────────────────────────────────
-- B-01 운임 헤더 (Freight Header)
--   · B/L 1:1, 다형 참조(bl_type + bl_id)
--   · 당사자 3종: actual_customer_code / liner_code / settle_partner_code
--   · 환율 기준(매출/매입/USD): *_rate_dt(일자) / *_rate_currency_code(통화) / *_rate(환율)
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bms.freight_header (
    freight_header_id     BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    bl_type               VARCHAR(10)   NOT NULL
                              CHECK (bl_type IN ('HOUSE', 'MASTER')),
    bl_id                 VARCHAR(50)   NOT NULL,
    actual_customer_code  VARCHAR(40),
    liner_code            VARCHAR(40),
    settle_partner_code   VARCHAR(40),
    sell_rate_dt             VARCHAR(8),
    buy_rate_dt              VARCHAR(8),
    usd_rate_dt              VARCHAR(8),
    sell_rate_currency_code  VARCHAR(10),
    buy_rate_currency_code   VARCHAR(10),
    sell_rate                NUMERIC(18,6),
    buy_rate                 NUMERIC(18,6),
    usd_rate                 NUMERIC(18,6),
    created_at            TIMESTAMP     NOT NULL,
    updated_at            TIMESTAMP     NOT NULL,
    created_by            VARCHAR(50),
    updated_by            VARCHAR(50),
    CONSTRAINT uq_freight_header_bl UNIQUE (bl_type, bl_id)
);

CREATE INDEX IF NOT EXISTS ix_freight_header_bl
    ON bms.freight_header (bl_type, bl_id);

COMMENT ON TABLE  bms.freight_header IS 'BMS: 운임 헤더 — B/L 1:1 정산 머리 레코드';
COMMENT ON COLUMN bms.freight_header.bl_type              IS 'B/L 종류: HOUSE | MASTER';
COMMENT ON COLUMN bms.freight_header.bl_id                IS 'B/L 식별자(soft 참조 — 스키마 간 하드 FK 없음)';
COMMENT ON COLUMN bms.freight_header.actual_customer_code IS '실화주 → ADMIN A-22 Customer (code)';
COMMENT ON COLUMN bms.freight_header.liner_code           IS '선사 → ADMIN A-14 Carrier (code)';
COMMENT ON COLUMN bms.freight_header.settle_partner_code  IS '정산처 → ADMIN A-22 Customer (code)';
COMMENT ON COLUMN bms.freight_header.sell_rate_dt             IS '매출 환율 기준 일자 (yyyyMMdd)';
COMMENT ON COLUMN bms.freight_header.buy_rate_dt              IS '매입 환율 기준 일자 (yyyyMMdd)';
COMMENT ON COLUMN bms.freight_header.usd_rate_dt             IS 'USD 환율 기준 일자 (yyyyMMdd)';
COMMENT ON COLUMN bms.freight_header.sell_rate_currency_code IS '매출 환율 기준 통화 → ADMIN A-16 Currency (code)';
COMMENT ON COLUMN bms.freight_header.buy_rate_currency_code  IS '매입 환율 기준 통화 → ADMIN A-16 Currency (code)';
COMMENT ON COLUMN bms.freight_header.sell_rate              IS '매출 환율';
COMMENT ON COLUMN bms.freight_header.buy_rate               IS '매입 환율';
COMMENT ON COLUMN bms.freight_header.usd_rate              IS 'USD 환율';

-- ──────────────────────────────────────────────────────────────────────────────
-- B-03 금융 서류 (Financial Document)
--   · freight_line 이 FK 참조하므로 freight_header 다음, freight_line 전에 생성
--   · document_no UNIQUE, document_type/document_status ENUM(VARCHAR)
--   · 합계 스냅샷 컬럼 포함(§6.13)
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bms.financial_document (
    financial_document_id BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    document_no           VARCHAR(20)   NOT NULL,
    document_type         VARCHAR(10)   NOT NULL
                              CHECK (document_type IN ('INVOICE', 'PAYMENT', 'DEBIT', 'CREDIT')),
    document_dt           VARCHAR(8),
    document_status       VARCHAR(10)   NOT NULL DEFAULT 'CREATED'
                              CHECK (document_status IN ('CREATED', 'GROUPED', 'TAX', 'SLIP', 'CLEAR')),
    group_financial_no    VARCHAR(20),
    customer_code         VARCHAR(40),
    settle_total_amount   NUMERIC(18,2),
    local_total_amount    NUMERIC(18,2),
    settle_total_vat      NUMERIC(18,2),
    local_total_vat       NUMERIC(18,2),
    usd_total_amount      NUMERIC(18,2),
    performance_dt        VARCHAR(8),
    team_code             VARCHAR(40),
    operator              VARCHAR(50),
    created_at            TIMESTAMP     NOT NULL,
    updated_at            TIMESTAMP     NOT NULL,
    created_by            VARCHAR(50),
    updated_by            VARCHAR(50),
    CONSTRAINT uq_financial_document_no UNIQUE (document_no)
);

CREATE INDEX IF NOT EXISTS ix_financial_document_type_status
    ON bms.financial_document (document_type, document_status);

CREATE INDEX IF NOT EXISTS ix_financial_document_customer
    ON bms.financial_document (customer_code);

COMMENT ON TABLE  bms.financial_document IS 'BMS: 금융 서류 — Invoice/Payment/D/C Note 단일 테이블';
COMMENT ON COLUMN bms.financial_document.document_no        IS '서류 번호(UNIQUE, 불변). 채번 규칙: 종류이니셜+YYMM+시퀀스5 (§6.11)';
COMMENT ON COLUMN bms.financial_document.document_type      IS '종류: INVOICE | PAYMENT | DEBIT | CREDIT';
COMMENT ON COLUMN bms.financial_document.document_dt        IS '발급일';
COMMENT ON COLUMN bms.financial_document.document_status    IS '상태: CREATED | GROUPED | TAX | SLIP | CLEAR (§6.12)';
COMMENT ON COLUMN bms.financial_document.group_financial_no IS '동일 타입 묶음 그룹 번호(별도 엔티티 없음)';
COMMENT ON COLUMN bms.financial_document.customer_code      IS '서류 고객 → ADMIN A-22 Customer (code)';
COMMENT ON COLUMN bms.financial_document.settle_total_amount IS '정산 통화 합계 스냅샷 (통화 혼재 단순 합산, §6.13)';
COMMENT ON COLUMN bms.financial_document.local_total_amount  IS '로컬(KRW) 합계 스냅샷';
COMMENT ON COLUMN bms.financial_document.settle_total_vat    IS '정산 통화 합계 세액 스냅샷';
COMMENT ON COLUMN bms.financial_document.local_total_vat     IS '로컬 합계 세액 스냅샷';
COMMENT ON COLUMN bms.financial_document.usd_total_amount    IS 'USD 환산 합계 스냅샷';
COMMENT ON COLUMN bms.financial_document.performance_dt      IS '실적 인정 일자(서류 기준, 묶인 라인에 전파 §6.15)';
COMMENT ON COLUMN bms.financial_document.team_code           IS '입력 팀 → ADMIN A-02 Team (code)';
COMMENT ON COLUMN bms.financial_document.operator            IS '입력 담당자 → ADMIN A-01 Admin User (username)';

-- ──────────────────────────────────────────────────────────────────────────────
-- B-02 운임 라인 (Freight Line)
--   · freight_header_id (NOT NULL FK) + financial_document_id (NULL FK)
--   · 금액·환율·세액 전 컬럼 NUMERIC
--   · freight_type / financial_doc_type / tax_type ENUM(VARCHAR)
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bms.freight_line (
    freight_line_id        BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    freight_header_id      BIGINT        NOT NULL,
    freight_type           VARCHAR(10)   NOT NULL
                               CHECK (freight_type IN ('SELLING', 'BUYING')),
    financial_doc_type     VARCHAR(10)
                               CHECK (financial_doc_type IN ('INVOICE', 'PAYMENT', 'DEBIT', 'CREDIT')),
    freight_code           VARCHAR(40),
    unit_quantity          NUMERIC(18,2),
    unit_price             NUMERIC(18,2),
    per                    VARCHAR(40),
    currency               VARCHAR(10),
    exchange_rate          NUMERIC(18,6),
    settle_amount          NUMERIC(18,2),
    local_amount           NUMERIC(18,2),
    settle_tax_amount      NUMERIC(18,2),
    local_tax_amount       NUMERIC(18,2),
    usd_exchange_rate      NUMERIC(18,6),
    usd_amount             NUMERIC(18,2),
    customer_code          VARCHAR(40),
    tax_type               VARCHAR(12)
                               CHECK (tax_type IN ('ZERO_RATED', 'EXEMPT', 'TAXABLE')),
    tax_no                 VARCHAR(50),
    tax_dt                 VARCHAR(8),
    slip_no                VARCHAR(50),
    slip_dt                VARCHAR(8),
    performance_dt         VARCHAR(8),
    financial_document_id  BIGINT,
    created_at             TIMESTAMP     NOT NULL,
    updated_at             TIMESTAMP     NOT NULL,
    created_by             VARCHAR(50),
    updated_by             VARCHAR(50),
    CONSTRAINT fk_freight_line_header
        FOREIGN KEY (freight_header_id) REFERENCES bms.freight_header(freight_header_id),
    CONSTRAINT fk_freight_line_document
        FOREIGN KEY (financial_document_id) REFERENCES bms.financial_document(financial_document_id)
);

CREATE INDEX IF NOT EXISTS ix_freight_line_header
    ON bms.freight_line (freight_header_id);

CREATE INDEX IF NOT EXISTS ix_freight_line_document
    ON bms.freight_line (financial_document_id);

COMMENT ON TABLE  bms.freight_line IS 'BMS: 운임 라인 — 헤더당 N행, 매출/매입 단일 테이블';
COMMENT ON COLUMN bms.freight_line.freight_header_id     IS '소속 운임 헤더 FK (헤더 삭제 시 동반 삭제 — 앱 계층 처리)';
COMMENT ON COLUMN bms.freight_line.freight_type          IS '매출/매입: SELLING | BUYING';
COMMENT ON COLUMN bms.freight_line.financial_doc_type    IS '금융 서류 종류(자동 산정 §6.16): INVOICE | PAYMENT | DEBIT | CREDIT';
COMMENT ON COLUMN bms.freight_line.freight_code          IS '운임 코드 → ADMIN A-18 Freight (code)';
COMMENT ON COLUMN bms.freight_line.unit_quantity         IS 'Per 기준 산정 단위 수량';
COMMENT ON COLUMN bms.freight_line.unit_price            IS '단위 가격';
COMMENT ON COLUMN bms.freight_line.per                   IS '산정 기준(Per) — BMS 자체 보유 코드';
COMMENT ON COLUMN bms.freight_line.currency              IS '거래 통화 → ADMIN A-16 Currency (code)';
COMMENT ON COLUMN bms.freight_line.exchange_rate         IS '환율 스냅샷(헤더 기준 자동 조회 §6.5)';
COMMENT ON COLUMN bms.freight_line.settle_amount         IS '정산 통화 금액';
COMMENT ON COLUMN bms.freight_line.local_amount          IS '로컬(KRW) 금액';
COMMENT ON COLUMN bms.freight_line.settle_tax_amount     IS '정산 통화 세액';
COMMENT ON COLUMN bms.freight_line.local_tax_amount      IS '로컬 세액';
COMMENT ON COLUMN bms.freight_line.usd_exchange_rate     IS 'USD 환산 환율';
COMMENT ON COLUMN bms.freight_line.usd_amount            IS 'USD 환산 금액';
COMMENT ON COLUMN bms.freight_line.customer_code         IS '청구 고객 → ADMIN A-22 Customer (code)';
COMMENT ON COLUMN bms.freight_line.tax_type              IS '세금 유형: ZERO_RATED | EXEMPT | TAXABLE (§6.7)';
COMMENT ON COLUMN bms.freight_line.tax_no                IS '세금계산서 번호(한 번호가 여러 라인 묶음 가능)';
COMMENT ON COLUMN bms.freight_line.tax_dt                IS '세금계산서 발급일';
COMMENT ON COLUMN bms.freight_line.slip_no               IS '전표 번호(한 번호가 여러 라인 묶음 가능)';
COMMENT ON COLUMN bms.freight_line.slip_dt               IS '전표 발급일';
COMMENT ON COLUMN bms.freight_line.performance_dt        IS '실적 인정 일자(서류 묶임 시 서류 값으로 갱신 §6.15)';
COMMENT ON COLUMN bms.freight_line.financial_document_id IS '소속 금융 서류 FK (NULL = 미발행 상태)';
