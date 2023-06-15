package com.fredo.logback;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogbackTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(LogbackTest.class);
    @Test
    public void test01(){

        LOGGER.error("error");
        LOGGER.warn("warn");
        System.out.println("Async...");
        LOGGER.info("info");
        LOGGER.debug("debug");
        LOGGER.trace("trace");
        /**
         * 默认配置结果:
         * 16:44:23.866 [main] ERROR com.fredo.logback.LogbackTest -- error
         * 16:44:23.871 [main] WARN com.fredo.logback.LogbackTest -- warn
         * 16:44:23.871 [main] INFO com.fredo.logback.LogbackTest -- info
         */
        /**
         * 添加logback.xml配置后
         * 2023-06-11 18:44:17.866 [main] ERROR com.fredo.logback.LogbackTest - error
         * 2023-06-11 18:44:17.869 [main] WARN  com.fredo.logback.LogbackTest - warn
         * 2023-06-11 18:44:17.870 [main] INFO  com.fredo.logback.LogbackTest - info
         * 2023-06-11 18:44:17.870 [main] DEBUG com.fredo.logback.LogbackTest - debug
         *
         * 同时在文件中也有日志生成
         */

        // 测试异步打印
    }
}
