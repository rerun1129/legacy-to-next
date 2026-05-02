-- =============================================================================
-- FMS DDL — JPA 엔티티 SSOT 기반 최종 통합본
-- 생성 기준: JPA 엔티티 (back-end/java-spring/…/persistence/**)
-- 컨벤션: rules/DDL_RULES.md
-- =============================================================================

-- =============================================================================
-- E-01 Master B/L 공통 본체
-- =============================================================================
CREATE TABLE IF NOT EXISTS master_bl (
    master_bl_id          BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    mbl_no                VARCHAR(50),
    master_ref_no         VARCHAR(50),
    job_div               VARCHAR(10)  NOT NULL,
    bound                 VARCHAR(3)   NOT NULL,
    shipper_code          VARCHAR(20),
    shipper_address       VARCHAR(500),
    consignee_code        VARCHAR(20),
    consignee_address     VARCHAR(500),
    notify_code           VARCHAR(20),
    notify_address        VARCHAR(500),
    pol_code              VARCHAR(10),
    pod_code              VARCHAR(10),
    etd                   VARCHAR(8),
    eta                   VARCHAR(8),
    freight_term          VARCHAR(10),
    operator_code         VARCHAR(20),
    team_code             VARCHAR(20),
    pkg_qty               INT,
    pkg_unit              VARCHAR(10),
    gross_weight_kg       NUMERIC(12,3),
    cbm                   NUMERIC(10,3),
    hs_code               VARCHAR(12),
    main_item_name        VARCHAR(100),
    settle_partner_code   VARCHAR(20),
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by            VARCHAR(50),
    updated_by            VARCHAR(50)
);

COMMENT ON COLUMN master_bl.job_div           IS '운송구분: SEA(해상) | AIR(항공)';
COMMENT ON COLUMN master_bl.bound             IS '수출입구분: EXP(수출) | IMP(수입)';
COMMENT ON COLUMN master_bl.mbl_no            IS 'Master B/L 번호';
COMMENT ON COLUMN master_bl.master_ref_no     IS '사내 Master 참조번호';
COMMENT ON COLUMN master_bl.shipper_code      IS '화주 거래처 코드';
COMMENT ON COLUMN master_bl.consignee_code    IS '수하인 거래처 코드';
COMMENT ON COLUMN master_bl.notify_code       IS '통보처 거래처 코드';
COMMENT ON COLUMN master_bl.pol_code          IS '선적항 코드 (Port of Loading)';
COMMENT ON COLUMN master_bl.pod_code          IS '양하항 코드 (Port of Discharge)';
COMMENT ON COLUMN master_bl.etd               IS '출항예정일 YYYYMMDD';
COMMENT ON COLUMN master_bl.eta               IS '도착예정일 YYYYMMDD';
COMMENT ON COLUMN master_bl.freight_term      IS '운임조건: PREPAID(선불) | COLLECT(후불)';
COMMENT ON COLUMN master_bl.operator_code     IS '담당자 코드';
COMMENT ON COLUMN master_bl.team_code         IS '담당팀 코드';
COMMENT ON COLUMN master_bl.pkg_qty           IS '포장 수량';
COMMENT ON COLUMN master_bl.pkg_unit          IS '포장 단위 (CTN, PLT 등)';
COMMENT ON COLUMN master_bl.gross_weight_kg   IS '총 중량(kg)';
COMMENT ON COLUMN master_bl.cbm               IS '용적(CBM)';

-- =============================================================================
-- E-03 Master B/L 해상 확장
-- =============================================================================
CREATE TABLE IF NOT EXISTS master_bl_sea (
    master_bl_sea_id    BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    master_bl_id        BIGINT       NOT NULL UNIQUE REFERENCES master_bl(master_bl_id),
    load_type           VARCHAR(10),
    liner_code          VARCHAR(20),
    vessel_name         VARCHAR(100),
    voyage_no           VARCHAR(20),
    onboard_date        VARCHAR(8),
    line_bkg_no         VARCHAR(50),
    issue_date          VARCHAR(8),
    vessel_nationality  VARCHAR(50),
    weight_unit         VARCHAR(5),
    service_term        VARCHAR(20),
    bl_type             VARCHAR(15),
    vessel_code         VARCHAR(20),
    por_code            VARCHAR(10),
    final_dest_code     VARCHAR(10),
    rton                NUMERIC(10,3),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by          VARCHAR(50),
    updated_by          VARCHAR(50)
);

COMMENT ON COLUMN master_bl_sea.master_bl_id  IS 'Master B/L 참조 FK';
COMMENT ON COLUMN master_bl_sea.load_type     IS '적재구분: FCL(풀컨테이너) | LCL(혼재) | BULK(벌크)';
COMMENT ON COLUMN master_bl_sea.liner_code    IS '선사 코드';
COMMENT ON COLUMN master_bl_sea.vessel_name   IS '선박명';
COMMENT ON COLUMN master_bl_sea.voyage_no     IS '항차번호';
COMMENT ON COLUMN master_bl_sea.onboard_date  IS '본선적재일 YYYYMMDD';
COMMENT ON COLUMN master_bl_sea.line_bkg_no   IS '선사 부킹 번호';
COMMENT ON COLUMN master_bl_sea.issue_date    IS 'B/L 발행일 YYYYMMDD';

-- =============================================================================
-- E-04 Master B/L 항공 확장
-- =============================================================================
CREATE TABLE IF NOT EXISTS master_bl_air (
    master_bl_air_id          BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    master_bl_id              BIGINT       NOT NULL UNIQUE REFERENCES master_bl(master_bl_id),
    airline_code              VARCHAR(10),
    charge_weight_kg          NUMERIC(12,3),
    volume_weight_kg          NUMERIC(12,3),
    rate_class                VARCHAR(10),
    currency_code             VARCHAR(5),
    declared_value_carriage   VARCHAR(20),
    declared_value_customs    VARCHAR(50),
    insurance                 VARCHAR(20),
    account_information       VARCHAR(100),
    security_status           VARCHAR(3),
    flight_type               VARCHAR(30),
    issue_date                VARCHAR(8),
    issue_place               VARCHAR(50),
    signature                 VARCHAR(100),
    other_term                VARCHAR(100),
    handling_info_code        VARCHAR(30),
    handling_info_text        VARCHAR(500),
    volume_divisor            VARCHAR(10),
    created_at                TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at                TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by                VARCHAR(50),
    updated_by                VARCHAR(50)
);

COMMENT ON COLUMN master_bl_air.master_bl_id             IS 'Master B/L 참조 FK';
COMMENT ON COLUMN master_bl_air.airline_code             IS '항공사 코드';
COMMENT ON COLUMN master_bl_air.charge_weight_kg         IS '운임 적용 중량(kg)';
COMMENT ON COLUMN master_bl_air.volume_weight_kg         IS '부피 환산 중량(kg)';
COMMENT ON COLUMN master_bl_air.rate_class               IS '요율 등급';
COMMENT ON COLUMN master_bl_air.currency_code            IS '통화 코드';
COMMENT ON COLUMN master_bl_air.declared_value_carriage  IS '운송신고가액 (미신고 시 N.V.D.)';
COMMENT ON COLUMN master_bl_air.declared_value_customs   IS '세관신고가액';
COMMENT ON COLUMN master_bl_air.insurance                IS '보험금액 (미가입 시 NIL)';
COMMENT ON COLUMN master_bl_air.security_status          IS '보안상태 코드';
COMMENT ON COLUMN master_bl_air.flight_type              IS '비행 편명/유형';
COMMENT ON COLUMN master_bl_air.issue_date               IS 'AWB 발행일 YYYYMMDD';
COMMENT ON COLUMN master_bl_air.issue_place              IS 'AWB 발행지';
COMMENT ON COLUMN master_bl_air.signature                IS '발행인 서명';
COMMENT ON COLUMN master_bl_air.volume_divisor           IS '항공 Dimension 그리드 단위 선택자 (CM/6000, INCH/366 등)';

-- =============================================================================
-- E-06 Master B/L 설명 (1:1)
-- =============================================================================
CREATE TABLE IF NOT EXISTS master_bl_desc (
    master_bl_desc_id  BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    master_bl_id       BIGINT       NOT NULL UNIQUE REFERENCES master_bl(master_bl_id),
    marks              TEXT,
    description        TEXT,
    desc_clause_1      VARCHAR(50),
    desc_clause_2      VARCHAR(50),
    remark             TEXT,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by         VARCHAR(50),
    updated_by         VARCHAR(50)
);

COMMENT ON COLUMN master_bl_desc.master_bl_id  IS 'Master B/L 참조 FK (1:1 UNIQUE)';
COMMENT ON COLUMN master_bl_desc.desc_clause_1 IS '부지약관 구문 1 (해상 수출 전용)';
COMMENT ON COLUMN master_bl_desc.desc_clause_2 IS '부지약관 구문 2 (해상 수출 전용)';
COMMENT ON COLUMN master_bl_desc.remark        IS '비고';

-- =============================================================================
-- E-05 Master B/L 치수 (1:N)
-- =============================================================================
CREATE TABLE IF NOT EXISTS master_bl_dim (
    master_bl_dim_id  BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    master_bl_id      BIGINT       NOT NULL REFERENCES master_bl(master_bl_id),
    length_cm         NUMERIC(10,2),
    width_cm          NUMERIC(10,2),
    height_cm         NUMERIC(10,2),
    quantity          INT,
    cbm               NUMERIC(10,3),
    volume_weight_kg  NUMERIC(12,3),
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by        VARCHAR(50),
    updated_by        VARCHAR(50)
);

COMMENT ON COLUMN master_bl_dim.master_bl_id     IS 'Master B/L 참조 FK';
COMMENT ON COLUMN master_bl_dim.length_cm        IS '길이(cm)';
COMMENT ON COLUMN master_bl_dim.width_cm         IS '너비(cm)';
COMMENT ON COLUMN master_bl_dim.height_cm        IS '높이(cm)';
COMMENT ON COLUMN master_bl_dim.quantity         IS '수량';
COMMENT ON COLUMN master_bl_dim.cbm              IS '용적(CBM)';
COMMENT ON COLUMN master_bl_dim.volume_weight_kg IS '부피 환산 중량(kg)';

CREATE INDEX IF NOT EXISTS idx_master_bl_dim_master_bl_id ON master_bl_dim(master_bl_id);

-- =============================================================================
-- E-07 Master B/L Schedule Leg (항공 전용, 1:N)
-- =============================================================================
CREATE TABLE IF NOT EXISTS master_bl_schedule_leg (
    master_bl_schedule_leg_id  BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    master_bl_id               BIGINT      NOT NULL REFERENCES master_bl(master_bl_id),
    to_code                    VARCHAR(10) NOT NULL,
    by_carrier                 VARCHAR(20),
    flight_no                  VARCHAR(20),
    on_board_dt                VARCHAR(8)  NOT NULL,
    on_board_tm                VARCHAR(4),
    arrival_dt                 VARCHAR(8)  NOT NULL,
    arrival_tm                 VARCHAR(4),
    created_at                 TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                 TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by                 VARCHAR(50),
    updated_by                 VARCHAR(50)
);

COMMENT ON COLUMN master_bl_schedule_leg.master_bl_id  IS 'Master B/L 참조 FK';
COMMENT ON COLUMN master_bl_schedule_leg.to_code       IS '도착지 공항 코드 (*To, 필수)';
COMMENT ON COLUMN master_bl_schedule_leg.by_carrier    IS '운송 캐리어 코드';
COMMENT ON COLUMN master_bl_schedule_leg.flight_no     IS '항공편명';
COMMENT ON COLUMN master_bl_schedule_leg.on_board_dt   IS '본선적재일 YYYYMMDD (필수)';
COMMENT ON COLUMN master_bl_schedule_leg.on_board_tm   IS '본선적재 시각 HHMM';
COMMENT ON COLUMN master_bl_schedule_leg.arrival_dt    IS '도착일 YYYYMMDD (필수)';
COMMENT ON COLUMN master_bl_schedule_leg.arrival_tm    IS '도착 시각 HHMM';

CREATE INDEX IF NOT EXISTS idx_master_bl_schedule_leg_master_bl_id ON master_bl_schedule_leg(master_bl_id);

-- =============================================================================
-- E-08a Master B/L Air Charge (항공 전용, 1:N)
-- =============================================================================
CREATE TABLE IF NOT EXISTS master_bl_air_charge (
    master_bl_air_charge_id  BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    master_bl_id             BIGINT       NOT NULL REFERENCES master_bl(master_bl_id),
    freight_code             VARCHAR(20),
    currency_code            VARCHAR(5),
    per                      VARCHAR(10),
    freight_term             VARCHAR(10),
    gross_weight_kg          NUMERIC(12,3),
    rate_class               VARCHAR(10),
    charge_weight_kg         NUMERIC(12,3),
    rate                     NUMERIC(12,3),
    created_at               TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by               VARCHAR(50),
    updated_by               VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_master_bl_air_charge_master_bl_id ON master_bl_air_charge(master_bl_id);

-- =============================================================================
-- E-08 House B/L 공통 본체
-- =============================================================================
CREATE TABLE IF NOT EXISTS house_bl (
    house_bl_id           BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    hbl_no                VARCHAR(50),
    job_div               VARCHAR(10)  NOT NULL,
    bound                 VARCHAR(3)   NOT NULL,
    shipment_type         VARCHAR(10),
    freight_term          VARCHAR(10),
    shipper_code          VARCHAR(20),
    shipper_address       VARCHAR(500),
    consignee_code        VARCHAR(20),
    consignee_address     VARCHAR(500),
    notify_code           VARCHAR(20),
    notify_address        VARCHAR(500),
    doc_partner_code      VARCHAR(20),
    doc_partner_address   VARCHAR(500),
    settle_partner_code   VARCHAR(20),
    pol_code              VARCHAR(10),
    pod_code              VARCHAR(10),
    etd                   VARCHAR(8),
    eta                   VARCHAR(8),
    pkg_qty               INT,
    pkg_unit              VARCHAR(10),
    gross_weight_kg       NUMERIC(12,3),
    cbm                   NUMERIC(10,3),
    actual_customer_code  VARCHAR(20),
    operator_code         VARCHAR(20),
    team_code             VARCHAR(20),
    sales_man_code        VARCHAR(20),
    master_bl_id          BIGINT       REFERENCES master_bl(master_bl_id),
    incoterms             VARCHAR(10),
    sales_class           VARCHAR(30),
    main_item_name        VARCHAR(100),
    hs_code               VARCHAR(12),
    mbl_no                VARCHAR(50),
    master_ref_no         VARCHAR(50),
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by            VARCHAR(50),
    updated_by            VARCHAR(50)
);

COMMENT ON COLUMN house_bl.job_div              IS '운송구분: SEA(해상) | AIR(항공) | TRUCK(육운) | NON_BL(비B/L)';
COMMENT ON COLUMN house_bl.bound                IS '수출입구분: EXP(수출) | IMP(수입)';
COMMENT ON COLUMN house_bl.hbl_no               IS 'House B/L 번호';
COMMENT ON COLUMN house_bl.shipment_type        IS '선적구분: HOUSE(혼재) | DIRECT(직접)';
COMMENT ON COLUMN house_bl.freight_term         IS '운임조건: PREPAID(선불) | COLLECT(후불)';
COMMENT ON COLUMN house_bl.shipper_code         IS '화주 거래처 코드';
COMMENT ON COLUMN house_bl.consignee_code       IS '수하인 거래처 코드';
COMMENT ON COLUMN house_bl.notify_code          IS '통보처 거래처 코드';
COMMENT ON COLUMN house_bl.doc_partner_code     IS '서류 파트너 거래처 코드';
COMMENT ON COLUMN house_bl.pol_code             IS '선적항 코드 (Port of Loading)';
COMMENT ON COLUMN house_bl.pod_code             IS '양하항 코드 (Port of Discharge)';
COMMENT ON COLUMN house_bl.etd                  IS '출항예정일 YYYYMMDD';
COMMENT ON COLUMN house_bl.eta                  IS '도착예정일 YYYYMMDD';
COMMENT ON COLUMN house_bl.pkg_qty              IS '포장 수량';
COMMENT ON COLUMN house_bl.pkg_unit             IS '포장 단위 (CTN, PLT 등)';
COMMENT ON COLUMN house_bl.gross_weight_kg      IS '총 중량(kg)';
COMMENT ON COLUMN house_bl.cbm                  IS '용적(CBM)';
COMMENT ON COLUMN house_bl.actual_customer_code IS '실제 고객 거래처 코드';
COMMENT ON COLUMN house_bl.operator_code        IS '담당자 코드';
COMMENT ON COLUMN house_bl.team_code            IS '담당팀 코드';
COMMENT ON COLUMN house_bl.sales_man_code       IS '영업담당자 코드';
COMMENT ON COLUMN house_bl.master_bl_id         IS '연결된 Master B/L FK (nullable)';

CREATE INDEX IF NOT EXISTS idx_house_bl_job_div_bound ON house_bl(job_div, bound);
CREATE INDEX IF NOT EXISTS idx_house_bl_etd           ON house_bl(etd);
CREATE INDEX IF NOT EXISTS idx_house_bl_hbl_no        ON house_bl(hbl_no);
CREATE INDEX IF NOT EXISTS idx_house_bl_master_bl_id  ON house_bl(master_bl_id);

-- =============================================================================
-- E-10 House B/L 해상 확장
-- =============================================================================
CREATE TABLE IF NOT EXISTS house_bl_sea (
    house_bl_sea_id             BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_bl_id                 BIGINT       NOT NULL UNIQUE REFERENCES house_bl(house_bl_id),
    load_type                   VARCHAR(10),
    liner_code                  VARCHAR(20),
    vessel_name                 VARCHAR(100),
    voyage_no                   VARCHAR(20),
    onboard_date                VARCHAR(8),
    por_code                    VARCHAR(10),
    final_dest_code             VARCHAR(10),
    issue_date                  VARCHAR(8),
    no_of_bl                    VARCHAR(10),
    issue_place                 VARCHAR(50),
    do_date                     VARCHAR(8),
    payable_at                  VARCHAR(50),
    triangle                    BOOLEAN      NOT NULL DEFAULT FALSE,
    service_term                VARCHAR(20),
    vessel_code                 VARCHAR(20),
    vessel_nationality          VARCHAR(50),
    weight_unit                 VARCHAR(5),
    rton                        NUMERIC(10,3),
    say_information             VARCHAR(500),
    no_of_container_or_packages VARCHAR(100),
    bl_type                     VARCHAR(15),
    delivery_code               VARCHAR(10),
    created_at                  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at                  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by                  VARCHAR(50),
    updated_by                  VARCHAR(50)
);

COMMENT ON COLUMN house_bl_sea.house_bl_id      IS 'House B/L 참조 FK';
COMMENT ON COLUMN house_bl_sea.load_type        IS '적재구분: FCL(풀컨테이너) | LCL(혼재) | BULK(벌크)';
COMMENT ON COLUMN house_bl_sea.liner_code       IS '선사 코드';
COMMENT ON COLUMN house_bl_sea.vessel_name      IS '선박명';
COMMENT ON COLUMN house_bl_sea.voyage_no        IS '항차번호';
COMMENT ON COLUMN house_bl_sea.onboard_date     IS '본선적재일 YYYYMMDD';
COMMENT ON COLUMN house_bl_sea.por_code         IS '수취지 코드 (Place of Receipt)';
COMMENT ON COLUMN house_bl_sea.final_dest_code  IS '최종목적지 코드';
COMMENT ON COLUMN house_bl_sea.issue_date       IS 'B/L 발행일 YYYYMMDD (수출 전용)';
COMMENT ON COLUMN house_bl_sea.no_of_bl         IS 'B/L 발행 부수 (수출 전용)';
COMMENT ON COLUMN house_bl_sea.issue_place      IS 'B/L 발행지 (수출 전용)';
COMMENT ON COLUMN house_bl_sea.do_date          IS 'D/O 발행일 YYYYMMDD (수입 전용)';
COMMENT ON COLUMN house_bl_sea.payable_at       IS '운임 지급지';
COMMENT ON COLUMN house_bl_sea.triangle         IS '삼각무역 여부';

-- =============================================================================
-- E-11 House B/L 항공 확장
-- =============================================================================
CREATE TABLE IF NOT EXISTS house_bl_air (
    house_bl_air_id           BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_bl_id               BIGINT       NOT NULL UNIQUE REFERENCES house_bl(house_bl_id),
    airline_code              VARCHAR(10),
    charge_weight_kg          NUMERIC(12,3),
    volume_weight_kg          NUMERIC(12,3),
    rate_class                VARCHAR(10),
    currency_code             VARCHAR(5),
    declared_value_carriage   VARCHAR(20),
    declared_value_customs    VARCHAR(50),
    insurance                 VARCHAR(20),
    account_information       VARCHAR(100),
    other_term                VARCHAR(100),
    issue_date                VARCHAR(8),
    issue_place               VARCHAR(50),
    signature                 VARCHAR(100),
    fhd                       VARCHAR(10),
    volume_divisor            VARCHAR(10),
    handling_info_code        VARCHAR(30),
    handling_info_text        VARCHAR(500),
    origin_of_goods           VARCHAR(100),
    cargo_type                VARCHAR(30),
    created_at                TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at                TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by                VARCHAR(50),
    updated_by                VARCHAR(50)
);

COMMENT ON COLUMN house_bl_air.house_bl_id              IS 'House B/L 참조 FK';
COMMENT ON COLUMN house_bl_air.airline_code             IS '항공사 코드';
COMMENT ON COLUMN house_bl_air.charge_weight_kg         IS '운임 적용 중량(kg)';
COMMENT ON COLUMN house_bl_air.volume_weight_kg         IS '부피 환산 중량(kg)';
COMMENT ON COLUMN house_bl_air.rate_class               IS '요율 등급';
COMMENT ON COLUMN house_bl_air.currency_code            IS '통화 코드';
COMMENT ON COLUMN house_bl_air.declared_value_carriage  IS '운송신고가액 (미신고 시 N.V.D.)';
COMMENT ON COLUMN house_bl_air.declared_value_customs   IS '세관신고가액';
COMMENT ON COLUMN house_bl_air.insurance                IS '보험금액 (미가입 시 NIL)';
COMMENT ON COLUMN house_bl_air.account_information      IS '계정 정보';
COMMENT ON COLUMN house_bl_air.other_term               IS '기타 운송 조건';
COMMENT ON COLUMN house_bl_air.issue_date               IS 'AWB 발행일 YYYYMMDD (수출 전용)';
COMMENT ON COLUMN house_bl_air.issue_place              IS 'AWB 발행지 (수출 전용)';
COMMENT ON COLUMN house_bl_air.signature                IS '발행인 서명 (수출 전용)';
COMMENT ON COLUMN house_bl_air.fhd                      IS '수입 인도 방법: Not | F.H.D | To Door (수입 전용)';
COMMENT ON COLUMN house_bl_air.volume_divisor           IS '항공 Dimension 그리드 단위 선택자 (CM/6000, INCH/366 등)';

-- =============================================================================
-- E-20 House B/L 트럭 확장
-- =============================================================================
CREATE TABLE IF NOT EXISTS house_bl_truck (
    house_bl_truck_id  BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_bl_id        BIGINT       NOT NULL UNIQUE REFERENCES house_bl(house_bl_id),
    vessel_name        VARCHAR(10)  NOT NULL DEFAULT 'TRUCK',
    voyage_no          VARCHAR(20),
    pickup_date        VARCHAR(8),
    pickup_tm          VARCHAR(4),
    etd_tm             VARCHAR(4),
    eta_tm             VARCHAR(4),
    trucker_code       VARCHAR(20),
    trucker_pic        VARCHAR(100),
    charge_weight_kg   NUMERIC(12,3),
    load_type          VARCHAR(10),
    service_term       VARCHAR(15),
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by         VARCHAR(50),
    updated_by         VARCHAR(50)
);

COMMENT ON COLUMN house_bl_truck.house_bl_id       IS 'House B/L 참조 FK';
COMMENT ON COLUMN house_bl_truck.vessel_name       IS '운송 수단명 (기본값: TRUCK)';
COMMENT ON COLUMN house_bl_truck.pickup_date       IS '화물 픽업일 YYYYMMDD';
COMMENT ON COLUMN house_bl_truck.trucker_code      IS '트럭 업체 거래처 코드';
COMMENT ON COLUMN house_bl_truck.trucker_pic       IS '트럭 업체 담당자명';
COMMENT ON COLUMN house_bl_truck.charge_weight_kg  IS '운임 적용 중량(kg)';

-- =============================================================================
-- E-24 House B/L Non-B/L 확장
-- =============================================================================
CREATE TABLE IF NOT EXISTS house_bl_non_bl (
    house_bl_non_bl_id  BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_bl_id         BIGINT       NOT NULL UNIQUE REFERENCES house_bl(house_bl_id),
    work_division       VARCHAR(15)  NOT NULL,
    original_bl_ref     VARCHAR(50),
    rton                NUMERIC(10,3),
    volume_wt_kg        NUMERIC(12,3),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by          VARCHAR(50),
    updated_by          VARCHAR(50)
);

COMMENT ON COLUMN house_bl_non_bl.house_bl_id      IS 'House B/L 참조 FK';
COMMENT ON COLUMN house_bl_non_bl.work_division    IS '업무구분: SEA | AIR | WAREHOUSE | TRUCKING';
COMMENT ON COLUMN house_bl_non_bl.original_bl_ref  IS '원 B/L 참조번호';

-- =============================================================================
-- E-13 House B/L 설명 (1:1)
-- =============================================================================
CREATE TABLE IF NOT EXISTS house_bl_desc (
    house_bl_desc_id  BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_bl_id       BIGINT       NOT NULL UNIQUE REFERENCES house_bl(house_bl_id),
    marks             TEXT,
    description       TEXT,
    desc_clause_1     VARCHAR(50),
    desc_clause_2     VARCHAR(50),
    remark            TEXT,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by        VARCHAR(50),
    updated_by        VARCHAR(50)
);

COMMENT ON COLUMN house_bl_desc.house_bl_id   IS 'House B/L 참조 FK (1:1 UNIQUE)';
COMMENT ON COLUMN house_bl_desc.desc_clause_1 IS '부지약관 구문 1 (해상 수출 전용)';
COMMENT ON COLUMN house_bl_desc.desc_clause_2 IS '부지약관 구문 2 (해상 수출 전용)';
COMMENT ON COLUMN house_bl_desc.remark        IS '비고';

-- =============================================================================
-- E-12 House B/L 치수 (1:N)
-- =============================================================================
CREATE TABLE IF NOT EXISTS house_bl_dim (
    house_bl_dim_id   BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_bl_id       BIGINT       NOT NULL REFERENCES house_bl(house_bl_id),
    length_cm         NUMERIC(10,2),
    width_cm          NUMERIC(10,2),
    height_cm         NUMERIC(10,2),
    quantity          INT,
    cbm               NUMERIC(10,3),
    volume_weight_kg  NUMERIC(12,3),
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by        VARCHAR(50),
    updated_by        VARCHAR(50)
);

COMMENT ON COLUMN house_bl_dim.house_bl_id       IS 'House B/L 참조 FK';
COMMENT ON COLUMN house_bl_dim.length_cm         IS '길이(cm)';
COMMENT ON COLUMN house_bl_dim.width_cm          IS '너비(cm)';
COMMENT ON COLUMN house_bl_dim.height_cm         IS '높이(cm)';
COMMENT ON COLUMN house_bl_dim.quantity          IS '수량';
COMMENT ON COLUMN house_bl_dim.cbm               IS '용적(CBM)';
COMMENT ON COLUMN house_bl_dim.volume_weight_kg  IS '부피 환산 중량(kg)';

CREATE INDEX IF NOT EXISTS idx_house_bl_dim_house_bl_id ON house_bl_dim(house_bl_id);

-- =============================================================================
-- E-14 House B/L Container (1:N)
-- PRD §2.2: TEU = length_feet / 20
-- =============================================================================
CREATE TABLE IF NOT EXISTS house_bl_container (
    house_bl_container_id  BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_bl_id            BIGINT       NOT NULL REFERENCES house_bl(house_bl_id),
    container_no           VARCHAR(20)  NOT NULL,
    container_type         VARCHAR(10),
    length_feet            INT          NOT NULL,
    seal_no_1              VARCHAR(30),
    seal_no_2              VARCHAR(30),
    seal_no_3              VARCHAR(30),
    seal_no_4              VARCHAR(30),
    seal_no_5              VARCHAR(30),
    seal_no_6              VARCHAR(30),
    pkg_qty                INT,
    pkg_unit               VARCHAR(10),
    gross_weight_kg        NUMERIC(12,3),
    net_weight_kg          NUMERIC(12,3),
    cbm                    NUMERIC(10,3),
    vgm_kg                 NUMERIC(12,3),
    soc                    BOOLEAN      NOT NULL DEFAULT FALSE,
    seq                    INT          NOT NULL DEFAULT 1,
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by             VARCHAR(50),
    updated_by             VARCHAR(50)
);

COMMENT ON COLUMN house_bl_container.house_bl_id      IS 'House B/L 참조 FK';
COMMENT ON COLUMN house_bl_container.container_no     IS '컨테이너 번호';
COMMENT ON COLUMN house_bl_container.container_type   IS '컨테이너 규격: 20GP | 40GP | 40HQ | RF | OT 등';
COMMENT ON COLUMN house_bl_container.length_feet      IS '컨테이너 길이(피트). TEU = length_feet / 20';
COMMENT ON COLUMN house_bl_container.seal_no_1        IS '봉인번호 1';
COMMENT ON COLUMN house_bl_container.seal_no_2        IS '봉인번호 2';
COMMENT ON COLUMN house_bl_container.pkg_qty          IS '포장 수량';
COMMENT ON COLUMN house_bl_container.pkg_unit         IS '포장 단위';
COMMENT ON COLUMN house_bl_container.gross_weight_kg  IS '총 중량(kg)';
COMMENT ON COLUMN house_bl_container.net_weight_kg    IS '순 중량(kg)';
COMMENT ON COLUMN house_bl_container.cbm              IS '용적(CBM)';
COMMENT ON COLUMN house_bl_container.vgm_kg           IS 'VGM 검증총중량(kg)';
COMMENT ON COLUMN house_bl_container.soc              IS 'Shipper Owned Container 여부';
COMMENT ON COLUMN house_bl_container.seq              IS '컨테이너 정렬 순번';

CREATE INDEX IF NOT EXISTS idx_hbl_container_house_bl_id ON house_bl_container(house_bl_id);

-- =============================================================================
-- E-19 House B/L Schedule Leg (항공 전용, 1:N)
-- =============================================================================
CREATE TABLE IF NOT EXISTS house_bl_schedule_leg (
    house_bl_schedule_leg_id  BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_bl_id               BIGINT      NOT NULL REFERENCES house_bl(house_bl_id),
    to_code                   VARCHAR(10) NOT NULL,
    by_carrier                VARCHAR(20),
    flight_no                 VARCHAR(20),
    on_board_dt               VARCHAR(8)  NOT NULL,
    on_board_tm               VARCHAR(4),
    arrival_dt                VARCHAR(8)  NOT NULL,
    arrival_tm                VARCHAR(4),
    created_at                TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by                VARCHAR(50),
    updated_by                VARCHAR(50)
);

COMMENT ON COLUMN house_bl_schedule_leg.house_bl_id  IS 'House B/L 참조 FK';
COMMENT ON COLUMN house_bl_schedule_leg.to_code      IS '도착지 공항 코드 (*To, 필수)';
COMMENT ON COLUMN house_bl_schedule_leg.by_carrier   IS '운송 캐리어 코드';
COMMENT ON COLUMN house_bl_schedule_leg.flight_no    IS '항공편명';
COMMENT ON COLUMN house_bl_schedule_leg.on_board_dt  IS '본선적재일 YYYYMMDD (필수)';
COMMENT ON COLUMN house_bl_schedule_leg.on_board_tm  IS '본선적재 시각 HHMM';
COMMENT ON COLUMN house_bl_schedule_leg.arrival_dt   IS '도착일 YYYYMMDD (필수)';
COMMENT ON COLUMN house_bl_schedule_leg.arrival_tm   IS '도착 시각 HHMM';

CREATE INDEX IF NOT EXISTS idx_house_bl_schedule_leg_house_bl_id ON house_bl_schedule_leg(house_bl_id);

-- =============================================================================
-- E-17 House B/L 면장 (수출 전용, 1:N)
-- =============================================================================
CREATE TABLE IF NOT EXISTS house_bl_license (
    house_bl_license_id    BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_bl_id            BIGINT       NOT NULL REFERENCES house_bl(house_bl_id),
    license_no             VARCHAR(50),
    pkg_qty                INT,
    pkg_unit               VARCHAR(10),
    gross_weight_kg        NUMERIC(12,3),
    combined_packing_mark  VARCHAR(50),
    combined_packing_qty   INT,
    combined_packing_unit  VARCHAR(10),
    partial_shipment       BOOLEAN      NOT NULL DEFAULT FALSE,
    partial_shipment_seq   INT,
    hsn_no                 VARCHAR(30),
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by             VARCHAR(50),
    updated_by             VARCHAR(50)
);

COMMENT ON COLUMN house_bl_license.house_bl_id            IS 'House B/L 참조 FK';
COMMENT ON COLUMN house_bl_license.license_no             IS '수출신고필증 번호 또는 화물관리번호';
COMMENT ON COLUMN house_bl_license.pkg_qty                IS '면장 기재 포장 수량';
COMMENT ON COLUMN house_bl_license.pkg_unit               IS '면장 기재 포장 단위';
COMMENT ON COLUMN house_bl_license.gross_weight_kg        IS '면장 기재 총중량(kg)';
COMMENT ON COLUMN house_bl_license.combined_packing_mark  IS '동시포장 기호';
COMMENT ON COLUMN house_bl_license.combined_packing_qty   IS '동시포장 수량';
COMMENT ON COLUMN house_bl_license.combined_packing_unit  IS '동시포장 단위';
COMMENT ON COLUMN house_bl_license.partial_shipment       IS '선적 분할(Partial Shipment) 여부';
COMMENT ON COLUMN house_bl_license.partial_shipment_seq   IS '선적 분할 차수';
COMMENT ON COLUMN house_bl_license.hsn_no                 IS 'HSN 번호';

CREATE INDEX IF NOT EXISTS idx_house_bl_license_house_bl_id ON house_bl_license(house_bl_id);

-- =============================================================================
-- E-18 House B/L Truck Order (트럭 전용, 1:N)
-- =============================================================================
CREATE TABLE IF NOT EXISTS house_bl_truck_order (
    house_bl_truck_order_id  BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_bl_id              BIGINT       NOT NULL REFERENCES house_bl(house_bl_id),
    truck_order_no           VARCHAR(30),
    pkg_qty                  INT,
    pkg_unit                 VARCHAR(10),
    gross_weight_kg          NUMERIC(12,3),
    cbm                      NUMERIC(10,3),
    truck_no                 VARCHAR(20),
    truck_type               VARCHAR(10),
    driver                   VARCHAR(50),
    mobile_no                VARCHAR(30),
    container_no             VARCHAR(20),
    container_type           VARCHAR(10),
    seal_no_1                VARCHAR(30),
    seal_no_2                VARCHAR(30),
    seal_no_3                VARCHAR(30),
    created_at               TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by               VARCHAR(50),
    updated_by               VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_house_bl_truck_order_house_bl_id ON house_bl_truck_order(house_bl_id);

-- =============================================================================
-- E-19a House B/L Air Charge (항공 전용, 1:N)
-- =============================================================================
CREATE TABLE IF NOT EXISTS house_bl_air_charge (
    house_bl_air_charge_id  BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_bl_id             BIGINT       NOT NULL REFERENCES house_bl(house_bl_id),
    freight_code            VARCHAR(20),
    currency_code           VARCHAR(5),
    per                     VARCHAR(10),
    freight_term            VARCHAR(10),
    gross_weight_kg         NUMERIC(12,3),
    rate_class              VARCHAR(10),
    charge_weight_kg        NUMERIC(12,3),
    rate                    NUMERIC(12,3),
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by              VARCHAR(50),
    updated_by              VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_house_bl_air_charge_house_bl_id ON house_bl_air_charge(house_bl_id);

-- =============================================================================
-- E-21 Switch B/L (1:1 per house_bl, 재Switch 불가)
-- =============================================================================
CREATE TABLE IF NOT EXISTS switch_bl (
    switch_bl_id       BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_bl_id        BIGINT       NOT NULL UNIQUE REFERENCES house_bl(house_bl_id),
    switch_bl_no       VARCHAR(50),
    bl_type            VARCHAR(15),
    incoterms          VARCHAR(10),
    shipper_code       VARCHAR(20)  NOT NULL,
    shipper_address    VARCHAR(500),
    consignee_code     VARCHAR(20),
    consignee_address  VARCHAR(500),
    notify_code        VARCHAR(20),
    notify_address     VARCHAR(500),
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by         VARCHAR(50),
    updated_by         VARCHAR(50)
);

COMMENT ON COLUMN switch_bl.house_bl_id     IS '원본 House B/L 참조 FK (1:1 UNIQUE, 재Switch 불가)';
COMMENT ON COLUMN switch_bl.switch_bl_no    IS 'Switch B/L 번호';
COMMENT ON COLUMN switch_bl.bl_type         IS 'B/L 종류 (해상 전용; 항공은 NULL)';
COMMENT ON COLUMN switch_bl.incoterms       IS '인코텀즈';
COMMENT ON COLUMN switch_bl.shipper_code    IS '교체 적용 송하인 코드 (필수)';
COMMENT ON COLUMN switch_bl.consignee_code  IS '교체 적용 수하인 코드';
COMMENT ON COLUMN switch_bl.notify_code     IS '교체 적용 통보처 코드';

-- =============================================================================
-- E-22 Switch B/L 설명 (1:1)
-- =============================================================================
CREATE TABLE IF NOT EXISTS switch_bl_description (
    switch_bl_description_id  BIGINT  GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    switch_bl_id              BIGINT  NOT NULL UNIQUE REFERENCES switch_bl(switch_bl_id),
    marks_left                TEXT,
    marks_right               TEXT,
    nature_quantity_left      TEXT,
    nature_quantity_right     TEXT,
    created_at                TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at                TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by                VARCHAR(50),
    updated_by                VARCHAR(50)
);

COMMENT ON COLUMN switch_bl_description.switch_bl_id           IS 'Switch B/L 참조 FK (1:1 UNIQUE)';
COMMENT ON COLUMN switch_bl_description.marks_left             IS 'Marks and Numbers 좌측';
COMMENT ON COLUMN switch_bl_description.marks_right            IS 'Marks and Numbers 우측';
COMMENT ON COLUMN switch_bl_description.nature_quantity_left   IS 'Nature & Quantity of Goods 좌측';
COMMENT ON COLUMN switch_bl_description.nature_quantity_right  IS 'Nature & Quantity of Goods 우측';
