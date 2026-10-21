package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public record WorkOrderRequest(
        Integer workOrder,
        LocalDate deadlineDate,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String model,
        String material,
        String productName,
        Float wireDia,
        Float totalQuantity,
        Float updateQuantity,
        String reason,
        Float kilogram,
        String factory,
        String furaceNo,
        String flow,
        String note,
        Float headWeight,
        Float toothWeight,
        @JsonAlias("approved_by") String approvedBy,
        @JsonAlias("reviewed_by") String reviewedBy,
        @JsonAlias("inspected_by") String inspectedBy,
        @JsonAlias("prepared_by") String preparedBy,
        @JsonAlias("client_name") String clientName,
        @JsonAlias("assigned_by") String assignedBy) {
    public Map<String, Object> toUpdateParameters() {
        Map<String, Object> fields = new LinkedHashMap<>();
        if (deadlineDate != null) fields.put("deadline_date", java.sql.Date.valueOf(deadlineDate));
        if (startAt != null) fields.put("start_at", java.sql.Timestamp.valueOf(startAt));
        if (endAt != null) fields.put("end_at", java.sql.Timestamp.valueOf(endAt));
        if (model != null) fields.put("model", model);
        if (material != null) fields.put("material", material);
        if (productName != null) fields.put("product_name", productName);
        if (wireDia != null) fields.put("wire_dia", wireDia);
        if (totalQuantity != null) fields.put("total_quantity", totalQuantity);
        if (updateQuantity != null) fields.put("update_quantity", updateQuantity);
        if (reason != null) fields.put("reason", reason);
        if (kilogram != null) fields.put("kilogram", kilogram);
        if (factory != null) fields.put("factory", factory);
        if (furaceNo != null) fields.put("furnaceNo", furaceNo);
        if (flow != null) fields.put("flow", flow);
        if (note != null) fields.put("note", note);
        if (headWeight != null) fields.put("head_weight", headWeight);
        if (toothWeight != null) fields.put("tooth_weight", toothWeight);
        if (approvedBy != null) fields.put("approved_by", approvedBy);
        if (reviewedBy != null) fields.put("reviewed_by", reviewedBy);
        if (inspectedBy != null) fields.put("inspected_by", inspectedBy);
        if (preparedBy != null) fields.put("prepared_by", preparedBy);
        if (clientName != null) fields.put("client_name", clientName);
        if (assignedBy != null) fields.put("assigned_by", assignedBy);
        return fields;
    }
}
