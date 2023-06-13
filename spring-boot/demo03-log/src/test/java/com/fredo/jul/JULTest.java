package com.fredo.jul;

import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.Logger;


public class JULTest {

    @Test
    public void test01(){
        // 1.获取日志记录器对象
        Logger logger = Logger.getLogger("com.fredo.jul.JULTest");
        // 2.日志输出
        logger.info("Hello JUL");
    }

    /**
     * 通用方法记录日志
     *  可设置日志级别
     */
    @Test
    public void test02(){
        // 1.获取日志记录器对象
        Logger logger = Logger.getLogger("com.fredo.jul.JULTest");
        // 2.日志输出
        logger.log(Level.INFO,"通用方法记录日志");

        // 占位符的方式输出日志--无需字符串拼接,性能高
        String name = "fredo";
        int age = 18;

        logger.log(Level.INFO, "用户信息:{0},{1}", new Object[]{name, age});
    }
}
