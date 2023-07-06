# SpringBoot核心特性

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

## 2.配置文件隔离

> 环境隔离: 快速切换开发、测试、生产环境
> 步骤：
>   1. 标识环境：指定哪些组件、配置在哪个环境生效
>   2. 切换环境：这个环境对应的所有组件和配置就应该生效

### 1.指定环境

Spring Profiles 提供一种隔离配置的方式, 使其仅在特定环境生效
任何组件都可用`@Profile`标记, 来指定何时被加载

例如:

- dev环境: @Profile({"dev"})
- dev和prod环境: @Profile({"dev", "prod"})

### 2.激活环境

配置文件激活

```properties
spring.profiles.active=prod
```

命令行激活

```shell
java -jar xxx.jar --spring.profiles.active=prod
```

注意:

- 不标注`@Profile`的组件无论是在哪个环境中都会激活
- 存在默认环境`@Profile({"default"})`,需要激活默认环境才有效
- 也可以设置默认激活的环境, `spring.profiles.default=dev`
- 推荐使用激活方式激活指定环境

### 3.环境包含

在配置文件(必须是在主配置文件中)中设置:

```properties
spring.profiles.include[0]=dev
spring.profiles.include[1]=test
或者=
spring.profiles.include=dev,test
```

#### 4.最佳实践

**生效的环境 = 激活的环境/默认环境 + 包含的环境**

项目中:

- 基础的配置`mybatis`,`log`,...：写到**包含环境**中
- 要动态切换变化的`db`, `redis`,...：写到**激活的环境**中

#### 5.Profile 分组

创建prod组，指定包含db和mq配置

```properties
spring.profiles.group.prod[0]=db
spring.profiles.group.prod[1]=mq
```

使用`--spring.profiles.active=prod`, 就会激活prod, 就会使用db, mq配置文件

#### 6.Profile 配置文件

- `application-{profile}.properties`可以作为指定环境的配置文件
- 激活这个环境，配置就会生效。最终生效的所有配置是
  - `application.properties`：主配置文件，任意时候都生效
  - `application-{profile}.properties`：指定环境配置文件，激活指定环境生效

profile优先级 > application

## 3.外置配置文件

> 场景：线上应用如何快速修改配置，并应用最新配置？
>- SpringBoot 使用  **配置优先级** + **外部配置**  简化配置更新、简化运维。
>- 只需要给jar应用所在的文件夹放一个`application.properties`最新配置文件，重启项目就能自动应用最新配置

SpringBoot允许将配置外部化, 以便可以在不同的环境中使用相同的应用程序代码.
我们可以使用各种**外部配置源**, 包括**Java Properties文件**、**YAML文件**、**环境变量**和**命令行参数**。

`@Value`可以获取值, 也可以用`@ConfigurationProperties`将所有属性**绑定**到java object中

### 1.配置优先级

以下是SpringBoot属性源加载顺序。**由低到高，高优先级配置覆盖低优先级**

1. **默认属性**（通过`SpringApplication.setDefaultProperties`指定的）
2. `@PropertySource`指定加载的配置（需要写在`@Configuration`类上才可生效）
3. 配置文件（application.properties/yml等）
4. `RandomValuePropertySource`支持的`random.*`配置（如：`@Value("${random.int}")`）
5. OS 环境变量
6. Java 系统属性（System.getProperties()）
7. JNDI 属性（来自java:comp/env）
8. ServletContext 初始化参数
9. ServletConfig 初始化参数
10. SPRING_APPLICATION_JSON属性（内置在环境变量或系统属性中的 JSON）
11. **命令行参数**
12. 测试属性。(@SpringBootTest进行测试时指定的属性)
13. 测试类@TestPropertySource注解
14. Devtools 设置的全局属性。($HOME/.config/spring-boot)

小结一下:
常见的优先级顺序：
命令行> 配置文件> springapplication配置

**配置文件优先级如下**：(后面覆盖前面)

1. jar 包内的application.properties/yml
2. jar 包内的application-{profile}.properties/yml
3. jar 包外的application.properties/yml
4. jar 包外的application-{profile}.properties/yml

> 建议：用一种格式的配置文件。如果.properties和.yml同时存在,则.properties优先

小结一下:

- 包外 > 包内
- 同级情况：profile配置 > application配置

所有参数均可由命令行传入，使用`--参数项=参数值`，将会被添加到环境变量中，并优先于配置文件
比如`java -jar app.jar --name="Spring"`,可以使用`@Value("${name}")`获取

演示场景：

- 包内： application.properties server.port=8000
- 包内： application-dev.properties server.port=9000
- 包外： application.properties server.port=8001
- 包外： application-dev.properties server.port=9001

启动端口: 命令行 > 9001 > 8001 > 9000 > 8000

### 2.外部配置

SpringBoot 应用启动时会自动寻找**application.properties**和**application.yaml**位置进行加载.
顺序如下：（后面覆盖前面）

- 类路径: 内部
  - 类根路径
  - 类下`/config`包
- 当前路径（项目所在的位置）
  - 当前路径
  - 当前下`/config`子目录
  - `/config`目录的直接子目录

最终效果：优先级由高到低，前面覆盖后面

- 命令行 > 包外config直接子目录 > 包外config目录 > 包外根目录 > 包内目录
- 同级比较：
  - profile配置 > 默认配置
  - properties配置 > yaml配置

<img alt="配置优先级" src="https://image.fu-jw.com/img/2023/07/06/64a67fc67db26.png"/>

规律：最外层的最优先

- 命令行 > 所有
- 包外 > 包内
- config目录 > 根目录
- profile > application

配置不同就都生效（互补），配置相同高优先级覆盖低优先级

### 3.导入配置

使用`spring.config.import`可以导入额外配置

```properties
spring.config.import=my.properties
my.property=value
```

无论以上写法的先后顺序, **my.properties**的值总是优先于直接在文件中编写的**my.property**

### 4.属性占位符

配置文件中可以使用`${name:default}`形式取出之前配置过的值

```properties
app.name=MyApp
app.description=${app.name} is a Spring Boot application written by ${username:Unknown}
```

## 4.日志

详细可参考[SpringBoot日志](https://blog.fu-jw.com/posts/4275e528.html)

## 5.单元测试-JUnit5

详细可参考[SpringBoot单元测试](http://localhost:4000/posts/6d37548c.html)

## 6.SpringBoot启动原理

> 下面内容由GPT-4生成

SpringBoot 是一个用于简化 Spring 应用开发的框架。它的启动原理可以分为以下几个关键步骤：

1. **创建 SpringApplication 对象**：接下来，Spring Boot 创建一个 `SpringApplication` 对象，该对象负责管理 Spring
   应用的启动和运行。在创建时，它会检测应用中的特定类，例如 `@SpringBootApplication` 注解标注的类。

2. **加载配置文件**：Spring Boot 首先加载应用的配置文件（如 `application.properties` 或 `application.yml`），以读取相关配置。

3. **设置 SpringApplication 对象属性**：`SpringApplication` 对象会根据加载的配置文件和应用上下文设置相应的属性。例如，它可能会设置应用的环境、激活的配置文件和日志配置等。

4. **运行 SpringApplication 对象**：通过调用 `SpringApplication.run()` 方法启动 Spring Boot 应用。这个方法会启动内嵌的
   Servlet 容器（如 Tomcat）、初始化 Spring 应用上下文，并注册所有需要的 Bean。

5. **自动配置**：Spring Boot 根据应用的依赖库和配置文件自动配置 Spring 应用。自动配置是 Spring Boot
   的一个核心特性，它通过扫描类路径和应用中的特定注解（如 `@Configuration` 和 `@Component`）来自动创建和注册 Bean。这大大简化了
   Spring 应用的配置过程。

6. **启动应用**：最后，Spring Boot 启动应用，并根据配置文件中的设置监听特定端口，提供 Web 服务。

总之，SpringBoot 的启动原理主要包括加载配置文件、创建和设置 SpringApplication 对象、运行 SpringApplication
对象、自动配置和启动应用等步骤。这一切都旨在简化 Spring 应用的开发和部署，让开发者能够快速构建和运行应用。


> 下面内容由CHAT-GLM生成

**SpringBoot 的启动原理**

SpringBoot 是一种基于 Spring 框架的快速开发工具，它简化了开发过程，提供了一种简便的方式来构建独立的、基于 Spring
的生产级应用程序。要理解 SpringBoot 的启动原理，我们需要探讨 SpringBoot 的主要组件和过程。

1. **依赖管理**

SpringBoot 使用了一种称为 "Starter" 的依赖管理方式，它将常用的依赖打包在一起，使得开发人员只需引入一个
Starter，即可自动添加并管理相关的依赖。这大大简化了项目的依赖管理，提高了开发效率。

2. **自动配置**

SpringBoot 的一个重要特性是 "约定优于配置"，它通过自动配置来减少开发人员的配置工作。在启动过程中，SpringBoot 会扫描类路径，检查已添加的
Starter 和依赖，并根据这些信息自动配置 Bean 和应用程序上下文。这样，开发人员只需关注自己的业务逻辑，而无需关注底层的配置细节。

3. **启动过程**

以下是 SpringBoot 启动过程的主要步骤：

a. **初始化 SpringApplication 对象**：SpringBoot 首先创建一个 SpringApplication 对象，该对象用于保存应用程序的上下文和配置信息。

b. **运行 SpringApplication 对象**：接下来，SpringBoot 会调用 SpringApplication 对象的 `run()` 方法，该方法会启动整个应用程序。

c. **加载应用程序上下文**：在 `run()` 方法中，SpringBoot 会加载应用程序的上下文，包括 Bean 定义、自动配置等信息。

d. **启动嵌入式容器**：如果应用程序是一个 Web 项目，SpringBoot 会启动一个嵌入式的 Web 容器（如 Tomcat、Jetty 或
Undertow），并将应用程序部署到该容器中。

e. **执行应用程序**：最后，SpringBoot 会执行应用程序的主类 `main()` 方法，从而启动整个应用程序。

4. **外部化配置**

SpringBoot 支持外部化配置，使得开发人员可以在不修改代码的情况下，通过修改配置文件或环境变量来调整应用程序的行为。这大大提高了应用程序的灵活性和可维护性。

总之，SpringBoot 的启动原理主要包括依赖管理、自动配置、启动过程和外部化配置等方面。通过这些特性和过程，SpringBoot
实现了快速开发、简化配置和高度灵活性，使得开发人员可以更专注于业务逻辑的开发。