package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
public class OutsourceForm {
    int id;
    String outsourceCode;
    int workOrderId;
    float outsourceQuantity;
    Float currentQuantity;
    String changeReason;
    Float outsourceUpOffsetQuantity;
    Float outsourceDownOffsetQuantity;
    String outsourcePosition;
    Date startedAt;
    Date endAt;
    String state;
}
