package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.dao.HeadMachineHistoryLogDao;
import com.example.demo.dto.HeadMachineHistoryLogRequest;
import com.example.demo.model.HeadMachineHistoryLog;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class HeadMachineHistoryLogService {
    private final HeadMachineHistoryLogDao dao;

    @Autowired
    public HeadMachineHistoryLogService(HeadMachineHistoryLogDao dao) {
        this.dao = dao;
    }

    public int addHeadMachineHistoryLog(HeadMachineHistoryLogRequest request) {
        MachineHistoryState.validate(request.state());
        if (request.machineId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "machineId is required");
        }
        return dao.addHeadMachineHistoryLog(request);
    }

    public List<HeadMachineHistoryLog> getHeadMachineHistoryLogs() {
        return dao.getHeadMachineHistoryLogs();
    }

    public Optional<HeadMachineHistoryLog> getHeadMachineHistoryLog(int id) {
        return dao.getHeadMachineHistoryLog(id);
    }

    public List<HeadMachineHistoryLog> getHeadMachineHistoryLogsByMachine(int machineId) {
        return dao.getHeadMachineHistoryLogsByMachine(machineId);
    }

    public void updateHeadMachineHistoryLog(int id, HeadMachineHistoryLogRequest request) {
        MachineHistoryState.validate(request.state());
        if (request.title() == null && request.time() == null && request.machineId() == null
                && request.log() == null && request.state() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one field is required");
        }
        if (dao.updateHeadMachineHistoryLog(id, request) == 0 && dao.getHeadMachineHistoryLog(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Machine history log not found");
        }
    }

    public void replaceHeadMachineHistoryLog(int id, HeadMachineHistoryLogRequest request) {
        MachineHistoryState.validate(request.state());
        if (request.machineId() == null || request.time() == null || request.state() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PUT requires machineId, time and state");
        }
        if (dao.replaceHeadMachineHistoryLog(id, request) == 0 && dao.getHeadMachineHistoryLog(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Machine history log not found");
        }
    }
}
