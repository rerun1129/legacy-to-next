CREATE TABLE admin.user_ui_layout (
    layout_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES admin.admin_user(user_id),
    storage_key VARCHAR(80)  NOT NULL,
    payload     JSONB        NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_ui_layout UNIQUE (user_id, storage_key)
);
