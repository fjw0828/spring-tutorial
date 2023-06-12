# SpringBoot日志

日志记录了系统行为的时间, 地点, 状态等相关信息, 能够帮助我们了解并监控系统状态, 
在发生错误或者接近某种危险状态时能及时提醒我们处理, 同时在系统产生问题, 能够帮助我们快速定位, 诊断问题.

日志在生产环境的重要性无需赘述
总结有如下优点:
- 记录系统行为
- 监控系统状态
- 提示系统风险
- 定位系统问题

## System.out
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
在**高并发**的情况下, 会严重**影响性能**

阿里巴巴Java开发手册
>【强制】 应用中不可直接使用日志系统（ Log4j、 Logback） 中的 API，而应依赖使用日志框架
（ SLF4J、 JCL--Jakarta Commons Logging） 中的 API，使用门面模式的日志框架，有利于维护和
各个类的日志处理方式统一</br></br>
>【强制】 生产环境禁止直接使用 System.out 或 System.err 输出日志或使用
e.printStackTrace()打印异常堆栈</br>
>说明： 标准日志输出与标准错误输出文件每次 Jboss 重启时才滚动，如果大量输出送往这两个文件，容易
造成文件大小超过操作系统大小限制</br></br>
> 【强制】 在日志输出时，字符串变量之间的拼接使用占位符的方式</br></br>
> 【强制】 对于 trace/debug/info 级别的日志输出，必须进行日志级别的开关判断

## 日志体系



















