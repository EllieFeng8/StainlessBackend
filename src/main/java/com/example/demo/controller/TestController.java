package com.example.demo.controller;

import com.example.demo.dto.Test;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    private final BuildProperties buildProperties;

    public TestController(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @GetMapping("/getTest")
    public Test test(){
        Test abc = new Test();
        abc.setMessage("test");
        abc.setVersion(buildProperties.getVersion());
        return abc;
    }
}
