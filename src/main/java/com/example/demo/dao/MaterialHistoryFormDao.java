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
import java.math.BigDecimal;

@Component
public class MaterialHistoryFormDao {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public MaterialHistoryFormDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int addMaterialHistoryForm(MaterialHistoryFormRequest request) {
        BigDecimal targetValue = null;
        BigDecimal alertUpperLimit = null;
        BigDecimal alertLowerLimit = null;
        if (request.moldNumber() != null && !request.moldNumber().isBlank()) {
            targetValue = request.targetValue() != null
                    ? request.targetValue() : new BigDecimal("10000");
            alertUpperLimit = request.alertUpperLimit() != null
                    ? request.alertUpperLimit() : targetValue.multiply(new BigDecimal("0.95"));
            alertLowerLimit = request.alertLowerLimit() != null
                    ? request.alertLowerLimit() : targetValue.multiply(new BigDecimal("0.90"));
        }
        String sql = """
                INSERT INTO mydb.material_history_form
                    (work_oder_code, material_code, material_name, unit, created_at, updated_at,
                     quantity, material_loss, remaining_material, remark, record_by,
                     target_value, alert_upper_limit, alert_lower_limit, machine_code, die_number, mold_number)
                VALUES
                    (:workOrderCode, :materialCode, :materialName, :unit, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(),
                     :quantity, :materialLoss, :remainingMaterial, :remark, :recordBy,
                     :targetValue, :alertUpperLimit, :alertLowerLimit, :machineCode, :dieNumber, :moldNumber)
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
                .addValue("recordBy", request.recordBy())
                .addValue("targetValue", targetValue)
                .addValue("alertUpperLimit", alertUpperLimit)
                .addValue("alertLowerLimit", alertLowerLimit)
                .addValue("machineCode", request.machineCode())
                .addValue("dieNumber", request.dieNumber())
                .addValue("moldNumber", request.moldNumber());
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

    private static final String MOLD_FILTER = " WHERE work_oder_code = :workOrderCode"
            + " AND machine_code = :machineCode AND mold_number IS NOT NULL AND TRIM(mold_number) <> ''";

    public int updateMoldLimits(int workOrderCode, String machineCode, Map<String, Object> fields) {
        String assignments = fields.keySet().stream()
                .map(column -> column + " = :" + column)
                .collect(java.util.stream.Collectors.joining(", "));
        Map<String, Object> params = new HashMap<>(fields);
        params.put("workOrderCode", workOrderCode);
        params.put("machineCode", machineCode);
        return jdbcTemplate.update("UPDATE mydb.material_history_form SET " + assignments
                + ", updated_at = CURRENT_TIMESTAMP()" + MOLD_FILTER, params);
    }

    public boolean existsMoldByWorkOrderAndMachine(int workOrderCode, String machineCode) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM mydb.material_history_form" + MOLD_FILTER,
                Map.of("workOrderCode", workOrderCode, "machineCode", machineCode), Integer.class);
        return count != null && count > 0;
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
