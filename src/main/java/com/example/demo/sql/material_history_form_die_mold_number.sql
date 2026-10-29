-- Run once after material_history_form_alert_limits.sql and before deploying.
-- Preserve existing values; allow new rows without mold_number to store NULL limits.
ALTER TABLE mydb.material_history_form
    ADD COLUMN die_number VARCHAR(255) NULL DEFAULT NULL,
    ADD COLUMN mold_number VARCHAR(255) NULL DEFAULT NULL,
    MODIFY COLUMN target_value DECIMAL(20, 6) NULL DEFAULT NULL,
    MODIFY COLUMN alert_upper_limit DECIMAL(20, 6) NULL DEFAULT NULL,
    MODIFY COLUMN alert_lower_limit DECIMAL(20, 6) NULL DEFAULT NULL;
