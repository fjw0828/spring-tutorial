package com.other.controller;

import com.fredo.bean.Cat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/other")
public class ComponentScanController {
    @Autowired
    private Cat cat;

    /**
     *  结果验证:
     *  只有当 @ComponentScan("com.other") 时, 下面方法才能访问
     */
    @GetMapping("/pet")
    public String pet(){
        return "我的宠物:" + cat.getName();
    }
}
