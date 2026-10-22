package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Internal nullable JDBC projection, never returned directly from a controller. */
public record WorkOrderHistoryRow(long id, String stage, String workOrderId, String machineName,
        String heatNumber, String productCode, boolean canNavigate, LocalDateTime timestamp,
        LocalDateTime startedAt, Integer orderClosed, Boolean approved, String productionStatus,
        String state, String reason, String remark, String opinion, String changeReason,
        BigDecimal targetQuantity, BigDecimal producedQuantity, BigDecimal oee) {}
