package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.dao.WorkOrderThreadingMachineDao;
import com.example.demo.dto.ThreadingProductionAssignmentResponse;
import com.example.demo.dto.WorkOrderThreadingMachineRequest;
import com.example.demo.model.WorkOrderThreadingMachine;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class WorkOrderThreadingMachineService {
    private final WorkOrderThreadingMachineDao dao;

    @Autowired
    public WorkOrderThreadingMachineService(WorkOrderThreadingMachineDao dao) {
        this.dao = dao;
    }

    public int addWorkOrderThreadingMachine(WorkOrderThreadingMachineRequest request) {
        if (request.productionStatus() != null || request.startedAt() != null || request.endAt() != null
                || request.materialLoss() != null || request.remainingMaterial() != null
                || (request.orderClosed() != null && request.orderClosed() != 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "請使用 production 流程 API 更新生產及結單欄位");
        }
        validate(request);
        return dao.addWorkOrderThreadingMachine(request);
    }

    public void patchWorkOrderThreadingMachine(int id, WorkOrderThreadingMachineRequest request) {
        if (request.productionStatus() != null || request.startedAt() != null || request.endAt() != null
                || request.materialLoss() != null || request.remainingMaterial() != null
                || (request.orderClosed() != null && request.orderClosed() != 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "請使用 production 流程 API 更新生產及結單欄位");
        }
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive");
        }
        if (java.util.stream.Stream.of(
                request.workOrderCode(), request.machineId(), request.materialId(), request.memberId(),
                request.updateMachine(), request.changeReason(), request.targetQuantity(),
                request.targetUpOffsetQuantity(), request.targetDownOffsetQuantity(),
                request.currentQuantity(), request.previousQuantity(), request.updateQuantity(),
                request.quantityChangeReason(), request.startedAt(), request.endAt(),
                request.productionStatus(), request.reason(), request.orderClosed(), request.remark(),
                request.materialLoss(), request.remainingMaterial(), request.oee(),
                request.employeeCode(), request.reviewer(), request.approved(), request.opinion())
                .allMatch(java.util.Objects::isNull)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "At least one non-null field is required");
        }
        int updated = dao.patchWorkOrderThreadingMachine(id, request);
        if (updated == 0 && dao.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "已開工派工請使用 production 流程 API");
        }
        if (updated == 0 && !dao.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Work order threading machine not found");
        }
    }

    public int deleteWorkOrderThreadingMachine(int id) {
        int deleted = dao.deleteWorkOrderThreadingMachine(id);
        if (deleted == 0 && dao.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "已開工或結單派工不可刪除");
        }
        return deleted;
    }

    public List<WorkOrderThreadingMachine> getWorkOrderThreadingMachines() {
        return dao.getWorkOrderThreadingMachines();
    }

    public List<WorkOrderThreadingMachine> getWorkOrderThreadingMachines(
            Integer workOrderId, Integer threadMachineId, Integer materialId) {
        return dao.getWorkOrderThreadingMachines(workOrderId, threadMachineId, materialId);
    }

    public ThreadingProductionAssignmentResponse verifyProductionAssignment(
            int workOrderId, int threadMachineId, int materialId) {
        if (workOrderId <= 0 || threadMachineId <= 0 || materialId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "workOrderId、threadMachineId、materialId 必須為正整數");
        }
        List<WorkOrderThreadingMachine> matches =
                dao.getWorkOrderThreadingMachines(workOrderId, threadMachineId, materialId);
        if (matches.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "工單、機台與物料對應關係不存在");
        }
        if (matches.size() != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "符合多筆派工，請人工確認");
        }
        return new ThreadingProductionAssignmentResponse(matches.getFirst().getId());
    }

    public Optional<WorkOrderThreadingMachine> getWorkOrderThreadingMachine(int id) {
        return dao.getWorkOrderThreadingMachine(id);
    }

    private void validate(WorkOrderThreadingMachineRequest request) {
        if (request.workOrderCode() == null || request.machineId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "workOrderCode and machineId are required");
        }
    }
}
