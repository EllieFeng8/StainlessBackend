package com.example.demo.dto;

import java.math.BigDecimal;

/** Reason is an exemption category for exemption, explanatory text for rerun/additional production. */
public record WorkOrderDetailCommandRequest(String reason, BigDecimal targetQuantity) {}
