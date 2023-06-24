package com.fredo.controller;

import com.fredo.bean.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ContentNegotiationController {

    @GetMapping("/person")
    public Person person() {
        Person person = new Person();
        person.setId(1);
        person.setName("fredo");
        person.setAge(20);
        return person;
    }
}
