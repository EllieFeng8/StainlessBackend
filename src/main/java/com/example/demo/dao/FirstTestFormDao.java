package com.example.demo.dao;

import com.example.demo.rowMapper.FirstTestFormRowMapper;
import com.example.demo.model.FirstTestForm;
import com.example.demo.dto.FirstTestFormRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FirstTestFormDao {

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public record SaveResult(int id, boolean created, java.time.LocalDateTime dateTime) {}

    public boolean lockWorkOrder(int id) {
        return !namedParameterJdbcTemplate.queryForList(
                "SELECT id FROM mydb.work_order WHERE id = :id FOR UPDATE",
                Map.of("id", id), Integer.class).isEmpty();
    }

    public SaveResult saveFirstTestForm(FirstTestFormRequest request, java.time.LocalDateTime dateTime) {
        String sql = "SELECT id FROM mydb.first_test_form WHERE work_order_code = :workOrderId";
        Map<String, Object> matchParams = new HashMap<>();
        matchParams.put("workOrderId", request.workOrderCode());
        // Match every supplied machine; a different machine must create a new record.
        if (request.headMachineId() != null) {
            sql += " AND head_machineId = :headMachineId";
            matchParams.put("headMachineId", request.headMachineId());
        }
        if (request.threadMachineId() != null) {
            sql += " AND thread_machineId = :threadMachineId";
            matchParams.put("threadMachineId", request.threadMachineId());
        }
        List<Integer> ids = namedParameterJdbcTemplate.queryForList(
                sql + " FOR UPDATE", matchParams, Integer.class);
        if (ids.size() > 1) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "同工單同機台已有多筆首件紀錄，請先合併");
        }
        Map<String, Object> params = request.toInsertParameters();
        params.put("dateTime", java.sql.Timestamp.valueOf(dateTime));
        if (!ids.isEmpty()) {
            params.remove("work_order_code");
            updateFirstTestForm(ids.getFirst(), params);
            return new SaveResult(ids.getFirst(), false, dateTime);
        }
        String columns = String.join(", ", params.keySet());
        String values = params.keySet().stream().map(column -> ":" + column)
                .collect(java.util.stream.Collectors.joining(", "));
        var keys = new org.springframework.jdbc.support.GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(
                "INSERT INTO mydb.first_test_form (" + columns + ") VALUES (" + values + ")",
                new org.springframework.jdbc.core.namedparam.MapSqlParameterSource(params), keys,
                new String[]{"id"});
        return new SaveResult(java.util.Objects.requireNonNull(keys.getKey()).intValue(), true, dateTime);
    }
    public int updateFirstTestForm(int id, Map<String, Object> fields) {
        String assignments = fields.keySet().stream()
                .map(column -> column + " = :" + column)
                .collect(java.util.stream.Collectors.joining(", "));
        Map<String, Object> params = new HashMap<>(fields);
        params.put("id", id);
        return namedParameterJdbcTemplate.update(
                "UPDATE mydb.first_test_form SET " + assignments + " WHERE id = :id", params);
    }

    public boolean existsById(int id) {
        Integer count = namedParameterJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM mydb.first_test_form WHERE id = :id", Map.of("id", id), Integer.class);
        return count != null && count > 0;
    }

    public List<FirstTestForm> getFirstTestForm() {
        String sql = "SELECT t.* FROM mydb.first_test_form t ";
        List<FirstTestForm> firstTest = namedParameterJdbcTemplate.query(sql,new FirstTestFormRowMapper());
        return firstTest;
    }

    public List<Map<String, Object>> getFirstTestFormByMachine(Integer headMachineId, Integer threadMachineId) {
        return getFirstTestFormByWorkOrderAndMachine(null, headMachineId, threadMachineId);
    }

    public List<Map<String, Object>> getFirstTestFormByWorkOrderAndMachine(
            Integer workOrderId, Integer headMachineId, Integer threadMachineId) {
        if (headMachineId == null && threadMachineId == null) {
            throw new IllegalArgumentException("headMachineId or threadMachineId is required");
        }
        String sql = "SELECT t.* FROM mydb.first_test_form t WHERE ";
        Map<String, Object> params = new HashMap<>();
        if (workOrderId != null) {
            sql += "t.work_order_code = :workOrderId AND ";
            params.put("workOrderId", workOrderId);
        }
        sql += "(";
        if (headMachineId != null) {
            sql += "t.head_machineId = :headMachineId";
            params.put("headMachineId", headMachineId);
        }
        if (threadMachineId != null) {
            if (headMachineId != null) {
                sql += " OR ";
            }
            sql += "t.thread_machineId = :threadMachineId";
            params.put("threadMachineId", threadMachineId);
        }
        sql += ")";
        List<Map<String, Object>> firstTest = namedParameterJdbcTemplate.query(sql, params,
                FirstTestFormRowMapper.forMachine(headMachineId != null, threadMachineId != null));
        return firstTest;
    }
}

