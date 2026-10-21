-- Run once before deploying the API, only if these columns do not exist yet.
ALTER TABLE mydb.work_order_heading_machine
    ADD COLUMN employee_code VARCHAR(255) NULL,
    ADD COLUMN reviewer VARCHAR(255) NULL,
    ADD COLUMN approved BOOLEAN NULL,
    ADD COLUMN opinion TEXT NULL;

ALTER TABLE mydb.work_order_threading_machine
    ADD COLUMN employee_code VARCHAR(255) NULL,
    ADD COLUMN reviewer VARCHAR(255) NULL,
    ADD COLUMN approved BOOLEAN NULL,
    ADD COLUMN opinion TEXT NULL;
