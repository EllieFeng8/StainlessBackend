package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
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
        @JsonAlias("record_by") String recordBy) {
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
        return params;
    }
}
