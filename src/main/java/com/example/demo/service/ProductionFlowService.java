package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.dto.ProductionFlowRequest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
public class ProductionFlowService {
    private final NamedParameterJdbcTemplate jdbc;
    @Autowired
    private WorkOrderFirstArticleStateService firstArticleState;
    @Autowired
    public ProductionFlowService(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    public enum Process {
        HEADING("work_order_heading_machine", "head"),
        THREADING("work_order_threading_machine", "thread");
        final String table, prefix;
        Process(String table, String prefix) { this.table = table; this.prefix = prefix; }
    }

    // 開始生產使用 /production/start；其他狀態不修改 started_at。
    private static final Set<String> STATUSES = Set.of("生產中", "換料", "換針", "目標產量設定", "目標產量修改",
            "等待", "待料", "待單", "待沖模", "待維修", "待調車", "無法續做", "模具/牙板異常", "盤元異常換料續做");

    @Transactional
    public void startProduction(Process process, int id) {
        require(id > 0, "id must be positive");
        firstArticleState.lockAssignmentOrder(process.prefix, id);
        var rows = jdbc.queryForList("SELECT * FROM mydb." + process.table
                + " WHERE id=:id FOR UPDATE", Map.of("id", id));
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "派工不存在");
        var row = rows.getFirst();
        int closed = row.get("order_closed") == null ? 0 : ((Number) row.get("order_closed")).intValue();
        conflict(closed == 0, "已結單或審核中的派工不可開始生產");
        firstArticleState.validateProductionClearance(process.prefix, row);
        jdbc.update("UPDATE mydb." + process.table
                        + " SET production_status=:status, started_at=:now WHERE id=:id",
                Map.of("status", "生產中", "id", id,
                        "now", java.time.LocalDateTime.now(WorkOrderDetailService.ZONE)));
    }

    @Transactional
    public void execute(Process process, int id, String action, ProductionFlowRequest r) {
        if ("start".equals(action)) {
            startProduction(process, id);
            return;
        }
        require(id > 0, "id must be positive");
        firstArticleState.lockAssignmentOrder(process.prefix, id);
        var rows = jdbc.queryForList("SELECT * FROM mydb." + process.table + " WHERE id=:id FOR UPDATE", Map.of("id", id));
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "派工不存在");
        var row = rows.getFirst();
        int closed = row.get("order_closed") == null ? 0 : ((Number) row.get("order_closed")).intValue();
        Map<String, Object> fields = new LinkedHashMap<>();
        switch (action) {
            case "status" -> {
                if ("生產中".equals(r.productionStatus())) firstArticleState.validateProductionClearance(process.prefix, row);
                require(!"開始生產".equals(r.productionStatus()), "開始生產請使用 /production/start API");
                conflict(closed == 0 && row.get("started_at") != null, "僅生產中的派工可變更狀態");
                require(r.productionStatus() != null && STATUSES.contains(r.productionStatus()), "不支援的 productionStatus");
                boolean targetChange = "目標產量設定".equals(r.productionStatus())
                        || "目標產量修改".equals(r.productionStatus());
                if (!"生產中".equals(r.productionStatus()) && !targetChange) {
                    require(text(r.reason()), "狀態變更需填原因");
                }
                fields.put("production_status", r.productionStatus());
                fields.put("reason", r.reason());
                if (targetChange) {
                    nonnegative(r.targetQuantity(), "targetQuantity");
                    String changeReason = text(r.quantityChangeReason()) ? r.quantityChangeReason() : r.reason();
                    require(text(changeReason), "目標產量設定需填 quantityChangeReason");
                    // 使用交易鎖定後的資料，避免前端傳入過期的修改前產量。
                    fields.put("previous_quantity", row.get("target_quantity"));
                    fields.put("target_quantity", r.targetQuantity());
                    if (r.targetUpOffsetQuantity() != null) {
                        nonnegative(r.targetUpOffsetQuantity(), "targetUpOffsetQuantity");
                        fields.put("target_up_offset_quantity", r.targetUpOffsetQuantity());
                    }
                    if (r.targetDownOffsetQuantity() != null) {
                        nonnegative(r.targetDownOffsetQuantity(), "targetDownOffsetQuantity");
                        fields.put("target_down_offset_quantity", r.targetDownOffsetQuantity());
                    }
                    fields.put("quantity_change_reason", changeReason);
                    fields.put("reason", changeReason);
                } else require(r.targetQuantity() == null && r.targetUpOffsetQuantity() == null
                                && r.targetDownOffsetQuantity() == null && r.quantityChangeReason() == null,
                        "目標產量相關欄位僅用於目標產量設定或修改");
            }
            case "close" -> {
                require(r.orderClosed() != null && (r.orderClosed() == 1 || r.orderClosed() == 2), "結單請傳 1 正常或 2 異常");
                // 已結單由審核人員改判時，沿用原結單資料，不要求重新填寫物料與時間。
                if ((closed == 1 || closed == 2) && text(r.reviewer())) {
                    conflict(row.get("started_at") != null, "尚未開工的派工不可再次結單");
                    String opinion = text(r.opinion()) ? r.opinion() : r.remark();
                    require(r.approved() != null && text(opinion), "審核人員再次結單需填 approved 與 opinion");
                    if (r.endAt() != null) {
                        Object startedAt = row.get("started_at");
                        Date startedDate = startedAt instanceof java.time.LocalDateTime local
                                ? java.sql.Timestamp.valueOf(local) : (Date) startedAt;
                        require(!r.endAt().before(startedDate), "endAt 不可早於開始時間");
                        fields.put("end_at", r.endAt());
                    }
                    if (r.materialLoss() != null) {
                        nonnegative(r.materialLoss(), "materialLoss");
                        fields.put("material_loss", r.materialLoss());
                    }
                    if (r.remainingMaterial() != null) {
                        nonnegative(r.remainingMaterial(), "remainingMaterial");
                        fields.put("remaining_material", r.remainingMaterial());
                    }
                    if (r.reason() != null) fields.put("reason", r.reason());
                    if (r.remark() != null) fields.put("remark", r.remark());
                    fields.put("order_closed", r.orderClosed());
                    fields.put("approved", r.approved());
                    fields.put("reviewer", r.reviewer());
                    fields.put("opinion", opinion);
                    if (r.employeeCode() != null) {
                        require(text(r.employeeCode()), "employee_code 不可為空白");
                        fields.put("employee_code", r.employeeCode());
                    }
                    break;
                }
                Object previousApproval = row.get("approved");
                boolean rejected = Boolean.FALSE.equals(previousApproval)
                        || (previousApproval instanceof Number number && number.intValue() == 0);
                conflict(row.get("started_at") != null
                        && (closed == 0 || closed == 1 || (closed == 2 && rejected)),
                        "僅已開工且未結單、正常結單或異常結單審核退回的派工可提交結單");
                require(r.endAt() != null, "endAt 必填");
                Object startedAt = row.get("started_at");
                // MySQL DATETIME is returned as LocalDateTime by getObject/queryForList.
                Date startedDate = startedAt instanceof java.time.LocalDateTime local
                        ? java.sql.Timestamp.valueOf(local) : (Date) startedAt;
                require(!r.endAt().before(startedDate), "endAt 不可早於開始時間");
                nonnegative(r.materialLoss(), "materialLoss");
                nonnegative(r.remainingMaterial(), "remainingMaterial");
                if (r.orderClosed() == 2) require(text(r.reason()) && text(r.remark()), "異常結單需填 reason 與 remark");
                fields.put("order_closed", r.orderClosed());
                // 正常結單直接完成；異常結單才進入待審核。
                fields.put("approved", r.orderClosed() == 1 ? Boolean.TRUE : null);
                fields.put("reviewer", null);
                fields.put("opinion", null);
                fields.put("end_at", r.endAt());
                if (r.employeeCode() != null) {
                    require(text(r.employeeCode()), "employee_code 不可為空白");
                    fields.put("employee_code", r.employeeCode());
                }
                fields.put("material_loss", r.materialLoss());
                fields.put("remaining_material", r.remainingMaterial());
                fields.put("reason", r.reason());
                fields.put("remark", r.remark());
            }
            case "material" -> {
                nonnegative(r.materialLoss(), "materialLoss");
                nonnegative(r.remainingMaterial(), "remainingMaterial");
                fields.put("material_loss", r.materialLoss());
                fields.put("remaining_material", r.remainingMaterial());
            }
            case "review" -> {
                conflict((closed == 1 || closed == 2) && row.get("approved") == null,
                        "僅已結單且待審的派工可進行審核");
                String opinion = text(r.opinion()) ? r.opinion() : r.remark();
                require(r.approved() != null && text(r.reviewer()) && text(opinion),
                        "approved、reviewer 與 opinion 必填（相容舊版 remark）");
                require(r.orderClosed() == null || r.orderClosed() == 1 || r.orderClosed() == 2,
                        "審核後結單類型僅允許 1 正常或 2 異常");
                fields.put("order_closed", r.orderClosed() == null ? closed : r.orderClosed());
                fields.put("approved", r.approved());
                fields.put("reviewer", r.reviewer());
                fields.put("opinion", opinion);
                // 結單原因及備註保留，審核意見獨立儲存。
            }
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND, "未知流程動作");
        }
        String assignments = fields.keySet().stream().map(k -> k + "=:" + k).collect(java.util.stream.Collectors.joining(","));
        Map<String, Object> params = new HashMap<>(fields);
        params.put("id", id);
        jdbc.update("UPDATE mydb." + process.table + " SET " + assignments + " WHERE id=:id", params);
        String log = process + " assignment=" + id + " workOrderId=" + row.get("work_order_code")
                + " machineId=" + row.get("machineId") + " materialId=" + row.get("material_id")
                + " memberId=" + row.get("member_id") + " previousOrderClosed=" + closed
                + " previousApproved=" + row.get("approved")
                + " previousReviewer=" + row.get("reviewer") + " previousOpinion=" + row.get("opinion")
                + " changes=" + fields
                + " reviewer=" + r.reviewer() + " remark=" + r.remark();
        jdbc.update("INSERT INTO mydb.history_log (title, `time`, work_order_code, log) VALUES (:title,CURRENT_TIMESTAMP,:work,:log)",
                Map.of("title", "生產流程－" + action, "work", row.get("work_order_code"),
                        "log", log));
        String historyTable = process == Process.HEADING
                ? "head_machine_history_log" : "thread_machine_history_log";
        // Store the effective production status/reason, including an explicitly cleared reason.
        Map<String, Object> machineHistory = new HashMap<>();
        machineHistory.put("title", fields.containsKey("production_status")
                ? fields.get("production_status") : row.get("production_status"));
        machineHistory.put("log", fields.containsKey("reason") ? fields.get("reason") : row.get("reason"));
        machineHistory.put("machine", row.get("machineId"));
        jdbc.update("INSERT INTO mydb." + historyTable + " (title, `time`, machine_id, log)"
                            + " VALUES (:title,CURRENT_TIMESTAMP,:machine,:log)",
                    machineHistory);
    }

    private static boolean text(String value) { return value != null && !value.isBlank(); }
    private static void nonnegative(Double value, String name) {
        require(value != null && Double.isFinite(value) && value >= 0, name + " 必須為非負數");
    }
    private static void require(boolean ok, String message) {
        if (!ok) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
    private static void conflict(boolean ok, String message) {
        if (!ok) throw new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}
