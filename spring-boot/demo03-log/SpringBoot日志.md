# SpringBoot日志

在前面[SpringBoot快速入门](https://blog.fu-jw.com/posts/4ccc97e8.html)中我们知道,

每个SpringBoot项目都会依赖**核心启动器**:`spring-boot-starter`, 核心启动器会依赖:`spring-boot-starter-logging`实现日志功能

`spring-boot-starter-logging`依赖`Logback`的日志实现和`SLF4J`桥接器,将其他日志实现桥接到`SLF4J`

即`SLF4J`+`Logback`的日志组合.

![SpringBoot日志依赖](https://s2.loli.net/2023/06/15/Rtsyneul2bhIKNr.webp)

<details> 
<summary><strong><span style="color: red; ">为什么日志不用SpringBoot的自动配置???</span></strong></summary>
<strong>日志是系统启动就要使用</strong>,
<strong>xxxAutoConfiguration</strong>是系统启动好了以后放好的组件,后来用的.<br>
日志是利用<strong>监听器机制</strong>配置好的,<strong>ApplicationListener</strong>

</details>

## 默认日志

```text
2023-06-12T20:57:23.829+08:00  INFO 5232 --- [           main] com.fredo.Main                           : Starting Main using Java 17.0.2 with PID 5232
2023-06-12T20:57:23.837+08:00  INFO 5232 --- [           main] com.fredo.Main                           : No active profile set, falling back to 1 default profile: "default"
2023-06-12T20:57:25.094+08:00  INFO 5232 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
```

默认输出格式：

- 时间和日期：毫秒级精度
- 日志级别：ERROR, WARN, INFO, DEBUG, or TRACE.
- 进程 ID
- ---： 消息分割符
- 线程名： 使用[]包含
- Logger 名： 通常是产生日志的类名
- 消息： 日志记录的内容

![SpringBoot日志默认值](https://s2.loli.net/2023/06/16/EiSUmc3tWpYnea8.webp)

```json
{
  "name": "logging.pattern.console",
  "type": "java.lang.String",
  "description": "Appender pattern for output to the console. Supported only with the default Logback setup.",
  "sourceType": "org.springframework.boot.context.logging.LoggingApplicationListener",
  "defaultValue": "%clr(%d{${LOG_DATEFORMAT_PATTERN:-yyyy-MM-dd'T'HH:mm:ss.SSSXXX}}){faint} %clr(${LOG_LEVEL_PATTERN:-%5p}) %clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}"
}
```

## 修改格式

在SpringBoot的配置文件**application.properties**中直接修改即可:

```text
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} ==== %msg%n
```

结果如下:

```text
2023-06-12 21:19:29.634 [main] INFO  com.fredo.Main ==== Starting Main using Java 17.0.2 with PID 11216
2023-06-12 21:19:29.639 [main] INFO  com.fredo.Main ==== No active profile set, falling back to 1 default profile: "default"
2023-06-12 21:19:30.922 [main] INFO  o.s.boot.web.embedded.tomcat.TomcatWebServer ==== Tomcat initialized with port(s): 8080 (http)
```

**简单方便**

也可以单独文件配置`logback`, **优先级高于SpringBoot的配置**
默认在**classpath**中依次寻找**logback-spring.xml, logback-spring.groovy, logback.groovy, logback.xml**文件

还可以在SpringBoot配置文件中指定文件位置

```text
logging.config=classpath:logback-bak.xml
```

## 日志使用

在每个类中添加代码:
`Logger log = LoggerFactory.getLogger(getClass());`

或者添加`lombok`依赖, 在类上直接使用`@Slf4j`注解即可代替上面代码

```xml

<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

然后在想要打印日志的位置使用即可
`log.info("...");`

## 日志级别

由低到高：

- ALL: 开启日志打印
- TRACE: 可跟踪打印框架底层信息
- DEBUG: 调试信息
- INFO: 普通信息
- WARN: 系统的告警信息
- ERROR: 系统的错误信息,异常信息
- FATAL: 可能导致系统崩溃的严重信息
- OFF: 关闭日志打印

注意：

- `Logback`没有`FATAL`级别, 对应的是`ERROR`
- **高级别包含低级别的所有信息**
- SpringBoot日志默认级别是`INFO`

> 默认所有日志没有精确指定级别就使用**root**的默认级别:</br>
> logging.level.root=info</br></br>
> 精确调整某个包下的日志级别</br>
> logging.level.com.fredo.logging.controller=debug</br>
> logging.level.com.fredo.logging.service=debug</br>

## 日志分组

在给不同包下的类定义日志级别时,还可以自定义分组,以组为单位划分日志级别

```text
logging.group.组名=包名
logging.level.组名=日志级别
```

例如:

```text
logging.group.tomcat=org.apache.catalina,org.apache.coyote,org.apache.tomcat
logging.level.tomcat=trace
```

SpringBoot 预定义两个组

| Name | Loggers                                                                                                                                                                                                                   |
|------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| web  | org.springframework.core.codec,</br> org.springframework.http,</br> org.springframework.web,</br> org.springframework.boot.actuate.endpoint.web,</br> org.springframework.boot.web.servlet.ServletContextInitializerBeans |
| sql  | org.springframework.jdbc.core,</br> org.hibernate.SQL,</br> org.jooq.tools.LoggerListener                                                                                                                                 |

## 文件输出

SpringBoot默认只把日志写在控制台,如果想额外记录到文件,
可以在`application.properties`中添加`logging.file.name` or `logging.file.path`配置项

| logging.file.name | logging.file.path | 示例       | 效果                             |
|-------------------|-------------------|----------|--------------------------------|
| -                 | -                 |          | 仅控制台输出                         |
| 指定                | -                 | my.log   | 写入指定文件。可以加路径                   |
| -                 | 指定                | /var/log | 写入指定目录，文件名为`spring.log`        |
| 指定                | 指定                |          | 以`logging.file.name`为准(path无效) |

### 归档与切割

> 归档：每天的日志单独存到一个文档中</br>
> 切割：每个文件10MB,超过大小切割成另外一个文件

每天的日志应该独立分割出来存档.如果使用`Logback`(SpringBoot默认日志实现),
可以通过`application.properties/yaml`文件指定日志滚动规则

如果是其他日志系统,需要自行配置(添加`log4j2.xml`或`log4j2-spring.xml`)

滚动规则设置如下:

| 配置项                                                      | 描述                                                              |
|----------------------------------------------------------|-----------------------------------------------------------------|
| **logging.logback.rollingpolicy.file-name-pattern**      | 日志存档的文件名格式（默认值：**${LOG_FILE}.%d{yyyy-MM-dd}.%i.gz**）            |
| **logging.logback.rollingpolicy.clean-history-on-start** | 应用启动时是否清除以前存档（默认值：**false**）                                    |
| **logging.logback.rollingpolicy.max-file-size**          | 存档前，每个日志文件的最大大小（默认值：**10MB**                                    |
| **logging.logback.rollingpolicy.total-size-cap**         | 日志文件被删除之前，可以容纳的最大大小（默认值：0B）。设置**1GB**则磁盘存储超过**1GB**日志后就会删除旧日志文件 |
| **logging.logback.rollingpolicy.max-history**            | 日志文件保存的最大天数(默认值：**7**)                                          |

## 自定义配置

通常配置`application.properties`就够了.当然也可以自定义. 比如:

| 日志系统    | 自定义文件                                                                     |
|---------|---------------------------------------------------------------------------|
| Logback | logback-spring.xml, logback-spring.groovy, logback.xml, or logback.groovy |
| Log4j2  | log4j2-spring.xml or log4j2.xml                                           |
| JUL     | logging.properties                                                        |

建议在日志配置中使用`-spring`变量(比如`logback-spring.xml`而不是`logback.xml`).
如果使用标准配置文件, `spring`无法完全控制日志初始化

## 切换日志组合

需要排除SpringBoot自带的日志

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter</artifactId>
<exclusions>
    <exclusion>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-logging</artifactId>
    </exclusion>
</exclusions>
</dependency>

<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-log4j2</artifactId>
</dependency>
```

`log4j2`支持`yaml`和`json`格式的配置文件

| 格式   | 依赖                                                                                                     | 文件名                      |
|------|--------------------------------------------------------------------------------------------------------|--------------------------|
| YAML | com.fasterxml.jackson.core:jackson-databind + com.fasterxml.jackson.dataformat:jackson-dataformat-yaml | log4j2.yaml + log4j2.yml |
| JSON | com.fasterxml.jackson.core:jackson-databind                                                            | log4j2.json + log4j2.jsn |

## 最佳实践
1. 导入任何第三方框架,先排除它的日志包,因为SpringBoot底层控制好了日志
2. 修改`application.properties`配置文件,就可以调整日志的所有行为.如果不够,可以编写日志框架自己的配置文件放在类路径下就行,比如`logback-spring.xml`,`log4j2-spring.xml`
3. 如需**对接专业日志系统**,也只需要把`logback`记录的日志配置文件导入`kafka`之类的中间件即可,这和SpringBoot没关系,都是日志框架自己的配置,修改配置文件即可
4. 业务中使用slf4j-api记录日志
