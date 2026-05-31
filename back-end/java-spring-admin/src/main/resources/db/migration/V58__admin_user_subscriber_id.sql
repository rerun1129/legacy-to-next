-- Admin: admin_user에 subscriber_id BIGINT FK 컬럼 추가 (admin.subscriber 참조)
ALTER TABLE admin.admin_user ADD COLUMN subscriber_id BIGINT;

ALTER TABLE admin.admin_user
    ADD CONSTRAINT fk_admin_user_subscriber
    FOREIGN KEY (subscriber_id) REFERENCES admin.subscriber(subscriber_id);

CREATE INDEX ix_admin_user_subscriber_id ON admin.admin_user(subscriber_id);

COMMENT ON COLUMN admin.admin_user.subscriber_id IS '소속 고객사 (FK → admin.subscriber.subscriber_id, NULL=미배정)';

-- subscriber_id 미배정 전원 → SELF 고객사로 일괄 매핑
UPDATE admin.admin_user u
SET subscriber_id = s.subscriber_id, updated_at = now()
FROM admin.subscriber s
WHERE s.subscriber_code = 'SELF'
  AND u.subscriber_id IS NULL;
