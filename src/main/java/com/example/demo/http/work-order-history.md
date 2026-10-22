# 歷史頁整合查詢 API

本次僅實作後端；前端參考檔案已讀取，尚未修改。既有工單、派工、委外、首件、結單與 historyLog 流程均未變更。

## 結構與資料來源

- `controller/WorkOrderHistoryController.java`：HTTP 綁定與回應。
- `dto/WorkOrderHistoryQuery.java`：不可變查詢參數，建構時驗證。
- `service/WorkOrderHistoryService.java`：協調列表、選項、匯出；統一狀態、detail、時間與回應映射。
- `dao/WorkOrderHistoryDao.java`：NamedParameterJdbcTemplate；三類來源 UNION ALL 後 LEFT JOIN 工單。機台使用 LEFT JOIN。沒有 HTTP 呼叫、逐張工單查詢或 historyLog 查詢。
- `dto/WorkOrderHistoryItem.java`、`WorkOrderHistoryPage.java`：API 回應；`WorkOrderHistoryRow.java` 僅作內部 nullable JDBC projection。

延續既有具體 Service 類別，沒有額外 ServiceImpl。讀取端點延續現有直接 JSON 回應慣例；不包 mutation API 的 ApiResponse。錯誤採既有 ResponseStatusException 與 Spring MVC 綁定錯誤（400）。

2026-10-21 已以 information_schema 核對本機實際表結構：

| 來源 | 工單關聯 | 機台／廠商 | 數量 | 時間 |
| --- | --- | --- | --- | --- |
| work_order_heading_machine | work_order_code → work_order.id | machineId → head_machine.id；head_machine_name | target_quantity / current_quantity | started_at / end_at |
| work_order_threading_machine | work_order_code → work_order.id | machineId → thread_machine.id；thread_rolling_machine_name | target_quantity / current_quantity | started_at / end_at |
| outsource_form | work_order_id → work_order.id | outsource_position | outsource_quantity / current_quantity | started_at / end_at |

`work_order.work_code` 是 INT（Java workOrder），`furnaceNo` 對應 furaceNo，`model` 對應 productCode。派工 approved 實際是可空 INT；以 JDBC Boolean 讀取，與既有 mapper 相同。兩派工表及模型均沒有 failureRate，所以一律 null；委外 oee 也是 null。所有數值以 nullable BigDecimal 投影，避免既有 primitive mapper 將 SQL NULL 轉為 0。

## 時間契約

實際時間欄位是 DATETIME，沒有儲存時區或 offset。本機資料庫 session/system 時區為 SYSTEM/UTC；既有 application.properties 明確指定 JDBC `serverTimezone=Asia/Taipei`。本 API **沿用該應用程式時間解讀慣例**，將 DATETIME 視為台北牆上時間，輸出 ISO 8601 offset 字串，例如 `2026-10-21T08:30:00+08:00`；不使用 JVM 或使用者瀏覽器預設時區。

這不表示資料庫可以證明舊資料最初的寫入時區；其他部署若有不同寫入慣例，需先確認並同步調整此時間契約，不能僅變更資料庫主機時區。新查詢不轉換或更新任何既有儲存值。

timestamp = end_at，缺漏才用 started_at；兩者皆無回 null。at 與 timestamp 完全相同。日期篩選使用同一組合時間，起日含當日 00:00:00，迄日採「隔日 00:00:00 不含」，完整保留秒以下精度。有任一日期篩選時排除空日期。

## 端點

context-path 既有設定為 `/api`。

### GET /api/work-order-history

| 參數 | 語意 |
| --- | --- |
| workOrderId | 顯示的工單編號，非資料庫 id |
| machineName | 機台／委外廠商名稱 |
| heatNumber | 爐號 |
| productCode | 產品型號 |
| startDate / endDate | 可省略，YYYY-MM-DD，台北日期；startDate 不可晚於 endDate |
| page | 從 1 起算，預設 1；超過最後一頁回空 items 且保留 total |
| pageSize | 預設 10，1–200 |

四個文字條件：去除輸入首尾空白、不分大小寫的包含比對，條件間 AND，空字串不篩選。百分比符號、底線視為普通字元，並非 SQL 萬用字元。所有使用者條件皆參數化。

先合併三種類型再全域排序、分頁與計數。工單編號及缺失工單 fallback id 都是整數，因此用 DECIMAL 排序即可得到此 schema 下的自然排序（2 在 10 前）。同工單時間倒序，空日期最後；再以 stage、來源 id 排序，確保相同快照中順序穩定。列表的 count/page 在唯讀 REPEATABLE_READ 交易內。不同請求間若資料新增或狀態改變，offset 分頁仍可能移動，並非跨請求快照。

```http
GET /api/work-order-history?workOrderId=2&machineName=Vendor&startDate=2026-10-01&endDate=2026-10-21&page=1&pageSize=10
```

以下為示意回應，並非實際業務資料：

```json
{
  "items": [{
    "recordId": "outsourcing-1",
    "combinationId": "outsourcing-1",
    "stage": "outsourcing",
    "workOrderId": "2",
    "machineName": "Vendor",
    "heatNumber": "Heat_A",
    "productCode": "M1",
    "timestamp": "2026-10-21T17:00:00+08:00",
    "at": "2026-10-21T17:00:00+08:00",
    "canNavigate": true,
    "currentStatus": "委外中",
    "detail": "變更交期",
    "targetQuantity": 50,
    "producedQuantity": 10,
    "oee": null,
    "failureRate": null
  }],
  "total": 1,
  "page": 1,
  "pageSize": 10
}
```

recordId/combinationId 分別使用 head-ID、thread-ID、outsourcing-ID。派工 detail 連接非 null、非空字串的 reason、remark、opinion（保留原字串空白，與 JS filter(Boolean) 相同）。委外直接使用 state、changeReason，不套用派工狀態判斷。

派工狀態優先順序：2 且 approved=true → 異常結單；1 → 正常結單；2 → 異常結單．待主管審核；3 → 異常結單；4 → 異常退回．待補正；正式生產 → 生產中；其他非空 productionStatus → 原值；有 startedAt → 生產中；其餘 → 等待生產。

找不到工單保留紀錄，以原始關聯 id 作 workOrderId，無 id 則空字串，canNavigate=false。找不到機台名稱顯示「機台 ID」，無 ID 則「—」。委外位置缺漏為空字串。其餘字串 null → 空字串；數值及時間缺漏 → null。

### GET /api/work-order-history/options

不接受篩選或分頁，從所有可讀紀錄取 distinct 選項，符合舊頁的完整資料選項行為。包含委外；排除空字串，保留大小寫不同的值；以台灣 Collator 搭配數字段自然排序。

```json
{
  "workOrderId": ["2", "10"],
  "machineName": ["Head 2", "Vendor"],
  "heatNumber": ["Heat_A"],
  "productCode": ["M1"]
}
```

### GET /api/work-order-history/export

接受與列表相同查詢參數；page/pageSize 若提供仍需合法，但不限制匯出筆數。篩選、排序、DTO 映射與狀態皆共用列表實作。回應 `text/csv;charset=UTF-8`，UTF-8 BOM，CRLF，標準雙引號跳脫，Content-Disposition attachment，檔名 work-order-history.csv。採 JDBC forward-only 串流，不將全部明細載入 Java List。

```http
GET /api/work-order-history/export?startDate=2026-10-01&endDate=2026-10-21
```

欄位：工單、爐號、料號、日期時間、製程、機台／委外廠商、狀態、目標產量、已生產、OEE、故障率、內容。狀態/detail 使用 API 原值，百分比欄輸出原數值；缺漏為空白。為防試算表公式執行，文字首個非空白字元為 =、+、-、@ 時在原文字前補單引號。

## 授權、效率與限制

現有程式與依賴中沒有 Spring Security、身份攔截器或 tenant/user 資料範圍條件；既有來源端點讀取全部資料。本功能保持相同範圍，未自行建立新的使用者權限模型，也沒有放寬已存在的條件。若部署環境透過反向代理做路徑授權，須把三個新路徑納入相同規則；此設定不在本專案中。未來加入資料隔離時，必須統一套用 DAO 共用來源，涵蓋 list/count/options/export。

列表兩次 SQL；選項四次 distinct SQL（非逐工單查詢）；匯出一次 SQL。篩選、全域排序與分頁都在 MySQL。contains、COALESCE 與 UNION 仍可能需掃描及資料庫暫存排序；不聲稱索引能解決任意包含查詢。選項僅載入 distinct 值，但選項基數很大時仍有記憶體成本。

## 前端改接位置（尚未修改）

1. `C:/0_working/5-datacanvas/datacanvas/src/services/manufacturingApi.js`：將 getWorkOrderEventHistory 改為帶查詢參數請求 `/work-order-history` 並回傳 page object。移除該函式中的五來源下載、關聯查找、detail 組合、assignmentStatus 呼叫與排序；其他頁使用的 assignmentStatus 應保留。另新增 options 與 export 呼叫。
2. `C:/0_working/5-datacanvas/datacanvas/src/pages/HistoryPage.jsx`：events 改接 items；新增 total；文字、日期、page、pageSize 改變時重新查詢。篩選改變回 page=1。focus/storage 刷新仍可保留；清理過期請求，避免舊回應覆蓋新篩選。
3. 移除 filteredEvents、rows grouping、slice 及由當頁推算選項的 useMemo；每筆 API item 已是唯一紀錄，row key 使用 recordId，時間直接讀 row.timestamp。頁數/筆數以 total 計算；超頁時由前端調整 page 再請求。
4. 初始化及重新整理時呼叫 `/work-order-history/options`，不以 items 產生 datalist。
5. exportHistory 改以相同篩選參數下載 `/work-order-history/export` 的 Blob；請勿使用現有只處理 JSON/text 的封裝來重建 CSV，也不要僅匯出目前 items。
6. 表頭、篩選名稱、CSV 相關標示改為「機台／委外廠商」。委外內容不再強制「—」，可顯示 detail/changeReason。
7. outsourcing 且 canNavigate=true 只可導向 `/work-orders/{workOrderId}`；不得拼出 field-status 或首件頁路徑。head/thread 可保留原有 combinationId 導航。canNavigate=false 時禁用導航。
8. 若 UI 要保留「等待生產／等待材料核對 → 未生產」或 detail 空時顯示狀態，這僅為顯示別名；不得再計算生產狀態。CSV 以後端原值為準。

## 與原有前端行為的差異

- 新增委外，每筆委外一列；不展開為歷程事件。
- 日期固定台北 offset，空日期由空字串改為 null；不再取決於使用者瀏覽器所在時區。迄日包含全部秒以下精度。
- 資料庫 NULL 數值不再受舊 primitive getter 影響而變成 0；本次按需求保留 null。
- 排序加入 stage/id 作相同工單及時間的穩定次排序；既有 JS 靠輸入順序。整數工單自然排序一致。選項的文字排序採 JDK 台灣 Collator，極少數字元可能與瀏覽器 ICU 排序不同。
- 超頁回空 items，不自動壓回最後一頁；前端應依 total 校正。
- CSV 缺漏空白，不補「—」；輸出原始狀態、數值與 detail，不套顯示別名／百分比格式／狀態補內容，委外 detail 不再強制空白；新增公式注入防護。
- 分頁後下拉資料由獨立完整選項端點提供。

## 測試

使用 Java 25（專案 pom 設定），在專案根目錄執行：

```powershell
./mvnw.cmd '-Dtest=WorkOrderHistoryServiceTest,WorkOrderHistoryMysqlTest' '-Dhistory.mysql.tests=true' '-Dhistory.frontend.tests=true' test
```

MySQL 整合測試讀取本機 application.properties，建立僅在測試連線內可見的 TEMPORARY tables，連線關閉即清除，不改動永久業務表。另有獨立唯讀實際 schema smoke test。一般不加 history.mysql.tests 時跳過資料庫整合；frontend parity 額外需要 Node 與上述原始前端檔案，缺檔將失敗，不以替代資料假裝成功。狀態單元測試涵蓋 210 種組合。

2026-10-21 實測結果：**222 tests，0 failures，0 errors，0 skipped，BUILD SUCCESS**。

- WorkOrderHistoryServiceTest：212（210 狀態組合、查詢驗證、完整選項自然排序）。
- WorkOrderHistoryMysqlTest：10（三來源映射、孤立／空值、日期邊界、全域排序／分頁／total、文字篩選、選項與 CSV、HTTP 契約、大小寫／空白選項、實際資料表唯讀查詢、直接執行原始 getWorkOrderEventHistory 的打頭／搓牙逐欄比較）。
- 編譯使用 Java 25；執行 Node v24.13.0 的原始前端函式比對；未執行原有 DemoApplicationTests 的完整應用程式啟動測試。
- 測試原始碼位於專案 `src/test/java/com/example/demo/service/WorkOrderHistoryServiceTest.java` 與 `WorkOrderHistoryMysqlTest.java`。
