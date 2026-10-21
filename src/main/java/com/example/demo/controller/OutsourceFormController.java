package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.OutsourceFormRequest;
import com.example.demo.model.OutsourceForm;
import com.example.demo.service.OutsourceFormService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/outsource")
public class OutsourceFormController {
    private final OutsourceFormService service;

    public OutsourceFormController(OutsourceFormService service) {
        this.service = service;
    }

    @PostMapping("/addOutsource")
    public ResponseEntity<ApiResponse<Void>> addOutsourceForm(@RequestBody OutsourceFormRequest request) {
        if (service.addOutsourceForm(request) != 1) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create outsource form"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("OK"));
    }

    @PatchMapping("/updateOutsource/{id}")
    public ResponseEntity<ApiResponse<Void>> updateOutsourceForm(
            @PathVariable("id") int id, @RequestBody OutsourceFormRequest request) {
        service.updateOutsourceForm(id, request);
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    @DeleteMapping("/deleteOutsource/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOutsourceForm(@PathVariable("id") int id) {
        service.deleteOutsourceForm(id);
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    @GetMapping("/getOutsource")
    public List<OutsourceForm> getOutsourceForms() {
        return service.getOutsourceForms();
    }

    @GetMapping("/getOutsource/{id}")
    public ResponseEntity<OutsourceForm> getOutsourceForm(@PathVariable("id") int id) {
        return ResponseEntity.of(service.getOutsourceForm(id));
    }

    @GetMapping("/getOutsourceByWorkOrder")
    public List<OutsourceForm> getOutsourceFormsByWorkOrder(@RequestParam("workOrderId") int workOrderId) {
        return service.getOutsourceFormsByWorkOrder(workOrderId);
    }
}
