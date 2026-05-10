-- house_bl_desc 분리: SEA + AIR + TRUCK ext별 분리
-- ER 재구조화 Phase 2 Step 2.1
-- DB 새로 채우는 환경 가정 — 기존 데이터 백필 없음. 기존 테이블 DROP 후 새 테이블 3개 생성.

-- 기존 단일 desc 테이블 제거
DROP TABLE IF EXISTS fms.house_bl_desc CASCADE;

-- SEA ext 전용 desc
CREATE TABLE fms.house_bl_sea_desc (
    house_bl_sea_desc_id  BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_bl_sea_id       BIGINT       NOT NULL UNIQUE
        REFERENCES fms.house_bl_sea(house_bl_sea_id) ON DELETE CASCADE,
    marks                 TEXT,
    description           TEXT,
    desc_clause_1         VARCHAR(50),
    desc_clause_2         VARCHAR(50),
    remark                TEXT,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by            VARCHAR(50),
    updated_by            VARCHAR(50)
);

-- AIR ext 전용 desc
CREATE TABLE fms.house_bl_air_desc (
    house_bl_air_desc_id  BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_bl_air_id       BIGINT       NOT NULL UNIQUE
        REFERENCES fms.house_bl_air(house_bl_air_id) ON DELETE CASCADE,
    marks                 TEXT,
    description           TEXT,
    desc_clause_1         VARCHAR(50),
    desc_clause_2         VARCHAR(50),
    remark                TEXT,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by            VARCHAR(50),
    updated_by            VARCHAR(50)
);

-- TRUCK ext 전용 desc
CREATE TABLE fms.house_bl_truck_desc (
    house_bl_truck_desc_id  BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    house_bl_truck_id       BIGINT       NOT NULL UNIQUE
        REFERENCES fms.house_bl_truck(house_bl_truck_id) ON DELETE CASCADE,
    marks                   TEXT,
    description             TEXT,
    desc_clause_1           VARCHAR(50),
    desc_clause_2           VARCHAR(50),
    remark                  TEXT,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by              VARCHAR(50),
    updated_by              VARCHAR(50)
);

COMMENT ON COLUMN fms.house_bl_sea_desc.house_bl_sea_id     IS 'House B/L SEA ext 참조 FK (1:1, ON DELETE CASCADE)';
COMMENT ON COLUMN fms.house_bl_sea_desc.desc_clause_1       IS '부지약관 구문 1 (해상 수출 전용)';
COMMENT ON COLUMN fms.house_bl_sea_desc.desc_clause_2       IS '부지약관 구문 2 (해상 수출 전용)';
COMMENT ON COLUMN fms.house_bl_air_desc.house_bl_air_id     IS 'House B/L AIR ext 참조 FK (1:1, ON DELETE CASCADE)';
COMMENT ON COLUMN fms.house_bl_truck_desc.house_bl_truck_id IS 'House B/L TRUCK ext 참조 FK (1:1, ON DELETE CASCADE)';
