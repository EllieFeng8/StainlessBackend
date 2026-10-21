package com.example.demo.controller;

import com.example.demo.model.FirstTestForm;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.FirstTestFormRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.demo.service.FirstTestFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/firstTest")
public class FirstTestFormController {

    @Autowired
    FirstTestFormService firstTestFormService;

    @PostMapping("/addFirstTest")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addFirstTestForm(@RequestBody FirstTestFormRequest request) {
        var result = firstTestFormService.addFirstTestForm(request);
        int status = result.created() ? 201 : 200;
        Map<String, Object> data = Map.of("id", result.id(), "workOrderId", request.workOrderCode(),
                "dateTime", result.dateTime());
        return ResponseEntity.status(status).body(new ApiResponse<>(true, status,
                result.created() ? "首件檢測已建立" : "首件檢測已更新", data));
    }
    @PatchMapping("/updateFirstTest/{id}")
    public ResponseEntity<ApiResponse<Void>> updateFirstTestForm(
            @PathVariable("id") int id, @RequestBody FirstTestFormRequest request) {
        firstTestFormService.updateFirstTestForm(id, request);
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    @GetMapping("/getFirstTest")
    public List<FirstTestForm> firstTestForms() {
        List<FirstTestForm> firstTest = firstTestFormService.firstTestFormList();
        return firstTest;
    }

    @GetMapping("/getFirstTestByWorkOrderAndMachine")
    public List<Map<String, Object>> firstTestFormsByWorkOrderAndMachine(
            @RequestParam("workOrderId") int workOrderId,
            @RequestParam(value = "headMachineId", required = false) Integer headMachineId,
            @RequestParam(value = "threadMachineId", required = false) Integer threadMachineId) {
        return firstTestFormService.firstTestFormListByWorkOrderAndMachine(
                workOrderId, headMachineId, threadMachineId);
    }

    @GetMapping("/getFirstTestByMachine")
    public List<Map<String, Object>> firstTestFormsByMachine(
            @RequestParam(value = "headMachineId", required = false) Integer headMachineId,
            @RequestParam(value = "threadMachineId", required = false) Integer threadMachineId) {
        if (headMachineId == null && threadMachineId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "headMachineId or threadMachineId is required");
        }
        List<Map<String, Object>> firstTest = firstTestFormService.firstTestFormListByMachine(headMachineId, threadMachineId);
        return firstTest;
    }

}

