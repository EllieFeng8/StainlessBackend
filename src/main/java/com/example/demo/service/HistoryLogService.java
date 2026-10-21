package com.example.demo.service;

import com.example.demo.dao.HistoryLogDao;
import com.example.demo.dto.HistoryLogRequest;
import com.example.demo.model.HistoryLog;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class HistoryLogService {
    private final HistoryLogDao dao;

    public HistoryLogService(HistoryLogDao dao) {
        this.dao = dao;
    }

    public int addHistoryLog(HistoryLogRequest request) {
        if (request.workOrderCode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "workOrderCode is required");
        }
        return dao.addHistoryLog(request);
    }

    public List<HistoryLog> getHistoryLogs() {
        return dao.getHistoryLogs();
    }

    public Optional<HistoryLog> getHistoryLog(int id) {
        return dao.getHistoryLog(id);
    }

    public List<HistoryLog> getHistoryLogsByWorkOrder(int workOrderCode) {
        return dao.getHistoryLogsByWorkOrder(workOrderCode);
    }
}
