package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.HistoryLogRequest;
import com.example.demo.model.HistoryLog;
import com.example.demo.service.HistoryLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/historyLog")
public class HistoryLogController {
    private final HistoryLogService service;

    @Autowired
    public HistoryLogController(HistoryLogService service) {
        this.service = service;
    }

    @PostMapping("/addHistoryLog")
    public ResponseEntity<ApiResponse<Void>> addHistoryLog(@RequestBody HistoryLogRequest request) {
        if (service.addHistoryLog(request) != 1) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create history log"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("OK"));
    }

    @GetMapping("/getHistoryLog")
    public List<HistoryLog> getHistoryLogs() {
        return service.getHistoryLogs();
    }

    @GetMapping("/getHistoryLog/{id}")
    public ResponseEntity<HistoryLog> getHistoryLog(@PathVariable("id") int id) {
        return ResponseEntity.of(service.getHistoryLog(id));
    }

    @GetMapping("/getHistoryLogByWorkOrder")
    public List<HistoryLog> getHistoryLogsByWorkOrder(@RequestParam("workOrderCode") int workOrderCode) {
        return service.getHistoryLogsByWorkOrder(workOrderCode);
    }
}
