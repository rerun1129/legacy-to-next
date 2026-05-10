-- house_bl_container 분리: SEA + NON_BL ext별 분리 (TRUCK·AIR 미사용)
-- ER 재구조화 Phase 3
-- DB 새로 채우는 환경 가정 — 기존 데이터 백필 없음. 기존 테이블 DROP 후 새 테이블 2개 생성.

-- 기존 단일 container 테이블 제거
DROP TABLE IF EXISTS fms.house_bl_container CASCADE;

-- SEA ext 전용 container
CREATE TABLE fms.house_bl_sea_container (
    house_bl_sea_container_id  BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_bl_sea_id            BIGINT       NOT NULL
        REFERENCES fms.house_bl_sea(house_bl_sea_id) ON DELETE CASCADE,
    container_no               VARCHAR(20),
    container_type             VARCHAR(10),
    length_feet                INT          NOT NULL,
    seal_no_1                  VARCHAR(30),
    seal_no_2                  VARCHAR(30),
    seal_no_3                  VARCHAR(30),
    seal_no_4                  VARCHAR(30),
    seal_no_5                  VARCHAR(30),
    seal_no_6                  VARCHAR(30),
    pkg_qty                    INT,
    pkg_unit                   VARCHAR(10),
    gross_weight_kg            NUMERIC(12,3),
    net_weight_kg              NUMERIC(12,3),
    cbm                        NUMERIC(10,3),
    vgm_kg                     NUMERIC(12,3),
    soc                        BOOLEAN      NOT NULL DEFAULT FALSE,
    seq                        INT          NOT NULL DEFAULT 1,
    created_at                 TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at                 TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by                 VARCHAR(50),
    updated_by                 VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_house_bl_sea_container_sea_id
    ON fms.house_bl_sea_container(house_bl_sea_id);

-- NON_BL ext 전용 container
CREATE TABLE fms.house_bl_nonbl_container (
    house_bl_nonbl_container_id  BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_bl_non_bl_id           BIGINT       NOT NULL
        REFERENCES fms.house_bl_non_bl(house_bl_non_bl_id) ON DELETE CASCADE,
    container_no                 VARCHAR(20),
    container_type               VARCHAR(10),
    length_feet                  INT          NOT NULL,
    seal_no_1                    VARCHAR(30),
    seal_no_2                    VARCHAR(30),
    seal_no_3                    VARCHAR(30),
    seal_no_4                    VARCHAR(30),
    seal_no_5                    VARCHAR(30),
    seal_no_6                    VARCHAR(30),
    pkg_qty                      INT,
    pkg_unit                     VARCHAR(10),
    gross_weight_kg              NUMERIC(12,3),
    net_weight_kg                NUMERIC(12,3),
    cbm                          NUMERIC(10,3),
    vgm_kg                       NUMERIC(12,3),
    soc                          BOOLEAN      NOT NULL DEFAULT FALSE,
    seq                          INT          NOT NULL DEFAULT 1,
    created_at                   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at                   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by                   VARCHAR(50),
    updated_by                   VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_house_bl_nonbl_container_nonbl_id
    ON fms.house_bl_nonbl_container(house_bl_non_bl_id);

COMMENT ON COLUMN fms.house_bl_sea_container.house_bl_sea_id        IS 'House B/L SEA ext 참조 FK (1:N, ON DELETE CASCADE)';
COMMENT ON COLUMN fms.house_bl_nonbl_container.house_bl_non_bl_id   IS 'House B/L NON_BL ext 참조 FK (1:N, ON DELETE CASCADE)';
