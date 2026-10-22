# 工單明細 API 驗證結果

驗證日期：2026-10-22（Asia/Taipei）。

- JDK 25；沿用專案 Spring Boot 4.1.1 與 Maven wrapper，離線相依套件。
- 全部 96 個後端 Java 來源編譯成功。
- **141 tests，0 failures，0 errors，0 skipped。**
- WorkOrderDetailTest：133 項（含 120 組狀態優先序參數案例）。
- WorkOrderDetailMysqlTest：8 項，使用實際 MySQL、連線內暫存表與實際 DAO／Service／交易。
- `git diff --check` 通過。

測試對照：

| 需求 | 證據 |
|---|---|
| 無派工／只有委外／三製程 | emptyAndOnlyOutsource、allStagesMissingRelationsAndActions、actualSqlReadOnlyContractAndSample |
| 結單／審核／生產狀態優先序 | statusPriority，5×2×6×2 = 120 組 |
| 明細與列表規則差異 | summaryIsDetailRuleNotListRule |
| 首件不得混用不同工單 | sameMachineDifferentOrdersAndDuplicateDetection，實際 SQL 工單過濾 |
| 製程欄位與共用 state 隔離 | zeroBlankAndIndependentInspectionFields、sharedRecordCompletionAndRerunDoNotCrossStages |
| 0／空字串／空白／null | zeroBlankAndIndependentInspectionFields |
| 已有值但要求重跑 | prepareCompleteRerunAndLegacyRollback，保留量測但撤銷完成與資格 |
| 免檢／追加跨服務實例持久化 | persistedExemptionAndAdditionalAcrossFreshServiceInstances、exemptionSurvivesNewServiceAndStaleCommandsFail、additionalProductionPersistsAndCannotOverrideRerun |
| 委外與缺值 | emptyAndOnlyOutsource、allStagesMissingRelationsAndActions |
| 百分比／零目標／8 位邊界／安全整數 | toleranceUsesQuantitiesAndEightDecimalBoundaries |
| 缺機台／物料／首件／歷史 | allStagesMissingRelationsAndActions、emptyAndOnlyOutsource |
| JSON／文字／壞 JSON／未知物件／陣列 | historyFormatsKnownJsonAndPreservesOtherLogs |
| GET 不寫入 | getUsesTenSelectsRegardlessOfAssignmentsAndIsReadOnly；MySQL 比較首件與狀態表數量 |
| 查詢數不隨派工數成長 | getUsesTenSelectsRegardlessOfAssignmentsAndIsReadOnly：100 筆派工仍只有 10 次 SELECT |
| 過期操作重驗 | staleExemptionCommandRevalidatesLockedState、productionStartChecksServerClearanceBeforeWrite、MySQL exemptionSurvivesNewServiceAndStaleCommandsFail |
| prepare 冪等／重複紀錄 | prepareIsIdempotentForExistingAndRejectsDuplicates、prepareCompleteRerunAndLegacyRollback |
| 既有首件新增相容與同步 | legacyAddRemainsIdempotentAndCreatesAuthority |
| 交易回滾 | MySQL 完成未填欄位回 400 且 state 更新回滾 |
| SQL 排序 | actualEventOrderAndAssignmentIdOrder |
| 404／資料庫故障 | http404AndDatabaseErrorsAreNotEmptyLists |

唯讀核對實際 schema：work_code 為 INT UNIQUE、deadline_date 為 DATE、wire_dia/quantity/offset 為 FLOAT；首件量測欄位為 TEXT，使用已確認的 major_diameter_d2；首件組合並不唯一。現有授權層未提供物料 ACL。這些限制與遷移策略詳見 `http/work-order-detail.md`。

MySQL 整合測試從 SHOW CREATE TABLE 建立 TEMPORARY shadow table，僅移除外鍵以避免連到真實業務資料；新狀態 migration 同樣改成 TEMPORARY TABLE 測試。沒有執行正式 migration 或更新正式資料。完整 JSON 範例由實際整合回應產生。

限制：測試未啟動完整部署服務或修改前端；未做跨連線併發壓力測試（不同連線的暫存表不共享），併發保護採與既有首件新增相同的工單 FOR UPDATE 鎖並擴及新命令／完成／開工。上線仍須先套 migration、切換前端寫入流程，及處理文件列出的物料權限缺口與工單編號儲存限制。
