package com.example.demo.rowMapper;

import com.example.demo.model.OutsourceForm;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OutsourceFormRowMapper implements RowMapper<OutsourceForm> {
    @Override
    public OutsourceForm mapRow(ResultSet rs, int rowNum) throws SQLException {
        OutsourceForm form = new OutsourceForm();
        form.setId(rs.getInt("id"));
        form.setOutsourceCode(rs.getString("outsource_code"));
        form.setWorkOrderId(rs.getInt("work_order_id"));
        form.setOutsourceQuantity(rs.getFloat("outsource_quantity"));
        form.setCurrentQuantity(rs.getObject("current_quantity", Float.class));
        form.setChangeReason(rs.getString("change_reason"));
        form.setOutsourceUpOffsetQuantity(rs.getObject("outsource_up_offset_quantity", Float.class));
        form.setOutsourceDownOffsetQuantity(rs.getObject("outsource_down_offset_quantity", Float.class));
        form.setOutsourcePosition(rs.getString("outsource_position"));
        form.setStartedAt(rs.getTimestamp("started_at"));
        form.setEndAt(rs.getTimestamp("end_at"));
        form.setState(rs.getString("state"));
        return form;
    }
}
