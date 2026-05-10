-- master_bl_desc 분리: SEA + AIR ext별 분리 (MasterBl은 TRUCK 미사용)
-- ER 재구조화 Phase 2 Step 2.2
-- DB 새로 채우는 환경 가정 — 기존 데이터 백필 없음. 기존 테이블 DROP 후 새 테이블 2개 생성.

-- 기존 단일 desc 테이블 제거
DROP TABLE IF EXISTS fms.master_bl_desc CASCADE;

-- SEA ext 전용 desc
CREATE TABLE fms.master_bl_sea_desc (
    master_bl_sea_desc_id  BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    master_bl_sea_id       BIGINT       NOT NULL UNIQUE
        REFERENCES fms.master_bl_sea(master_bl_sea_id) ON DELETE CASCADE,
    marks                  TEXT,
    description            TEXT,
    desc_clause_1          VARCHAR(50),
    desc_clause_2          VARCHAR(50),
    remark                 TEXT,
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by             VARCHAR(50),
    updated_by             VARCHAR(50)
);

-- AIR ext 전용 desc
CREATE TABLE fms.master_bl_air_desc (
    master_bl_air_desc_id  BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    master_bl_air_id       BIGINT       NOT NULL UNIQUE
        REFERENCES fms.master_bl_air(master_bl_air_id) ON DELETE CASCADE,
    marks                  TEXT,
    description            TEXT,
    desc_clause_1          VARCHAR(50),
    desc_clause_2          VARCHAR(50),
    remark                 TEXT,
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by             VARCHAR(50),
    updated_by             VARCHAR(50)
);

COMMENT ON COLUMN fms.master_bl_sea_desc.master_bl_sea_id IS 'Master B/L SEA ext 참조 FK (1:1, ON DELETE CASCADE)';
COMMENT ON COLUMN fms.master_bl_sea_desc.desc_clause_1   IS '부지약관 구문 1 (해상 수출 전용)';
COMMENT ON COLUMN fms.master_bl_sea_desc.desc_clause_2   IS '부지약관 구문 2 (해상 수출 전용)';
COMMENT ON COLUMN fms.master_bl_air_desc.master_bl_air_id IS 'Master B/L AIR ext 참조 FK (1:1, ON DELETE CASCADE)';
