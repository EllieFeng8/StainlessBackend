package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public record MaterialHistoryMoldLimitsRequest(
        @JsonAlias("work_order_code") Integer workOrderCode,
        @JsonAlias("machine_code") String machineCode,
        @JsonAlias("target_value") BigDecimal targetValue,
        @JsonAlias("alert_upper_limit") BigDecimal alertUpperLimit,
        @JsonAlias("alert_lower_limit") BigDecimal alertLowerLimit) {
    public Map<String, Object> toUpdateParameters() {
        Map<String, Object> fields = new LinkedHashMap<>();
        if (targetValue != null) fields.put("target_value", targetValue);
        if (alertUpperLimit != null) fields.put("alert_upper_limit", alertUpperLimit);
        if (alertLowerLimit != null) fields.put("alert_lower_limit", alertLowerLimit);
        return fields;
    }
}
