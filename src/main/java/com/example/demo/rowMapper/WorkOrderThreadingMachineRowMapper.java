package com.example.demo.rowMapper;

import com.example.demo.model.WorkOrderThreadingMachine;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WorkOrderThreadingMachineRowMapper implements RowMapper<WorkOrderThreadingMachine> {
    @Override
    public WorkOrderThreadingMachine mapRow(ResultSet rs, int rowNum) throws SQLException {
        WorkOrderThreadingMachine item = new WorkOrderThreadingMachine();
        item.setId(rs.getInt("id"));
        item.setWorkOrderCode(rs.getInt("work_order_code"));
        item.setMachineId(rs.getInt("machineId"));
        item.setMaterialId(rs.getInt("material_id"));
        item.setMemberId(rs.getObject("member_id", Integer.class));
        item.setUpdateMachine(rs.getString("update_machine"));
        item.setChangeReason(rs.getString("change_reason"));
        item.setTargetQuantity(rs.getFloat("target_quantity"));
        item.setTargetUpOffsetQuantity(rs.getFloat("target_up_offset_quantity"));
        item.setTargetDownOffsetQuantity(rs.getFloat("target_down_offset_quantity"));
        item.setCurrentQuantity(rs.getFloat("current_quantity"));
        item.setPreviousQuantity(rs.getFloat("previous_quantity"));
        item.setUpdateQuantity(rs.getFloat("update_quantity"));
        item.setQuantityChangeReason(rs.getString("quantity_change_reason"));
        item.setStartedAt(rs.getTimestamp("started_at"));
        item.setEndAt(rs.getTimestamp("end_at"));
        item.setProductionStatus(rs.getString("production_status"));
        item.setReason(rs.getString("reason"));
        item.setOrderClosed(rs.getInt("order_closed"));
        item.setRemark(rs.getString("remark"));
        item.setMaterialLoss(rs.getFloat("material_loss"));
        item.setRemainingMaterial(rs.getFloat("remaining_material"));
        item.setOee(rs.getFloat("oee"));
        item.setEmployeeCode(rs.getString("employee_code"));
        item.setReviewer(rs.getString("reviewer"));
        boolean approved = rs.getBoolean("approved");
        item.setApproved(rs.wasNull() ? null : approved);
        item.setOpinion(rs.getString("opinion"));
        return item;
    }
}
