package com.fredo.custom.bean;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class Sheep {
    private long id;
    private String name;
    private int age;
}
