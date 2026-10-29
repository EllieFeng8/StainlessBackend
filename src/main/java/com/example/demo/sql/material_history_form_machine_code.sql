-- Run once after material_history_form_machine_name.sql, before deploying the API.
-- Rename only; preserve existing values and nullable VARCHAR(255) type.
ALTER TABLE mydb.material_history_form
    CHANGE COLUMN machine_name machine_code VARCHAR(255) NULL DEFAULT NULL;
