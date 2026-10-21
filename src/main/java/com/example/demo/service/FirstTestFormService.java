package com.example.demo.service;

import com.example.demo.dao.FirstTestFormDao;
import com.example.demo.model.FirstTestForm;
import com.example.demo.dto.FirstTestFormRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FirstTestFormService {
    @Autowired
    FirstTestFormDao firstTestFormDao;

    @org.springframework.transaction.annotation.Transactional
    public FirstTestFormDao.SaveResult addFirstTestForm(FirstTestFormRequest request) {
        if (request.headMachineId() == null && request.threadMachineId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "headMachineId or threadMachineId is required");
        }
        if (request.workOrderCode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "work_order_code is required");
        }
        java.time.LocalDateTime dateTime = java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Taipei"))
                .truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        if (!firstTestFormDao.lockWorkOrder(request.workOrderCode())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "工單不存在");
        }
        return firstTestFormDao.saveFirstTestForm(request, dateTime);
    }
    public void updateFirstTestForm(int id, FirstTestFormRequest request) {
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive");
        }
        Map<String, Object> params = request.toUpdateParameters();
        if (params.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one non-null field is required");
        }
        int updated = firstTestFormDao.updateFirstTestForm(id, params);
        if (updated == 0 && !firstTestFormDao.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "First test form not found");
        }
    }

    public List<FirstTestForm> firstTestFormList(){return firstTestFormDao.getFirstTestForm();}

    public List<Map<String, Object>> firstTestFormListByWorkOrderAndMachine(
            int workOrderId, Integer headMachineId, Integer threadMachineId) {
        if (headMachineId == null && threadMachineId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "headMachineId or threadMachineId is required");
        }
        if (workOrderId <= 0 || (headMachineId != null && headMachineId <= 0)
                || (threadMachineId != null && threadMachineId <= 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IDs must be positive");
        }
        return firstTestFormDao.getFirstTestFormByWorkOrderAndMachine(
                workOrderId, headMachineId, threadMachineId);
    }

    public List<Map<String, Object>> firstTestFormListByMachine(Integer headMachineId, Integer threadMachineId) {
        return firstTestFormDao.getFirstTestFormByMachine(headMachineId, threadMachineId);
    }

}

