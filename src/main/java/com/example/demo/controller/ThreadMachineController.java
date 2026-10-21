package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ThreadMachineRequest;
import com.example.demo.model.ThreadMachine;
import com.example.demo.service.ThreadMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/threadMachine")
public class ThreadMachineController {

    @Autowired
    ThreadMachineService threadMachineService;

    @PostMapping("/addMachine")
    public ResponseEntity<ApiResponse<Void>> addMachine(@RequestBody ThreadMachineRequest request) {
        if (threadMachineService.addThreadMachine(request) != 1) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create thread machine"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("OK"));
    }

    @PutMapping("/updateMachine/{id}")
    public ResponseEntity<ApiResponse<Void>> updateMachine(@PathVariable("id") int id,
                                                          @RequestBody ThreadMachineRequest request) {
        if (threadMachineService.updateThreadMachine(id, request) == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(404, "Thread machine not found"));
        }
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    @DeleteMapping("/deleteMachine/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMachine(@PathVariable("id") int id) {
        if (threadMachineService.deleteThreadMachine(id) == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(404, "Thread machine not found"));
        }
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    @GetMapping("/getMachines")
    public List<ThreadMachine> getMachines() {
        return threadMachineService.threadMachineList();
    }

    @GetMapping("/getMachine/{id}")
    public ThreadMachine getMachine(@PathVariable("id") int id) {
        return threadMachineService.singleThreadMachine(id);
    }
}
