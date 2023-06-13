package com.fredo.jcl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

public class JCLTest {

    @Test
    public void test01(){

        // 1.获取日志记录器对象
        Log log = LogFactory.getLog(JCLTest.class);
        // 2.日志记录输出
        log.info("Hello JCL");
        /**
         * 结果记录:
         * 没有其他依赖时:
         *  输出:15:39:25.767 [main] INFO com.fredo.jcl.JCLTest -- Hello JCL
         */
    }
}
