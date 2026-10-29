package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.HeadMachineHistoryLogRequest;
import com.example.demo.model.HeadMachineHistoryLog;
import com.example.demo.service.HeadMachineHistoryLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/headMachineHistoryLog")
public class HeadMachineHistoryLogController {
    private final HeadMachineHistoryLogService service;

    @Autowired
    public HeadMachineHistoryLogController(HeadMachineHistoryLogService service) {
        this.service = service;
    }

    @PostMapping("/addHeadMachineHistoryLog")
    public ResponseEntity<ApiResponse<Void>> addHeadMachineHistoryLog(@RequestBody HeadMachineHistoryLogRequest request) {
        if (service.addHeadMachineHistoryLog(request) != 1) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create head machine history log"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("OK"));
    }

    @GetMapping("/getHeadMachineHistoryLog")
    public List<HeadMachineHistoryLog> getHeadMachineHistoryLogs() {
        return service.getHeadMachineHistoryLogs();
    }

    @GetMapping("/getHeadMachineHistoryLog/{id}")
    public ResponseEntity<HeadMachineHistoryLog> getHeadMachineHistoryLog(@PathVariable("id") int id) {
        return ResponseEntity.of(service.getHeadMachineHistoryLog(id));
    }

    @GetMapping("/getHeadMachineHistoryLogByMachine")
    public List<HeadMachineHistoryLog> getHeadMachineHistoryLogsByMachine(@RequestParam("machineId") int machineId) {
        return service.getHeadMachineHistoryLogsByMachine(machineId);
    }

    @PatchMapping("/updateHeadMachineHistoryLog/{id}")
    public ResponseEntity<ApiResponse<Void>> updateHeadMachineHistoryLog(
            @PathVariable("id") int id, @RequestBody HeadMachineHistoryLogRequest request) {
        service.updateHeadMachineHistoryLog(id, request);
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    @PutMapping("/updateHeadMachineHistoryLog/{id}")
    public ResponseEntity<ApiResponse<Void>> replaceHeadMachineHistoryLog(
            @PathVariable("id") int id, @RequestBody HeadMachineHistoryLogRequest request) {
        service.replaceHeadMachineHistoryLog(id, request);
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }
}
