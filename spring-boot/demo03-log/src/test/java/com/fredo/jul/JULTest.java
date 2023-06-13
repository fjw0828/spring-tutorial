package com.fredo.jul;

import org.junit.Test;

import java.io.IOException;
import java.util.logging.*;


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

    /**
     * 日志级别
     *  OFF > SEVERE > WARNING > INFO > CONFIG > FINE > FINER > FINEST > ALL
     *  默认 INFO
     */
    @Test
    public void test03(){
        // 1.获取日志记录器对象
        Logger logger = Logger.getLogger("com.fredo.jul.JULTest");

        // 2.日志输出
        logger.severe("severe, 输出错误信息");
        logger.warning("warning, 警告信息");
        logger.info("info");
        logger.config("config");
        logger.fine("fine");
        logger.finer("finer");
        logger.finest("finest");
//        logger.log(Level.OFF,"关闭,最高级别");
//        logger.log(Level.SEVERE,"错误,输出错误信息");
//        logger.log(Level.WARNING,"警告,输出警告信息");
//        logger.log(Level.INFO,"普通信息,最高级别");
//        logger.log(Level.CONFIG,"debug信息");
//        logger.log(Level.FINE,"关闭,最高级别");
//        logger.log(Level.FINER,"关闭,最高级别");
//        logger.log(Level.FINEST,"关闭,最高级别");
//        logger.log(Level.ALL,"所有信息");

    }

    /**
     * 自定义日志级别
     */
    @Test
    public void test04() throws IOException {
        // 1.获取日志记录器对象
        Logger logger = Logger.getLogger("com.fredo.jul.JULTest");

        // 关闭系统默认配置
        logger.setUseParentHandlers(false);

        // 自定义
        // 创建ConsoleHandler
        Handler consoleHandler = new ConsoleHandler();
        // 创建简单格式转换对象
        SimpleFormatter formatter = new SimpleFormatter();
        // 关联
        consoleHandler.setFormatter(formatter);
        logger.addHandler(consoleHandler);
        // 设置日志级别
        logger.setLevel(Level.ALL);
        consoleHandler.setLevel(Level.ALL);

        // 还可以创建输出到文件的处理器
        Handler fileHandler = new FileHandler("jul.log");
        // 关联
        fileHandler.setFormatter(formatter);
        logger.addHandler(fileHandler);

        // 日志输出
        logger.severe("severe, 输出错误信息");
        logger.warning("warning, 警告信息");
        logger.info("info");
        logger.config("config");
        logger.fine("fine");
        logger.finer("finer");
        logger.finest("finest");
    }
}
