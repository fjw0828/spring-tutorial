package com.fredo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class Main {

    @RequestMapping("/")
    String home() {
        return "Hello Fredo!";
    }

    public static void main(String[] args) {
        // jdk10 的新特性:var 可用于局部变量类型自动推断
        var ioc = SpringApplication.run(Main.class, args);
        String[] names = ioc.getBeanDefinitionNames();

        for (String name : names) {
            System.out.println("容器中的Bean:" + name);
        }
    }
}