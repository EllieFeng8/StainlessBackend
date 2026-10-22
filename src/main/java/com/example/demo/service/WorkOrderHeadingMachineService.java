package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.dao.WorkOrderHeadingMachineDao;
import com.example.demo.dto.HeadingProductionAssignmentResponse;
import com.example.demo.dto.WorkOrderHeadingMachineRequest;
import com.example.demo.model.WorkOrderHeadingMachine;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class WorkOrderHeadingMachineService {
    private final WorkOrderHeadingMachineDao dao;

    @Autowired
    public WorkOrderHeadingMachineService(WorkOrderHeadingMachineDao dao) {
        this.dao = dao;
    }

    public int addWorkOrderHeadingMachine(WorkOrderHeadingMachineRequest request) {
        if (request.productionStatus() != null || request.startedAt() != null || request.endAt() != null
                || request.materialLoss() != null || request.remainingMaterial() != null
                || (request.orderClosed() != null && request.orderClosed() != 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "請使用 production 流程 API 更新生產及結單欄位");
        }
        validate(request);
        return dao.addWorkOrderHeadingMachine(request);
    }

    public void patchWorkOrderHeadingMachine(int id, WorkOrderHeadingMachineRequest request) {
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
        int updated = dao.patchWorkOrderHeadingMachine(id, request);
        if (updated == 0 && dao.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "已開工派工請使用 production 流程 API");
        }
        if (updated == 0 && !dao.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Work order heading machine not found");
        }
    }

    public int deleteWorkOrderHeadingMachine(int id) {
        int deleted = dao.deleteWorkOrderHeadingMachine(id);
        if (deleted == 0 && dao.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "已開工或結單派工不可刪除");
        }
        return deleted;
    }

    public List<WorkOrderHeadingMachine> getWorkOrderHeadingMachines() {
        return dao.getWorkOrderHeadingMachines();
    }

    public List<WorkOrderHeadingMachine> getWorkOrderHeadingMachines(
            Integer workOrderId, Integer headMachineId, Integer materialId) {
        return dao.getWorkOrderHeadingMachines(workOrderId, headMachineId, materialId);
    }

    public HeadingProductionAssignmentResponse verifyProductionAssignment(
            int workOrderId, int headMachineId, int materialId) {
        if (workOrderId <= 0 || headMachineId <= 0 || materialId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "workOrderId、headMachineId、materialId 必須為正整數");
        }
        List<WorkOrderHeadingMachine> matches =
                dao.getWorkOrderHeadingMachines(workOrderId, headMachineId, materialId);
        if (matches.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "工單、機台與物料對應關係不存在");
        }
        if (matches.size() != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "符合多筆派工，請人工確認");
        }
        return new HeadingProductionAssignmentResponse(matches.getFirst().getId());
    }

    public Optional<WorkOrderHeadingMachine> getWorkOrderHeadingMachine(int id) {
        return dao.getWorkOrderHeadingMachine(id);
    }

    private void validate(WorkOrderHeadingMachineRequest request) {
        if (request.workOrderCode() == null || request.machineId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "workOrderCode and machineId are required");
        }
    }
}
