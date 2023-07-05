package com.fredo.custom;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

public class MyFailureAnalyzer extends AbstractFailureAnalyzer {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, Throwable cause) {
        return new FailureAnalysis("[自定义]启动失败:", "端口占用", cause.getCause());
    }
}
