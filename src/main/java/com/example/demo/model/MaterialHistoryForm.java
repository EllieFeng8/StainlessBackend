package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


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
}
