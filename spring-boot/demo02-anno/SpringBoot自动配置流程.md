# SpringBoot自动配置流程

在日常开发中, 通常我们只需要引入某个`场景启动器`, 再加上一些相应的配置即可, 无需费心复杂的整合操作, 这也是 SpringBoot 的强大之处.

SpringBoot 是如何省去繁杂的整合过程的呢?

接下来按照流程一步一步解析.

## 1.导入`starter`
以`web场景`为例, 导入了web开发场景
1. 场景启动器导入了相关场景的所有依赖, 如下:
    - `starter-json`,`starter-tomcat`,`springMVC`
2. 每个场景启动器都引入了一个`spring-boot-starter`, **核心场景启动器**
3. **核心场景启动器**引入了`spring-boot-autoconfigure`包
4. `spring-boot-autoconfigure`里面囊括了所有场景的所有配置
5. 只要这个包下的所有类都能生效, 那么相当于SpringBoot官方写好的整合功能就生效了
6. SpringBoot默认却扫描不到`spring-boot-autoconfigure`下写好的所有**配置类**.
   - 这些**配置类**给我们做了整合操作,默认只扫描主程序所在的包

## 2.主程序：@SpringBootApplication
为什么引入场景,无需配置就已经整合完成了?一切都要从`@SpringBootApplication`开始
1. `@SpringBootApplication`由三个注解组成`@SpringBootConfiguration`,`@EnableAutoConfiguration`,`@ComponentScan`
2. SpringBoot默认只能扫描自己主程序所在的包及其下面的子包, 扫描不到`spring-boot-autoconfigure`包中官方写好的**配置类**
3. `@EnableAutoConfiguration`就是SpringBoot开启**自动配置**的核心
```java
@Import(AutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration {
    //...
}
```
- @Import(AutoConfigurationImportSelector.class) 可以批量给容器中导入组件
- SpringBoot@3.1.0 会批量导入146个**自动配置类**(`xxxAutoConfiguration`)
```java
public class AutoConfigurationImportSelector implements DeferredImportSelector, BeanClassLoaderAware,
		ResourceLoaderAware, BeanFactoryAware, EnvironmentAware, Ordered { 
    // ...

    /**
     * 获取候选配置类
     * 位置:META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
     */
    protected List<String> getCandidateConfigurations(AnnotationMetadata metadata, AnnotationAttributes attributes) {
        List<String> configurations = ImportCandidates.load(AutoConfiguration.class, getBeanClassLoader())
                .getCandidates();
        Assert.notEmpty(configurations,
                "No auto configuration classes found in "
                        + "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports. If you "
                        + "are using a custom packaging, make sure that file is correct.");
        return configurations;
    }
    // ...
}
```
4. 按需生效
- 虽然导入了146个**自动配置类**, 并不是都能生效
- 每一个**自动配置类**, 都有条件注解`@ConditionalOnxxx`, 只有条件成立, 才能生效 

## 3.`xxxAutoConfiguration`自动配置类
1. 给容器中使用`@Bean`放一堆组件
2. 每个**自动配置类**都可能有这个注解`@EnableConfigurationProperties(XxxProperties.class)`
   - 用来把配置文件中配的指定前缀的属性值封装到`XxxProperties`**属性类**中
3. 以**Tomcat**为例：把服务器的所有配置都是以`server`开头的。配置都封装到了属性类中







