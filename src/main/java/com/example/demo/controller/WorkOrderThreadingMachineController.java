package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ThreadingProductionAssignmentResponse;
import com.example.demo.dto.ProductionFlowRequest;
import com.example.demo.service.ProductionFlowService;
import com.example.demo.dto.WorkOrderThreadingMachineRequest;
import com.example.demo.model.WorkOrderThreadingMachine;
import com.example.demo.service.WorkOrderThreadingMachineService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workOrderThreadingMachine")
public class WorkOrderThreadingMachineController {
    private final WorkOrderThreadingMachineService service;
    private final ProductionFlowService productionFlow;

    public WorkOrderThreadingMachineController(WorkOrderThreadingMachineService service, ProductionFlowService productionFlow) {
        this.productionFlow = productionFlow;
        this.service = service;
    }

    @GetMapping("/production/verify")
    public ThreadingProductionAssignmentResponse verifyProductionAssignment(
            @RequestParam("workOrderId") int workOrderId,
            @RequestParam("threadMachineId") int threadMachineId,
            @RequestParam("materialId") int materialId) {
        return service.verifyProductionAssignment(workOrderId, threadMachineId, materialId);
    }

    @PostMapping("/{id}/production/start")
    public ResponseEntity<ApiResponse<Void>> startProduction(
            @PathVariable("id") int id) {
        productionFlow.startProduction(ProductionFlowService.Process.THREADING, id);
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    @PostMapping("/{id}/production/status")
    public ResponseEntity<ApiResponse<Void>> updateProductionStatus(
            @PathVariable("id") int id, @RequestBody ProductionFlowRequest request) {
        productionFlow.execute(ProductionFlowService.Process.THREADING, id, "status", request);
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    @PostMapping("/{id}/production/close")
    public ResponseEntity<ApiResponse<Void>> closeProduction(
            @PathVariable("id") int id, @RequestBody ProductionFlowRequest request) {
        productionFlow.execute(ProductionFlowService.Process.THREADING, id, "close", request);
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    @PostMapping("/{id}/production/material")
    public ResponseEntity<ApiResponse<Void>> saveMaterial(
            @PathVariable("id") int id, @RequestBody ProductionFlowRequest request) {
        productionFlow.execute(ProductionFlowService.Process.THREADING, id, "material", request);
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    @PostMapping("/{id}/production/review")
    public ResponseEntity<ApiResponse<Void>> reviewProduction(
            @PathVariable("id") int id, @RequestBody ProductionFlowRequest request) {
        productionFlow.execute(ProductionFlowService.Process.THREADING, id, "review", request);
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    @PostMapping("/addWorkOrderThreadingMachine")
    public ResponseEntity<ApiResponse<Void>> addWorkOrderThreadingMachine(@RequestBody WorkOrderThreadingMachineRequest request) {
        try {
            if (service.addWorkOrderThreadingMachine(request) != 1) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to create work order threading machine"));
            }
        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.fail(409, "此工單與機台的派工已存在"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("OK"));
    }

    @GetMapping("/getWorkOrderThreadingMachine")
    public ResponseEntity<?> getWorkOrderThreadingMachines(
            @RequestParam(value = "workOrderId", required = false) Integer workOrderId,
            @RequestParam(value = "threadMachineId", required = false) Integer threadMachineId,
            @RequestParam(value = "materialId", required = false) Integer materialId) {
        List<WorkOrderThreadingMachine> matches =
                service.getWorkOrderThreadingMachines(workOrderId, threadMachineId, materialId);
        if (matches.isEmpty() && (workOrderId != null || threadMachineId != null || materialId != null)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(404, "工單、機台與物料對應關係不存在"));
        }
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/getWorkOrderThreadingMachine/{id}")
    public ResponseEntity<WorkOrderThreadingMachine> getWorkOrderThreadingMachine(@PathVariable("id") int id) {
        return ResponseEntity.of(service.getWorkOrderThreadingMachine(id));
    }

    @PatchMapping("/updateWorkOrderThreadingMachine/{id}")
    public ResponseEntity<ApiResponse<Void>> patchWorkOrderThreadingMachine(
            @PathVariable("id") int id, @RequestBody WorkOrderThreadingMachineRequest request) {
        service.patchWorkOrderThreadingMachine(id, request);
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    @DeleteMapping("/deleteWorkOrderThreadingMachine/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWorkOrderThreadingMachine(@PathVariable("id") int id) {
        if (service.deleteWorkOrderThreadingMachine(id) == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(404, "Work order threading machine not found"));
        }
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }
}
