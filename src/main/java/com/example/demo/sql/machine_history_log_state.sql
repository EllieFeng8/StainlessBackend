-- Run each statement only if the corresponding column does not already exist.
ALTER TABLE mydb.head_machine_history_log ADD COLUMN state INT NULL;
ALTER TABLE mydb.thread_machine_history_log ADD COLUMN state INT NULL;
