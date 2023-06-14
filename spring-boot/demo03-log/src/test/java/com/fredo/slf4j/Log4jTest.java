package com.fredo.slf4j;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * 桥接器演示
 *  假设以前使用的日志是:Log4j
 *  现在要换成Logback(SpringBoot 默认自带,无需引入依赖)
 */
public class Log4jTest {


    @Test
    public void test(){
        Logger logger = Logger.getLogger(Log4jTest.class);

        logger.info("Hello Log4j");
    }
    /**
     * 使用log4j时,打印信息:
     *  2023-06-11 21:52:29.039 Hello Log4j
     *
     * 使用log4j和slf4j的桥接器时,打印信息:
     *  22:10:38.352 [main] INFO com.fredo.slf4j.Log4jTest -- Hello Log4j
     */
}
