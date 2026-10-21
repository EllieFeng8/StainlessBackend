package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.HeadMachineRequest;
import com.example.demo.model.HeadMachine;
import com.example.demo.service.HeadMashineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/headMachine")
public class HeadMachineController {

    @Autowired
    HeadMashineService headMashineService;

    @PostMapping("/addMachine")
    public ResponseEntity<ApiResponse<Void>> addMachine(@RequestBody HeadMachineRequest request) {
        if (headMashineService.addHeadMachine(request) != 1) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create head machine"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("OK"));
    }

    @PatchMapping("/updateMachine/{id}")
    public ResponseEntity<ApiResponse<Void>> updateMachine(@PathVariable("id") int id,
                                                          @RequestBody HeadMachineRequest request) {
        if (headMashineService.updateHeadMachine(id, request) == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(404, "Head machine not found"));
        }
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    @DeleteMapping("/deleteMachine/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMachine(@PathVariable("id") int id) {
        if (headMashineService.deleteHeadMachine(id) == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(404, "Head machine not found"));
        }
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    @GetMapping("/getMachines")
    public List<HeadMachine> headId(){
        List<HeadMachine> da = headMashineService.headMachineList();
        return da;
    }

    @GetMapping("/getMachine/{id}")
    public ResponseEntity<?> headId(@PathVariable("id") int id){
        return headMashineService.singleHeadMachine(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.fail(404, "打頭機台不存在")));
    }
}
