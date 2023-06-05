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
3.result
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
        cat.setName("fred0");
        return cat;
    }
}
```
## 2.常见注解

### 1.组件注册

#### @Configuration和@Bean
`@Configuration` 标注在**类**上, 声明这个类是一个配置类, 加入ioc容器中, 相当于以前的xml配置文件
`@Bean` 标注在**方法**上, 该方法的输出结果就是一个 JavaBean 

`@Configuration`和`@Bean`配合使用就可完全替代XML的配置文件

注意:
- bean的名称, 默认是方法名
- 可以修改, `@Bean("newName")`








