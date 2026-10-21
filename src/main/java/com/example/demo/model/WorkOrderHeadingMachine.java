package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class WorkOrderHeadingMachine {
    int id;
    int workOrderCode;
    int machineId;
    int materialId;
    Integer memberId;
    String employeeCode;
    String reviewer;
    Boolean approved;
    String opinion;
    String updateMachine;
    String changeReason;
    float targetQuantity;
    float targetUpOffsetQuantity;
    float targetDownOffsetQuantity;
    float currentQuantity;
    float previousQuantity;
    float updateQuantity;
    String quantityChangeReason;
    Date startedAt;
    Date endAt;
    String productionStatus;
    String reason;
    int orderClosed;
    String remark;
    float materialLoss;
    float remainingMaterial;
    float oee;

}
