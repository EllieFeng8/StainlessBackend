-- Run once before deploying the API changes, if these columns do not exist yet.
-- Existing rows receive 10000 / 9500 / 9000.
-- API POST calculates omitted limits from the supplied target_value.
ALTER TABLE mydb.material_history_form
    ADD COLUMN target_value DECIMAL(20, 6) NOT NULL DEFAULT 10000,
    ADD COLUMN alert_upper_limit DECIMAL(20, 6) NOT NULL DEFAULT 9500,
    ADD COLUMN alert_lower_limit DECIMAL(20, 6) NOT NULL DEFAULT 9000;
