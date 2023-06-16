# 日志体系

日志记录了系统行为的时间, 地点, 状态等相关信息, 能够帮助我们了解并监控系统状态, 
在发生错误或者接近某种危险状态时能及时提醒我们处理, 同时在系统产生问题, 能够帮助我们快速定位, 诊断问题.

在生产环境中日志可能是我们**了解系统运行状况的唯一方式**, 其重要性无需赘述
总结有如下优点:
- 记录系统行为
- 监控系统状态
- 提示系统风险
- 定位系统问题

## 0.System.out
同样可以打印信息, 为什么不用`System.out.print("")`?

1. 难定位
`System.out.print`输出的日志只打印在控制台, 没有存储到一个日志文件中, 且格式不统一, 在生产环境这样打印出来很难定位信息, 意义不大.
2. 性能弱
```java
public void print(String s) {
    write(String.valueOf(s));
}
```
```java
private void write(String s) {
    try {
        synchronized (this) {
            ensureOpen();
            textOut.write(s);
            textOut.flushBuffer();
            charOut.flushBuffer();
            if (autoFlush && (s.indexOf('\n') >= 0))
                out.flush();
        }
    }
    catch (InterruptedIOException x) {
        Thread.currentThread().interrupt();
    }
    catch (IOException x) {
        trouble = true;
    }
}
```
由上面的源码可以看出`print`的方法实现是由`synchronized`包裹的同步代码块
不能异步打印日志, 在**高并发**的情况下, 会严重**影响性能**

**阿里巴巴Java开发手册:**
>【强制】 应用中不可直接使用日志系统（ Log4j、 Logback） 中的 API，而应依赖使用日志框架
（ SLF4J、 JCL--Jakarta Commons Logging） 中的 API，使用门面模式的日志框架，有利于维护和
各个类的日志处理方式统一</br></br>
>【强制】 生产环境禁止直接使用 System.out 或 System.err 输出日志或使用
e.printStackTrace()打印异常堆栈</br></br>
> 【强制】 在日志输出时，字符串变量之间的拼接使用占位符的方式</br></br>
> 【强制】 对于 trace/debug/info 级别的日志输出，必须进行日志级别的开关判断


## Log4j
Log for Java

Ceki Gülcü于2001年发布了Log4j, 并将其捐献给Apache软件基金会, 成为Apache基金会的顶级项目.
Apache基金会最早实现的一套日志框架, 通过使用`Log4j`
- 可以**控制日志信息输送的目的地**是控制台, 文件等
- 可以控制每一条日志的**输出格式**
- 通过定义每一条**日志信息的级别**, 我们能够更加细致地控制日志的生成过程
- 可以通过一个**配置文件**来灵活地进行配置, 而不需要修改应用的代码 ☆☆

`Log4j`主要由`Loggers`(日志记录器), `Appenders`(输出端), `Layout`(格式化).
- `Loggers`控制日志输出级别与日志是否输出
- `Appenders`指定日志的输出方式(输出到控制台或文件等)
- `Layout`控制日志的输出格式

>2015年9月, Apache软件基金业宣布, Log4j不再维护, 建议所有相关项目升级到Log4j2.

## JUL
Java Util Logging

SUN公司在JDK1.4发布的Java原生的日志框架, 使用时不需要另外引用第三方的类库, 相对其他的框架使用方便

1. 初始化`LogManager`
    1. `LogManager`加载logging.properties配置
    2. 添加`Logger`到`LogManager`
2. 从`LogManager`中获取`Logger`
3. 设置级别`Level`, 并指定日志记录`LogRecord`
4. `Filter`提供日志级别之外更细粒度的控制
5. `Handler`是用来处理日志输出位置
6. `Formatter`用来格式化LogRecord

![JUL流程](https://s2.loli.net/2023/06/13/bCoUrAMwVqHFGz7.webp)

## JCL
Jakarta Commons Logging

>JUL的api与log4j是完全不同的(参数只接受string).由于日志系统没有互相关联,彼此没有约定,不同人的代码使用不同日志,替换和统一也就变成了一件非常棘手的事情.
> 那我们该如何解决这个问题呢?
> 抽象出一个接口层,对每个日志实现都适配或者转接,这样这些提供给别人的库都直接使用抽象层即可,以后需要调用的时候,就调用这些接口.

Apache基金下Jakarta小组开发的通用日志API

主要功能是给**所有的日志实现**提供一个**统一接口**, 本身也提供日志实现(SimpleLog),但是很弱一般不用.
常见的日志实现如:最早的**Log4j**以及JDK自带的**JUL**等

![JCL](https://s2.loli.net/2023/06/13/EZ62fIKGMwTsRDn.webp)

日志门面的好处:
- **面向接口编程**  解耦合
- 可灵活切换日志框架
- 统一日志API方便使用
- 统一日志配置和管理

>由于设计缺陷,只支持当时主流的几个日志实现,不利于其他日志的使用(需修改源码),现已淘汰!!!

## SLF4J
>`Log4j`的作者觉得`JCL`不好用,自己又写了一个新的接口api,就是`SLF4J`,
并且为了追求更极致的性能,新增了一套日志的实现,就是`logback`

Simple Logging Facade For Java

`SLF4J`主要给Java日志访问提供一套标准,规范的API框架.

目前市面最流行的日志门面,主要功能:
- 日志框架的绑定
- 日志框架的桥接

### SLF4J的各种组合
![SLF4J](https://s2.loli.net/2023/06/14/q1GbgIwUevMZ8WA.webp)
需对应的适配器

- SLF4J unbound:没有绑定实现,则只有接口,没有日志功能

- SLF4J bound to logback:logback是后出现的日志实现,实现了SLF4J接口,可直接搭配使用
```text
 SLF4J + logback 组合结果(黑色字体):
 20:24:32.167 [main] ERROR com.fredo.slf4j.SLF4JTest -- error
 20:24:32.172 [main] WARN com.fredo.slf4j.SLF4JTest -- warn
 20:24:32.172 [main] INFO com.fredo.slf4j.SLF4JTest -- info
 20:24:32.172 [main] ERROR com.fredo.slf4j.SLF4JTest -- 出现异常:java.lang.ArithmeticException: / by zero

 注意:注释slf4j-simple, 并且不要排除SpringBoot内置的日志实现:logback
```
- SLF4J bound to reload4j:reload4j是先出现的日志实现,没有实现了SLF4J接口,需配合适配器使用
```text
SLF4J + log4j 组合结果:
 2023-06-11 20:38:19.738 error
 2023-06-11 20:38:19.739 warn
 2023-06-11 20:38:19.739 info
 2023-06-11 20:38:19.739 debug
 2023-06-11 20:38:19.740 trace
 2023-06-11 20:38:19.741 出现异常:java.lang.ArithmeticException: / by zero

 注意:需要适配器:slf4j-log4j12以及log4j对应的配置文件
```
- SLF4J bound to JUL:JUL是先出现的日志实现,没有实现了SLF4J接口,需适配器使用
```text
SLF4J + JUL 组合结果(红色字体):
 6月 11, 2023 20:42:46 上午 com.fredo.slf4j.SLF4JTest test01
 SEVERE: error
 6月 11, 2023 20:42:46 上午 com.fredo.slf4j.SLF4JTest test01
 WARNING: warn
 6月 11, 2023 20:42:46 上午 com.fredo.slf4j.SLF4JTest test01
 INFO: info
 6月 11, 2023 20:42:46 上午 com.fredo.slf4j.SLF4JTest test01
 SEVERE: 出现异常:java.lang.ArithmeticException: / by zero

 注意:JDK默认自带无需依赖,但需要对应的适配器:slf4j-jdk14

```
- SLF4J bound to simple:simple是后出现的日志实现,实现了SLF4J接口,可直接搭配使用
```text
SLF4J + SLF4J-Simple 组合结果(红色字体):
 [main] ERROR com.fredo.slf4j.SLF4JTest - error
 [main] WARN com.fredo.slf4j.SLF4JTest - warn
 [main] INFO com.fredo.slf4j.SLF4JTest - info
 [main] ERROR com.fredo.slf4j.SLF4JTest - 出现异常:java.lang.ArithmeticException: / by zero

 注意:需要提前排除:logback.SpringBoot默认的日志实现
 <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-web</artifactId>
     <exclusions>
         <exclusion>
             <groupId>ch.qos.logback</groupId>
             <artifactId>logback-classic</artifactId>
         </exclusion>
     </exclusions>
 </dependency>
```
- SLF4J bound to nop:nop是日志开关,关闭日志实现,不打印日志
```text
SLF4J + slf4j-nop 组合结果:
 无任何打印(slf4j-nop关闭了日志打印)

 注意:提前排除SpringBoot默认自带的logback日志实现.
```

小结一下:
- 添加依赖:`slf4j-api`,使用`sl4j`的API在项目中进行统一日志记录
- 绑定具体的日志实现
  - 对于已经实现了`sl4j`API的日志实现,直接添加对应的依赖即可
  - 对于没有实现`sl4j`API的日志实现,需要先添加对应的日志**适配器**,再添加对应的依赖
- `sl4j`有且仅有一个日志实现的绑定,出现多个时默认使用第一个

### SLF4J桥接器
>以上都是SLF4J和不同的日志组合实现,
可以发现不同的日志框架打印的日志格式不一!!!</br>
这是因为SLF4J为了适配不同的日志框架提供了不同的**适配器**,具体的日志记录还是交给对应的日志框架实现的</br></br>
>SLF4J还提供了**桥接器**!!!</br>
桥接器则是由SLF4J实现一套对应的日志框架的API,日志的记录打印完全由这套API实现,可以做到格式统一,且旧代码无需任何更改.

**桥接器**解决的是项目中日志的遗留问题,当系统中存在之前的日志API可以通过桥接转换到SLF4J的实现
1. 移除旧日志框架的依赖--此时旧代码会报错(没有依赖了)
2. 添加SLF4J提供的对应的桥接组件--SLF4J自己提供一套API,旧代码不再报错
3. 为项目添加SLF4J的具体实现--就是新的日志实现(SLF4J作为日志门面,还需要具体的日志实现)

![SLF4J桥接](https://s2.loli.net/2023/06/14/YI3nCNavq5oZFbU.webp)

### 代码演示
pom.xml部分内容:
```xml
<!--
假设以前使用了log4j的日志框架,但后续想要改用其他日志框架,如logback
原来的代码由于使用的log4j,依赖也是log4j的API,现在没有了log4j的依赖则会报错
当有了桥接器则只需要引入对应的桥接器即可,旧代码无任何改动
-->
<!--        <dependency>-->
<!--            <groupId>log4j</groupId>-->
<!--            <artifactId>log4j</artifactId>-->
<!--            <version>1.2.17</version>-->
<!--        </dependency>-->

<!--log4j和slf4j的桥接器-->
<!--将log4j桥接到slf4j,slf4j再去使用logback记录日志-->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>log4j-over-slf4j</artifactId>
    <version>1.7.25</version>
</dependency>
```
测试代码:
```java
/**
 * 桥接器演示
 *  假设以前使用的日志是:Log4j
 *  现在要换成Logback(SpringBoot 默认自带,无需引入依赖)
 */
public class Log4jTest {
    @Test
    public void test(){
        Logger logger = Logger.getLogger(Log4jTest.class);

        logger.info("Hello Log4j");
    }
    /**
     * 使用log4j时,打印信息:
     *  2023-06-11 21:52:29.039 Hello Log4j
     *
     * 使用log4j和slf4j的桥接器时,打印信息:
     *  22:10:38.352 [main] INFO com.fredo.slf4j.Log4jTest -- Hello Log4j
     */
}
```
注意:适配器和桥接器不要同时出现!!!否则可能出现`StackOverflowError`异常(我没演示出来~~~)

## Logback
是`Log4j`的创始人设计的另一个开源日志组件,性能比`Log4j`好.

logback分为三个模块:
- logback-core:其他两个模块的核心
- logback-classic:兼容`log4j 1.x`和`JUL`,并进行了改进
- logback-access:与Servlet容器(如Tomcat和Jetty)集成,以提供`HTTP`访问日志功能

## Log4j2
Apache `Log4j2`是对`Log4j`的重大升级,并参考了`logback`的一些优秀设计,主要特点:
- 异常处理机制: 在`logback`中,`Appender`中的异常不会被感知到,但在`log4j2`中提供了异常处理机制
- 性能提升: 相较`log4j`和`logback`都有明显性能提升
- 自动重载配置: 参考`logback`的设计,自动刷新参数配置,生产环境可动态修改日志配置
- 无垃圾机制: 避免频繁的日志收集导致JVM的GC.

注意:`Log4j2`既是日志门面又是日志实现,但`SLF4J`日志门面更早出现更流行,所以市面上主流的搭配还是`SLF4J`+`Logback`/`Log4J2`
- log4j-api: 是日志门面
- log4j-core: 是日志实现

## 总结
市面上的日志框架可分成两类:日志门面和日志实现

![日志分类](https://s2.loli.net/2023/06/16/6ApzJit1ovaQ9w5.webp)

项目中会选一个日志门面和一个日志实现组合
- 日志门面一般选`SLF4J`,相比`JCL`更简单好用
- 日志实现一般选`Logback`.`SLF4J`和`Logback`/`Log4j`是同一个作者更易搭配,且`Logback`是`Log4j`的升级版

市面上常见搭配是:`SLF4J`+`Logback`(也是SpringBoot的默认)

而Apache基金新推出的`Log4j2`比`Logback`性能更好,`SLF4J`+`Log4j2`也是大势所趋.
