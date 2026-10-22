# 工單明細整合 API

## 使用與部署

`GET /api/work-orders/{workOrderCode}/detail`

`/api` 是既有 `server.servlet.context-path`，Controller mapping 不重複加 `/api`。路徑參數是顯示工單編號 **字串**，不等於資料庫 ID，不做數值轉換、不移除前導零。空白／超過 255 字元回 400，找不到工單 404，顯示編號重複 409，資料庫失敗正常向上拋出 5xx，不回假空陣列。

先於維護窗口執行 `../sql/work_order_detail_first_article_state.sql`，再部署後端與切換前端。此專案沒有 Flyway／Liquibase；SQL 是沿用現有 `sql/` 的一次性人工 migration。此次只在暫存表驗證 migration，**未修改正式表**。勿在應用啟動或 GET 執行 migration。

GET 使用 read-only、REPEATABLE_READ 交易；只有 SELECT。固定 10 次 SQL：工單 1、機台 2、物料 1、首件 1、權威首件狀態 1、三製程 3、歷史 1。首件按工單整批查，不呼叫 HTTP，不逐機台查詢。重用既有工單索引；首件狀態表有 `(work_order_id, stage, machine_id)` 主鍵。

請求範例：

```http
GET /api/work-orders/123/detail
Accept: application/json
```

成功直接回 DTO（與既有讀取 API 一致，不套 command 的 ApiResponse）。完整實際序列化結果見 [work-order-detail.example.json](work-order-detail.example.json)。該檔由 **真實 Service + MySQL 暫存表整合測試**產生；資料為測試案例，不是正式客戶資料，也不是 API 內的固定假資料。

## 完整契約

完整型別定義在 `../dto/WorkOrderDetailResponse.java`；不暴露 Entity 或原始資料列。所有非日期的文字缺值為 `""`，明確例外：reasonCode、unavailableReason 無問題為 null。所有識別碼、數量、日期缺值為 null；以下相容預設例外另列。

| 路徑 | 型別／來源／語意 |
|---|---|
| order.databaseId | long，work_order.id |
| order.workOrderCode | string，work_order.work_code 的字串，不經舊 Entity 的 int getter |
| order.quantity | decimal，total_quantity，缺值 0 |
| order.deadlineDate | ISO date `yyyy-MM-dd` 或 null，實際資料庫欄位為 DATE |
| order.dueDateDisplay | `yyyy/M/d`，缺值 `—` |
| order.productName | product_name，缺值空白，不使用 model |
| order.productCode / process | model / flow |
| order.details.customerName / specification | client_name / model |
| order.details.materialGrade | material |
| order.details.wireDiameter / wireWeightKg | decimal 或 null，實際 wire_dia / kilogram 都是 FLOAT |
| order.details.wireDrawingFactory / heatNumber / remark | factory / furnaceNo / note |
| order.details.wireLength / surfaceTreatment / inspectionStandard | 空字串，目前無來源 |
| summary | statusCode / statusLabel / statusTone |
| processGroups | 固定順序 head、outsourcing、thread，含 stage、name、rows（可為空陣列） |
| processGroups[].rows[].rowId | `head-{id}` / `outsourcing-{id}` / `thread-{id}` |
| assignmentId / machineId / materialId | 派工或委外主鍵／機台主鍵／物料主鍵；後兩者可 null，委外皆 null |
| name / nameDisplay | 機台名稱（ID）；缺機台用機台（ID），仍保留派工。委外 name 保留空字串、nameDisplay 備援「未填寫公司」 |
| targetQuantity / producedQuantity | decimal 或 null；機台不任意補 0；委外 target 缺值用工單 quantity，current 缺值 0 |
| startedAt / endedAt | ISO offset datetime 或 null |
| rawStatus | production_status / state；委外缺值相容為「有委外」 |
| statusCode / statusLabel / statusTone | Service 已計算，前端直接顯示 |
| material | id、name、specification、barcode、available、unavailableReason；委外 null |
| firstArticle | 下表；委外 null |
| tolerance | 下表 |
| actions | openFirstArticle、openFieldStatus、confirmFirstArticleExemption；委外 `{}` |
| machineChoices | head/thread 陣列，各含 id、name，名稱已含（ID） |
| materialChoices | 與 material 相同型別，全物料來源，見權限缺口 |
| events | id、type、detail、occurredAt、occurredAtDisplay |
| dataIssues | code、rowId（工單層級為 null）、message |
| timeZone | `Asia/Taipei` |

三組內依資料庫 ID 數值升冪，不依名稱、機台 ID 或開工時間。所有 SQL DATETIME 明確解讀為 Asia/Taipei 壁鐘時間，輸出如 `2026-10-22T13:05:00+08:00`。新狀態寫入時間由指定 ZoneId 產生；DATE 不附加時區。日期顯示另設欄位，不把「已完成」或「未提供」塞入時間欄位。若既有 DATETIME 實際儲存 UTC，須先確認資料遷移策略，不能靠伺服器預設時區補猜。

### 狀態

| statusCode | statusLabel | statusTone |
|---|---|---|
| READY | 等待生產 | ready |
| RUNNING | 生產中 | running |
| CLOSED | 正常結單 | closed |
| CLOSED_ABNORMAL | 異常結單 | closed-error |
| PENDING_REVIEW | 異常結單．待主管審核 | error |
| CORRECTION_REQUIRED | 異常退回．待補正 | error |
| CANNOT_CONTINUE | 無法續做 | error |
| OUTSOURCE_COMPLETED | 完成 | waiting |
| OUTSOURCE_NOT_STARTED | 尚未開始 | waiting |
| CUSTOM | 其他原始 production_status | 含異常為 error，否則 waiting |

機台判斷依序為 `order_closed=2 && approved=true`、1、2、3、4，然後 production_status，再開工時間，最後等待生產。「正式生產」轉生產中、「等待材料核對」轉等待生產、「待主管審核」轉完整待審核文字。

summary **只包含 head/thread**：有派工且全結單時任一異常則異常結單、否則正常結單；其他情況任一非空且非等待生產則生產中；其餘等待生產。因此只有委外仍等待生產、部分結單仍生產中、全部待審仍生產中。未使用列表的「未派工」或優先顯示待審核規則。

委外 state「完成」→完成；「開始／生產中」→生產中；其他→尚未開始；rawStatus 保留編輯值。

### 允收範圍

`tolerance` 包含原始 `lowerOffsetQuantity`、`upperOffsetQuantity`（可 null），計算後 `targetOffsetLowerPercent`、`targetOffsetUpperPercent`、`minimumQuantity`、`maximumQuantity`、`acceptableRangeDisplay`、`valid`、`reasonCode`。

offset 是數量差。計算時 null offset 當 0；target=0 時百分比及上下界為 0。正目標時 percent=offset/target×100（BigDecimal，百分比 16 位小數）。範圍直接用等價且不損精度的 target±offset，先 HALF_UP 保留 8 位再 CEILING/FLOOR。範圍顯示例如 `950～1,050`。避免用已四捨五入的百分比再反算數量。

支援數值絕對值及最終整數上下界至 9,007,199,254,740,991；target 非負；正目標時下限 offset 在 0～target，上限非負。target=0 沿用前端百分比皆為 0 的規則。無效值回 valid=false、計算欄位 null、顯示 `—` 並提供 dataIssues，不因單筆壞資料丟棄整張工單。極細小數捨入沿用 8 位規則；FLOAT 原有儲存精度損失不能由 BigDecimal 復原。

機台 target/current 缺值改為明確 null，**不保留舊 runtime 被 null 覆蓋後產生的意外運算**。委外 target 備援工單總量後，以同一有效 target 計算百分比和範圍，修正前端先用 0 計百分比、再用工單量顯示所造成的不一致。

### 首件

firstArticle 欄位：`firstArticleExists`、`firstArticleCompleted`、`requiresFirstArticle`、`firstArticleCompletedAt`、`firstArticleCompletedAtDisplay`、`missingInspectionFields`、`hasFirstArticleClearance`、`recordId`、`exemptionConfirmedAt`、`exemptionReason`、`additionalProductionAt`、`ambiguous`。

以工單資料庫 ID + stage + machine ID 關聯，不加物料／派工篩選。實際首件表沒有此組合唯一約束；多筆時 **不選任何一筆**，exists=true、ambiguous=true、recordId=null、requires=true、completed/clearance=false，附 `FIRST_ARTICLE_AMBIGUOUS`。GET 仍可查看，prepare／完成命令則 409，須先人工合併。這是針對舊 `find()` 任取第一筆的明確修正。

各製程 11 個檢驗欄位逐欄 trim 判斷；0 是有效內容，null、空字串、全空白未填。完成須存在唯一紀錄、requires=false、該製程全部欄位有值；不靠共用 state。已完成但無時間時 completedAt=null、顯示「已完成」，不以顯示文字當放行憑證。

**搓牙欄位確認：**需求已確認正確欄位為 `major_diameter_d2`（D2 斜齒牙外徑），與實際資料庫、DTO、RowMapper、前端及本次實作一致。搓牙完成判斷使用既有 11 個必填欄位，missingInspectionFields 回傳實際資料庫欄名；此項不再列為資料缺口。

### 權威狀態與遷移

新表 `work_order_first_article_state` 主鍵為工單＋製程＋機台，符合既有首件關聯粒度。多筆相同機台派工會共享這個粒度的資格，未默默改為逐物料／派工。

| 情境 | 策略 |
|---|---|
| 舊首件紀錄 | migration 建 requires=true；本機重跑旗標不可還原，因此不從舊 state 或檢驗值直接授權。需完成一次檢驗（可沿用已填值）以建立後端資格 |
| 無首件、無狀態的新組合 | requires=false，completed=false、clearance=false。只允許符合等待生產條件時詢問免首檢，絕不自動確認 |
| 未遷移但有首件 | GET 保守 requires=true，附 FIRST_ARTICLE_STATE_UNMIGRATED |
| prepare 首次建立 | requires=true，清除歷史放行來源，讀取不建立 |
| 完成該製程 | 檢查 11 欄後 requires=false、記 completed_at；不影響另一製程 |
| 重跑 | requires=true，清除 completed/exemption/additional，保留檢驗原值供查看 |
| 確認免首檢 | 保存時間與明確原因，GET 不會像舊 runtimeFromRow 一樣清除 |
| 追加生產 | 保存時間、提高目標、已結單派工轉待續產；保留 requires=true 的優先權 |

clearance = !requires && (有效完成時間 || 免首檢時間 || 追加生產時間)。不存在／重複首件不被當成完成。前端不應再回傳 requiresFirstArticle 或本機 clearance 當權威資料。

## 操作與命令

每個 action 包含 `visible, enabled, label, reasonCode, reasonMessage, confirmationRequired, confirmationMessage, targetType, workOrderCode, workOrderDatabaseId, stage, assignmentId, machineId, firstArticleId`。導覽操作一律保留已結單可查看行為。openFieldStatus 的 enabled **不是可開工授權**。requires=true 時仍可導覽，正式生產寫入會阻擋未放行。

targetType FIRST_ARTICLE：先呼叫 prepare 取得 recordId，再導向首件頁。FIELD_STATUS：直接導覽現場頁；若 confirmationRequired，先顯示後端文案、取得原因並呼叫 exemption，成功再導覽。FIRST_ARTICLE_EXEMPTION：免首檢寫入。前端只解讀描述與識別碼，不重算條件。

新命令共用前綴：

`POST /api/work-orders/{workOrderCode}/assignments/{stage}/{assignmentId}/first-article/{action}`

| action | body | 目的／重新驗證 |
|---|---|---|
| prepare | 無 | 工單、派工、製程歸屬；無紀錄時建立、已有唯一紀錄則回原 ID；重複 409 |
| rerun | `{"reason":"換模重檢"}` | 必須有原因，鎖定目前派工後要求重跑並撤銷放行 |
| exemption | `{"reason":"其他製程"}` | 原因僅允許其他製程／委外／盤元異常補領；重新檢查等待生產、未放行、requires=false |
| additional-production | `{"reason":"追加訂單","targetQuantity":1200}` | 僅完成正常／異常結單，目標大於原目標及 current、允收有效；保留重跑要求 |

成功 200：

```json
{"success":true,"code":200,"message":"OK","data":{"firstArticleId":51,"firstArticle":{"firstArticleExists":true,"firstArticleCompleted":false,"requiresFirstArticle":true,"firstArticleCompletedAt":null,"firstArticleCompletedAtDisplay":"","missingInspectionFields":["heading_length","head_dia","head_angle_s1","head_angle_s2","punch_type_no","punch_width","punch_gauge_depth","head_thickness_h1","head_thickness_h2","enlarged_shank_dia","enlarged_length"],"hasFirstArticleClearance":false,"recordId":51,"exemptionConfirmedAt":null,"exemptionReason":"","additionalProductionAt":null,"ambiguous":false}}}
```

既有命令保留路徑／request／response：

- `POST /api/firstTest/addFirstTest`：沿用工單 FOR UPDATE 鎖與查找／新增流程，同交易同步首件權威狀態。
- `PATCH /api/firstTest/updateFirstTest/{id}`：同交易鎖工單、寫檢驗、驗證該製程並同步權威狀態。state=true 且缺欄位回 400，整筆回滾。state=false 視為重跑意圖，清除該製程放行。
- 共用 head/thread 紀錄若只傳 state 而無法識別製程，回 409，要求傳既有 head_machineId 或 thread_machineId；這是避免跨製程互相影響的必要相容性限制。單製程舊 `{state:false}` 仍支援。
- `/api/workOrderHeadingMachine/{id}/production/start`、threading 同路徑：重新鎖定、驗證首件放行後才開工。
- 上述 `production/status` 設為生產中時，也重新檢查放行，不能透過續做繞過重跑。
- 其他結單、審核、物料流程保留；此次未重寫生產系統。既有物料核對程序仍需執行，導覽或首件免檢不代表已核對物料。

新命令與舊首件新增／更新均使用工單 FOR UPDATE 序列化，讀取鎖在先、寫入交易內重驗；準備首件不會因同時點擊而重複建立。外部直接寫 SQL 不在此保護之內，既有重複紀錄需先清理；此次不任意加入會破壞舊資料的唯一索引。

完整可執行 HTTP 範例見 [work-order-detail.http](work-order-detail.http)。

## 作業紀錄與物料

歷史順序維持實際舊 HistoryLogDao 的 `time DESC, id DESC`，同時間用 ID 穩定排序、MySQL NULL 時間排最後。JSON 物件按 machineId→assignedBy→outsourcePosition→outsourceCode→materialId→targetQuantity→outsourceQuantity→允收範圍→currentQuantity→quantityChangeReason→reason→remark 組字，用「；」與句點。title/結果全文移除「主管」。一般文字、無效 JSON、陣列、未知物件保留原內容；數值無法解析也保留整段原文。歷史允收仍直接 target±offset，可含小數，**不套表格 ceil/floor**。

全物料及工單物料其實同出 material_history_form；material_id 為主鍵，全表已包含工單子集，因此一次查詢可滿足原本「全表優先、工單次之」且無歧義。名稱缺漏「物料 ID」、規格 remark 備援工單 model、條碼 material_code。失聯物料 available=false 並保留 ID。選項含現有所有機台與物料，無另一次 options API。

**權限缺口：**專案沒有 Spring Security／authenticated principal、租戶或物料授權表；不能實作可驗證的「依登入者過濾物料」。本次沿用原全物料 API 可見範圍，並固定回 `MATERIAL_AUTHORIZATION_NOT_CONFIGURED` 問題說明（不是固定業務假資料）。部署到需要隔離資料的環境前，必須提供權限模型，且同時套用選項、已綁物料與所有寫入流程。目前不宣稱符合逐使用者 ACL。

**工單編號儲存限制：**唯讀查證實際 work_code 為 INT UNIQUE。API 端保留字串、精確比較，不會讓 `00123` 誤匹配 `123`；既有 INT 已遺失的前導零無法還原。未擅自把整個工單系統的 INT Entity／寫入 API 遷移為 VARCHAR。

## 前端切換清單

已核對 WorkOrderPage.jsx → workOrderApi.getWorkOrder → bridge.loadOrder → manufacturingApi.getOrderContext，以及逐派工 loadFirstArticle。

1. 明細載入改為一次 GET detail；停止明細呼叫全物料、各機台、各派工、委外、歷史及逐機台首件 API。
2. 移除明細使用的 normalizeOrder、assignmentStatus、runtimeFromRow、loadFirstArticle、inspectionValuesFromRecord/isInspectionComplete 與整體狀態推導。檢驗編輯表單可保留輸入提示，但完成驗證以後端為準。
3. 使用 processGroups 原順序與 rows；不再 filter、join、排序機台或物料。選項直接用 machineChoices/materialChoices。
4. 移除明細的 getCombinationRuntime/getWorkOrderRuntime、hasFirstArticleClearance、getTargetOffsetRange、百分比換算與狀態 tone 推導。
5. 移除 localStorage 中影響 requiresFirstArticle、firstArticleCompletedAt、firstArticleExemption、additionalProductionAt 的讀寫；不得將舊本機值覆蓋新回應。
6. 重跑改 rerun；開首件改 prepare；免檢確認後呼叫 exemption；追加生產改 additional-production。完成首件仍使用原 PATCH，傳 state=true 及該製程機台 ID、全部檢驗值。寫入成功後重新 GET detail。
7. 不再於進入頁面清除免檢／追加狀態；不再將首件日期缺漏的「已完成」當時間。
8. 按 action 顯示 label、confirmation、reason 與導覽；對 400/404/409 顯示錯誤並重新載入，不使用舊 enabled 值繞過命令。
9. 作業紀錄直接顯示 events，不再使用 orderEvents／formatWorkOrderEventDetail／formatFieldEventText 二次轉換。

前端檔案此次僅核對，未修改；後端 migration 與上述前端切換需要一起安排，舊前端只有 localStorage 免檢將無法通過新正式生產驗證。

## 測試

測試原始碼在 `../verification/*.java.txt`，避免位於目前可寫的 main/java 工作目錄而被正式編譯器當 production source。`run-detail-tests.ps1` 會產生隔離 Maven POM／測試來源到被 gitignore 的 `.verification/`，編譯全部後端類別，再執行 JUnit。需求 JDK 25、既有 Maven wrapper 與已下載相依套件。

```powershell
$env:JAVA_HOME = '你的 JDK 25 路徑'
./verification/run-detail-tests.ps1
./verification/run-detail-tests.ps1 -Mysql -DatabaseProperties '你的 application.properties 完整路徑'
```

MySQL 測試讀取設定，不輸出認證資料；複製正式表的 DDL 成連線內 TEMPORARY TABLE、移除外鍵（避免關聯正式業務表）、套用暫存版 migration；斷線即消失。測試不修改正式表、不執行正式 migration。併發保護由共享工單資料列鎖實作；此暫存表測試驗證冪等、交易回滾與鎖定 SQL，但不聲稱已完成跨連線壓力測試。

涵蓋狀態優先序、明細／列表差異、三製程、失聯資料、首件 0/空白/null、重跑、跨工單隔離、跨裝置持久化、共享首件製程隔離、允收邊界、歷史轉換、純 GET、固定查詢數、過期操作重驗與真實 MySQL 欄位映射。結果見 `../verification/RESULTS.md`。
