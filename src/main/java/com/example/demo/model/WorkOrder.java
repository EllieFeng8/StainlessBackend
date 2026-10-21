package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class WorkOrder {
    int id;
    int workOrder;
    Date deadlineDate;
    Date startAt;
    Date endAt;
    String model;
    String material;
    String productName;
    String wireDia;
    float totalQuantity;
    float updateQuantity;
    String reason;
    float kilogram;
    String factory;
    String furaceNo;
    String flow;
    String note;
    float headWeight;
    float toothWeight;
    String approvedBy;
    String reviewedBy;
    String inspectedBy;
    String preparedBy;
    String clientName;
    String assignedBy;
}
