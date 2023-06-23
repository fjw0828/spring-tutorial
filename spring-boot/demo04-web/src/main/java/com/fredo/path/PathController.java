package com.fredo.path;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class PathController {
    /**
     * 两种模式都可以
     */
    @GetMapping("/a*/b?/{p:[a-f]+}")
    public String hello(HttpServletRequest request,
                        @PathVariable("p") String path) {

        log.info("路径变量p： {}", path);
        //获取请求路径
        return request.getRequestURI();
    }

    /**
     * AntPatternParser
     * 默认path匹配策略, /** 不能在路径中间
     * 需要修改配置文件:spring.mvc.pathmatch.matching-strategy=ant_path_matcher
     */
//    @GetMapping("/ant/**/{p1:[a-f]+}")
//    public String ant(HttpServletRequest request,
//                        @PathVariable("p1") String path) {
//
//        log.info("ant-路径变量p1： {}", path);
//        //获取请求路径
//        return request.getRequestURI();
//    }

    /**
     * 默认path匹配策略
     * /** 只能在路径最后出现
     */
    @GetMapping("/path/{p2:[a-f]+}/**")
    public String path(HttpServletRequest request,
                       @PathVariable("p2") String path) {

        log.info("path-路径变量p2： {}", path);
        //获取请求路径
        return request.getRequestURI();
    }
}
