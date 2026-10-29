package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.MaterialHistoryFormRequest;
import com.example.demo.dto.MaterialHistoryMoldLimitsRequest;
import com.example.demo.model.MaterialHistoryForm;
import com.example.demo.service.MaterialHistoryFormService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/materialHistory")
public class MaterialHistoryFormController {
    private final MaterialHistoryFormService service;

    @Autowired
    public MaterialHistoryFormController(MaterialHistoryFormService service) {
        this.service = service;
    }

    @PostMapping("/addMaterialHistory")
    public ResponseEntity<ApiResponse<Void>> addMaterialHistoryForm(@RequestBody MaterialHistoryFormRequest request) {
        if (service.addMaterialHistoryForm(request) != 1) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create material history form"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("OK"));
    }

    @PatchMapping("/updateMaterialHistory/{id}")
    public ResponseEntity<ApiResponse<Void>> updateMaterialHistoryForm(
            @PathVariable("id") int id, @RequestBody MaterialHistoryFormRequest request) {
        service.updateMaterialHistoryForm(id, request);
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    @PatchMapping("/updateMoldLimits")
    public ResponseEntity<ApiResponse<Void>> updateMoldLimits(
            @RequestBody MaterialHistoryMoldLimitsRequest request) {
        service.updateMoldLimits(request);
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    @GetMapping("/getMaterialHistory")
    public List<MaterialHistoryForm> getMaterialHistoryForms() {
        return service.getMaterialHistoryForms();
    }

    @GetMapping("/getMaterialHistory/{materialCode}")
    public ResponseEntity<MaterialHistoryForm> getMaterialHistoryForm(@PathVariable("materialCode") String materialCode) {
        return ResponseEntity.of(service.getMaterialHistoryForm(materialCode));
    }

    @GetMapping("/getMaterialHistoryByWorkOrder")
    public List<MaterialHistoryForm> getMaterialHistoryFormsByWorkOrder(
            @RequestParam("workOrderCode") int workOrderCode) {
        return service.getMaterialHistoryFormsByWorkOrder(workOrderCode);
    }
}
