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
        LOGGER.info("info");
        LOGGER.debug("debug");
        LOGGER.trace("trace");
        /**
         * 结果:
         * 16:44:23.866 [main] ERROR com.fredo.logback.LogbackTest -- error
         * 16:44:23.871 [main] WARN com.fredo.logback.LogbackTest -- warn
         * 16:44:23.871 [main] INFO com.fredo.logback.LogbackTest -- info
         */
    }
}
