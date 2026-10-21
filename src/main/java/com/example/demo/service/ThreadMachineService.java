package com.example.demo.service;

import com.example.demo.dao.ThreadMachineDao;
import com.example.demo.dto.ThreadMachineRequest;
import com.example.demo.model.ThreadMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ThreadMachineService {

    @Autowired
    ThreadMachineDao threadMachineDao;

    public int addThreadMachine(ThreadMachineRequest request) {
        validateRequest(request);
        return threadMachineDao.addThreadMachine(request);
    }

    public int updateThreadMachine(int id, ThreadMachineRequest request) {
        validateRequest(request);
        return threadMachineDao.updateThreadMachine(id, request);
    }

    private void validateRequest(ThreadMachineRequest request) {
        if (request.threadRollingMachineCode() == null || request.threadRollingMachineCode().isBlank()
                || request.threadRollingMachineName() == null || request.threadRollingMachineName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "threadRollingMachineCode and threadRollingMachineName are required");
        }
    }

    public int deleteThreadMachine(int id) {
        return threadMachineDao.deleteThreadMachine(id);
    }

    public List<ThreadMachine> threadMachineList() {
        return threadMachineDao.getThreadMachines();
    }

    public ThreadMachine singleThreadMachine(int id) {
        return threadMachineDao.getThreadMachine(id);
    }
}
