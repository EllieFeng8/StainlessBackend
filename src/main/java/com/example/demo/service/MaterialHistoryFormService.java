package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.dao.MaterialHistoryFormDao;
import com.example.demo.dto.MaterialHistoryFormRequest;
import com.example.demo.model.MaterialHistoryForm;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MaterialHistoryFormService {
    private final MaterialHistoryFormDao dao;

    @Autowired
    public MaterialHistoryFormService(MaterialHistoryFormDao dao) {
        this.dao = dao;
    }

    public int addMaterialHistoryForm(MaterialHistoryFormRequest request) {
        if (request.workOrderCode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "workOrderCode is required");
        }
        return dao.addMaterialHistoryForm(request);
    }

    public void updateMaterialHistoryForm(int id, MaterialHistoryFormRequest request) {
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id must be positive");
        }
        Map<String, Object> params = request.toUpdateParameters();
        if (params.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one non-null field is required");
        }
        int updated = dao.updateMaterialHistoryForm(id, params);
        if (updated == 0 && !dao.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Material history form not found");
        }
    }

    public List<MaterialHistoryForm> getMaterialHistoryForms() {
        return dao.getMaterialHistoryForms();
    }

    public Optional<MaterialHistoryForm> getMaterialHistoryForm(String materialCode) {
        return dao.getMaterialHistoryForm(materialCode);
    }

    public List<MaterialHistoryForm> getMaterialHistoryFormsByWorkOrder(int workOrderCode) {
        return dao.getMaterialHistoryFormsByWorkOrder(workOrderCode);
    }
}
