package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ThreadMachineHistoryLogRequest;
import com.example.demo.model.ThreadMachineHistoryLog;
import com.example.demo.service.ThreadMachineHistoryLogService;
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
@RequestMapping("/threadMachineHistoryLog")
public class ThreadMachineHistoryLogController {
    private final ThreadMachineHistoryLogService service;

    @Autowired
    public ThreadMachineHistoryLogController(ThreadMachineHistoryLogService service) {
        this.service = service;
    }

    @PostMapping("/addThreadMachineHistoryLog")
    public ResponseEntity<ApiResponse<Void>> addThreadMachineHistoryLog(@RequestBody ThreadMachineHistoryLogRequest request) {
        if (service.addThreadMachineHistoryLog(request) != 1) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create thread machine history log"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("OK"));
    }

    @GetMapping("/getThreadMachineHistoryLog")
    public List<ThreadMachineHistoryLog> getThreadMachineHistoryLogs() {
        return service.getThreadMachineHistoryLogs();
    }

    @GetMapping("/getThreadMachineHistoryLog/{id}")
    public ResponseEntity<ThreadMachineHistoryLog> getThreadMachineHistoryLog(@PathVariable("id") int id) {
        return ResponseEntity.of(service.getThreadMachineHistoryLog(id));
    }

    @GetMapping("/getThreadMachineHistoryLogByMachine")
    public List<ThreadMachineHistoryLog> getThreadMachineHistoryLogsByMachine(@RequestParam("machineId") int machineId) {
        return service.getThreadMachineHistoryLogsByMachine(machineId);
    }
}
