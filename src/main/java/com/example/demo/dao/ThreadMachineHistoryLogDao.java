package com.example.demo.dao;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.rowMapper.ThreadMachineHistoryLogRowMapper;
import com.example.demo.dto.ThreadMachineHistoryLogRequest;
import com.example.demo.model.ThreadMachineHistoryLog;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ThreadMachineHistoryLogDao {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ThreadMachineHistoryLogDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int addThreadMachineHistoryLog(ThreadMachineHistoryLogRequest request) {
        String sql = """
                INSERT INTO mydb.thread_machine_history_log (title, `time`, machine_id, log)
                VALUES (:title, COALESCE(:time, CURRENT_TIMESTAMP), :machineId, :log)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("title", request.title())
                .addValue("time", request.time() == null ? null : new Timestamp(request.time().getTime()), Types.TIMESTAMP)
                .addValue("machineId", request.machineId())
                .addValue("log", request.log());
        return jdbcTemplate.update(sql, params);
    }

    public List<ThreadMachineHistoryLog> getThreadMachineHistoryLogs() {
        return jdbcTemplate.query("SELECT * FROM mydb.thread_machine_history_log ORDER BY `time` DESC, id DESC",
                new ThreadMachineHistoryLogRowMapper());
    }

    public Optional<ThreadMachineHistoryLog> getThreadMachineHistoryLog(int id) {
        return jdbcTemplate.query("SELECT * FROM mydb.thread_machine_history_log WHERE id = :id",
                Map.of("id", id), new ThreadMachineHistoryLogRowMapper()).stream().findFirst();
    }

    public List<ThreadMachineHistoryLog> getThreadMachineHistoryLogsByMachine(int machineId) {
        return jdbcTemplate.query("""
                SELECT * FROM mydb.thread_machine_history_log WHERE machine_id = :machineId
                ORDER BY `time` DESC, id DESC
                """, Map.of("machineId", machineId), new ThreadMachineHistoryLogRowMapper());
    }
}
