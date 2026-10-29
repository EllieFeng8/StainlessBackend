package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.math.BigDecimal;


@Getter
@Setter
public class MaterialHistoryForm {
    int materialId;
    int workOrderCode;
    String materialCode;
    String materialName;
    String unit;
    LocalDateTime createAt;
    LocalDateTime updateAt;
    float quantity;
    float materialLoss;
    float remainingMaterial;
    String remark;
    String recordBy;
    BigDecimal targetValue;
    BigDecimal alertUpperLimit;
    BigDecimal alertLowerLimit;
    String machineCode;
    String dieNumber;
    String moldNumber;
}
