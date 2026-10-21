package com.example.demo.service;

import com.example.demo.dao.ThreadMachineHistoryLogDao;
import com.example.demo.dto.ThreadMachineHistoryLogRequest;
import com.example.demo.model.ThreadMachineHistoryLog;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ThreadMachineHistoryLogService {
    private final ThreadMachineHistoryLogDao dao;

    public ThreadMachineHistoryLogService(ThreadMachineHistoryLogDao dao) {
        this.dao = dao;
    }

    public int addThreadMachineHistoryLog(ThreadMachineHistoryLogRequest request) {
        if (request.machineId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "machineId is required");
        }
        return dao.addThreadMachineHistoryLog(request);
    }

    public List<ThreadMachineHistoryLog> getThreadMachineHistoryLogs() {
        return dao.getThreadMachineHistoryLogs();
    }

    public Optional<ThreadMachineHistoryLog> getThreadMachineHistoryLog(int id) {
        return dao.getThreadMachineHistoryLog(id);
    }

    public List<ThreadMachineHistoryLog> getThreadMachineHistoryLogsByMachine(int machineId) {
        return dao.getThreadMachineHistoryLogsByMachine(machineId);
    }
}
