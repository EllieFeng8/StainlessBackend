package com.example.demo.controller;

import com.example.demo.dto.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/getTest")
    public Test test(){
        Test abc = new Test();
        abc.setMessage("test");
        return abc;
    }
}
