# 物料目標值與警戒上下限

機台欄位改為 `machine_code VARCHAR(255) NULL DEFAULT NULL`。目前已有 machine_name 的資料庫，部署前執行 `sql/material_history_form_machine_code.sql`，保留既有值；若目前只有 machine_id，先執行 `sql/material_history_form_machine_name.sql`；若連 machine_id 都尚未建立，依序執行 machine_id、machine_name、machine_code 三份 migration。舊 ID 會保留為文字，實際機台代碼若不同需另行更新。POST／PATCH 接受 `machine_code` 或 `machineCode`，GET 回傳 `machineCode`。API 不再使用 machine_name／machineName。POST 未填或傳 null 時儲存 NULL；PATCH 未填或傳 null 時保留原值，與其他欄位的部分更新規則一致。

部署 API 前先執行 `sql/material_history_form_alert_limits.sql`（僅執行一次，三個欄位尚未存在時使用）。欄位型別為 DECIMAL(20,6)，既有資料補為 10000、9500、9000。

接著執行 `sql/material_history_form_die_mold_number.sql`（僅一次）：新增 `die_number`、`mold_number`，型別皆為可空的 VARCHAR(255)，並將三個數值欄位改為允許 NULL、預設 NULL；既有數值保留，不回溯清空。已執行前次 migration 的資料庫只需執行此新增 migration。

POST `/materialHistory/addMaterialHistory` 與 PATCH `/materialHistory/updateMaterialHistory/{id}` 接受 camelCase 或 snake_case 新欄位名稱。GET 所有物料查詢沿用 camelCase 回傳 `targetValue`、`alertUpperLimit`、`alertLowerLimit`。

`die_number`／`dieNumber` 與 `mold_number`／`moldNumber` 同樣支援 POST、PATCH，GET 回傳 `dieNumber`、`moldNumber`，未填預設 NULL。

POST 範例：

```json
{"workOrderCode":1,"materialCode":"M001","die_number":"D-001","mold_number":"M-001","target_value":20000}
```

結果目標值為 20000，上限為 19000，下限為 18000。未填目標值或傳 null 時採 10000；各上下限未填或傳 null 時，分別以本次目標值的 95% 和 90% 計算。明確傳入的數值（包括 0）保留。

上述數值規則只在 POST 的 mold_number 為非空白字串時套用。未填、null、空字串或純空白均視為未提供模具編號，此時三個數值一律存 NULL，即使請求中有傳數值亦不採用。只填 die_number 不會啟用數值預設。

PATCH 僅更新非 null 的傳入欄位；只改 target_value 或 mold_number 不會自動修改上下限。若需一起變更，請一併傳入三個數值欄位。

百分比預設邏輯在 API POST 執行。直接 SQL INSERT 的資料庫預設值為 NULL，不會自動計算百分比。

驗證：`verification/run-detail-tests.ps1` 使用 JDK 25 執行，包含 14 項物料 API／DAO／映射測試；合計 147 項通過，8 項 MySQL 整合測試略過。實際資料庫 migration 尚未執行。

## 依工單與機台更新模具數值

`PATCH /materialHistory/updateMoldLimits`

```json
{
  "workOrderCode": 1,
  "machine_code": "PRESS-42",
  "target_value": 20000,
  "alert_upper_limit": 19000,
  "alert_lower_limit": 18000
}
```

工單使用現有整數 workOrderCode（資料庫欄位為 work_oder_code），也接受 work_order_code；machine_code 也接受 machineCode。數值欄位亦接受 camelCase。

更新此工單、此機台所有 mold_number 非 NULL 且非空白的紀錄，只更新傳入的非 null 數值，不修改模號、機台或工單。至少需傳入一個數值，未傳／null 保留原值，不重新計算上下限。成功或值未變更回傳 200；工單非正整數、機台空白／超過 255 字元或沒有有效更新欄位回傳 400；找不到符合條件的模具紀錄回傳 404。
