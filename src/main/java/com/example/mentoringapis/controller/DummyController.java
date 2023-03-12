package com.example.mentoringapis.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("api/dummy")
public class DummyController {

    @GetMapping(value = "helloWorld")
    public String helloWorld(){
        return "Hello world!";
    }
}
