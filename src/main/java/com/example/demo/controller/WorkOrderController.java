package com.example.demo.controller;

import com.example.demo.model.WorkOrder;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.WorkOrderRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.demo.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/workOrder")
public class WorkOrderController {

    @Autowired
    private WorkOrderService workOrderService;

    @DeleteMapping("/deleteWorkOrder/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWorkOrder(@PathVariable("id") int id) {
        if (workOrderService.deleteWorkOrder(id) == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(404, "Work order not found"));
        }
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    @PostMapping("/addWorkOrder")
    public ResponseEntity<ApiResponse<Void>> addWorkOrder(@RequestBody WorkOrderRequest request) {
        if (workOrderService.addWorkOrder(request) != 1) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create work order"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("OK"));
    }

    @PatchMapping("/updateWorkOrder/{id}")
    public ResponseEntity<ApiResponse<Void>> updateWorkOrder(
            @PathVariable("id") int id, @RequestBody WorkOrderRequest request) {
        workOrderService.updateWorkOrder(id, request);
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    @GetMapping("/getWorkOrder")
    public List<WorkOrder> getWorkOrder() {
        return workOrderService.workOrderList();
    }

    @GetMapping("/getWorkOrder/{id}")
    public ResponseEntity<WorkOrder> getWorkOrderById(@PathVariable("id") int id) {
        return ResponseEntity.of(workOrderService.getWorkOrder(id));
    }

    @GetMapping("/getWorkOrderByCode/{workOrderCode}")
    public ResponseEntity<WorkOrder> getWorkOrderByCode(@PathVariable("workOrderCode") int workOrderCode) {
        return ResponseEntity.of(workOrderService.getWorkOrderByCode(workOrderCode));
    }
}
