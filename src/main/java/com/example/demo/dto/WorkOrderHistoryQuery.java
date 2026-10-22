package com.example.demo.dto;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;

/** Immutable MVC query binding; an empty text filter matches every row. */
public record WorkOrderHistoryQuery(String workOrderId, String machineName, String heatNumber,
        String productCode, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate, Integer page, Integer pageSize) {
    public WorkOrderHistoryQuery {
        workOrderId = clean(workOrderId); machineName = clean(machineName);
        heatNumber = clean(heatNumber); productCode = clean(productCode);
        page = page == null ? 1 : page;
        pageSize = pageSize == null ? 10 : pageSize;
        if (page < 1 || pageSize < 1 || pageSize > 200)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page >= 1; pageSize must be 1..200");
        if (startDate != null && endDate != null && startDate.isAfter(endDate))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDate must not exceed endDate");
        if (endDate != null && endDate.getYear() >= 9999)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endDate must be before year 9999");
    }
    private static String clean(String value) { return value == null ? "" : value.strip(); }
}
