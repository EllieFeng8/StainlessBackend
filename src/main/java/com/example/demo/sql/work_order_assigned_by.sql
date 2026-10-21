-- Run once only if this column has not already been added.
ALTER TABLE mydb.work_order
    ADD COLUMN assigned_by VARCHAR(255) NULL;
