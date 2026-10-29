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
                INSERT INTO mydb.thread_machine_history_log (title, `time`, machine_id, log, state)
                VALUES (:title, COALESCE(:time, CURRENT_TIMESTAMP), :machineId, :log, :state)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("title", request.title())
                .addValue("time", request.time() == null ? null : new Timestamp(request.time().getTime()), Types.TIMESTAMP)
                .addValue("machineId", request.machineId())
                .addValue("log", request.log())
                .addValue("state", request.state(), Types.INTEGER);
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

    public int updateThreadMachineHistoryLog(int id, ThreadMachineHistoryLogRequest request) {
        java.util.ArrayList<String> assignments = new java.util.ArrayList<>();
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
        if (request.title() != null) {
            assignments.add("title = :title");
            params.addValue("title", request.title());
        }
        if (request.time() != null) {
            assignments.add("`time` = :time");
            params.addValue("time", new Timestamp(request.time().getTime()), Types.TIMESTAMP);
        }
        if (request.machineId() != null) {
            assignments.add("machine_id = :machineId");
            params.addValue("machineId", request.machineId());
        }
        if (request.log() != null) {
            assignments.add("log = :log");
            params.addValue("log", request.log());
        }
        if (request.state() != null) {
            assignments.add("state = :state");
            params.addValue("state", request.state(), Types.INTEGER);
        }
        if (assignments.isEmpty()) return 0;
        return jdbcTemplate.update("UPDATE mydb.thread_machine_history_log SET "
                + String.join(", ", assignments) + " WHERE id = :id", params);
    }

    public int replaceThreadMachineHistoryLog(int id, ThreadMachineHistoryLogRequest request) {
        return jdbcTemplate.update("UPDATE mydb.thread_machine_history_log SET title = :title, `time` = :time, "
                + "machine_id = :machineId, log = :log, state = :state WHERE id = :id",
                new MapSqlParameterSource().addValue("id", id)
                        .addValue("title", request.title()).addValue("time", new Timestamp(request.time().getTime()), Types.TIMESTAMP)
                        .addValue("machineId", request.machineId()).addValue("log", request.log())
                        .addValue("state", request.state(), Types.INTEGER));
    }
}
