-- Run once only on the old schema, before deploying the updated API.
-- Offset quantities follow outsource_quantity's FLOAT representation and allow NULL.
ALTER TABLE mydb.outsource_form
    ADD COLUMN outsource_up_offset_quantity FLOAT NULL,
    ADD COLUMN outsource_down_offset_quantity FLOAT NULL,
    RENAME COLUMN status TO state;
