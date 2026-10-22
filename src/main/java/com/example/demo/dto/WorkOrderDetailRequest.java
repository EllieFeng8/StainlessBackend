package com.example.demo.dto;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Display code, deliberately never parsed as a number. */
public record WorkOrderDetailRequest(String workOrderCode) {
    public WorkOrderDetailRequest {
        if (workOrderCode == null || workOrderCode.isBlank() || workOrderCode.length() > 255)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "workOrderCode 必填且最多 255 字元");
    }
}
