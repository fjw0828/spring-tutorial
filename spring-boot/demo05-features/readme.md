# SpringBoot 核心特性

[官方文档:](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html)

> Spring Application: SpringApplication <br/>
> External Configuration: External Configuration <br/>
> Profiles: Profiles <br/>
> Logging: Logging

## 1.SpringApplication

`SpringApplication`简化了Spring程序的启动过程.
大多数情况下, 可以委托给静态`SpringApplication.run`方法, 如以下示例所示:

```java

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
```

启动日志:

```text
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.1.0)

2023-07-05T14:07:10.668+08:00  INFO 9584 --- [           main] com.fredo.Main                           : Starting Main using Java 17.0.2 with PID 9584 (D:\Develop\Projects\ideaProject\spring-tutorial\spring-boot\demo05-features\target\classes started by Administrator in D:\Develop\Projects\ideaProject\spring-tutorial)
2023-07-05T14:07:10.673+08:00  INFO 9584 --- [           main] com.fredo.Main                           : No active profile set, falling back to 1 default profile: "default"
2023-07-05T14:07:12.122+08:00  INFO 9584 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2023-07-05T14:07:12.123+08:00  INFO 9584 --- [           main] o.a.catalina.core.AprLifecycleListener   : An older version [1.2.34] of the Apache Tomcat Native library is installed, while Tomcat recommends a minimum version of [2.0.1]
2023-07-05T14:07:12.123+08:00  INFO 9584 --- [           main] o.a.catalina.core.AprLifecycleListener   : Loaded Apache Tomcat Native library [1.2.34] using APR version [1.7.0].
2023-07-05T14:07:12.140+08:00  INFO 9584 --- [           main] o.a.catalina.core.AprLifecycleListener   : OpenSSL successfully initialized [OpenSSL 1.1.1o  3 May 2022]
2023-07-05T14:07:12.159+08:00  INFO 9584 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2023-07-05T14:07:12.159+08:00  INFO 9584 --- [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.8]
2023-07-05T14:07:12.356+08:00  INFO 9584 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2023-07-05T14:07:12.360+08:00  INFO 9584 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1596 ms
2023-07-05T14:07:13.208+08:00  INFO 9584 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2023-07-05T14:07:13.219+08:00  INFO 9584 --- [           main] com.fredo.Main                           : Started Main in 3.223 seconds (process running for 4.118)
```

注意: 只有前两行和最后一行是SpringBoot的**启动日志**
启动日志可以关闭:

- 配置文件

```properties
spring.main.log-startup-info=false
```

- 代码:

```java
SpringApplication application=new SpringApplication(Main.class);

// 关闭启动日志(注意:配置文件的优先级大于代码优先级!!!)
        application.setLogStartupInfo(false);
        application.run(args);
```

### 1.启动失败

如果应用程序无法启动, 已注册的`FailureAnalyzers`将有机会提供专用的错误消息和解决问题的具体操作
以端口占用为例:

```text
***************************
APPLICATION FAILED TO START
***************************

Description:

Embedded servlet container failed to start. Port 8080 was already in use.

Action:

Identify and stop the process that is listening on port 8080 or configure this application to listen on another port.
```

`FailureAnalyzers`是一个用于分析应用程序失败原因的工具.当应用程序发生失败时,`FailureAnalyzers`会收集失败信息,
并分析失败原因,以便开发人员能够更好地理解应用程序的健康状况.

可自定义:

- 1.自定义类继承`AbstractFailureAnalyzer`,并重写`analyze`方法.可参考:`BindValidationFailureAnalyzer`
- 2.注册在META-INF/spring.factories 文件中

```text
org.springframework.boot.diagnostics.FailureAnalyzer=\
com.fredo.custom.MyFailureAnalyzer
```

### 2.延迟初始化

在容器启动时不初始化Bean,只有在第一次使用时再初始化

配置方法:

- 配置文件

```properties
spring.main.lazy-initialization=true
```

- 代码:

```java
// 开启延迟初始化(默认关闭,不建议开启)
application.setLazyInitialization(true);
```

可以使用注解`@Lazy(false)`, 单独指定某些Bean延迟初始化

### 3.自定义Banner

Banner 就是程序启动时最上面的信息

类路径添加文件:**banner.txt**或设置`spring.banner.location`就可以定制 Banner
[推荐网址](https://www.bootschool.net/ascii)

关闭

- 配置文件

````properties
spring.main.banner-mode=off
````

- 代码:

```java
// 关闭Banner,默认值:CONSOLE
application.setBannerMode(Banner.Mode.OFF);
```

### 4.自定义SpringApplication

```java
public static void main(String[]args){
        SpringApplication application=new SpringApplication(Main.class);

        // 关闭启动日志(注意:配置文件的优先级大于代码优先级!!!)
        application.setLogStartupInfo(false);

        // 开启延迟初始化(默认关闭,不建议开启)
        application.setLazyInitialization(true);

        // 关闭Banner,默认值:CONSOLE
//        application.setBannerMode(Banner.Mode.OFF);

        application.run(args);
        }
```

自定义信息在`application.properties`中,
具体可[参考官网](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)

### 5.流式构造API

可用于构建父子层次结构

```java
public static void main(String[]args){
        new SpringApplicationBuilder()
        .sources(Main.class) // 父
        .child(Main.class) // 子,非必须
        .bannerMode(Banner.Mode.OFF)
        .run(args);
        }
```

## 2.外置配置文件

## 3.配置文件隔离

## 4.日志

详细可参考[SpringBoot日志](https://blog.fu-jw.com/posts/4275e528.html)

SpringBoot 的启动原理可以分为两个阶段：引导阶段和运行阶段。

引导阶段：

初始化SpringApplication对象，设置应用程序的配置类和启动参数等信息。
根据配置类，创建一个SpringApplication实例。
解析和加载应用程序的配置文件，包括application.properties、application.yml等。
执行SpringApplicationRunListeners的starting()方法，通知所有的监听器应用程序即将开始启动。
创建Environment对象，用于读取应用程序的配置信息。
配置应用程序的上下文，包括加载外部配置文件、设置默认的配置属性等。
准备应用程序的上下文，包括加载Spring Boot的自动配置、注册Bean等。
执行SpringApplicationRunListeners的environmentPrepared()方法，通知所有的监听器应用程序的环境已经准备好。
执行ApplicationContextInitializer的initialize()方法，用于对应用程序的上下文进行初始化。
执行SpringApplicationRunListeners的contextPrepared()方法，通知所有的监听器应用程序的上下文已经准备好。

运行阶段：

刷新应用程序的上下文，包括创建单例Bean、解析属性占位符等。
执行SpringApplicationRunListeners的contextLoaded()方法，通知所有的监听器应用程序的上下文已经加载完毕。
执行应用程序的命令行程序，如果有的话。
执行SpringApplicationRunListeners的started()方法，通知所有的监听器应用程序已经开始运行。
运行应用程序，处理请求和响应。
执行SpringApplicationRunListeners的running()方法，通知所有的监听器应用程序正在运行。
应用程序运行结束，执行SpringApplicationRunListeners的failed()方法，通知所有的监听器应用程序运行失败。

总的来说，Spring Boot 的启动过程主要包括配置的解析和加载、环境的准备和配置、上下文的创建和初始化、监听器的通知等。通过这些步骤，Spring
Boot 可以快速、方便地启动应用程序，并提供了一些额外的功能，如自动配置、命令行参数处理等。






 