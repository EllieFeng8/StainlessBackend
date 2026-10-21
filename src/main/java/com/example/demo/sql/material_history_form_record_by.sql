-- Run once only if this column has not already been added.
ALTER TABLE mydb.material_history_form
    ADD COLUMN record_by VARCHAR(255) NULL;
