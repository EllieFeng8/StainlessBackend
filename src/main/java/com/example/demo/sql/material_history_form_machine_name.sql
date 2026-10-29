-- Run once after material_history_form_machine_id.sql, before deploying the new API.
-- Existing numeric IDs are preserved as text; populate actual machine names separately.
ALTER TABLE mydb.material_history_form
    CHANGE COLUMN machine_id machine_name VARCHAR(255) NULL DEFAULT NULL;
