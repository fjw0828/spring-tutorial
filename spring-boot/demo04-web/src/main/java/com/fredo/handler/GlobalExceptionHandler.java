package com.fredo.handler;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice // 统一处理所有Controller
public class GlobalExceptionHandler {

    /**
     * 自定义处理所有错误
     *
     * @ExceptionHandler 可以标识一个方法, 默认只能处理这个类发生的指定错误
     * @ControllerAdvice AOP思想, 可以统一处理所有方法
     */
    @ResponseBody
//    @ExceptionHandler(Exception.class)
    public String handleException(Exception e) {

        return "统一处理,错误已发生,原因:" + e.getMessage();
    }
}
