# SpringBoot自动配置原理浅析
>我的[SpringBoot项目](https://github.com/fu-jw/spring-tutorial)第二个模块

在日常开发中, 通常我们只需要引入某个`场景启动器`, 再加上一些相应的配置即可, 无需费心复杂的整合操作, 这也是 SpringBoot 的强大之处.

SpringBoot 是如何省去繁杂的整合过程的呢?

接下来按照流程一步一步解析.
## SpringBoot自动配置流程
### 1.导入`starter`
以`web场景`为例, 导入了web开发场景
1. 场景启动器导入了相关场景的所有依赖, 如下:
    - `starter-json`,`starter-tomcat`,`springMVC`
2. 每个场景启动器都引入了一个`spring-boot-starter`, **核心场景启动器**
3. **核心场景启动器**引入了`spring-boot-autoconfigure`包
4. `spring-boot-autoconfigure`里面囊括了所有场景的所有配置
5. 只要这个包下的所有类都能生效, 那么相当于SpringBoot官方写好的整合功能就生效了
6. SpringBoot默认却扫描不到`spring-boot-autoconfigure`下写好的所有**配置类**.
   - 这些**配置类**给我们做了整合操作,默认只扫描主程序所在的包

### 2.主程序：@SpringBootApplication
为什么引入场景,无需配置就已经整合完成了?一切都要从`@SpringBootApplication`开始

`@SpringBootApplication`由三个注解组成`@SpringBootConfiguration`,`@EnableAutoConfiguration`,`@ComponentScan`
   - `@SpringBootConfiguration` 其实就是`@Configuration`, 声明主程序类是一个**配置类**
   - `@ComponentScan` SpringBoot默认只能扫描自己主程序所在的包及其下面的子包, 扫描不到`spring-boot-autoconfigure`包中官方写好的**配置类**
   - `@EnableAutoConfiguration`就是SpringBoot开启**自动配置**的核心
```java
@Import(AutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration {
    //...
}
```
- **`@Import(AutoConfigurationImportSelector.class)` 可以批量给容器中导入组件**
- SpringBoot@3.1.0 会批量导入146个**自动配置类**(`xxxAutoConfiguration`)
```java
public class AutoConfigurationImportSelector implements DeferredImportSelector, BeanClassLoaderAware,
		ResourceLoaderAware, BeanFactoryAware, EnvironmentAware, Ordered { 
    // ...

   /**
    * 批量获取候选配置
    * @param    annotationMetadata 注解元信息(主程序全类名,本项目为:com.fredo.Main)
    * @return   候选配置的数组
    */
   @Override
   public String[] selectImports(AnnotationMetadata annotationMetadata) {
      if (!isEnabled(annotationMetadata)) {
         return NO_IMPORTS;
      }
      // 获取自动配置节点
      AutoConfigurationEntry autoConfigurationEntry = getAutoConfigurationEntry(annotationMetadata);
      return StringUtils.toStringArray(autoConfigurationEntry.getConfigurations());
   }

   /**
    * 获取自动配置节点
    */
   protected AutoConfigurationEntry getAutoConfigurationEntry(AnnotationMetadata annotationMetadata) {
      if (!isEnabled(annotationMetadata)) {
         return EMPTY_ENTRY;
      }
      // 返回注解属性, 类型:LinkedHashMap, 值(exclude->[],excludeName->[])
      AnnotationAttributes attributes = getAttributes(annotationMetadata);
      // 获取候选配置类 值(146个自动配置类的全类名)
      List<String> configurations = getCandidateConfigurations(annotationMetadata, attributes);
      // 去重, 移除重复项
      configurations = removeDuplicates(configurations);
      // 获取排除项
      Set<String> exclusions = getExclusions(annotationMetadata, attributes);
      checkExcludedClasses(configurations, exclusions);
      // 移除要排除的配置项
      configurations.removeAll(exclusions);
      configurations = getConfigurationClassFilter().filter(configurations);
      fireAutoConfigurationImportEvents(configurations, exclusions);
      return new AutoConfigurationEntry(configurations, exclusions);
   }

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
- **按需生效**
  - 虽然导入了146个**自动配置类**, 并不是都能生效
  - 每一个**自动配置类**, 都有条件注解`@ConditionalOnxxx`, 只有条件成立, 才能生效 
  - 导入对应的依赖包, 即满足条件, 该自动配置类就会生效

### 3.`xxxAutoConfiguration`自动配置类
1. 每个`xxxAutoConfiguration`自动配置类,都需要满足某个条件
```java
@ConditionalOnClass(Xxx.class) // 条件就是需要引入依赖包含 Xxx.class
@Conditional(XxxCondition.class)
@EnableConfigurationProperties(XxxProperties.class)
public class XxxAutoConfiguration {
    // ...
}
```
2. 条件满足, **自动配置类**就会给容器中使用`@Bean`放一堆组件

3. 每个**自动配置类**都可能有这个注解`@EnableConfigurationProperties(XxxProperties.class)`
   - 用来把配置文件中配的**指定前缀**的属性值封装到`XxxProperties`**属性类**中
4. 以**Tomcat**为例：把服务器的所有配置都是以`server`开头的。配置都封装到了属性类中
```java
@ConfigurationProperties(prefix = "server", ignoreUnknownFields = true)
public class ServerProperties {
    // ...
}
```

## SpringBoot核心配置流程
1. 导入`starter`, 就会导入`autoconfigure`包
2. `autoconfigure`包里有文件:**META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports**,里面指定的所有启动要加载的**自动配置类**
3. `@EnableAutoConfiguration`会自动的把上面文件里所有**自动配置类**都导入进来. `XxxAutoConfiguration`是有**条件注解进行按需加载**
4. `XxxAutoConfiguration`给容器中导入一堆组件, 组件都是从`xxxProperties`中提取属性值
5. `XxxProperties`又和**配置文件**进行绑定

达到的效果:导入`starter` --> 修改配置文件, 就能修改底层行为

![SpringBoot自动装配](https://cdn.jsdelivr.net/gh/fu-jw/picture/hexoPic/SpringBoot%E8%87%AA%E5%8A%A8%E8%A3%85%E9%85%8D.png)

## 小试牛刀
接下来检验下SpringBoot自动配置原理的理解, 尝试回答如下问题:

<details> <summary><strong><span style="color: red; ">为什么项目启动后默认端口号是8080, 以及如何修改???</span></strong></summary>

1. 引入`web场景`
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```
自动引入了`spring-boot-starter`和`spring-boot-starter-tomcat`
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
    <version>3.1.0</version>
    <scope>compile</scope>
</dependency>
```
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-tomcat</artifactId>
    <version>3.1.0</version>
    <scope>compile</scope>
</dependency>
```
`spring-boot-starter`自动引入依赖`spring-boot-autoconfigure`包
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-autoconfigure</artifactId>
    <version>3.1.0</version>
    <scope>compile</scope>
</dependency>
```
`spring-boot-autoconfigure`
包里有文件:
- **additional-spring-configuration-metadata.json**, 里面包含了所有的默认值
```json
{
  "name": "server.port",
  "defaultValue": 8080
}
```
- **META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports**,里面指定的所有启动要加载的**自动配置类**

![spring-boot-autoconfigure包](https://cdn.jsdelivr.net/gh/fu-jw/picture/hexoPic/autoconfig%E5%8C%85.png)

2. 合成注解`@SpringBootApplication`中的`@EnableAutoConfiguration`
   自动的把上面文件里所有**自动配置类**都导入进来. `XxxAutoConfiguration`是有**条件注解进行按需加载**

其中就包括`ServletWebServerFactoryAutoConfiguration.java`
```java
@AutoConfiguration(after = SslAutoConfiguration.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnClass(ServletRequest.class)
@ConditionalOnWebApplication(type = Type.SERVLET)
@EnableConfigurationProperties(ServerProperties.class)
@Import({ ServletWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar.class,
		ServletWebServerFactoryConfiguration.EmbeddedTomcat.class,
		ServletWebServerFactoryConfiguration.EmbeddedJetty.class,
		ServletWebServerFactoryConfiguration.EmbeddedUndertow.class })
public class ServletWebServerFactoryAutoConfiguration {
    // ...
}
```
绑定了属性文件`ServerProperties`
```java
@ConfigurationProperties(prefix = "server", ignoreUnknownFields = true)
public class ServerProperties {
    private Integer port;
    // ...
}
```
只需要在自己的配置文件中自定义`server.port`的值

SpringBoot启动时就会去resources路径下加载符合要求的文件, 从该文件中查找配置并覆盖默认配置, 
于是就完成了配置自定义

`spring-boot-starter-parent-3.1.0.pom`文件:
```xml
<build>
    <resources>
      <resource>
        <directory>${basedir}/src/main/resources</directory>
        <filtering>true</filtering>
        <includes>
          <include>**/application*.yml</include>
          <include>**/application*.yaml</include>
          <include>**/application*.properties</include>
        </includes>
      </resource>
      <resource>
        <directory>${basedir}/src/main/resources</directory>
        <excludes>
          <exclude>**/application*.yml</exclude>
          <exclude>**/application*.yaml</exclude>
          <exclude>**/application*.properties</exclude>
        </excludes>
      </resource>
    </resources>
    ...
</build>
```
</details>



