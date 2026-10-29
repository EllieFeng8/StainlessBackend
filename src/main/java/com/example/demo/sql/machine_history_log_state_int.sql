-- For existing state columns. Inspect/convert legacy nonnumeric values before running.
-- Do not cast legacy labels blindly: MySQL may convert unknown strings to 0 (disconnected).
ALTER TABLE mydb.head_machine_history_log MODIFY COLUMN state INT NULL;
ALTER TABLE mydb.thread_machine_history_log MODIFY COLUMN state INT NULL;
