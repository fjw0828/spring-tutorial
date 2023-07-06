package com.fredo;

import com.fredo.custom.bean.Cat;
import com.fredo.custom.bean.Dog;
import com.fredo.custom.bean.Pig;
import com.fredo.custom.bean.Sheep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Main.class);

        // 关闭启动日志(注意:配置文件的优先级大于代码优先级!!!)
        application.setLogStartupInfo(false);

        // 开启延迟初始化(默认关闭,不建议开启)
        application.setLazyInitialization(true);

        // 关闭Banner,默认值:CONSOLE
//        application.setBannerMode(Banner.Mode.OFF);

        ConfigurableApplicationContext context = application.run(args);

        // 多环境验证:
        // 默认情况下,容器启动后下面的组件都会存在
        // 下面使用注解 @Profile({"..."})标识不同的环境
        // 默认的环境是: 'default'
        try {
            Cat cat = context.getBean(Cat.class); // @Profile({"dev"})
            log.info("cat:{}", cat);
        } catch (Exception e) {

        }
        try {
            Dog dog = context.getBean(Dog.class); // @Profile({"test"})
            log.info("dog:{}", dog);
        } catch (Exception e) {

        }
        try {
            Pig pig = context.getBean(Pig.class); // @Profile({"prod"})
            log.info("pig:{}", pig);
        } catch (Exception e) {

        }
        Sheep sheep = context.getBean(Sheep.class);// 不标识,即所有环境都有
        log.info("sheep:{}", sheep);
    }

    // 流式API
//    public static void main(String[] args) {
//        ConfigurableApplicationContext context = new SpringApplicationBuilder()
//                .sources(Main.class)
//                .child(Main.class)
//                .bannerMode(Banner.Mode.OFF)
//                .run(args);
//        context.getBean()
//    }
}