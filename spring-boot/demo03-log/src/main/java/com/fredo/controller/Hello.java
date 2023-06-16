package com.fredo.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class Hello {

//    Logger log = LoggerFactory.getLogger(getClass());

    @GetMapping("/hello")
    public String hello(){
        log.info("Hello 日志测试");
        return "hello";
    }
}
