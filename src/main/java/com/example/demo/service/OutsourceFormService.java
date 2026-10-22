package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.dao.OutsourceFormDao;
import com.example.demo.dto.OutsourceFormRequest;
import com.example.demo.model.OutsourceForm;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OutsourceFormService {
    private final OutsourceFormDao dao;

    @Autowired
    public OutsourceFormService(OutsourceFormDao dao) {
        this.dao = dao;
    }

    public int addOutsourceForm(OutsourceFormRequest request) {
        if (request.workOrderId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "workOrderId is required");
        }
        return dao.addOutsourceForm(request);
    }

    public void updateOutsourceForm(int id, OutsourceFormRequest request) {
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive");
        }
        Map<String, Object> params = request.toUpdateParameters();
        if (params.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one non-null field is required");
        }
        int updated = dao.updateOutsourceForm(id, params);
        if (updated == 0 && !dao.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Outsource form not found");
        }
    }

    public void deleteOutsourceForm(int id) {
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive");
        }
        if (dao.deleteOutsourceForm(id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Outsource form not found");
        }
    }

    public List<OutsourceForm> getOutsourceForms() {
        return dao.getOutsourceForms();
    }

    public Optional<OutsourceForm> getOutsourceForm(int id) {
        return dao.getOutsourceForm(id);
    }

    public List<OutsourceForm> getOutsourceFormsByWorkOrder(int workOrderId) {
        return dao.getOutsourceFormsByWorkOrder(workOrderId);
    }
}
