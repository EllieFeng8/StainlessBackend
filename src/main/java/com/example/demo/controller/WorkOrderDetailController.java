package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.dto.*;
import com.example.demo.service.WorkOrderDetailService;
import com.example.demo.service.WorkOrderFirstArticleStateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/work-orders")
public class WorkOrderDetailController {
    private final WorkOrderDetailService service;
    private final WorkOrderFirstArticleStateService commands;
    @Autowired
    public WorkOrderDetailController(WorkOrderDetailService service,WorkOrderFirstArticleStateService commands) {
        this.service=service; this.commands=commands;
    }
    @GetMapping("/{workOrderCode}/detail")
    public ResponseEntity<WorkOrderDetailResponse> detail(@PathVariable("workOrderCode") String code) {
        return ResponseEntity.ok(service.detail(new WorkOrderDetailRequest(code)));
    }
    @PostMapping("/{workOrderCode}/assignments/{stage}/{assignmentId}/first-article/{action}")
    public ResponseEntity<ApiResponse<WorkOrderFirstArticleStateService.CommandResult>> command(
            @PathVariable("workOrderCode") String code, @PathVariable("stage") String stage,
            @PathVariable("assignmentId") long assignmentId, @PathVariable("action") String action,
            @RequestBody(required=false) WorkOrderDetailCommandRequest request) {
        var result=commands.command(new WorkOrderDetailRequest(code),stage,assignmentId,action,request);
        return ResponseEntity.ok(new ApiResponse<>(true,200,"OK",result));
    }
}
