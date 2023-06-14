# SpringBoot日志

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

## 1.日志体系

### Log4j
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

### JUL
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

### JCL
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

### SLF4J和logback
>`Log4j`的作者觉得`JCL`不好用,自己又写了一个新的接口api,就是`SLF4J`,
并且为了追求更极致的性能,新增了一套日志的实现,就是`logback`

Simple Logging Facade For Java

`SLF4J`主要给Java日志访问提供一套标准,规范的API框架.

目前市面最流行的日志门面,主要功能:
- 日志框架的绑定
- 日志框架的桥接

#### SLF4J的各种组合
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















