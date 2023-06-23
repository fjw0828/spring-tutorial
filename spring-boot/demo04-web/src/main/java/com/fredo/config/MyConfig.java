package com.fredo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MyConfig {
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {// 与下面代码一样效果
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/static/**")
                        .addResourceLocations("/static");
            }

            // 快捷键:Ctrl+O,显示可以重写的方法
            // 还可添加拦截器配置
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                WebMvcConfigurer.super.addInterceptors(registry);
            }

            /**
             * 此方法可以修改路径匹配规则
             * 从spring5.3 开始,默认 PathPatternParser
             * 想要修改为 AntPathMatcher,则只需设置为空即可
             */
//            @Override
//            public void configurePathMatch(PathMatchConfigurer configurer) {
//                configurer.setPatternParser(null);
//            }
        };
    }
}


//@Configuration
//public class MyConfig implements WebMvcConfigurer { // 还可以上面写法,效果一样
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        // 保留默认配置
//        WebMvcConfigurer.super.addResourceHandlers(registry);
//
//        // 自定义配置
//        registry.addResourceHandler("/static/**")// 设置静态资源访问前缀,同配置文件中的spring.mvc.static-path-pattern:
//                .addResourceLocations("/static");// 设置静态资源获取路径,同配置文件中的spring.web.resources.static-locations: classpath:/static/
//
//    }
//}
