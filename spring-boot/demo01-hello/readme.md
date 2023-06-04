# demo01-hello
Spring Boot 项目的第一个模块

## 快速开发
按照官网一步一步操作即可: 
https://docs.spring.io/spring-boot/docs/current/reference/html/getting-started.html#getting-started.first-application

### 可能遇到的问题
#### pom.xml中出现Provides transitive vulnerable dependency maven:org.yaml:snakeyaml:1.33警告
- 告警原因:Maven项目中使用了一个被认为是有漏洞的依赖项，并且这个依赖项也被其他依赖项所传递
  - org.yaml:snakeyaml:1.33这个库是存在漏洞
- 解决警告:升级依赖项
  - 在中央仓库搜索无告警版本: https://mvnrepository.com/
  - 在Maven项目中，可以使用dependencyManagement标签来管理依赖项
  - 这个标签中，可以指定一个特定版本，以便所有依赖项都将使用这个版本
```yaml
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.yaml</groupId>
                <artifactId>snakeyaml</artifactId>
                <version>2.0</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
```
- 解决警告:移除依赖项
  - 如果这个库不是必须的，可以考虑从项目中移除它; 或者idea中设置忽略(眼不见心不烦)
```yaml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
            <exclusions>
                <exclusion>
                    <groupId>org.yaml</groupId>
                    <artifactId>snakeyaml</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
```
#### 项目启动报错：An incompatible version [1.1.29] of the Apache Tomcat Native library is installed, while Tomcat requires version [1.2.34]
- 打开网页 http://archive.apache.org/dist/tomcat/tomcat-connectors/native/
- 查找对应版本文件: http://archive.apache.org/dist/tomcat/tomcat-connectors/native/1.2.34/binaries/
- 下载对应的zip文件
- 解压文件,里面有32位和64位的 tcnative-1.dll 文件
- 根据自己的jdk和tomcat版本选择一个，复制到 jdk 的bin目录下即可
- 重启 spring boot项目

## 模块小结
### SpringBoot 是什么
SpringBoot 帮我们简单、快速地创建一个独立的、生产级别的 Spring 应用（说明：SpringBoot底层是Spring）

大多数 SpringBoot 应用只需要编写少量配置即可快速整合 Spring 平台以及第三方技术

### 特点
1. 简化整合
   - SpringBoot提出`场景启动器`的概念
   - 导入相关的场景，即拥有相关的功能。
   - 默认支持的所有场景：https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.build-systems.starters
     - 官方提供的场景：命名为：spring-boot-starter-*
     - 第三方提供场景：命名为：*-spring-boot-starter
   - 场景一导入，万物皆就绪
2. 简化开发
   - 无需编写任何配置，直接开发业务
3. 简化配置
   - application.properties：
   - 集中式管理配置。只需要修改这个文件就行 。
   - 配置基本都有默认值
   - 能写的所有配置都在： https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#appendix.application-properties
4. 简化部署
   - 打包为可执行的jar包。
   - linux服务器上有java环境。
5. 简化运维
   - 修改配置（外部放一个application.properties文件）、监控、健康检查。

## 应用分析
### 依赖管理机制

1. 为什么导入`场景启动器`所有的依赖就都导入进来了?
   - 根据`maven`的依赖传递原则, A依赖B,B依赖C;则A同时依赖B C
   - 以`web场景启动器`为例,引入`spring-boot-starter-web`
   - `spring-boot-starter-web` 会有很多依赖,包括其他`场景启动器`
     如`spring-boot-starter-tomcat`,`spring-web`,`spring-webmvc`等
   - **小结一下**: 该`场景启动器`会将所有的依赖提前准备好,根据`maven`的依赖传递原则就可全部引入

2. 为什么版本号不用写?
   - 每个boot项目都有一个父项目`spring-boot-starter-parent`
   - parent的父项目是`spring-boot-dependencies`
   - 也称`版本仲裁中心`,使用`dependencyManagement`标签已将所有常见的依赖版本号提前声明好

3. 自定义版本号
   - 根据`maven`的就近原则
   - 直接在当前项目`properties`标签中修改版本号
   - 或者直接在导入依赖的时候声明版本

4. 第三方的jar包
   - boot 父项目没有管理的需要自行声明好

![SpringBoot依赖管理](https://cdn.jsdelivr.net/gh/fu-jw/picture/hexoPic/springboot%E4%BE%9D%E8%B5%96%E7%AE%A1%E7%90%86.png)

### 自动装配机制
#### 自动配置
不用 SpringBoot 时, 要整合 SpringMVC 就需要自己手动配置:
- `DispatcherServlet`: 拦截请求的组件
- `ViewResolver`: 视图解析组件
- `CharacterEncodingFilter`: 处理字符编码的组件
- ...

现在有了 SpringBoot 以上组件都不用再自己手动配置了, 直接引入`web场景启动器`即可

#### 默认包扫描规则
- `@SpringBootApplication` 标注的类就是主程序类
- SpringBoot 会默认扫描主程序所在的包及其子包, 即自动的 **component-scan** 功能
- 可自定义扫描路径
  - @SpringBootApplication(scanBasePackages = "com.fredo")
  - @ComponentScan("com.fredo") 直接指定扫描的路径

#### 配置默认值
- **配置文件**的所有配置项和某个类的对象值进行一一绑定
- 绑定了配置文件中每一项值的类称为: **属性类**
- 参照[官方文档](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html)：或者参照绑定的**属性类**

#### 按需加载

- `场景启动器`除了导入相关功能依赖, 还会导入一个`spring-boot-starter`
- `spring-boot-starter`, 是所有**starter**的**starter**, 基础核心starter
- `spring-boot-starter`会导入包`spring-boot-autoconfigure`. 包里面都是全场景的 AutoConfiguration 自动配置类
- 虽然全场景的自动配置都在包:`spring-boot-autoconfigure`, 但并不会全都开启
  - 根据条件开启
  - `@ConditionalOnXxx`
  - `@ConditionalOnMissingXxx`

小结一下: 导入`场景启动器`, 触发`spring-boot-autoconfigure`这个包的自动配置生效, 容器中就会具有相关场景的功能

#### 自动配置流程细节梳理
1. 导入`场景启动器`, 以`spring-boot-starter-web`为例
   - 场景启动器就会导入相关场景的所有依赖, 如`starter-json`,`starter-tomcat`, `springmvc`
   - 每个场景启动器都会引入一个`spring-boot-starter`, 核心场景启动器
   - **核心场景启动器**会引入包:`spring-boot-autoconfigure`
   - `spring-boot-autoconfigure`里面包含所有场景的所有配置
   - 只要这个包下的所有类都能生效, 那么SpringBoot官方的整合功能就能生效
   - SpringBoot 默认扫描不到`spring-boot-autoconfigure`下的所有配置类(这些配置类给我们做了整合操作), 默认只扫描主程序所在的包
2. 主程序：@SpringBootApplication
   - `@SpringBootApplication`由三个注解组成: `@SpringBootConfiguration`, `@EnableAutoConfiguratio`, `@ComponentScan`
   - SpringBoot 默认只扫描主程序所在的包及其下子包, 扫描不到`spring-boot-autoconfigure`包中的**配置类**
   - `@EnableAutoConfiguration`: SpringBoot 开启自动配置的核心
      - 是由`@Import(AutoConfigurationImportSelector.class)`提供功能：批量给容器中导入组件
      - SpringBoot 默认加载142个配置类
      - 这142个配置类来自于`spring-boot-autoconfigure`下 META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports 文件指定
      - 项目启动的时候利用 @Import 批量导入组件机制, 把 autoconfigure 包下的**自动配置类**: xxxAutoConfiguration 导入进来(SpringBoot3.1.0有146个)
   - 按需生效
      - 这146个自动配置类并不全都生效
      - 每个自动配置类都有条件注解`@ConditionalOnXxx`, 只有条件成立, 才能生效
3. `xxxAutoConfiguration`自动配置类
   - 给容器中使用`@Bean`放一堆组件
   - 每个自动配置类都可能有这个注解`@EnableConfigurationProperties(XxxProperties.class)`, 用来把配置文件中配的指定前缀的属性值封装到`xxxProperties`属性类中
   - 以`Tomcat`为例: 把服务器的所有配置都是以`server`开头. 配置都封装到了属性类中
   - 给容器中放的所有组件的一些核心参数, 都来自于`XxxProperties`. `XxxProperties`都是和配置文件绑定
   - **只需要改配置文件的值, 核心组件的底层参数都能修改**
4. 只需关注业务, 全程无需关心各种整合(底层这些已经整合完成)

小结一下:
1. 导入`starter`, 就会导入`autoconfigure`包
2. `autoconfigure`包里有文件: META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports, 里面指定了所有启动要加载的**自动配置类**
3. `@EnableAutoConfiguration`会自动的把上面文件里面写的所有**自动配置类**都导入进来
4. `xxxAutoConfiguration`是有条件注解进行按需加载
5. `xxxAutoConfiguration`给容器中导入许多组件, 组件都是从`xxxProperties`中提取属性值
6. `xxxProperties`又和**配置文件**进行了绑定

达到的效果: 导入`starter`, 修改配置文件, 就能修改底层行为








