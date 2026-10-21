-- Run after adding the review columns and before starting the updated API.
-- Legacy 3/4 both represent abnormal closure, with approved/rejected review.
-- Legacy 1/2 retain their classification and existing approved value.
-- Reviewer/opinion previously stored only in history_log are not reconstructed.
START TRANSACTION;

UPDATE mydb.work_order_heading_machine
SET approved = CASE order_closed WHEN 3 THEN TRUE WHEN 4 THEN FALSE END,
    order_closed = 2
WHERE order_closed IN (3, 4);

UPDATE mydb.work_order_threading_machine
SET approved = CASE order_closed WHEN 3 THEN TRUE WHEN 4 THEN FALSE END,
    order_closed = 2
WHERE order_closed IN (3, 4);

COMMIT;
