package com.fredo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ThymeleafTestController {

    @GetMapping("test")
    public String hello(Model model) {

        String name = "<span style='color:red'>Fredo</span>";
        model.addAttribute("name", name);

        String style = "text-decoration: underline wavy red;";
        model.addAttribute("style", style);

        return "thymeleaf";
    }
}
