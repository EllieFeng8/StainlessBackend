-- Run once only if client_name has not already been added.
ALTER TABLE mydb.work_order
    ADD COLUMN client_name VARCHAR(255) NULL;
