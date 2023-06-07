package com.fredo.controller;

import com.fredo.bean.Cat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scope")
public class ScopeController {
    @Autowired
    private Cat cat1;

    @Autowired
    private Cat cat2;

    /**
     *  结果验证:
     *  只有当 @Scope("prototype") 时, 下面方法返回 false, 默认返回 true
     */
    @GetMapping("/same")
    public boolean scope(){
        return cat1 == cat2;
    }

}
