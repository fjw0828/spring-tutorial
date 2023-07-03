package com.fredo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

    /**
     * 测试MVC的错误处理机制
     * 默认情况下--不处理错误:
     * 浏览器返回白页,因为请求头中: Accept:text/html
     * 移动端postman返回JSON.因为请求头中: (Accept:* 所有类型,优先JSON)
     * 自己处理错误: handleException()
     */
    @GetMapping("testError")
    public String testError() {

        // 错误出现
        int i = 12 / 0;

        return "testError";
    }

    /**
     * 自定义处理所有错误
     * @ExceptionHandler 可以标识一个方法, 默认只能处理这个类发生的指定错误
     * @ControllerAdvice AOP思想, 可以统一处理所有方法, 如 GlobalExceptionHandler.java
     */
//    @ResponseBody
//    @ExceptionHandler(Exception.class)
//    public String handleException(Exception e) {
//
//        return "错误已发生,原因:" + e.getMessage();
//    }
}
