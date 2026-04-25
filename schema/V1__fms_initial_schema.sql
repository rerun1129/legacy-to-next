-- =============================================================================
-- FMS DB 스키마 — V1 초기 설계
-- PRD: 01_DOMAIN.md §2 핵심 엔티티 기반
-- 작성 후 back-end/java-spring/src/main/resources/db/migration/ 에 복사하고
-- application.yml: spring.flyway.enabled=true 로 변경
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
    id               UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    job_div          VARCHAR(10)  NOT NULL,              -- 'SEA' | 'AIR'
    bound            VARCHAR(3)   NOT NULL,              -- 'EXP' | 'IMP'
    mbl_no           VARCHAR(50),
    master_ref_no    VARCHAR(50),
    shipper_code     VARCHAR(20),
    consignee_code   VARCHAR(20),
    notify_code      VARCHAR(20),
    pol_code         VARCHAR(10),
    pod_code         VARCHAR(10),
    etd              DATE,
    eta              DATE,
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

-- E-03 Master B/L 해상 확장
CREATE TABLE IF NOT EXISTS master_bl_sea (
    master_bl_id         UUID         NOT NULL PRIMARY KEY REFERENCES master_bl(id) ON DELETE CASCADE,
    load_type            VARCHAR(10),
    liner_code           VARCHAR(20),
    vessel_name          VARCHAR(100),
    voyage_no            VARCHAR(20),
    onboard_date         DATE,
    line_bkg_no          VARCHAR(50),
    issue_date           DATE
);

-- E-04 Master B/L 항공 확장
CREATE TABLE IF NOT EXISTS master_bl_air (
    master_bl_id              UUID         NOT NULL PRIMARY KEY REFERENCES master_bl(id) ON DELETE CASCADE,
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
    issue_date                DATE,
    issue_place               VARCHAR(50),
    signature                 VARCHAR(100)
);

-- =============================================================================
-- E-08 House B/L 공통 본체
-- =============================================================================
CREATE TABLE IF NOT EXISTS house_bl (
    id                    UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    job_div               VARCHAR(10)  NOT NULL,          -- 'SEA'|'AIR'|'TRUCK'|'NON_BL'
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
    etd                   DATE,
    eta                   DATE,
    pkg_qty               INT,
    pkg_unit              VARCHAR(10),
    gross_weight_kg       NUMERIC(12,3),
    cbm                   NUMERIC(10,3),
    actual_customer_code  VARCHAR(20),
    operator_code         VARCHAR(20),
    team_code             VARCHAR(20),
    sales_man_code        VARCHAR(20),
    master_bl_id          UUID         REFERENCES master_bl(id),
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by            VARCHAR(50),
    updated_by            VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_house_bl_job_div_bound ON house_bl(job_div, bound);
CREATE INDEX IF NOT EXISTS idx_house_bl_etd           ON house_bl(etd);
CREATE INDEX IF NOT EXISTS idx_house_bl_hbl_no        ON house_bl(hbl_no);
CREATE INDEX IF NOT EXISTS idx_house_bl_master_bl_id  ON house_bl(master_bl_id);

-- E-10 House B/L 해상 확장
CREATE TABLE IF NOT EXISTS house_bl_sea (
    house_bl_id      UUID         NOT NULL PRIMARY KEY REFERENCES house_bl(id) ON DELETE CASCADE,
    load_type        VARCHAR(10),
    liner_code       VARCHAR(20),
    vessel_name      VARCHAR(100),
    voyage_no        VARCHAR(20),
    onboard_date     DATE,
    por_code         VARCHAR(10),
    final_dest_code  VARCHAR(10),
    -- 수출 전용
    issue_date       DATE,
    no_of_bl         INT,
    issue_place      VARCHAR(50),
    -- 수입 전용
    do_date          DATE,
    -- 공통
    incoterms        VARCHAR(10),
    payable_at       VARCHAR(50),
    triangle         BOOLEAN      NOT NULL DEFAULT FALSE,
    co_load          BOOLEAN      NOT NULL DEFAULT FALSE,
    mbl_no           VARCHAR(50)
);

-- E-11 House B/L 항공 확장
CREATE TABLE IF NOT EXISTS house_bl_air (
    house_bl_id               UUID         NOT NULL PRIMARY KEY REFERENCES house_bl(id) ON DELETE CASCADE,
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
    -- 수출 전용
    issue_date                DATE,
    issue_place               VARCHAR(50),
    signature                 VARCHAR(100),
    -- 수입 전용
    fhd                       VARCHAR(10)  -- 'Not'|'F.H.D'|'To Door'
);

-- E-20 House B/L 트럭 확장
CREATE TABLE IF NOT EXISTS house_bl_truck (
    house_bl_id       UUID         NOT NULL PRIMARY KEY REFERENCES house_bl(id) ON DELETE CASCADE,
    vessel_name       VARCHAR(10)  NOT NULL DEFAULT 'TRUCK',
    pickup_date       DATE,
    trucker_code      VARCHAR(20),
    trucker_pic       VARCHAR(100),
    charge_weight_kg  NUMERIC(12,3),
    incoterms         VARCHAR(10)
);

-- E-24 House B/L Non-B/L 확장
CREATE TABLE IF NOT EXISTS house_bl_non_bl (
    house_bl_id          UUID        NOT NULL PRIMARY KEY REFERENCES house_bl(id) ON DELETE CASCADE,
    work_division        VARCHAR(15) NOT NULL,  -- 'SEA'|'AIR'|'WAREHOUSE'|'TRUCKING'
    settle_partner_code  VARCHAR(20),
    status               VARCHAR(20) DEFAULT '접수',
    original_bl_ref      VARCHAR(50)
);

-- =============================================================================
-- E-14a Container
-- E-14b House B/L ↔ Container 배정 (junction + cargo)
-- PRD §1.7: TEU = length_feet / 20 (별도 factor 테이블 없음)
-- =============================================================================
CREATE TABLE IF NOT EXISTS house_bl_container (
    id               UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    house_bl_id      UUID         NOT NULL REFERENCES house_bl(id) ON DELETE CASCADE,
    container_no     VARCHAR(20)  NOT NULL,
    container_type   VARCHAR(10),                     -- 20GP/40GP/40HQ/RF/OT 등
    length_feet      INT          NOT NULL,           -- TEU 환산 기준 (20/40/45)
    seal_no_1        VARCHAR(30),
    seal_no_2        VARCHAR(30),
    pkg_qty          INT,
    pkg_unit         VARCHAR(10),
    gross_weight_kg  NUMERIC(12,3),
    net_weight_kg    NUMERIC(12,3),
    cbm              NUMERIC(10,3),
    vgm_kg           NUMERIC(12,3),
    soc              BOOLEAN      NOT NULL DEFAULT FALSE,
    seq              INT          NOT NULL DEFAULT 1,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by       VARCHAR(50),
    updated_by       VARCHAR(50)
);

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
