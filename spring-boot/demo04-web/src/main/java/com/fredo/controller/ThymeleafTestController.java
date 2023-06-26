package com.fredo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ThymeleafTestController {

    @GetMapping("test")
    public String hello(Model model) {

        String data = "<span style='color:red'>Fredo</span>";
        model.addAttribute("data", data);
        return "thymeleaf";
    }
}
