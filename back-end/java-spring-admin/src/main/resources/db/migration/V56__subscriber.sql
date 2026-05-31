-- Admin: 구독 고객사(subscriber) 테이블 생성
CREATE TABLE IF NOT EXISTS admin.subscriber (
    subscriber_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    subscriber_code VARCHAR(40)   NOT NULL,
    name            VARCHAR(200)  NOT NULL,
    name_en         VARCHAR(200),
    business_no     VARCHAR(50),
    representative  VARCHAR(100),
    phone           VARCHAR(50),
    email           VARCHAR(200),
    memo            VARCHAR(1000),
    active          BOOLEAN       NOT NULL DEFAULT TRUE,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    created_by      VARCHAR(50),
    updated_by      VARCHAR(50),
    CONSTRAINT uq_subscriber_code UNIQUE (subscriber_code)
);

CREATE INDEX IF NOT EXISTS ix_subscriber_code_active ON admin.subscriber(subscriber_code, active);

COMMENT ON TABLE  admin.subscriber              IS '구독 고객사';
COMMENT ON COLUMN admin.subscriber.subscriber_id   IS '구독 고객사 PK';
COMMENT ON COLUMN admin.subscriber.subscriber_code IS '고객사 식별 코드 (UNIQUE)';
COMMENT ON COLUMN admin.subscriber.name            IS '고객사명 (한글)';
COMMENT ON COLUMN admin.subscriber.name_en         IS '고객사명 (영문)';
COMMENT ON COLUMN admin.subscriber.business_no     IS '사업자등록번호';
COMMENT ON COLUMN admin.subscriber.representative  IS '대표자명';
COMMENT ON COLUMN admin.subscriber.phone           IS '연락처';
COMMENT ON COLUMN admin.subscriber.email           IS '이메일';
COMMENT ON COLUMN admin.subscriber.memo            IS '비고';
COMMENT ON COLUMN admin.subscriber.active          IS '활성 여부';
COMMENT ON COLUMN admin.subscriber.deleted_at      IS '소프트 삭제 시각';

-- 자사(내부 운영자) 고객사 시드: 무제한 구독 기준 고객사
INSERT INTO admin.subscriber (subscriber_code, name, name_en, active)
VALUES ('SELF', '자사', 'Self', TRUE)
ON CONFLICT (subscriber_code) DO NOTHING;
