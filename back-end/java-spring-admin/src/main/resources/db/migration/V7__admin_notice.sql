CREATE TABLE IF NOT EXISTS admin.notice (
    notice_id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title        VARCHAR(500) NOT NULL,
    content      TEXT         NOT NULL,
    pinned       BOOLEAN      NOT NULL DEFAULT FALSE,
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    published_at TIMESTAMPTZ,
    expires_at   TIMESTAMPTZ,
    deleted_at   TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by   VARCHAR(50),
    updated_by   VARCHAR(50)
);

COMMENT ON TABLE  admin.notice              IS '공지사항';
COMMENT ON COLUMN admin.notice.notice_id    IS '공지사항 식별자 (IDENTITY PK)';
COMMENT ON COLUMN admin.notice.title        IS '공지 제목';
COMMENT ON COLUMN admin.notice.content      IS '공지 본문 (plain text, 줄바꿈 보존)';
COMMENT ON COLUMN admin.notice.pinned       IS '상단 고정 여부';
COMMENT ON COLUMN admin.notice.active       IS '활성 여부';
COMMENT ON COLUMN admin.notice.published_at IS '게시 시각 (null = 임시저장)';
COMMENT ON COLUMN admin.notice.expires_at   IS '게시 만료 시각 (null = 만료 없음)';
COMMENT ON COLUMN admin.notice.deleted_at   IS 'soft delete 시각';
COMMENT ON COLUMN admin.notice.created_at   IS '생성 시각';
COMMENT ON COLUMN admin.notice.updated_at   IS '최종 수정 시각';
COMMENT ON COLUMN admin.notice.created_by   IS '생성자';
COMMENT ON COLUMN admin.notice.updated_by   IS '최종 수정자';

CREATE INDEX IF NOT EXISTS ix_admin_notice_pinned_published
    ON admin.notice(pinned DESC, published_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS ix_admin_notice_published_at
    ON admin.notice(published_at) WHERE deleted_at IS NULL AND published_at IS NOT NULL;
