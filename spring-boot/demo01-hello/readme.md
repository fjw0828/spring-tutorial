# demo01-hello
Spring Boot 项目的第一个模块

## 快速开发
按照官网一步一步操作即可: 
https://docs.spring.io/spring-boot/docs/current/reference/html/getting-started.html#getting-started.first-application

### 可能遇到的问题
#### pom.xml中出现Provides transitive vulnerable dependency maven:org.yaml:snakeyaml:1.33警告
- 告警原因:Maven项目中使用了一个被认为是有漏洞的依赖项，并且这个依赖项也被其他依赖项所传递
  - org.yaml:snakeyaml:1.33这个库是存在漏洞
- 解决警告:升级依赖项
  - 在中央仓库搜索无告警版本: https://mvnrepository.com/
  - 在Maven项目中，可以使用dependencyManagement标签来管理依赖项
  - 这个标签中，可以指定一个特定版本，以便所有依赖项都将使用这个版本
```yaml
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.yaml</groupId>
                <artifactId>snakeyaml</artifactId>
                <version>2.0</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
```
- 解决警告:移除依赖项
  - 如果这个库不是必须的，可以考虑从项目中移除它; 或者idea中设置忽略(眼不见心不烦)
```yaml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
            <exclusions>
                <exclusion>
                    <groupId>org.yaml</groupId>
                    <artifactId>snakeyaml</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
```
#### 项目启动报错：An incompatible version [1.1.29] of the Apache Tomcat Native library is installed, while Tomcat requires version [1.2.34]
- 打开网页 http://archive.apache.org/dist/tomcat/tomcat-connectors/native/
- 查找对应版本文件: http://archive.apache.org/dist/tomcat/tomcat-connectors/native/1.2.34/binaries/
- 下载对应的zip文件
- 加压文件,里面有32位和64位的 tcnative-1.dll 文件
- 根据自己的jdk和tomcat版本选择一个，复制到 jdk 的bin目录下即可
- 重启 spring boot项目

## 模块小结
### SpringBoot 是什么
SpringBoot 帮我们简单、快速地创建一个独立的、生产级别的 Spring 应用（说明：SpringBoot底层是Spring）

大多数 SpringBoot 应用只需要编写少量配置即可快速整合 Spring 平台以及第三方技术

### 特点
1. 简化整合
- SpringBoot提出`场景启动器`的概念
- 导入相关的场景，即拥有相关的功能。
- 默认支持的所有场景：https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.build-systems.starters
  - 官方提供的场景：命名为：spring-boot-starter-*
  - 第三方提供场景：命名为：*-spring-boot-starter
- 场景一导入，万物皆就绪
2. 简化开发
- 无需编写任何配置，直接开发业务
3. 简化配置
- application.properties：
- 集中式管理配置。只需要修改这个文件就行 。
- 配置基本都有默认值
- 能写的所有配置都在： https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#appendix.application-properties
4. 简化部署
- 打包为可执行的jar包。
- linux服务器上有java环境。
5. 简化运维
- 修改配置（外部放一个application.properties文件）、监控、健康检查。
   .....

