package com.example.demo.rowMapper;

import com.example.demo.model.MaterialHistoryForm;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MaterialHistoryFormRowMapper implements RowMapper<MaterialHistoryForm> {
    @Override
    public MaterialHistoryForm mapRow(ResultSet rs, int rowNum) throws SQLException {
        MaterialHistoryForm form = new MaterialHistoryForm();
        form.setMaterialId(rs.getInt("material_id"));
        form.setWorkOrderCode(rs.getInt("work_oder_code"));
        form.setMaterialCode(rs.getString("material_code"));
        form.setMaterialName(rs.getString("material_name"));
        form.setUnit(rs.getString("unit"));
        form.setCreateAt(rs.getObject("created_at", java.time.LocalDateTime.class));
        form.setUpdateAt(rs.getObject("updated_at", java.time.LocalDateTime.class));
        form.setQuantity(rs.getFloat("quantity"));
        form.setMaterialLoss(rs.getFloat("material_loss"));
        form.setRemainingMaterial(rs.getFloat("remaining_material"));
        form.setRemark(rs.getString("remark"));
        form.setRecordBy(rs.getString("record_by"));
        return form;
    }
}
