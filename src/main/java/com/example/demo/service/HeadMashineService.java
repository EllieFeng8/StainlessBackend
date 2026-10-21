package com.example.demo.service;

import com.example.demo.dao.HeadMachineDao;
import com.example.demo.dto.HeadMachineRequest;
import com.example.demo.model.HeadMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class HeadMashineService {

    @Autowired
    HeadMachineDao headMachineDao;

    public int addHeadMachine(HeadMachineRequest request) {
        if (request.headMachineCode() == null || request.headMachineName() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "headMachineCode and headMachineName are required");
        }
        validateName(request);
        return headMachineDao.addHeadMachine(request);
    }

    public int updateHeadMachine(int id, HeadMachineRequest request) {
        if (request.headMachineCode() == null && request.headMachineName() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "At least one of headMachineCode or headMachineName is required");
        }
        validateName(request);
        return headMachineDao.updateHeadMachine(id, request);
    }

    private void validateName(HeadMachineRequest request) {
        if (request.headMachineName() != null && request.headMachineName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "headMachineName must not be blank");
        }
    }

    public int deleteHeadMachine(int id) {
        return headMachineDao.deleteHeadMachine(id);
    }


    public List<HeadMachine> headMachineList() {
        return headMachineDao.getHeadMachines();
    }
    public Optional<HeadMachine> singleHeadMachine(int id) {
        return headMachineDao.getHeadMachine(id);
    }

}
