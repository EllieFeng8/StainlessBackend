# 人機 HTTP 通訊 Service

`HmiCommunicationService` 專責向人機（HMI）或人機閘道發送 HTTP 請求；工單、產量儲存與生產流程仍由原有 Service 負責。

## 設定

在 application.properties 或部署環境設定實際人機端網址：

```properties
hmi.target-quantity-url=http://192.168.1.100:8080/api/target-quantity
hmi.connect-timeout-ms=3000
hmi.read-timeout-ms=5000
```

網址為示例，請換成實際設備或閘道端點。未設定網址不影響啟動，呼叫時回報 503；非法網址或非正數逾時設定會在啟動時失敗。

## 呼叫方式

由需要通知人機的業務 Service 透過建構子注入 `HmiCommunicationService`，呼叫：

```java
hmiCommunicationService.setTargetQuantity(
        HmiCommunicationService.MachineType.HEADING,
        "H-001",
        new BigDecimal("10000"));
```

`HEADING` 表示打頭機，`THREADING` 表示搓牙機。傳入機台代碼，不是派工 ID。目標產量必須非 null 且大於等於零；保留現有系統可使用小數的語意。

## 人機端 API 約定（待設備端確認）

目前採用 POST 至設定網址，Content-Type 為 application/json：

```json
{
  "machineType": "HEADING",
  "machineCode": "H-001",
  "targetQuantity": 10000
}
```

人機端需要接收此請求，依機台類型與代碼更新目標產量，並將值繫結至顯示畫面。若設備採用不同路徑、方法、認證或欄位格式，需依設備規格調整本 Service。

所有 2xx 回覆代表 HTTP 請求被接受，不代表畫面更新已確認；目前不解析回應 body。其他狀態（包含重新導向）、連線失敗與逾時回報 502。逾時可能發生在設備已收到請求之後，因此不自動重送。

目前只新增通訊 Service，未自動掛入 `ProductionFlowService`、新增前端 API 或連線實際設備。確認設備協定後再整合業務流程；資料庫交易與 HTTP 無法一起回滾，若需可靠同步，應在資料儲存時持久化待送命令並處理確認與重送，避免把資料已儲存當成設備已更新。
