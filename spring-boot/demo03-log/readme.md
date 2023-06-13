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

Apache基金会最早实现的一套日志框架, 通过使用`Log4j`
- 可以**控制日志信息输送的目的地**是控制台, 文件等
- 可以控制每一条日志的**输出格式**
- 通过定义每一条**日志信息的级别**, 我们能够更加细致地控制日志的生成过程
- 可以通过一个**配置文件**来灵活地进行配置, 而不需要修改应用的代码 ☆☆

`Log4j`主要由`Loggers`(日志记录器), `Appenders`(输出端), `Layout`(格式化).
- `Loggers`控制日志输出级别与日志是否输出
- `Appenders`指定日志的输出方式(输出到控制台或文件等)
- `Layout`控制日志的输出格式

### JUL
Java Util Logging

JDK1.4发布的Java原生的日志框架, 使用时不需要另外引用第三方的类库, 相对其他的框架使用方便

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

是Apache基金下Jakarta小组开发的通用日志API

主要功能是给**所有的日志实现**提供一个**统一接口**, 本身也提供日志实现(SimpleLog),但是很弱一般不用.
常见的日志实现如:最早的**Log4j**以及JDK自带的**JUL**等

**面向接口编程**  解耦合
![JCL](https://s2.loli.net/2023/06/13/EZ62fIKGMwTsRDn.webp)



