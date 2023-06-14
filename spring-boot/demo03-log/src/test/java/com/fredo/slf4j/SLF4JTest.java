package com.fredo.slf4j;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SLF4JTest {

    public static final Logger LOGGER = LoggerFactory.getLogger(SLF4JTest.class);
    @Test
    public void test01(){

        LOGGER.error("error");
        LOGGER.warn("warn");
        LOGGER.info("info");
        LOGGER.debug("debug");
        LOGGER.trace("trace");

        try {
            int i = 1/0;
        }catch (Exception e){
            LOGGER.error("出现异常:"+e);
        }

        /**
         * SLF4J + SLF4J-Simple 组合结果(红色字体):
         * [main] ERROR com.fredo.slf4j.SLF4JTest - error
         * [main] WARN com.fredo.slf4j.SLF4JTest - warn
         * [main] INFO com.fredo.slf4j.SLF4JTest - info
         * [main] ERROR com.fredo.slf4j.SLF4JTest - 出现异常:java.lang.ArithmeticException: / by zero
         *
         * 注意:需要提前排除:logback.SpringBoot默认的日志实现
         * <dependency>
         *     <groupId>org.springframework.boot</groupId>
         *     <artifactId>spring-boot-starter-web</artifactId>
         *     <exclusions>
         *         <exclusion>
         *             <groupId>ch.qos.logback</groupId>
         *             <artifactId>logback-classic</artifactId>
         *         </exclusion>
         *     </exclusions>
         * </dependency>
         */

        /**
         * SLF4J + logback 组合结果(黑色字体):
         * 10:24:32.167 [main] ERROR com.fredo.slf4j.SLF4JTest -- error
         * 10:24:32.172 [main] WARN com.fredo.slf4j.SLF4JTest -- warn
         * 10:24:32.172 [main] INFO com.fredo.slf4j.SLF4JTest -- info
         * 10:24:32.172 [main] ERROR com.fredo.slf4j.SLF4JTest -- 出现异常:java.lang.ArithmeticException: / by zero
         *
         * 注意:注释slf4j-simple, 并且不要排除SpringBoot内置的日志实现:logback
         */

        /**
         * SLF4J + slf4j-nop 组合结果:
         *  无任何打印(slf4j-nop关闭了日志打印)
         *
         *  注意:提前排除SpringBoot默认自带的logback日志实现.
         */

        /**
         * SLF4J + log4j 组合结果:
         * 2023-06-14 10:38:19.738 error
         * 2023-06-14 10:38:19.739 warn
         * 2023-06-14 10:38:19.739 info
         * 2023-06-14 10:38:19.739 debug
         * 2023-06-14 10:38:19.740 trace
         * 2023-06-14 10:38:19.741 出现异常:java.lang.ArithmeticException: / by zero
         *
         * 注意:需要适配器:slf4j-log4j12以及log4j对应的配置文件
         */

        /**
         * SLF4J + JUL 组合结果(红色字体):
         * 6月 14, 2023 10:42:46 上午 com.fredo.slf4j.SLF4JTest test01
         * SEVERE: error
         * 6月 14, 2023 10:42:46 上午 com.fredo.slf4j.SLF4JTest test01
         * WARNING: warn
         * 6月 14, 2023 10:42:46 上午 com.fredo.slf4j.SLF4JTest test01
         * INFO: info
         * 6月 14, 2023 10:42:46 上午 com.fredo.slf4j.SLF4JTest test01
         * SEVERE: 出现异常:java.lang.ArithmeticException: / by zero
         *
         * 注意:JDK默认自带无需依赖,但需要对应的适配器:slf4j-jdk14
         */
        /////////////////////////////////////////////////////////////////
        /**
         * 以上都是SLF4J和不同的日志组合实现,
         * 可以发现不同的日志框架打印的日志格式不一!!!
         * 这是因为SLF4J为了适配不同的日志框架提供了不同的适配器,具体的日志记录还是交给对应的日志框架实现的
         *
         * SLF4J还提供了桥接器!!!
         * 桥接器则是由SLF4J实现一套对应的日志框架的API,日志的记录打印完全由这套API实现,可以做到格式统一.
         */
    }
}
