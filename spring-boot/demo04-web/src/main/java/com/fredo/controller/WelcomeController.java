package com.fredo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WelcomeController {

    /**
     * @PathParam: 获取请求行中的参数.servlet提供
     * @PathVariable: 获取请求行中的参数.是Spring提供
     * @RequestParam: 获取请求行中和请求体中的参数, 可以获取复杂参数, 文件等.是Spring提供
     */
    @GetMapping("hello")
    public String hello(@RequestParam("name") String name, Model model) {

        model.addAttribute("msg", name);
        return "welcome";
    }
}
