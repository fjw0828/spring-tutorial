package com.fredo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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

        application.run(args);
    }

    // 流式API
//    public static void main(String[] args) {
//        new SpringApplicationBuilder()
//                .sources(Main.class)
//                .child(Main.class)
//                .bannerMode(Banner.Mode.OFF)
//                .run(args);
//    }
}