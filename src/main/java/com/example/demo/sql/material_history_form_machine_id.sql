-- Run once before deploying the API changes, if machine_id does not exist yet.
ALTER TABLE mydb.material_history_form
    ADD COLUMN machine_id INT NULL DEFAULT NULL;
