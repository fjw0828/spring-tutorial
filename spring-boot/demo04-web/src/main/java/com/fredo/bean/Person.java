package com.fredo.bean;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@JacksonXmlRootElement  // 可以写出为xml文档
@Data
public class Person {
    private long id;
    private String name;
    private int age;
}
