package com.example.demo.dao;

import com.example.demo.rowMapper.WorkOrderRowMapper;
import com.example.demo.model.WorkOrder;
import com.example.demo.dto.WorkOrderRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class WorkOrderDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public int deleteWorkOrder(int id) {
        String sql = "DELETE FROM mydb.work_order WHERE id = :id";
        return namedParameterJdbcTemplate.update(sql, Map.of("id", id));
    }

    public int addWorkOrder(WorkOrderRequest request) {
        String sql = """
                INSERT INTO mydb.work_order
                    (work_code, deadline_date, start_at, end_at, model, material, product_name,
                     wire_dia, total_quantity, update_quantity, reason, kilogram, factory,
                     furnaceNo, flow, note, head_weight, tooth_weight,
                     approved_by, reviewed_by, inspected_by, prepared_by, client_name, assigned_by)
                VALUES
                    (:workOrder, :deadlineDate, :startAt, :endAt, :model, :material, :productName,
                     :wireDia, :totalQuantity, :updateQuantity, :reason, :kilogram, :factory,
                     :furaceNo, :flow, :note, :headWeight, :toothWeight,
                     :approvedBy, :reviewedBy, :inspectedBy, :preparedBy, :clientName, :assignedBy)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("workOrder", request.workOrder())
                .addValue("deadlineDate", request.deadlineDate(), java.sql.Types.DATE)
                .addValue("startAt", request.startAt(), java.sql.Types.TIMESTAMP)
                .addValue("endAt", request.endAt(), java.sql.Types.TIMESTAMP)
                .addValue("model", request.model())
                .addValue("material", request.material())
                .addValue("productName", request.productName())
                .addValue("wireDia", request.wireDia())
                .addValue("totalQuantity", request.totalQuantity())
                .addValue("updateQuantity", request.updateQuantity())
                .addValue("reason", request.reason())
                .addValue("kilogram", request.kilogram())
                .addValue("factory", request.factory())
                .addValue("furaceNo", request.furaceNo())
                .addValue("flow", request.flow())
                .addValue("note", request.note())
                .addValue("headWeight", request.headWeight())
                .addValue("toothWeight", request.toothWeight())
                .addValue("approvedBy", request.approvedBy())
                .addValue("reviewedBy", request.reviewedBy())
                .addValue("inspectedBy", request.inspectedBy())
                .addValue("preparedBy", request.preparedBy())
                .addValue("clientName", request.clientName())
                .addValue("assignedBy", request.assignedBy());
        return namedParameterJdbcTemplate.update(sql, params);
    }

    public int updateWorkOrder(int id, Map<String, Object> fields) {
        String assignments = fields.keySet().stream()
                .map(column -> column + " = :" + column)
                .collect(java.util.stream.Collectors.joining(", "));
        Map<String, Object> params = new HashMap<>(fields);
        params.put("id", id);
        return namedParameterJdbcTemplate.update(
                "UPDATE mydb.work_order SET " + assignments + " WHERE id = :id", params);
    }

    public boolean existsById(int id) {
        Integer count = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM mydb.work_order WHERE id = :id",
                Map.of("id", id), Integer.class);
        return count != null && count > 0;
    }

    public List<WorkOrder> getWorkOrders() {
        String sql = "SELECT t.* FROM mydb.work_order t";
        return namedParameterJdbcTemplate.query(sql, new WorkOrderRowMapper());
    }

    public Optional<WorkOrder> getWorkOrder(int id) {
        String sql = "SELECT t.* FROM mydb.work_order t WHERE t.id = :id";
        List<WorkOrder> workOrders = namedParameterJdbcTemplate.query(
                sql, Map.of("id", id), new WorkOrderRowMapper());
        return workOrders.stream().findFirst();
    }

    public Optional<WorkOrder> getWorkOrderByCode(int workOrderCode) {
        String sql = "SELECT t.* FROM mydb.work_order t WHERE t.work_code = :workOrderCode";
        List<WorkOrder> workOrders = namedParameterJdbcTemplate.query(
                sql, Map.of("workOrderCode", workOrderCode), new WorkOrderRowMapper());
        return workOrders.stream().findFirst();
    }
}
