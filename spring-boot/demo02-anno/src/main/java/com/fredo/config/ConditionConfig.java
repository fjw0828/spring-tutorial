package com.fredo.config;

import com.fredo.bean.Cat;
import com.fredo.bean.Dog;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConditionConfig {

    @Bean("欧迪")
    @ConditionalOnBean(value = Cat.class)
    public Dog dog() {
        System.out.println("欧迪在容器的条件是: 猫在容器");
        return new Dog();
    }

    /**
     * 条件注册结果验证:
     * 猫在容器,则欧迪也在容器
     */
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(ConditionConfig.class);
        System.out.println("IOC容器创建完成");
    }
}
