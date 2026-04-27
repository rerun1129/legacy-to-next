-- =============================================================================
-- FMS DB 스키마 — V2 컨벤션 개정
-- 변경 요약:
--   1. 모든 PK: UUID → BIGINT GENERATED ALWAYS AS IDENTITY, 컬럼명 {테이블명}_id
--   2. 확장 테이블: PFK → 독립 PK + FK 분리
--   3. 비즈니스 날짜: DATE → VARCHAR(8) YYYYMMDD
--   4. FK ON DELETE CASCADE 제거
--   5. 인라인 -- 컬럼 설명 제거, COMMENT ON COLUMN으로 이전
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 공통: 확장 타입
-- -----------------------------------------------------------------------------
-- CREATE TYPE job_div      AS ENUM ('SEA', 'AIR', 'TRUCK', 'NON_BL');
-- CREATE TYPE bound        AS ENUM ('EXP', 'IMP');
-- CREATE TYPE load_type    AS ENUM ('FCL', 'LCL', 'BULK');
-- CREATE TYPE bl_type      AS ENUM ('ORIGINAL', 'SURRENDER', 'SEAWAY', 'NORMAL', 'EXPRESS');
-- CREATE TYPE freight_term AS ENUM ('PREPAID', 'COLLECT');
-- CREATE TYPE shipment_type AS ENUM ('HOUSE', 'DIRECT');

-- =============================================================================
-- E-01 Master B/L 공통 본체
-- =============================================================================
CREATE TABLE IF NOT EXISTS master_bl (
    master_bl_id     BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    job_div          VARCHAR(10)  NOT NULL,
    bound            VARCHAR(3)   NOT NULL,
    mbl_no           VARCHAR(50),
    master_ref_no    VARCHAR(50),
    shipper_code     VARCHAR(20),
    consignee_code   VARCHAR(20),
    notify_code      VARCHAR(20),
    pol_code         VARCHAR(10),
    pod_code         VARCHAR(10),
    etd              VARCHAR(8),
    eta              VARCHAR(8),
    freight_term     VARCHAR(10),
    operator_code    VARCHAR(20),
    team_code        VARCHAR(20),
    pkg_qty          INT,
    pkg_unit         VARCHAR(10),
    gross_weight_kg  NUMERIC(12,3),
    cbm              NUMERIC(10,3),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by       VARCHAR(50),
    updated_by       VARCHAR(50)
);

COMMENT ON COLUMN master_bl.job_div        IS '운송구분: SEA(해상) | AIR(항공)';
COMMENT ON COLUMN master_bl.bound          IS '수출입구분: EXP(수출) | IMP(수입)';
COMMENT ON COLUMN master_bl.mbl_no         IS 'Master B/L 번호';
COMMENT ON COLUMN master_bl.master_ref_no  IS '사내 Master 참조번호';
COMMENT ON COLUMN master_bl.shipper_code   IS '화주 거래처 코드';
COMMENT ON COLUMN master_bl.consignee_code IS '수하인 거래처 코드';
COMMENT ON COLUMN master_bl.notify_code    IS '통보처 거래처 코드';
COMMENT ON COLUMN master_bl.pol_code       IS '선적항 코드 (Port of Loading)';
COMMENT ON COLUMN master_bl.pod_code       IS '양하항 코드 (Port of Discharge)';
COMMENT ON COLUMN master_bl.etd            IS '출항예정일 YYYYMMDD';
COMMENT ON COLUMN master_bl.eta            IS '도착예정일 YYYYMMDD';
COMMENT ON COLUMN master_bl.freight_term   IS '운임조건: PREPAID(선불) | COLLECT(후불)';
COMMENT ON COLUMN master_bl.operator_code  IS '담당자 코드';
COMMENT ON COLUMN master_bl.team_code      IS '담당팀 코드';
COMMENT ON COLUMN master_bl.pkg_qty        IS '포장 수량';
COMMENT ON COLUMN master_bl.pkg_unit       IS '포장 단위 (CTN, PLT 등)';
COMMENT ON COLUMN master_bl.gross_weight_kg IS '총 중량(kg)';
COMMENT ON COLUMN master_bl.cbm           IS '용적(CBM)';

-- =============================================================================
-- E-03 Master B/L 해상 확장
-- =============================================================================
CREATE TABLE IF NOT EXISTS master_bl_sea (
    master_bl_sea_id  BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    master_bl_id      BIGINT       NOT NULL REFERENCES master_bl(master_bl_id),
    load_type         VARCHAR(10),
    liner_code        VARCHAR(20),
    vessel_name       VARCHAR(100),
    voyage_no         VARCHAR(20),
    onboard_date      VARCHAR(8),
    line_bkg_no       VARCHAR(50),
    issue_date        VARCHAR(8)
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
    master_bl_id              BIGINT       NOT NULL REFERENCES master_bl(master_bl_id),
    airline_code              VARCHAR(10),
    departure_code            VARCHAR(10),
    mawb_no                   VARCHAR(50),
    charge_weight_kg          NUMERIC(12,3),
    volume_weight_kg          NUMERIC(12,3),
    rate_class                VARCHAR(10),
    currency_code             VARCHAR(5),
    declared_value_carriage   VARCHAR(20)  DEFAULT 'N.V.D.',
    declared_value_customs    VARCHAR(50),
    insurance                 VARCHAR(20)  DEFAULT 'NIL',
    account_information       VARCHAR(100),
    security_status           VARCHAR(20),
    flight_type               VARCHAR(20),
    issue_date                VARCHAR(8),
    issue_place               VARCHAR(50),
    signature                 VARCHAR(100)
);

COMMENT ON COLUMN master_bl_air.master_bl_id             IS 'Master B/L 참조 FK';
COMMENT ON COLUMN master_bl_air.airline_code             IS '항공사 코드';
COMMENT ON COLUMN master_bl_air.departure_code           IS '출발공항 코드';
COMMENT ON COLUMN master_bl_air.mawb_no                  IS 'Master AWB 번호';
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

-- =============================================================================
-- E-08 House B/L 공통 본체
-- =============================================================================
CREATE TABLE IF NOT EXISTS house_bl (
    house_bl_id           BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    job_div               VARCHAR(10)  NOT NULL,
    bound                 VARCHAR(3)   NOT NULL,
    hbl_no                VARCHAR(50),
    shipment_type         VARCHAR(10),
    bl_type               VARCHAR(15),
    freight_term          VARCHAR(10),
    shipper_code          VARCHAR(20),
    consignee_code        VARCHAR(20),
    notify_code           VARCHAR(20),
    doc_partner_code      VARCHAR(20),
    pol_code              VARCHAR(10),
    pod_code              VARCHAR(10),
    delivery_code         VARCHAR(10),
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
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by            VARCHAR(50),
    updated_by            VARCHAR(50)
);

COMMENT ON COLUMN house_bl.job_div            IS '운송구분: SEA(해상) | AIR(항공) | TRUCK(육운) | NON_BL(비B/L)';
COMMENT ON COLUMN house_bl.bound              IS '수출입구분: EXP(수출) | IMP(수입)';
COMMENT ON COLUMN house_bl.hbl_no             IS 'House B/L 번호';
COMMENT ON COLUMN house_bl.shipment_type      IS '선적구분: HOUSE(혼재) | DIRECT(직접)';
COMMENT ON COLUMN house_bl.bl_type            IS 'B/L종류: ORIGINAL | SURRENDER | SEAWAY | NORMAL | EXPRESS';
COMMENT ON COLUMN house_bl.freight_term       IS '운임조건: PREPAID(선불) | COLLECT(후불)';
COMMENT ON COLUMN house_bl.shipper_code       IS '화주 거래처 코드';
COMMENT ON COLUMN house_bl.consignee_code     IS '수하인 거래처 코드';
COMMENT ON COLUMN house_bl.notify_code        IS '통보처 거래처 코드';
COMMENT ON COLUMN house_bl.doc_partner_code   IS '서류 파트너 거래처 코드';
COMMENT ON COLUMN house_bl.pol_code           IS '선적항 코드 (Port of Loading)';
COMMENT ON COLUMN house_bl.pod_code           IS '양하항 코드 (Port of Discharge)';
COMMENT ON COLUMN house_bl.delivery_code      IS '인도지 코드';
COMMENT ON COLUMN house_bl.etd               IS '출항예정일 YYYYMMDD';
COMMENT ON COLUMN house_bl.eta               IS '도착예정일 YYYYMMDD';
COMMENT ON COLUMN house_bl.pkg_qty            IS '포장 수량';
COMMENT ON COLUMN house_bl.pkg_unit           IS '포장 단위 (CTN, PLT 등)';
COMMENT ON COLUMN house_bl.gross_weight_kg    IS '총 중량(kg)';
COMMENT ON COLUMN house_bl.cbm              IS '용적(CBM)';
COMMENT ON COLUMN house_bl.actual_customer_code IS '실제 고객 거래처 코드';
COMMENT ON COLUMN house_bl.operator_code     IS '담당자 코드';
COMMENT ON COLUMN house_bl.team_code         IS '담당팀 코드';
COMMENT ON COLUMN house_bl.sales_man_code    IS '영업담당자 코드';
COMMENT ON COLUMN house_bl.master_bl_id      IS '연결된 Master B/L FK (nullable)';

CREATE INDEX IF NOT EXISTS idx_house_bl_job_div_bound ON house_bl(job_div, bound);
CREATE INDEX IF NOT EXISTS idx_house_bl_etd           ON house_bl(etd);
CREATE INDEX IF NOT EXISTS idx_house_bl_hbl_no        ON house_bl(hbl_no);
CREATE INDEX IF NOT EXISTS idx_house_bl_master_bl_id  ON house_bl(master_bl_id);

-- =============================================================================
-- E-10 House B/L 해상 확장
-- =============================================================================
CREATE TABLE IF NOT EXISTS house_bl_sea (
    house_bl_sea_id  BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_bl_id      BIGINT       NOT NULL REFERENCES house_bl(house_bl_id),
    load_type        VARCHAR(10),
    liner_code       VARCHAR(20),
    vessel_name      VARCHAR(100),
    voyage_no        VARCHAR(20),
    onboard_date     VARCHAR(8),
    por_code         VARCHAR(10),
    final_dest_code  VARCHAR(10),
    issue_date       VARCHAR(8),
    no_of_bl         INT,
    issue_place      VARCHAR(50),
    do_date          VARCHAR(8),
    incoterms        VARCHAR(10),
    payable_at       VARCHAR(50),
    triangle         BOOLEAN      NOT NULL DEFAULT FALSE,
    co_load          BOOLEAN      NOT NULL DEFAULT FALSE,
    mbl_no           VARCHAR(50)
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
COMMENT ON COLUMN house_bl_sea.incoterms        IS '인코텀즈 조건';
COMMENT ON COLUMN house_bl_sea.payable_at       IS '운임 지급지';
COMMENT ON COLUMN house_bl_sea.triangle         IS '삼각무역 여부';
COMMENT ON COLUMN house_bl_sea.co_load          IS '공동적재(Co-Load) 여부';
COMMENT ON COLUMN house_bl_sea.mbl_no           IS '연결 Master B/L 번호 (표시용)';

-- =============================================================================
-- E-11 House B/L 항공 확장
-- =============================================================================
CREATE TABLE IF NOT EXISTS house_bl_air (
    house_bl_air_id           BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_bl_id               BIGINT       NOT NULL REFERENCES house_bl(house_bl_id),
    airline_code              VARCHAR(10),
    departure_code            VARCHAR(10),
    mawb_no                   VARCHAR(50),
    charge_weight_kg          NUMERIC(12,3),
    volume_weight_kg          NUMERIC(12,3),
    rate_class                VARCHAR(10),
    currency_code             VARCHAR(5),
    declared_value_carriage   VARCHAR(20)  DEFAULT 'N.V.D.',
    declared_value_customs    VARCHAR(50),
    insurance                 VARCHAR(20)  DEFAULT 'NIL',
    account_information       VARCHAR(100),
    other_term                VARCHAR(100),
    incoterms                 VARCHAR(10),
    freight_term_air          VARCHAR(10),
    issue_date                VARCHAR(8),
    issue_place               VARCHAR(50),
    signature                 VARCHAR(100),
    fhd                       VARCHAR(10)
);

COMMENT ON COLUMN house_bl_air.house_bl_id              IS 'House B/L 참조 FK';
COMMENT ON COLUMN house_bl_air.airline_code             IS '항공사 코드';
COMMENT ON COLUMN house_bl_air.departure_code           IS '출발공항 코드';
COMMENT ON COLUMN house_bl_air.mawb_no                  IS 'Master AWB 번호';
COMMENT ON COLUMN house_bl_air.charge_weight_kg         IS '운임 적용 중량(kg)';
COMMENT ON COLUMN house_bl_air.volume_weight_kg         IS '부피 환산 중량(kg)';
COMMENT ON COLUMN house_bl_air.rate_class               IS '요율 등급';
COMMENT ON COLUMN house_bl_air.currency_code            IS '통화 코드';
COMMENT ON COLUMN house_bl_air.declared_value_carriage  IS '운송신고가액 (미신고 시 N.V.D.)';
COMMENT ON COLUMN house_bl_air.declared_value_customs   IS '세관신고가액';
COMMENT ON COLUMN house_bl_air.insurance                IS '보험금액 (미가입 시 NIL)';
COMMENT ON COLUMN house_bl_air.account_information      IS '계정 정보';
COMMENT ON COLUMN house_bl_air.other_term               IS '기타 운송 조건';
COMMENT ON COLUMN house_bl_air.incoterms                IS '인코텀즈 조건';
COMMENT ON COLUMN house_bl_air.freight_term_air         IS '항공 운임 조건';
COMMENT ON COLUMN house_bl_air.issue_date               IS 'AWB 발행일 YYYYMMDD (수출 전용)';
COMMENT ON COLUMN house_bl_air.issue_place              IS 'AWB 발행지 (수출 전용)';
COMMENT ON COLUMN house_bl_air.signature                IS '발행인 서명 (수출 전용)';
COMMENT ON COLUMN house_bl_air.fhd                      IS '수입 인도 방법: Not | F.H.D | To Door (수입 전용)';

-- =============================================================================
-- E-20 House B/L 트럭 확장
-- =============================================================================
CREATE TABLE IF NOT EXISTS house_bl_truck (
    house_bl_truck_id  BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_bl_id        BIGINT       NOT NULL REFERENCES house_bl(house_bl_id),
    vessel_name        VARCHAR(10)  NOT NULL DEFAULT 'TRUCK',
    pickup_date        VARCHAR(8),
    trucker_code       VARCHAR(20),
    trucker_pic        VARCHAR(100),
    charge_weight_kg   NUMERIC(12,3),
    incoterms          VARCHAR(10)
);

COMMENT ON COLUMN house_bl_truck.house_bl_id       IS 'House B/L 참조 FK';
COMMENT ON COLUMN house_bl_truck.vessel_name       IS '운송 수단명 (기본값: TRUCK)';
COMMENT ON COLUMN house_bl_truck.pickup_date       IS '화물 픽업일 YYYYMMDD';
COMMENT ON COLUMN house_bl_truck.trucker_code      IS '트럭 업체 거래처 코드';
COMMENT ON COLUMN house_bl_truck.trucker_pic       IS '트럭 업체 담당자명';
COMMENT ON COLUMN house_bl_truck.charge_weight_kg  IS '운임 적용 중량(kg)';
COMMENT ON COLUMN house_bl_truck.incoterms         IS '인코텀즈 조건';

-- =============================================================================
-- E-24 House B/L Non-B/L 확장
-- =============================================================================
CREATE TABLE IF NOT EXISTS house_bl_non_bl (
    house_bl_non_bl_id   BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_bl_id          BIGINT      NOT NULL REFERENCES house_bl(house_bl_id),
    work_division        VARCHAR(15) NOT NULL,
    settle_partner_code  VARCHAR(20),
    status               VARCHAR(20) DEFAULT '접수',
    original_bl_ref      VARCHAR(50)
);

COMMENT ON COLUMN house_bl_non_bl.house_bl_id         IS 'House B/L 참조 FK';
COMMENT ON COLUMN house_bl_non_bl.work_division        IS '업무구분: SEA | AIR | WAREHOUSE | TRUCKING';
COMMENT ON COLUMN house_bl_non_bl.settle_partner_code  IS '정산 파트너 거래처 코드';
COMMENT ON COLUMN house_bl_non_bl.status               IS '처리상태 (기본값: 접수)';
COMMENT ON COLUMN house_bl_non_bl.original_bl_ref      IS '원 B/L 참조번호';

-- =============================================================================
-- E-14a Container
-- E-14b House B/L ↔ Container 배정 (junction + cargo)
-- PRD §1.7: TEU = length_feet / 20 (별도 factor 테이블 없음)
-- =============================================================================
CREATE TABLE IF NOT EXISTS house_bl_container (
    house_bl_container_id  BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_bl_id            BIGINT       NOT NULL REFERENCES house_bl(house_bl_id),
    container_no           VARCHAR(20)  NOT NULL,
    container_type         VARCHAR(10),
    length_feet            INT          NOT NULL,
    seal_no_1              VARCHAR(30),
    seal_no_2              VARCHAR(30),
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

COMMENT ON COLUMN house_bl_container.house_bl_id       IS 'House B/L 참조 FK';
COMMENT ON COLUMN house_bl_container.container_no      IS '컨테이너 번호';
COMMENT ON COLUMN house_bl_container.container_type    IS '컨테이너 규격: 20GP | 40GP | 40HQ | RF | OT 등';
COMMENT ON COLUMN house_bl_container.length_feet       IS '컨테이너 길이(피트). TEU = length_feet / 20';
COMMENT ON COLUMN house_bl_container.seal_no_1         IS '봉인번호 1';
COMMENT ON COLUMN house_bl_container.seal_no_2         IS '봉인번호 2';
COMMENT ON COLUMN house_bl_container.pkg_qty           IS '포장 수량';
COMMENT ON COLUMN house_bl_container.pkg_unit          IS '포장 단위';
COMMENT ON COLUMN house_bl_container.gross_weight_kg   IS '총 중량(kg)';
COMMENT ON COLUMN house_bl_container.net_weight_kg     IS '순 중량(kg)';
COMMENT ON COLUMN house_bl_container.cbm              IS '용적(CBM)';
COMMENT ON COLUMN house_bl_container.vgm_kg           IS 'VGM 검증총중량(kg)';
COMMENT ON COLUMN house_bl_container.soc              IS 'Shipper Owned Container 여부';
COMMENT ON COLUMN house_bl_container.seq              IS '컨테이너 정렬 순번';

CREATE INDEX IF NOT EXISTS idx_hbl_container_house_bl_id ON house_bl_container(house_bl_id);

-- =============================================================================
-- TODO: 아래 엔티티는 PRD §2.1 기준으로 스키마 설계 후 추가
-- E-05  Master B/L 치수 (항공 Dimension 그리드)
-- E-06  Master B/L 설명 (Marks / Description)
-- E-07  Master B/L Schedule Leg (항공 전용)
-- E-09  House B/L 기타정보
-- E-12  House B/L 치수 (항공 전용)
-- E-13  House B/L 설명
-- E-15  Container Item (분해)
-- E-16  HS Code
-- E-17  수출면장 (EXP 전용)
-- E-18  참고번호 (Reference No)
-- E-19  House B/L Schedule Leg (항공 전용)
-- E-21  Switch B/L
-- E-22  Switch B/L 설명
-- E-23  거래처 마스터 (Party Master)
-- =============================================================================
