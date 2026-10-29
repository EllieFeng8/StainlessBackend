package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;

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

    @Autowired
    public ThreadMachineHistoryLogService(ThreadMachineHistoryLogDao dao) {
        this.dao = dao;
    }

    public int addThreadMachineHistoryLog(ThreadMachineHistoryLogRequest request) {
        MachineHistoryState.validate(request.state());
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

    public void updateThreadMachineHistoryLog(int id, ThreadMachineHistoryLogRequest request) {
        MachineHistoryState.validate(request.state());
        if (request.title() == null && request.time() == null && request.machineId() == null
                && request.log() == null && request.state() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one field is required");
        }
        if (dao.updateThreadMachineHistoryLog(id, request) == 0 && dao.getThreadMachineHistoryLog(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Machine history log not found");
        }
    }

    public void replaceThreadMachineHistoryLog(int id, ThreadMachineHistoryLogRequest request) {
        MachineHistoryState.validate(request.state());
        if (request.machineId() == null || request.time() == null || request.state() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PUT requires machineId, time and state");
        }
        if (dao.replaceThreadMachineHistoryLog(id, request) == 0 && dao.getThreadMachineHistoryLog(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Machine history log not found");
        }
    }
}
