package com.example.demo.rowMapper;

import com.example.demo.model.HistoryLog;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class HistoryLogRowMapper implements RowMapper<HistoryLog> {
    @Override
    public HistoryLog mapRow(ResultSet rs, int rowNum) throws SQLException {
        HistoryLog historyLog = new HistoryLog();
        historyLog.setId(rs.getInt("id"));
        historyLog.setTitle(rs.getString("title"));
        Timestamp time = rs.getTimestamp("time");
        historyLog.setTime(time == null ? null : time.toLocalDateTime());
        historyLog.setWorkOrderCode(rs.getInt("work_order_code"));
        historyLog.setLog(rs.getString("log"));
        return historyLog;
    }
}
