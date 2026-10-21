package com.example.demo.service;

import com.example.demo.dao.WorkOrderDao;
import com.example.demo.model.WorkOrder;
import com.example.demo.dto.WorkOrderRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WorkOrderService {

    @Autowired
    WorkOrderDao workOrderDao;

    public int deleteWorkOrder(int id) {
        return workOrderDao.deleteWorkOrder(id);
    }

    public int addWorkOrder(WorkOrderRequest request) {
        if (request.workOrder() == null || request.startAt() == null
                || request.endAt() == null || request.updateQuantity() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "workOrder, startAt, endAt and updateQuantity are required");
        }
        return workOrderDao.addWorkOrder(request);
    }

    public void updateWorkOrder(int id, WorkOrderRequest request) {
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive");
        }
        if (request.workOrder() != null) {
            WorkOrder existing = workOrderDao.getWorkOrder(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Work order not found"));
            if (request.workOrder() != existing.getWorkOrder()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "workOrder cannot be changed");
            }
        }
        Map<String, Object> fields = request.toUpdateParameters();
        if (fields.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one non-null field is required");
        }
        int updated = workOrderDao.updateWorkOrder(id, fields);
        if (updated == 0 && !workOrderDao.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Work order not found");
        }
    }

    public List<WorkOrder> workOrderList() {
        return workOrderDao.getWorkOrders();
    }

    public Optional<WorkOrder> getWorkOrder(int id) {
        return workOrderDao.getWorkOrder(id);
    }

    public Optional<WorkOrder> getWorkOrderByCode(int workOrderCode) {
        return workOrderDao.getWorkOrderByCode(workOrderCode);
    }
}
