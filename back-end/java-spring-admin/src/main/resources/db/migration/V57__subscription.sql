-- Admin: 모듈 구독(subscription) 테이블 생성
CREATE TABLE IF NOT EXISTS admin.subscription (
    subscription_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    subscriber_id   BIGINT        NOT NULL,
    module_code     VARCHAR(40)   NOT NULL,
    start_date      DATE          NOT NULL,
    end_date        DATE          NOT NULL,
    active          BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    created_by      VARCHAR(50),
    updated_by      VARCHAR(50),
    CONSTRAINT fk_subscription_subscriber  FOREIGN KEY (subscriber_id) REFERENCES admin.subscriber(subscriber_id),
    CONSTRAINT uq_subscription_subscriber_module UNIQUE (subscriber_id, module_code),
    CONSTRAINT ck_subscription_date_range  CHECK (start_date <= end_date)
);

CREATE INDEX IF NOT EXISTS ix_subscription_lookup ON admin.subscription(subscriber_id, active);

COMMENT ON TABLE  admin.subscription                 IS '모듈 구독';
COMMENT ON COLUMN admin.subscription.subscription_id IS '구독 PK';
COMMENT ON COLUMN admin.subscription.subscriber_id   IS '구독 고객사 (FK → admin.subscriber)';
COMMENT ON COLUMN admin.subscription.module_code     IS '구독 모듈 (admin.attribute_value attribute_key=module 값: ADMIN/FMS)';
COMMENT ON COLUMN admin.subscription.start_date      IS '구독 시작일';
COMMENT ON COLUMN admin.subscription.end_date        IS '구독 종료일';
COMMENT ON COLUMN admin.subscription.active          IS '활성 여부';

-- SELF 고객사의 무제한 구독 시드 (module 카탈로그 = admin.attribute_value attribute_key='module')
INSERT INTO admin.subscription (subscriber_id, module_code, start_date, end_date, active)
SELECT s.subscriber_id, av.value, DATE '2000-01-01', DATE '9999-12-31', TRUE
FROM admin.subscriber s
CROSS JOIN admin.attribute_value av
WHERE s.subscriber_code = 'SELF'
  AND av.attribute_key = 'module'
  AND av.value IN ('ADMIN', 'FMS')
ON CONFLICT (subscriber_id, module_code) DO NOTHING;
