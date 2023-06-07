# SpringBoot 中常见注解

Spring 开始是使用的XML的配置方式, 但随着配置内容的增加, 编写XML配置文件的方式不仅繁琐, 而且还很容易出错,
另外，每个项目都编写大量的XML文件来配置Spring, 也大大增加了项目维护的复杂度, 
往往很多个项目的Spring XML文件的配置大部分是相同的, 只有很少量的配置不同, 这也造成了配置文件上的冗余
Spring注解驱动来开发, 你会发现以上这些问题都将不存在.
SpringBoot 则从一开始就改为全注解驱动.

## 0.基于XML配置方式

1. resources/ioc.xml
```xml
<bean id="person" class="com.fredo.bean.Person">
    <property name="id" value="1"/>
    <property name="name" value="fredo"/>
</bean>

<bean id="cat" class="com.fredo.bean.Cat">
    <property name="id" value="1"/>
    <property name="name" value="tom"/>
</bean>
```
2. Main.java
```java
// XML 的方式
var ioc = new ClassPathXmlApplicationContext("ioc.xml");
String[] names = ioc.getBeanDefinitionNames();

for (String name : names) {
    System.out.println("容器中的Bean:" + name);
}
```
3.output
```text
容器中的Bean:person
容器中的Bean:cat
```
## 1.注解的方式

在代码中使用`@Configuration`和`@Bean`两个注解, 即可替代以前复杂的xml配置

config/AppConfig.java
```java
@Configuration // 这是一个配置类, 替代以前的xml配置文件
public class AppConfig {
    @Bean // 替代以前xml中的bean标签
    public Person person(){
        var person = new Person();
        person.setId(1);
        person.setName("fred0");
        return person;
    }
    @Bean
    public Cat cat(){
        var cat = new Cat();
        cat.setId(1);
        cat.setName("Tom");
        return cat;
    }
}
```
## 2.常见注解

### 1.组件注册

#### @Component及其衍生组件(@Repository, @Controller, @Service, @Configuration)
- @Component 是通用组件
- @Repository 用于持久层
- @Controller 用于表现层
- @Service 用于业务逻辑层
- @Configuration 用于配置文件 

#### @Configuration和@Bean
- `@Configuration` 标注在**类**上, 声明这个类是一个配置类, 加入ioc容器中, 相当于以前的xml配置文件
- `@Bean` 标注在**方法**上, 该方法的输出结果就是一个 JavaBean 

`@Configuration`和`@Bean`配合使用就可完全替代XML的配置文件

`@SpringBootConfiguration`是 SpringBoot 中的特有注解, `@Configuration`是通用版注解, 功能一样
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
@Indexed
public @interface SpringBootConfiguration {
	@AliasFor(annotation = Configuration.class)
	boolean proxyBeanMethods() default true;
}
```

注意:
- bean的名称, 默认是方法名
- 可以修改, `@Bean("newName")`

<details> <summary>已经有`@Component`及其衍生组件,为什么还要`@Bean`注解?</summary>

- 类似`@Component`,`@Repository`,`@Controller`,`@Service`这些注册Bean的注解存在局限性:只能作用于自己编写的类, 如果是一个jar包第三方库要加入IOC容器的话, 这些注解就手无缚鸡之力了, 但是`@Bean`就可以做到这一点！
- 能够动态获取一个Bean对象，能够根据环境不同得到不同的Bean对象
</details>

### @Component和@ComponentScan
在实际项目中, 我们更多的是使用 Spring 的包扫描功能对项目中的包进行扫描, 
凡是在指定的包或其子包中的类上标注了`@Repository`, `@Service`, `@Controller`, `@Component`注解的类都会被扫描到, 并将这个类注入到 Spring 容器中

Spring 包扫描功能可以使用XML配置文件进行配置, 也可以直接使用`@ComponentScan`注解进行设置, 使用`@ComponentScan`注解进行设置比使用XML配置文件来配置要简单的多

XML方式
```xml
<!-- 包扫描：只要是标注了我们熟悉的@Controller、@Service、@Repository、@Component这四个注解中的任何一个的组件，它就会被自动扫描，并加进容器中 -->
<context:component-scan base-package="com.fredo"></context:component-scan>
```

`@ComponentScan`方式
```java
@ComponentScan(value="com.fredo") // value指定要扫描的包
@Configuration // 这是一个配置类, 替代以前的xml配置文件
public class AppConfig {
    // ...
}
```
注意:
0. 主程序标注了`@SpringBootApplication`, 就会默认扫描主程序所在的包及其子包; 
1. 如果设置了`@ComponentScan("...")`, 则以设置的扫描路径为主, 默认值不再生效
2. `@ComponentScan`还可以设置包含某些组件(`includeFilters()`)或者排除某些组件(`excludeFilters()`)
```java
@ComponentScan(value="com.fredo", excludeFilters={
    /*
     * type：指定你要排除的规则，是按照注解进行排除，还是按照给定的类型进行排除，还是按照正则表达式进行排除，等等
     * classes：除了@Controller和@Service标注的组件之外，IOC容器中剩下的组件我都要，即相当于是我要排除@Controller和@Service这俩注解标注的组件。
     */
    @Filter(type=FilterType.ANNOTATION, classes={Controller.class, Service.class})
}) // value指定要扫描的包
```
3. `@ComponentScan`是可重复注解
```java
@ComponentScan(value="com.fredo", includeFilters={
    /*
     * type：指定你要排除的规则，是按照注解进行排除，还是按照给定的类型进行排除，还是按照正则表达式进行排除，等等
     * classes：我们需要Spring在扫描时，只包含@Controller注解标注的类
     */
    @Filter(type=FilterType.ANNOTATION, classes={Controller.class})
}, useDefaultFilters=false) // value指定要扫描的包
@ComponentScan(value="com.fredo", includeFilters={
    /*
     * type：指定你要排除的规则，是按照注解进行排除，还是按照给定的类型进行排除，还是按照正则表达式进行排除，等等
     * classes：我们需要Spring在扫描时，只包含@Service注解标注的类
     */
    @Filter(type=FilterType.ANNOTATION, classes={Service.class})
}, useDefaultFilters=false) // value指定要扫描的包
```

小结一下:
- `@Component` 声明 Bean, 告诉 Spring 这是一个 Bean
- `@ComponentScan` 设置扫描路径, 告诉 Spring 到哪里找到这些 Bean 

### @ComponentScan.Filter
Spring 的强大之处不仅仅是提供了 IOC 容器, 
可以设置包含某些组件(`includeFilters()`)或者排除某些组件(`excludeFilters()`), 它还能够通过自定义`TypeFilter`来指定过滤规则. 

`FilterType`中常用的规则如下: 
1. ANNOTATION：按照注解进行包含或者排除
```java
@ComponentScan(value="com.fredo", includeFilters={
    /*
     * type：指定你要排除的规则，是按照注解进行排除，还是按照给定的类型进行排除，还是按照正则表达式进行排除，等等
     * classes：我们需要Spring在扫描时，只包含@Controller注解标注的类
     */
    @Filter(type=FilterType.ANNOTATION, classes={Controller.class})
}, useDefaultFilters=false) // value指定要扫描的包
```
2. ASSIGNABLE_TYPE：按照给定的类型进行包含或者排除
```java
@ComponentScan(value="com.fredo", includeFilters={
    /*
     * type：指定你要排除的规则，是按照注解进行排除，还是按照给定的类型进行排除，还是按照正则表达式进行排除，等等
     */
    // 只要是BookService这种类型的组件都会被加载到容器中，不管是它的子类还是什么它的实现类。记住，只要是BookService这种类型的
    @Filter(type=FilterType.ASSIGNABLE_TYPE, classes={BookService.class})
}, useDefaultFilters=false) // value指定要扫描的包
```
3. ASPECTJ：使用表达式来挑选复杂的类子集
```java
@ComponentScan(value="com.fredo", includeFilters={
    /*
     * type：指定你要排除的规则，是按照注解进行排除，还是按照给定的类型进行排除，还是按照正则表达式进行排除，等等
     */
    @Filter(type=FilterType.ASPECTJ, classes={AspectJTypeFilter.class})
}, useDefaultFilters=false) // value指定要扫描的包
```
4. REGEX：按照正则表达式进行包含或者排除
```java
@ComponentScan(value="com.fredo", includeFilters={
    /*
     * type：指定你要排除的规则，是按照注解进行排除，还是按照给定的类型进行排除，还是按照正则表达式进行排除，等等
     */
    @Filter(type=FilterType.REGEX, classes={RegexPatternTypeFilter.class})
}, useDefaultFilters=false) // value指定要扫描的包
```
5. CUSTOM：按照自定义规则进行包含或者排除
```java
@ComponentScan(value="com.fredo", includeFilters={
    /*
     * type：指定你要排除的规则，是按照注解进行排除，还是按照给定的类型进行排除，还是按照正则表达式进行排除，等等
     */
    // 指定新的过滤规则，这个过滤规则是我们自个自定义的，过滤规则就是由我们这个自定义的MyTypeFilter类返回true或者false来代表匹配还是没匹配
    @Filter(type=FilterType.CUSTOM, classes={MyTypeFilter.class})
}, useDefaultFilters=false) // value指定要扫描的包
```
MyTypeFilter.java
```java
public class MyTypeFilter implements TypeFilter {
	/**
	 * 参数：
	 * metadataReader：读取到的当前正在扫描的类的信息
	 * metadataReaderFactory：可以获取到其他任何类的信息的（工厂）
     * 
     * 返回值:
     * 当返回true时，表示符合规则，会包含在Spring容器中；
     * 当返回false时，表示不符合规则，就不会被包含在Spring容器中
	 */
	@Override
	public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
		// 自定义过滤规则
        return false; 
	}
}
```
属性值:
- type: 要排除的方式:`FilterType`
- value: 要排除的类型, 同 `classes`
- classes: 同 `value`
- pattern: 正则表达式

### @Scope
用于设置组件的**作用域**, 默认值是`singleton`单例

作用域取值:
- singleton: 单例, 默认值, 容器启动就会调用方法创建对象到ioc容器中
- prototype: 多例, 容器启动时并不会调用方法创建对象, 每次获取时才会调用方法创建对象
- request: web 环境中, 同一个请求只创建一个对象
- session: web 环境中, 同一个 session 只创建一个对象
- application: web 环境中, 一个应用程序只创建一个对象, 全局

### @Lazy
懒加载就是Spring容器启动的时候, 先不创建对象, 在第一次使用(获取)bean的时候再来创建对象, 并进行一些初始化

注意: 懒加载, 仅针对单例bean生效

### 
