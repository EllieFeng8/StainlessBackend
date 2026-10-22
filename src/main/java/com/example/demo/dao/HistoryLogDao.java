package com.example.demo.dao;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.rowMapper.HistoryLogRowMapper;
import com.example.demo.dto.HistoryLogRequest;
import com.example.demo.model.HistoryLog;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class HistoryLogDao {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public HistoryLogDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int addHistoryLog(HistoryLogRequest request) {
        String sql = """
                INSERT INTO mydb.history_log (title, `time`, work_order_code, log)
                VALUES (:title, COALESCE(:time, CURRENT_TIMESTAMP), :workOrderCode, :log)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("title", request.title())
                .addValue("time", request.time() == null ? null : Timestamp.valueOf(request.time()), Types.TIMESTAMP)
                .addValue("workOrderCode", request.workOrderCode())
                .addValue("log", request.log());
        return jdbcTemplate.update(sql, params);
    }

    public List<HistoryLog> getHistoryLogs() {
        return jdbcTemplate.query("SELECT * FROM mydb.history_log ORDER BY `time` DESC, id DESC",
                new HistoryLogRowMapper());
    }

    public Optional<HistoryLog> getHistoryLog(int id) {
        return jdbcTemplate.query("SELECT * FROM mydb.history_log WHERE id = :id",
                Map.of("id", id), new HistoryLogRowMapper()).stream().findFirst();
    }

    public List<HistoryLog> getHistoryLogsByWorkOrder(int workOrderCode) {
        return jdbcTemplate.query("""
                SELECT * FROM mydb.history_log WHERE work_order_code = :workOrderCode
                ORDER BY `time` DESC, id DESC
                """, Map.of("workOrderCode", workOrderCode), new HistoryLogRowMapper());
    }
}
