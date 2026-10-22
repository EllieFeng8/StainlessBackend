package com.example.demo.dao;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.rowMapper.HeadMachineHistoryLogRowMapper;
import com.example.demo.dto.HeadMachineHistoryLogRequest;
import com.example.demo.model.HeadMachineHistoryLog;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class HeadMachineHistoryLogDao {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public HeadMachineHistoryLogDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int addHeadMachineHistoryLog(HeadMachineHistoryLogRequest request) {
        String sql = """
                INSERT INTO mydb.head_machine_history_log (title, `time`, machine_id, log)
                VALUES (:title, COALESCE(:time, CURRENT_TIMESTAMP), :machineId, :log)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("title", request.title())
                .addValue("time", request.time() == null ? null : Timestamp.valueOf(request.time()), Types.TIMESTAMP)
                .addValue("machineId", request.machineId())
                .addValue("log", request.log());
        return jdbcTemplate.update(sql, params);
    }

    public List<HeadMachineHistoryLog> getHeadMachineHistoryLogs() {
        return jdbcTemplate.query("SELECT * FROM mydb.head_machine_history_log ORDER BY `time` DESC, id DESC",
                new HeadMachineHistoryLogRowMapper());
    }

    public Optional<HeadMachineHistoryLog> getHeadMachineHistoryLog(int id) {
        return jdbcTemplate.query("SELECT * FROM mydb.head_machine_history_log WHERE id = :id",
                Map.of("id", id), new HeadMachineHistoryLogRowMapper()).stream().findFirst();
    }

    public List<HeadMachineHistoryLog> getHeadMachineHistoryLogsByMachine(int machineId) {
        return jdbcTemplate.query("""
                SELECT * FROM mydb.head_machine_history_log WHERE machine_id = :machineId
                ORDER BY `time` DESC, id DESC
                """, Map.of("machineId", machineId), new HeadMachineHistoryLogRowMapper());
    }
}
