package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.HeadingProductionAssignmentResponse;
import com.example.demo.dto.ProductionFlowRequest;
import com.example.demo.service.ProductionFlowService;
import com.example.demo.dto.WorkOrderHeadingMachineRequest;
import com.example.demo.model.WorkOrderHeadingMachine;
import com.example.demo.service.WorkOrderHeadingMachineService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workOrderHeadingMachine")
public class WorkOrderHeadingMachineController {
    private final WorkOrderHeadingMachineService service;
    private final ProductionFlowService productionFlow;

    public WorkOrderHeadingMachineController(WorkOrderHeadingMachineService service, ProductionFlowService productionFlow) {
        this.productionFlow = productionFlow;
        this.service = service;
    }

    @GetMapping("/production/verify")
    public HeadingProductionAssignmentResponse verifyProductionAssignment(
            @RequestParam("workOrderId") int workOrderId,
            @RequestParam("headMachineId") int headMachineId,
            @RequestParam("materialId") int materialId) {
        return service.verifyProductionAssignment(workOrderId, headMachineId, materialId);
    }

    @PostMapping("/{id}/production/start")
    public ResponseEntity<ApiResponse<Void>> startProduction(
            @PathVariable("id") int id) {
        productionFlow.startProduction(ProductionFlowService.Process.HEADING, id);
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    @PostMapping("/{id}/production/status")
    public ResponseEntity<ApiResponse<Void>> updateProductionStatus(
            @PathVariable("id") int id, @RequestBody ProductionFlowRequest request) {
        productionFlow.execute(ProductionFlowService.Process.HEADING, id, "status", request);
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    @PostMapping("/{id}/production/close")
    public ResponseEntity<ApiResponse<Void>> closeProduction(
            @PathVariable("id") int id, @RequestBody ProductionFlowRequest request) {
        productionFlow.execute(ProductionFlowService.Process.HEADING, id, "close", request);
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    @PostMapping("/{id}/production/material")
    public ResponseEntity<ApiResponse<Void>> saveMaterial(
            @PathVariable("id") int id, @RequestBody ProductionFlowRequest request) {
        productionFlow.execute(ProductionFlowService.Process.HEADING, id, "material", request);
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    @PostMapping("/{id}/production/review")
    public ResponseEntity<ApiResponse<Void>> reviewProduction(
            @PathVariable("id") int id, @RequestBody ProductionFlowRequest request) {
        productionFlow.execute(ProductionFlowService.Process.HEADING, id, "review", request);
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    @PostMapping("/addWorkOrderHeadingMachine")
    public ResponseEntity<ApiResponse<Void>> addWorkOrderHeadingMachine(@RequestBody WorkOrderHeadingMachineRequest request) {
        try {
            if (service.addWorkOrderHeadingMachine(request) != 1) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to create work order heading machine"));
            }
        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.fail(409, "此工單與機台的派工已存在"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("OK"));
    }

    @GetMapping("/getWorkOrderHeadingMachine")
    public ResponseEntity<?> getWorkOrderHeadingMachines(
            @RequestParam(value = "workOrderId", required = false) Integer workOrderId,
            @RequestParam(value = "headMachineId", required = false) Integer headMachineId,
            @RequestParam(value = "materialId", required = false) Integer materialId) {
        List<WorkOrderHeadingMachine> matches =
                service.getWorkOrderHeadingMachines(workOrderId, headMachineId, materialId);
        if (matches.isEmpty() && (workOrderId != null || headMachineId != null || materialId != null)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(404, "工單、機台與物料對應關係不存在"));
        }
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/getWorkOrderHeadingMachine/{id}")
    public ResponseEntity<WorkOrderHeadingMachine> getWorkOrderHeadingMachine(@PathVariable("id") int id) {
        return ResponseEntity.of(service.getWorkOrderHeadingMachine(id));
    }

    @PatchMapping("/updateWorkOrderHeadingMachine/{id}")
    public ResponseEntity<ApiResponse<Void>> patchWorkOrderHeadingMachine(
            @PathVariable("id") int id, @RequestBody WorkOrderHeadingMachineRequest request) {
        service.patchWorkOrderHeadingMachine(id, request);
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    @DeleteMapping("/deleteWorkOrderHeadingMachine/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWorkOrderHeadingMachine(@PathVariable("id") int id) {
        if (service.deleteWorkOrderHeadingMachine(id) == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(404, "Work order heading machine not found"));
        }
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }
}
