CREATE TABLE IF NOT EXISTS admin.faq_category (
    faq_category_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    sort_order      INTEGER      NOT NULL DEFAULT 0,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by      VARCHAR(50),
    updated_by      VARCHAR(50),
    CONSTRAINT uq_admin_faq_category_name UNIQUE (name)
);
CREATE INDEX IF NOT EXISTS ix_admin_faq_category_sort_order
    ON admin.faq_category(sort_order ASC) WHERE deleted_at IS NULL;

COMMENT ON TABLE  admin.faq_category                    IS 'FAQ 카테고리';
COMMENT ON COLUMN admin.faq_category.faq_category_id   IS 'FAQ 카테고리 식별자 (IDENTITY PK)';
COMMENT ON COLUMN admin.faq_category.name              IS '카테고리명 (UNIQUE)';
COMMENT ON COLUMN admin.faq_category.sort_order        IS '정렬 순서 (오름차순)';
COMMENT ON COLUMN admin.faq_category.active            IS '활성 여부';
COMMENT ON COLUMN admin.faq_category.deleted_at        IS 'soft delete 시각';
COMMENT ON COLUMN admin.faq_category.created_at        IS '생성 시각';
COMMENT ON COLUMN admin.faq_category.updated_at        IS '최종 수정 시각';
COMMENT ON COLUMN admin.faq_category.created_by        IS '생성자';
COMMENT ON COLUMN admin.faq_category.updated_by        IS '최종 수정자';

CREATE TABLE IF NOT EXISTS admin.faq (
    faq_id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    faq_category_id BIGINT       NOT NULL,
    question        VARCHAR(500) NOT NULL,
    answer          TEXT         NOT NULL,
    sort_order      INTEGER      NOT NULL DEFAULT 0,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by      VARCHAR(50),
    updated_by      VARCHAR(50),
    CONSTRAINT fk_faq_category FOREIGN KEY (faq_category_id)
        REFERENCES admin.faq_category(faq_category_id) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS ix_admin_faq_category_sort
    ON admin.faq(faq_category_id, sort_order ASC) WHERE deleted_at IS NULL;

COMMENT ON TABLE  admin.faq                    IS 'FAQ';
COMMENT ON COLUMN admin.faq.faq_id             IS 'FAQ 식별자 (IDENTITY PK)';
COMMENT ON COLUMN admin.faq.faq_category_id    IS 'FAQ 카테고리 FK';
COMMENT ON COLUMN admin.faq.question           IS '질문';
COMMENT ON COLUMN admin.faq.answer             IS '답변 (plain text)';
COMMENT ON COLUMN admin.faq.sort_order         IS '정렬 순서 (오름차순)';
COMMENT ON COLUMN admin.faq.active             IS '활성 여부';
COMMENT ON COLUMN admin.faq.deleted_at         IS 'soft delete 시각';
COMMENT ON COLUMN admin.faq.created_at         IS '생성 시각';
COMMENT ON COLUMN admin.faq.updated_at         IS '최종 수정 시각';
COMMENT ON COLUMN admin.faq.created_by         IS '생성자';
COMMENT ON COLUMN admin.faq.updated_by         IS '최종 수정자';
