package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public record MaterialHistoryFormRequest(
        Integer workOrderCode,
        String materialCode,
        String materialName,
        String unit,
        Float quantity,
        Float materialLoss,
        Float remainingMaterial,
        String remark,
        @JsonAlias("record_by") String recordBy,
        @JsonAlias("target_value") BigDecimal targetValue,
        @JsonAlias("alert_upper_limit") BigDecimal alertUpperLimit,
        @JsonAlias("alert_lower_limit") BigDecimal alertLowerLimit,
        @JsonAlias("machine_code") String machineCode,
        @JsonAlias("die_number") String dieNumber,
        @JsonAlias("mold_number") String moldNumber) {
    public Map<String, Object> toUpdateParameters() {
        Map<String, Object> params = new LinkedHashMap<>();
        if (workOrderCode != null) params.put("work_oder_code", workOrderCode);
        if (materialCode != null) params.put("material_code", materialCode);
        if (materialName != null) params.put("material_name", materialName);
        if (unit != null) params.put("unit", unit);
        if (quantity != null) params.put("quantity", quantity);
        if (materialLoss != null) params.put("material_loss", materialLoss);
        if (remainingMaterial != null) params.put("remaining_material", remainingMaterial);
        if (remark != null) params.put("remark", remark);
        if (recordBy != null) params.put("record_by", recordBy);
        if (targetValue != null) params.put("target_value", targetValue);
        if (alertUpperLimit != null) params.put("alert_upper_limit", alertUpperLimit);
        if (alertLowerLimit != null) params.put("alert_lower_limit", alertLowerLimit);
        if (machineCode != null) params.put("machine_code", machineCode);
        if (dieNumber != null) params.put("die_number", dieNumber);
        if (moldNumber != null) params.put("mold_number", moldNumber);
        return params;
    }
}
