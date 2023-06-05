package com.fredo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        // XML 的方式
//        var ioc = new ClassPathXmlApplicationContext("ioc.xml");
        // SpringBoot 的方式(纯注解)
        var ioc = SpringApplication.run(Main.class, args);
        String[] names = ioc.getBeanDefinitionNames();
        for (String name : names) {
            System.out.println("容器中的Bean:" + name);
        }
    }
}