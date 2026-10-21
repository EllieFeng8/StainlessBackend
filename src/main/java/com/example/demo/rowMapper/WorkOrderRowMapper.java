package com.example.demo.rowMapper;

import com.example.demo.model.WorkOrder;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WorkOrderRowMapper implements RowMapper<WorkOrder> {

    @Override
    public WorkOrder mapRow(ResultSet rs, int rowNum) throws SQLException {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setId(rs.getInt("id"));
        workOrder.setWorkOrder(rs.getInt("work_code"));
        workOrder.setDeadlineDate(rs.getTimestamp("deadline_date"));
        workOrder.setStartAt(rs.getTimestamp("start_at"));
        workOrder.setEndAt(rs.getTimestamp("end_at"));
        workOrder.setModel(rs.getString("model"));
        workOrder.setMaterial(rs.getString("material"));
        workOrder.setProductName(rs.getString("product_name"));
        workOrder.setWireDia(rs.getString("wire_dia"));
        workOrder.setTotalQuantity(rs.getFloat("total_quantity"));
        workOrder.setUpdateQuantity(rs.getFloat("update_quantity"));
        workOrder.setReason(rs.getString("reason"));
        workOrder.setKilogram(rs.getFloat("kilogram"));
        workOrder.setFactory(rs.getString("factory"));
        workOrder.setFuraceNo(rs.getString("furnaceNo"));
        workOrder.setFlow(rs.getString("flow"));
        workOrder.setNote(rs.getString("note"));
        workOrder.setHeadWeight(rs.getFloat("head_weight"));
        workOrder.setToothWeight(rs.getFloat("tooth_weight"));
        workOrder.setApprovedBy(rs.getString("approved_by"));
        workOrder.setReviewedBy(rs.getString("reviewed_by"));
        workOrder.setInspectedBy(rs.getString("inspected_by"));
        workOrder.setPreparedBy(rs.getString("prepared_by"));
        workOrder.setClientName(rs.getString("client_name"));
        workOrder.setAssignedBy(rs.getString("assigned_by"));
        return workOrder;
    }
}
