package com.example.demo.dto;

import java.math.BigDecimal;

public record WorkOrderHistoryItem(String recordId, String combinationId, String stage,
        String workOrderId, String machineName, String heatNumber, String productCode,
        String timestamp, String at, boolean canNavigate, String currentStatus, String detail,
        BigDecimal targetQuantity, BigDecimal producedQuantity, BigDecimal oee, BigDecimal failureRate) {}
