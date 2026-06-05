ALTER TABLE bms.freight_header
    ALTER COLUMN bl_id TYPE BIGINT USING bl_id::bigint;
