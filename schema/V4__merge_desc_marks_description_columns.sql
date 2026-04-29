-- V4: house_bl_desc / master_bl_desc 필드 병합
-- marks_left + marks_right → marks
-- description_left + description_right → description

ALTER TABLE house_bl_desc
    DROP COLUMN marks_left,
    DROP COLUMN marks_right,
    DROP COLUMN description_left,
    DROP COLUMN description_right,
    ADD COLUMN marks        TEXT,
    ADD COLUMN description  TEXT;

ALTER TABLE master_bl_desc
    DROP COLUMN marks_left,
    DROP COLUMN marks_right,
    DROP COLUMN description_left,
    DROP COLUMN description_right,
    ADD COLUMN marks        TEXT,
    ADD COLUMN description  TEXT;
