package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.sql.Timestamp;

public record OutsourceFormRequest(
        String outsourceCode,
        Integer workOrderId,
        Float outsourceQuantity,
        Float outsourceUpOffsetQuantity,
        Float outsourceDownOffsetQuantity,
        String outsourcePosition,
        Date startedAt,
        Date endAt,
        String state,
        @JsonAlias("current_quantity") Float currentQuantity,
        @JsonAlias("change_reason") String changeReason) {
    public Map<String, Object> toUpdateParameters() {
        Map<String, Object> params = new LinkedHashMap<>();
        if (outsourceCode != null) params.put("outsource_code", outsourceCode);
        if (workOrderId != null) params.put("work_order_id", workOrderId);
        if (outsourceQuantity != null) params.put("outsource_quantity", outsourceQuantity);
        if (outsourceUpOffsetQuantity != null) params.put("outsource_up_offset_quantity", outsourceUpOffsetQuantity);
        if (outsourceDownOffsetQuantity != null) params.put("outsource_down_offset_quantity", outsourceDownOffsetQuantity);
        if (outsourcePosition != null) params.put("outsource_position", outsourcePosition);
        if (startedAt != null) params.put("started_at", new Timestamp(startedAt.getTime()));
        if (endAt != null) params.put("end_at", new Timestamp(endAt.getTime()));
        if (state != null) params.put("state", state);
        if (currentQuantity != null) params.put("current_quantity", currentQuantity);
        if (changeReason != null) params.put("change_reason", changeReason);
        return params;
    }
}
