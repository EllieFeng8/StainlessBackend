package com.example.demo.dao;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.rowMapper.MaterialHistoryFormRowMapper;
import com.example.demo.dto.MaterialHistoryFormRequest;
import com.example.demo.model.MaterialHistoryForm;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class MaterialHistoryFormDao {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public MaterialHistoryFormDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int addMaterialHistoryForm(MaterialHistoryFormRequest request) {
        String sql = """
                INSERT INTO mydb.material_history_form
                    (work_oder_code, material_code, material_name, unit, created_at, updated_at,
                     quantity, material_loss, remaining_material, remark, record_by)
                VALUES
                    (:workOrderCode, :materialCode, :materialName, :unit, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(),
                     :quantity, :materialLoss, :remainingMaterial, :remark, :recordBy)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("workOrderCode", request.workOrderCode())
                .addValue("materialCode", request.materialCode())
                .addValue("materialName", request.materialName())
                .addValue("unit", request.unit())
                .addValue("quantity", request.quantity())
                .addValue("materialLoss", request.materialLoss())
                .addValue("remainingMaterial", request.remainingMaterial())
                .addValue("remark", request.remark())
                .addValue("recordBy", request.recordBy());
        return jdbcTemplate.update(sql, params);
    }

    public int updateMaterialHistoryForm(int id, Map<String, Object> fields) {
        String assignments = fields.keySet().stream()
                .map(column -> column + " = :" + column)
                .collect(java.util.stream.Collectors.joining(", "));
        Map<String, Object> params = new HashMap<>(fields);
        params.put("id", id);
        return jdbcTemplate.update(
                "UPDATE mydb.material_history_form SET " + assignments
                        + ", updated_at = CURRENT_TIMESTAMP() WHERE material_id = :id", params);
    }

    public boolean existsById(int id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM mydb.material_history_form WHERE material_id = :id",
                Map.of("id", id), Integer.class);
        return count != null && count > 0;
    }

    public List<MaterialHistoryForm> getMaterialHistoryForms() {
        return jdbcTemplate.query("SELECT * FROM mydb.material_history_form",
                new MaterialHistoryFormRowMapper());
    }

    public Optional<MaterialHistoryForm> getMaterialHistoryForm(String materialCode) {
        return jdbcTemplate.query(
                "SELECT * FROM mydb.material_history_form WHERE material_code = :materialCode",
                Map.of("materialCode", materialCode), new MaterialHistoryFormRowMapper()).stream().findFirst();
    }

    public List<MaterialHistoryForm> getMaterialHistoryFormsByWorkOrder(int workOrderCode) {
        return jdbcTemplate.query(
                "SELECT * FROM mydb.material_history_form WHERE work_oder_code = :workOrderCode",
                Map.of("workOrderCode", workOrderCode), new MaterialHistoryFormRowMapper());
    }
}
