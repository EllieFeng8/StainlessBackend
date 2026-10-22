package com.example.demo.dao;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.rowMapper.WorkOrderHeadingMachineRowMapper;
import com.example.demo.dto.WorkOrderHeadingMachineRequest;
import com.example.demo.model.WorkOrderHeadingMachine;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class WorkOrderHeadingMachineDao {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public WorkOrderHeadingMachineDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int addWorkOrderHeadingMachine(WorkOrderHeadingMachineRequest request) {
        String sql = """
                INSERT INTO mydb.work_order_heading_machine
                    (work_order_code, machineId, material_id, member_id, update_machine, change_reason, target_quantity, target_up_offset_quantity, target_down_offset_quantity, current_quantity, previous_quantity, update_quantity, quantity_change_reason, started_at, end_at, production_status, reason, order_closed, remark, material_loss, remaining_material, oee, employee_code, reviewer, approved, opinion)
                VALUES
                    (:workOrderCode, :machineId, :materialId, :memberId, :updateMachine, :changeReason, :targetQuantity, :targetUpOffsetQuantity, :targetDownOffsetQuantity, :currentQuantity, :previousQuantity, :updateQuantity, :quantityChangeReason, :startedAt, :endAt, :productionStatus, :reason, :orderClosed, :remark, :materialLoss, :remainingMaterial, :oee, :employeeCode, :reviewer, :approved, :opinion)
                """;
        return jdbcTemplate.update(sql, parameters(request));
    }

    // Only these fixed column names may appear in a partial update.
    private static final Map<String, String> PATCH_COLUMNS = Map.ofEntries(
            Map.entry("workOrderCode", "work_order_code"),
            Map.entry("machineId", "machineId"),
            Map.entry("materialId", "material_id"),
            Map.entry("memberId", "member_id"),
            Map.entry("updateMachine", "update_machine"),
            Map.entry("changeReason", "change_reason"),
            Map.entry("targetQuantity", "target_quantity"),
            Map.entry("targetUpOffsetQuantity", "target_up_offset_quantity"),
            Map.entry("targetDownOffsetQuantity", "target_down_offset_quantity"),
            Map.entry("currentQuantity", "current_quantity"),
            Map.entry("previousQuantity", "previous_quantity"),
            Map.entry("updateQuantity", "update_quantity"),
            Map.entry("quantityChangeReason", "quantity_change_reason"),
            Map.entry("startedAt", "started_at"),
            Map.entry("endAt", "end_at"),
            Map.entry("productionStatus", "production_status"),
            Map.entry("reason", "reason"),
            Map.entry("orderClosed", "order_closed"),
            Map.entry("remark", "remark"),
            Map.entry("materialLoss", "material_loss"),
            Map.entry("remainingMaterial", "remaining_material"),
            Map.entry("oee", "oee"),
            Map.entry("employeeCode", "employee_code"),
            Map.entry("reviewer", "reviewer"),
            Map.entry("approved", "approved"),
            Map.entry("opinion", "opinion"));

    public int patchWorkOrderHeadingMachine(int id, WorkOrderHeadingMachineRequest request) {
        MapSqlParameterSource params = parameters(request);
        String assignments = PATCH_COLUMNS.entrySet().stream()
                .filter(entry -> params.getValue(entry.getKey()) != null)
                .map(entry -> entry.getValue() + " = :" + entry.getKey())
                .collect(java.util.stream.Collectors.joining(", "));
        if (assignments.isEmpty()) {
            throw new IllegalArgumentException("At least one non-null field is required");
        }
        return jdbcTemplate.update(
                "UPDATE mydb.work_order_heading_machine SET " + assignments + " WHERE id = :id AND started_at IS NULL AND COALESCE(order_closed,0)=0",
                params.addValue("id", id));
    }

    public boolean existsById(int id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM mydb.work_order_heading_machine WHERE id = :id",
                Map.of("id", id), Integer.class);
        return count != null && count > 0;
    }

    public int deleteWorkOrderHeadingMachine(int id) {
        return jdbcTemplate.update("DELETE FROM mydb.work_order_heading_machine WHERE id = :id AND started_at IS NULL AND COALESCE(order_closed,0)=0",
                Map.of("id", id));
    }

    public List<WorkOrderHeadingMachine> getWorkOrderHeadingMachines() {
        return jdbcTemplate.query("SELECT * FROM mydb.work_order_heading_machine",
                new WorkOrderHeadingMachineRowMapper());
    }

    public Optional<WorkOrderHeadingMachine> getWorkOrderHeadingMachine(int id) {
        return jdbcTemplate.query("SELECT * FROM mydb.work_order_heading_machine WHERE id = :id",
                Map.of("id", id), new WorkOrderHeadingMachineRowMapper()).stream().findFirst();
    }

    public List<WorkOrderHeadingMachine> getWorkOrderHeadingMachines(
            Integer workOrderId, Integer headMachineId, Integer materialId) {
        StringBuilder sql = new StringBuilder("SELECT h.* FROM mydb.work_order_heading_machine h WHERE 1=1");
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (workOrderId != null) {
            sql.append(" AND EXISTS (SELECT 1 FROM mydb.work_order w"
                    + " WHERE w.id = :workOrderId AND w.id = h.work_order_code)");
            params.addValue("workOrderId", workOrderId);
        }
        if (headMachineId != null) {
            sql.append(" AND h.machineId = :headMachineId");
            params.addValue("headMachineId", headMachineId);
        }
        if (materialId != null) {
            sql.append(" AND h.material_id = :materialId");
            params.addValue("materialId", materialId);
        }
        return jdbcTemplate.query(sql.toString(), params, new WorkOrderHeadingMachineRowMapper());
    }

    private MapSqlParameterSource parameters(WorkOrderHeadingMachineRequest request) {
        return new MapSqlParameterSource()
                .addValue("workOrderCode", request.workOrderCode())
                .addValue("machineId", request.machineId())
                .addValue("materialId", request.materialId())
                .addValue("memberId", request.memberId())
                .addValue("updateMachine", request.updateMachine())
                .addValue("changeReason", request.changeReason())
                .addValue("targetQuantity", request.targetQuantity())
                .addValue("targetUpOffsetQuantity", request.targetUpOffsetQuantity())
                .addValue("targetDownOffsetQuantity", request.targetDownOffsetQuantity())
                .addValue("currentQuantity", request.currentQuantity())
                .addValue("previousQuantity", request.previousQuantity())
                .addValue("updateQuantity", request.updateQuantity())
                .addValue("quantityChangeReason", request.quantityChangeReason())
                .addValue("startedAt", request.startedAt(), java.sql.Types.TIMESTAMP)
                .addValue("endAt", request.endAt(), java.sql.Types.TIMESTAMP)
                .addValue("productionStatus", request.productionStatus())
                .addValue("reason", request.reason())
                .addValue("orderClosed", request.orderClosed())
                .addValue("remark", request.remark())
                .addValue("materialLoss", request.materialLoss())
                .addValue("remainingMaterial", request.remainingMaterial())
                .addValue("oee", request.oee())
                .addValue("employeeCode", request.employeeCode())
                .addValue("reviewer", request.reviewer())
                .addValue("approved", request.approved(), java.sql.Types.BOOLEAN)
                .addValue("opinion", request.opinion());
    }
}
