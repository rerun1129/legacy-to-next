-- house_bl_dim 분리: AIR + TRUCK + NON_BL ext별 분리 (SEA 미사용)
-- ER 재구조화 Phase 4 Step 4.1
-- DB 새로 채우는 환경 가정 — 기존 데이터 백필 없음. 기존 테이블 DROP 후 새 테이블 3개 생성.

-- 기존 단일 dim 테이블 제거
DROP TABLE IF EXISTS fms.house_bl_dim CASCADE;

-- AIR ext 전용 dim
CREATE TABLE fms.house_bl_air_dim (
    house_bl_air_dim_id  BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_bl_air_id      BIGINT       NOT NULL
        REFERENCES fms.house_bl_air(house_bl_air_id) ON DELETE CASCADE,
    length_cm            NUMERIC(10,2),
    width_cm             NUMERIC(10,2),
    height_cm            NUMERIC(10,2),
    quantity             INT,
    cbm                  NUMERIC(10,3),
    volume_weight_kg     NUMERIC(12,3),
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by           VARCHAR(50),
    updated_by           VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_house_bl_air_dim_air_id
    ON fms.house_bl_air_dim(house_bl_air_id);

-- TRUCK ext 전용 dim
CREATE TABLE fms.house_bl_truck_dim (
    house_bl_truck_dim_id  BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_bl_truck_id      BIGINT       NOT NULL
        REFERENCES fms.house_bl_truck(house_bl_truck_id) ON DELETE CASCADE,
    length_cm              NUMERIC(10,2),
    width_cm               NUMERIC(10,2),
    height_cm              NUMERIC(10,2),
    quantity               INT,
    cbm                    NUMERIC(10,3),
    volume_weight_kg       NUMERIC(12,3),
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by             VARCHAR(50),
    updated_by             VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_house_bl_truck_dim_truck_id
    ON fms.house_bl_truck_dim(house_bl_truck_id);

-- NON_BL ext 전용 dim
CREATE TABLE fms.house_bl_nonbl_dim (
    house_bl_nonbl_dim_id  BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_bl_non_bl_id     BIGINT       NOT NULL
        REFERENCES fms.house_bl_non_bl(house_bl_non_bl_id) ON DELETE CASCADE,
    length_cm              NUMERIC(10,2),
    width_cm               NUMERIC(10,2),
    height_cm              NUMERIC(10,2),
    quantity               INT,
    cbm                    NUMERIC(10,3),
    volume_weight_kg       NUMERIC(12,3),
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by             VARCHAR(50),
    updated_by             VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_house_bl_nonbl_dim_nonbl_id
    ON fms.house_bl_nonbl_dim(house_bl_non_bl_id);

COMMENT ON COLUMN fms.house_bl_air_dim.house_bl_air_id        IS 'House B/L AIR ext 참조 FK (1:N, ON DELETE CASCADE)';
COMMENT ON COLUMN fms.house_bl_truck_dim.house_bl_truck_id    IS 'House B/L TRUCK ext 참조 FK (1:N, ON DELETE CASCADE)';
COMMENT ON COLUMN fms.house_bl_nonbl_dim.house_bl_non_bl_id   IS 'House B/L NON_BL ext 참조 FK (1:N, ON DELETE CASCADE)';
