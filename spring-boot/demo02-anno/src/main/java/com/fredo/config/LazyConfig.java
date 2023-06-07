package com.fredo.config;

import com.fredo.bean.Dog;
import com.fredo.bean.Person;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class LazyConfig {

    @Bean("Odie")
//    @Lazy
    public Dog dog() {
        System.out.println("汪汪...");
        return new Dog();
    }

    /**
     * 懒加载结果验证:
     * 默认非懒加载, 会打印: 汪汪...
     * 加注解:@Lazy 变懒加载, 不会打印: 汪汪...
     */
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(LazyConfig.class);
        System.out.println("IOC容器创建完成");
    }
}
