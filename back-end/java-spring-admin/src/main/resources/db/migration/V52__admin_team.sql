-- Admin: 조직/부서 마스터 테이블 + 초기 6팀 시드
CREATE TABLE admin.team (
    team_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    team_code   VARCHAR(20)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    sort_order  INT,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  VARCHAR(50),
    updated_by  VARCHAR(50),
    CONSTRAINT uq_admin_team_team_code UNIQUE (team_code)
);

CREATE INDEX ix_admin_team_active ON admin.team(active);

COMMENT ON TABLE  admin.team             IS 'Admin: 조직/부서 마스터';
COMMENT ON COLUMN admin.team.team_id     IS '팀 PK (IDENTITY)';
COMMENT ON COLUMN admin.team.team_code   IS '팀 코드 (UNIQUE, FMS team_code와 어휘 정합)';
COMMENT ON COLUMN admin.team.name        IS '팀 표시명';
COMMENT ON COLUMN admin.team.description IS '팀 설명';
COMMENT ON COLUMN admin.team.sort_order  IS '정렬 순서';
COMMENT ON COLUMN admin.team.active      IS '활성 여부';
COMMENT ON COLUMN admin.team.created_at  IS '생성 일시';
COMMENT ON COLUMN admin.team.updated_at  IS '수정 일시';
COMMENT ON COLUMN admin.team.created_by  IS '생성자';
COMMENT ON COLUMN admin.team.updated_by  IS '수정자';

INSERT INTO admin.team (team_code, name, sort_order, active) VALUES
    ('SEA',      '해상팀', 1, TRUE),
    ('AIR',      '항공팀', 2, TRUE),
    ('SALES',    '영업팀', 3, TRUE),
    ('FINANCE',  '재무팀', 4, TRUE),
    ('HR',       '인사팀', 5, TRUE),
    ('STRATEGY', '전략팀', 6, TRUE)
ON CONFLICT (team_code) DO NOTHING;
