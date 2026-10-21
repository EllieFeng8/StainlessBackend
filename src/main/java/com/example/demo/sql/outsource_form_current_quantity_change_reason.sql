-- Run once before deploying the updated API if these columns do not exist yet.
-- Quantity follows outsource_quantity's FLOAT representation; old rows allow NULL.
ALTER TABLE mydb.outsource_form
    ADD COLUMN current_quantity FLOAT NULL,
    ADD COLUMN change_reason TEXT NULL;
