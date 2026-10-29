package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.Date;

public record ProductionFlowRequest(
        Integer workOrderId, Integer machineId, Integer materialId,
        @JsonAlias("production_status") String productionStatus,
        String reason, String remark,
        @JsonAlias("target_quantity") Double targetQuantity,
        @JsonAlias("target_up_offset_quantity") Double targetUpOffsetQuantity,
        @JsonAlias("target_down_offset_quantity") Double targetDownOffsetQuantity,
        @JsonAlias("quantity_change_reason") String quantityChangeReason,
        @JsonAlias("order_closed") Integer orderClosed,
        @JsonAlias("end_at") Date endAt,
        @JsonAlias("material_loss") Double materialLoss,
        @JsonAlias("remaining_material") Double remainingMaterial,
        Boolean approved, String reviewer, String opinion,
        @JsonAlias("employee_code") String employeeCode,
        @tools.jackson.databind.annotation.JsonDeserialize(using = MachineHistoryStateDeserializer.class)
        Integer state) {}
