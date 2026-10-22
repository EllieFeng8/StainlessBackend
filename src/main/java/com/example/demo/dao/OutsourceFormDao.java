package com.example.demo.dao;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.rowMapper.OutsourceFormRowMapper;
import com.example.demo.dto.OutsourceFormRequest;
import com.example.demo.model.OutsourceForm;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class OutsourceFormDao {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public OutsourceFormDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int addOutsourceForm(OutsourceFormRequest request) {
        String sql = """
                INSERT INTO mydb.outsource_form
                    (outsource_code, work_order_id, outsource_quantity,
                     outsource_up_offset_quantity, outsource_down_offset_quantity, outsource_position,
                     started_at, end_at, state, current_quantity, change_reason)
                VALUES
                    (:outsourceCode, :workOrderId, :outsourceQuantity,
                     :outsourceUpOffsetQuantity, :outsourceDownOffsetQuantity, :outsourcePosition,
                     :startedAt, :endAt, :state, :currentQuantity, :changeReason)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("outsourceCode", request.outsourceCode())
                .addValue("workOrderId", request.workOrderId())
                .addValue("outsourceQuantity", request.outsourceQuantity())
                .addValue("outsourceUpOffsetQuantity", request.outsourceUpOffsetQuantity())
                .addValue("outsourceDownOffsetQuantity", request.outsourceDownOffsetQuantity())
                .addValue("outsourcePosition", request.outsourcePosition())
                .addValue("startedAt", request.startedAt(), Types.TIMESTAMP)
                .addValue("endAt", request.endAt(), Types.TIMESTAMP)
                .addValue("state", request.state())
                .addValue("currentQuantity", request.currentQuantity())
                .addValue("changeReason", request.changeReason());
        return jdbcTemplate.update(sql, params);
    }

    public int updateOutsourceForm(int id, Map<String, Object> fields) {
        String assignments = fields.keySet().stream()
                .map(column -> column + " = :" + column)
                .collect(java.util.stream.Collectors.joining(", "));
        Map<String, Object> params = new HashMap<>(fields);
        params.put("id", id);
        return jdbcTemplate.update(
                "UPDATE mydb.outsource_form SET " + assignments + " WHERE id = :id", params);
    }

    public int deleteOutsourceForm(int id) {
        return jdbcTemplate.update("DELETE FROM mydb.outsource_form WHERE id = :id", Map.of("id", id));
    }

    public boolean existsById(int id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM mydb.outsource_form WHERE id = :id",
                Map.of("id", id), Integer.class);
        return count != null && count > 0;
    }

    public List<OutsourceForm> getOutsourceForms() {
        return jdbcTemplate.query("SELECT * FROM mydb.outsource_form", new OutsourceFormRowMapper());
    }

    public Optional<OutsourceForm> getOutsourceForm(int id) {
        return jdbcTemplate.query("SELECT * FROM mydb.outsource_form WHERE id = :id",
                Map.of("id", id), new OutsourceFormRowMapper()).stream().findFirst();
    }

    public List<OutsourceForm> getOutsourceFormsByWorkOrder(int workOrderId) {
        return jdbcTemplate.query("SELECT * FROM mydb.outsource_form WHERE work_order_id = :workOrderId",
                Map.of("workOrderId", workOrderId), new OutsourceFormRowMapper());
    }
}
