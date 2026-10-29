package com.example.demo.rowMapper;

import com.example.demo.model.ThreadMachineHistoryLog;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ThreadMachineHistoryLogRowMapper implements RowMapper<ThreadMachineHistoryLog> {
    @Override
    public ThreadMachineHistoryLog mapRow(ResultSet rs, int rowNum) throws SQLException {
        ThreadMachineHistoryLog threadMachineHistoryLog = new ThreadMachineHistoryLog();
        threadMachineHistoryLog.setId(rs.getInt("id"));
        threadMachineHistoryLog.setTitle(rs.getString("title"));
        Timestamp time = rs.getTimestamp("time");
        threadMachineHistoryLog.setTime(time);
        threadMachineHistoryLog.setMachineId(rs.getInt("machine_id"));
        threadMachineHistoryLog.setLog(rs.getString("log"));
        threadMachineHistoryLog.setState(rs.getObject("state", Integer.class));
        return threadMachineHistoryLog;
    }
}
