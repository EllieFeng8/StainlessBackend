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
        conflict(row.get("started_at") == null, "派工已開工，恢復生產請使用狀態更新 API");
        firstArticleState.recordProductionWithoutClearance(process.prefix, row);
        jdbc.update("UPDATE mydb." + process.table
                        + " SET production_status=:status, started_at=:now WHERE id=:id",
                Map.of("status", "生產中", "id", id,
                        "now", java.time.LocalDateTime.now(WorkOrderDetailService.ZONE)));
        addMachineHistory(process, row, "生產中", row.get("reason"), 2);
    }

    @Transactional
    public void execute(Process process, int id, String action, ProductionFlowRequest r) {
        MachineHistoryState.validate(r.state());
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
                if ("生產中".equals(r.productionStatus())) firstArticleState.recordProductionWithoutClearance(process.prefix, row);
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
                    // Review changes retain the original production end, not the review time.
                    fields.put("end_at", closingTime(row));
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
                var endAt = closingTime(row);
                nonnegative(r.materialLoss(), "materialLoss");
                nonnegative(r.remainingMaterial(), "remainingMaterial");
                if (r.orderClosed() == 2) require(text(r.reason()) && text(r.remark()), "異常結單需填 reason 與 remark");
                fields.put("order_closed", r.orderClosed());
                // 正常結單直接完成；異常結單才進入待審核。
                fields.put("approved", r.orderClosed() == 1 ? Boolean.TRUE : null);
                fields.put("reviewer", null);
                fields.put("opinion", null);
                fields.put("end_at", endAt);
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
        if ("close".equals(action)) {
            // Keep the original result on resubmission; later machine events belong to other work.
            fields.put("oee", closed != 0 && row.get("end_at") != null && row.get("oee") != null
                    ? row.get("oee")
                    : calculateOee(process, row, localTime(fields.get("end_at"))));
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
        Object productionStatus = fields.containsKey("production_status")
                ? fields.get("production_status") : row.get("production_status");
        boolean closing = "close".equals(action);
        Object machineLog = closing ? (r.orderClosed() == 1 ? "正常結單" : "異常結單")
                : (fields.containsKey("reason") ? fields.get("reason") : row.get("reason"));
        Integer machineState = closing ? 2 : MachineHistoryState.forProductionStatus(productionStatus, r.state());
        addMachineHistory(process, row, productionStatus, machineLog, machineState);
    }

    private void addMachineHistory(Process process, Map<String, Object> row,
                                   Object status, Object reason, Integer state) {
        String historyTable = process == Process.HEADING
                ? "head_machine_history_log" : "thread_machine_history_log";
        // Store the effective production status/reason, including an explicitly cleared reason.
        Map<String, Object> machineHistory = new HashMap<>();
        machineHistory.put("title", status);
        machineHistory.put("log", reason);
        machineHistory.put("machine", row.get("machineId"));
        machineHistory.put("state", state);
        machineHistory.put("eventTime", java.time.LocalDateTime.now(WorkOrderDetailService.ZONE));
        jdbc.update("INSERT INTO mydb." + historyTable + " (title, `time`, machine_id, log, state)"
                            + " VALUES (:title,:eventTime,:machine,:log,:state)",
                    machineHistory);
    }

    record MachineEvent(java.time.LocalDateTime time, Integer state) {}

    private java.math.BigDecimal calculateOee(Process process, Map<String, Object> row,
                                             java.time.LocalDateTime end) {
        var start = localTime(row.get("started_at"));
        String table = process == Process.HEADING ? "head_machine_history_log" : "thread_machine_history_log";
        // Fetch the last state at/before start plus all changes within [start, end).
        // IDs make equal-time events deterministic; the last event at that time wins.
        var events = jdbc.query("SELECT `time`, state FROM ("
                        + "(SELECT id, `time`, state FROM mydb." + table
                        + " WHERE machine_id=:machine AND `time`<=:start ORDER BY `time` DESC, id DESC LIMIT 1)"
                        + " UNION ALL SELECT id, `time`, state FROM mydb." + table
                        + " WHERE machine_id=:machine AND `time`>:start AND `time`<:end"
                        + ") events ORDER BY `time`, id",
                Map.of("machine", row.get("machineId"), "start", start, "end", end),
                (rs, index) -> new MachineEvent(rs.getObject("time", java.time.LocalDateTime.class),
                        rs.getObject("state", Integer.class)));
        return runningPercentage(start, end, events);
    }

    static java.math.BigDecimal runningPercentage(java.time.LocalDateTime start,
                                                  java.time.LocalDateTime end, List<MachineEvent> events) {
        long total = java.time.Duration.between(start, end).toMillis();
        if (total <= 0) return java.math.BigDecimal.ZERO;
        long running = 0;
        var cursor = start;
        Integer state = null;
        for (var event : events) {
            if (!event.time().isBefore(end)) break;
            var boundary = event.time().isBefore(start) ? start : event.time();
            if (Integer.valueOf(2).equals(state)) {
                running += java.time.Duration.between(cursor, boundary).toMillis();
            }
            cursor = boundary;
            state = event.state();
        }
        if (Integer.valueOf(2).equals(state)) running += java.time.Duration.between(cursor, end).toMillis();
        return java.math.BigDecimal.valueOf(running).multiply(java.math.BigDecimal.valueOf(100))
                .divide(java.math.BigDecimal.valueOf(total), 2, java.math.RoundingMode.HALF_UP);
    }

    private static java.time.LocalDateTime closingTime(Map<String, Object> row) {
        // Preserve the first close time on resubmission or review reclassification.
        Object previousEnd = row.get("end_at");
        int closed = row.get("order_closed") instanceof Number n ? n.intValue() : 0;
        var end = closed != 0 && previousEnd != null ? localTime(previousEnd)
                : java.time.LocalDateTime.now(WorkOrderDetailService.ZONE);
        require(!end.isBefore(localTime(row.get("started_at"))), "結束時間不可早於開始時間");
        return end;
    }

    private static java.time.LocalDateTime localTime(Object value) {
        if (value instanceof java.time.LocalDateTime local) return local;
        if (value instanceof java.sql.Timestamp timestamp) return timestamp.toLocalDateTime();
        return ((Date) value).toInstant().atZone(WorkOrderDetailService.ZONE).toLocalDateTime();
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
