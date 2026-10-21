package com.example.demo.rowMapper;

import com.example.demo.model.HeadMachineHistoryLog;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class HeadMachineHistoryLogRowMapper implements RowMapper<HeadMachineHistoryLog> {
    @Override
    public HeadMachineHistoryLog mapRow(ResultSet rs, int rowNum) throws SQLException {
        HeadMachineHistoryLog headMachineHistoryLog = new HeadMachineHistoryLog();
        headMachineHistoryLog.setId(rs.getInt("id"));
        headMachineHistoryLog.setTitle(rs.getString("title"));
        Timestamp time = rs.getTimestamp("time");
        headMachineHistoryLog.setTime(time == null ? null : time.toLocalDateTime());
        headMachineHistoryLog.setMachineId(rs.getInt("machine_id"));
        headMachineHistoryLog.setLog(rs.getString("log"));
        return headMachineHistoryLog;
    }
}
