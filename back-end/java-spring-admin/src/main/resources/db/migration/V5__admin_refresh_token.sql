CREATE TABLE admin.refresh_token (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    token_hash   VARCHAR(64)  NOT NULL,
    expires_at   TIMESTAMPTZ  NOT NULL,
    revoked_at   TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id)
        REFERENCES admin.admin_user(user_id) ON DELETE CASCADE
);
CREATE INDEX ix_refresh_token_user_id ON admin.refresh_token(user_id);

COMMENT ON TABLE  admin.refresh_token IS 'JWT refresh token (hash 저장, raw 미보관)';
COMMENT ON COLUMN admin.refresh_token.token_hash IS 'SHA-256 hex of raw refresh token';
COMMENT ON COLUMN admin.refresh_token.revoked_at IS 'revoke 시각 (NULL = active)';
