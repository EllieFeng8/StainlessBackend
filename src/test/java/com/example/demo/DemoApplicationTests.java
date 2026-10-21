package com.example.demo;

import com.example.demo.dao.HeadMachineDao;
import com.example.demo.model.HeadMachine;
import com.example.demo.model.FirstTestForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class DemoApplicationTests {

    @Autowired
    HeadMachineDao headMachineDao;


    @Test
    void contextLoads() {
    }
    @Test
    void testDao(){
        List<HeadMachine> data = headMachineDao.getHeadMachines();
        System.out.println("D");
    }

}
