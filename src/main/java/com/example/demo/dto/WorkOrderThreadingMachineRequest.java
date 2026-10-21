package com.example.demo.dto;

import java.util.Date;

public record WorkOrderThreadingMachineRequest(
        Integer workOrderCode,
        Integer machineId,
        Integer materialId,
        @com.fasterxml.jackson.annotation.JsonAlias("member_id") Integer memberId,
        String updateMachine,
        String changeReason,
        Float targetQuantity,
        Float targetUpOffsetQuantity,
        Float targetDownOffsetQuantity,
        Float currentQuantity,
        Float previousQuantity,
        Float updateQuantity,
        String quantityChangeReason,
        Date startedAt,
        Date endAt,
        String productionStatus,
        String reason,
        Integer orderClosed,
        String remark,
        Float materialLoss,
        Float remainingMaterial,
        Float oee,
        @com.fasterxml.jackson.annotation.JsonAlias("employee_code") String employeeCode,
        String reviewer,
        Boolean approved,
        String opinion) {
}
