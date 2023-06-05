package com.fredo.config;

import com.fredo.bean.Cat;
import com.fredo.bean.Person;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // 这是一个配置类, 替代以前的xml配置文件
public class AppConfig {
    @Bean // 替代以前xml中的bean标签
    public Person person(){
        var person = new Person();
        person.setId(1);
        person.setName("fred0");
        return person;
    }
    @Bean
    public Cat cat(){
        var cat = new Cat();
        cat.setId(1);
        cat.setName("fred0");
        return cat;
    }
}
