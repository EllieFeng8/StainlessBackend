-- Apply once before deploying this feature. No GET performs schema changes or backfills.
-- Existing first-test records are NOT proof that a browser did not request a rerun.
CREATE TABLE mydb.work_order_first_article_state (
  work_order_id INT NOT NULL,
  stage VARCHAR(6) NOT NULL,
  machine_id INT NOT NULL,
  requires_first_article BOOLEAN NOT NULL,
  completed_at DATETIME(6) NULL,
  exemption_confirmed_at DATETIME(6) NULL,
  exemption_reason VARCHAR(255) NULL,
  additional_production_at DATETIME(6) NULL,
  PRIMARY KEY (work_order_id, stage, machine_id),
  CONSTRAINT detail_stage CHECK (stage IN ('head','thread'))
);
-- Fail closed for legacy records, including complete measurements: local rerun flags are unavailable.
INSERT INTO mydb.work_order_first_article_state (work_order_id, stage, machine_id, requires_first_article)
SELECT DISTINCT work_order_code, 'head', head_machineId, TRUE FROM mydb.first_test_form
WHERE work_order_code IS NOT NULL AND head_machineId > 0;
INSERT INTO mydb.work_order_first_article_state (work_order_id, stage, machine_id, requires_first_article)
SELECT DISTINCT work_order_code, 'thread', thread_machineId, TRUE FROM mydb.first_test_form
WHERE work_order_code IS NOT NULL AND thread_machineId > 0;
-- No unique constraint is imposed on legacy first_test_form: duplicates must be reconciled,
-- not silently discarded. All application creation paths serialize on work_order FOR UPDATE.
-- Existing first_test_form.work_order_code index is used for the batch lookup.
