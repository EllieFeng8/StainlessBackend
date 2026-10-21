-- Run once only if these columns have not already been added.
ALTER TABLE mydb.work_order
    ADD COLUMN approved_by VARCHAR(255) NULL,
    ADD COLUMN reviewed_by VARCHAR(255) NULL,
    ADD COLUMN inspected_by VARCHAR(255) NULL,
    ADD COLUMN prepared_by VARCHAR(255) NULL;
