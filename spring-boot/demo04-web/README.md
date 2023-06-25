# SpringBoot Web开发

> 我的[SpringBoot项目](https://github.com/fu-jw/spring-tutorial)第四个模块

由前面[SpringBoot快速入门](https://blog.fu-jw.com/posts/4ccc97e8.html)分析可知,SpringBoot提出场景启动器的概念,
将场景中需要的所有依赖囊括进来,并自动装配,简化配置.
场景一引入,配置即完成

web开发同样需要**web场景启动器**

## 0.引入web场景启动器

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

`spring-boot-starter-web`依赖**核心场景启动器:**`spring-boot-starter`
`spring-boot-starter`依赖:`spring-boot-autoconfigure`
`spring-boot-autoconfigure`会加载:META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
中的自动配置类
其中web相关有如下:

```text
org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration
org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration
org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration
org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration
org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration
org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration
org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
------------------------------以下是响应式编程相关--------------------------------------
org.springframework.boot.autoconfigure.web.reactive.HttpHandlerAutoConfiguration
org.springframework.boot.autoconfigure.web.reactive.ReactiveMultipartAutoConfiguration
org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration
org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration
org.springframework.boot.autoconfigure.web.reactive.WebSessionIdResolverAutoConfiguration
org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration
org.springframework.boot.autoconfigure.web.reactive.function.client.ClientHttpConnectorAutoConfiguration
org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration
```

上面的自动配置类会绑定的配置有:

- `server` ==>服务器相关
- `spring.mvc` ==>springMVC相关
- `server.servlet.encoding` ==>servlet编码相关
- `spring.servlet.multipart` ==>servlet文件处理相关

SpringBoot 的自动装配功能在`web场景`中添加了如下特性:

[官网介绍](https://docs.spring.io/spring-boot/docs/current/reference/html/web.html#web.servlet.spring-mvc):
> 1. Inclusion of ContentNegotiatingViewResolver and BeanNameViewResolver beans.
>2. Support for serving static resources, including support for WebJars (covered later in this document).
>3. Automatic registration of Converter, GenericConverter, and Formatter beans.
>4. Support for HttpMessageConverters (covered later in this document).
>5. Automatic registration of MessageCodesResolver (covered later in this document).
>6. Static index.html support.
>7. Automatic use of a ConfigurableWebBindingInitializer bean (covered later in this document).
>
> If you want to keep those Spring Boot MVC customizations and make more
> MVC customizations (interceptors, formatters, view controllers, and other features),
> you can add your own @Configuration class of type WebMvcConfigurer but without @EnableWebMvc.
>
> If you want to provide custom instances of RequestMappingHandlerMapping,
> RequestMappingHandlerAdapter, or ExceptionHandlerExceptionResolver,
> and still keep the Spring Boot MVC customizations, you can declare a bean of
> type WebMvcRegistrations and use it to provide custom instances of those components.
>
> If you want to take complete control of Spring MVC,
> you can add your own @Configuration annotated with @EnableWebMvc,
> or alternatively add your own @Configuration-annotated DelegatingWebMvcConfiguration
> as described in the Javadoc of @EnableWebMvc

`web场景`引入后,就有了如下功能:

1. 包含`ContentNegotiatingViewResolver`和`BeanNameViewResolver`组件,方便视图解析
2. 默认的静态资源处理机制: 静态资源放在**static**文件夹下即可直接访问
3. 自动注册了`Converter`,`GenericConverter`,`Formatter`组件,适配常见**数据类型转换**和**格式化**需求
4. 支持`HttpMessageConverters`,可以方便返回json等数据类型
5. 自动注册`MessageCodesResolver`,方便**国际化**及错误消息处理
6. 支持静态 index.html
7. 自动使用`ConfigurableWebBindingInitializer`,实现**消息处理**,**数据绑定**,**类型转化**,**数据校验**等功能

注意:

- 如果想保持 SpringBoot MVC 的默认配置,并且自定义更多的 MVC 配置,如:`interceptors`,`formatters`,`view controllers`
  等.可以使用`@Configuration`注解添加一个**WebMvcConfigurer**类型的配置类,但不要标注`@EnableWebMvc`
- 如果想保持 SpringBoot MVC
  的默认配置,但要自定义核心组件实例,比如:`RequestMappingHandlerMapping`,`RequestMappingHandlerAdapter`
  ,或`ExceptionHandlerExceptionResolver`,给容器中放一个`WebMvcRegistrations`组件即可
- 如果想全面接管 Spring MVC,`@Configuration`标注一个配置类,并加上`@EnableWebMvc`注解,实现**WebMvcConfigurer**接口

接下来分别展开分析.

## 1.静态资源

由前面知道引入`web场景`会加载自动配置类:`WebMvcAutoConfiguration`

下面简要分析`WebMvcAutoConfiguration`原理

### 1.生效条件

```java

@AutoConfiguration(after = {DispatcherServletAutoConfiguration.class, TaskExecutionAutoConfiguration.class,
        ValidationAutoConfiguration.class}) // 需要在这三个自动配置之后
@ConditionalOnWebApplication(type = Type.SERVLET) // //需要是普通的SERVLET类型的 web应用就生效,REACTIVE类型的响应式web是另一套
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class}) // 需要包括这三个类
@ConditionalOnMissingBean(WebMvcConfigurationSupport.class) // 需要容器中没有这个bean
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10) // 优先级
@ImportRuntimeHints(WebResourcesRuntimeHints.class)
// 项目启动就注册的静态资源:"META-INF/resources/","resources/","static/","public/"
public class WebMvcAutoConfiguration {
    //...
}
```

生效后, 在容器中放入两个bean:

### 2.`HiddenHttpMethodFilter`: 过滤页面表单提交的Rest请求

> - Rest 请求有: `GET`, `POST`, `PUT`, `DELETE`, `PATCH`
>- 请求数据包含三部分: **请求行**, **请求头**, **请求体**
>- 浏览器只能发送`GET`请求和`POST`请求
>- `GET`请求没有**请求体**, 参数在**请求行**中,获取参数的方法是`getQueryString()`
>- `POST`请求的参数在**请求体**中, 获取方法是`getReader()`或`getInputStream()`
>- getMethod() 获取具体哪种请求方式

```java
/**
 * 该方法将浏览器不支持的请求,转换成标准的HTTP方法:
 *
 * 将发出请求的方法参数转换为 HTTP 方法，可通过 HttpServletRequest.getMethod() 检索。
 * 由于浏览器目前仅支持GET和POST，通常是使用带有附加隐藏表单字段（_method）的普通POST来传递“真正的”HTTP方法。
 * 例如:<form action="..." method="post">
 *       <input type="hidden" name="_method" value="put" />
 *       ......
 *     </form>
 * 此过滤器读取该参数并相应地更改 HttpServletRequestWrapper.getMethod（） 返回值。
 * 只允许使用“PUT”、“DELETE”和“PATCH” HTTP方法。
 * 请求参数的名称默认为 _method，但可以通过 methodParam 属性进行调整。
 *
 * 注意：在大部分 POST 请求的情况下，此过滤器需要在 MultipartFilter 处理后运行，因为它本来就需要检查 POST 正文参数。
 * 所以通常，在你的web.xml过滤器链中，在隐藏的HttpMethodFilter之前放置一个 
 * Spring org.springframework.web.multipart.support.MultipartFilter。
 *
 * 即在此过滤之前会有专门处理 POST 请求的 MultipartFilter.
 */
public class HiddenHttpMethodFilter extends OncePerRequestFilter {

    private static final List<String> ALLOWED_METHODS =
            List.of(HttpMethod.PUT.name(), HttpMethod.DELETE.name(), HttpMethod.PATCH.name());

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        HttpServletRequest requestToUse = request;

        if ("POST".equals(request.getMethod()) && request.getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE) == null) {
            String paramValue = request.getParameter(this.methodParam);
            if (StringUtils.hasLength(paramValue)) {
                String method = paramValue.toUpperCase(Locale.ENGLISH);
              if (ALLOWED_METHODS.contains(method)) {
                requestToUse = new HttpMethodRequestWrapper(request, method);
              }
            }
        }
      filterChain.doFilter(requestToUse, response);
    }
  // ...
}
```

### 3.`FormContentFilter`:用于分析表单内容, 与前面的过滤器配合使用,

同样只针对`PUT`,`PATCH`,`DELETE`三种HTTP请求

### 4.静态内部类`WebMvcAutoConfigurationAdapter`

在`WebMvcAutoConfiguration`中有静态内部类`WebMvcAutoConfigurationAdapter`源码如下:

```java
// Defined as a nested config to ensure WebMvcConfigurer is not read when not on the classpath
@Configuration(proxyBeanMethods = false)
@Import(EnableWebMvcConfiguration.class)
@EnableConfigurationProperties({WebMvcProperties.class, WebProperties.class})
@Order(0)
public static class WebMvcAutoConfigurationAdapter implements WebMvcConfigurer, ServletContextAware {
  // ...
}
```

#### 接口`WebMvcConfigurer`

`WebMvcAutoConfigurationAdapter`实现了`WebMvcConfigurer`接口,可以重写一些有关Web MVC的配置方法,比如添加拦截器、配置视图解析器、配置静态资源等.
通过重写这些方法,可以根据自己的需要定制化Web MVC的行为.可定制功能有:

![WebMvcConfigurer](https://s2.loli.net/2023/06/22/dijPSRce7JlBIt3.webp)

- addArgumentResolvers:添加参数解析器
- addCorsMappings:添加跨域映射
- addFormatters:添加格式化器
- addInterceptors:添加拦截器
- addResourceHandlers:添加资源处理器,处理静态资源规则
- addReturnValueHandlers:添加返回值处理器
- addViewControllers:添加视图控制器,指定某个请求路径跳转到指定页面
- configureAsyncSupport:配置异步支持
- configureContentNegotiation:配置内容协商
- configureDefaultServletHandling:配置默认的处理,默认接收: /
- configureHandlerExceptionResolvers:配置异常解析器
- configureMessageConverters:配置消息转化器
- configurePathMatch:配置路径匹配
- configureViewResolvers:配置视图解析器
- extendHandlerExceptionResolvers:扩展处理异常解析器
- extendMessageConverters扩展消息转换器
- getMessageCodesResolver:获取消息编码解析器
- getValidator:获取校验器

#### 静态资源规则源码浅析

由上面分析,`addResourceHandlers`用来处理静态资源,源码:

```java
private static final String SERVLET_LOCATION="/";
/**
 * Add handlers to serve static resources such as images, js, and, css files from specific locations under web application root, the classpath,and others.
 * 即根据配置情况，添加不同的静态资源处理器，用于处理静态资源的访问请求
 */
public void addResourceHandlers(ResourceHandlerRegistry registry){
        // 判断是否需要启用默认的资源处理。
        // 如果不需要启用，默认资源处理被禁用，同时输出调试信息，并直接返回
        if(!this.resourceProperties.isAddMappings()){
        logger.debug("Default resource handling disabled");
        return;
        }
        // 需要启用默认资源处理,下面添加两种静态资源处理规则
        // private String webjarsPathPattern = "/webjars/**";        
        // 访问路径:/webjars/**, 就去路径:classpath:/META-INF/resources/webjars/ 下面找对应资源
        addResourceHandler(registry,this.mvcProperties.getWebjarsPathPattern(),
        "classpath:/META-INF/resources/webjars/");
        // private String staticPathPattern = "/**";
        // private String[] staticLocations = 
        // "classpath:/META-INF/resources/","classpath:/resources/", "classpath:/static/", "classpath:/public/"
        addResourceHandler(registry,this.mvcProperties.getStaticPathPattern(),(registration)->{
        registration.addResourceLocations(this.resourceProperties.getStaticLocations());
        if(this.servletContext!=null){
        ServletContextResource resource=new ServletContextResource(this.servletContext,SERVLET_LOCATION);
        registration.addResourceLocations(resource);
        }
        });
        }
```

```java
/**
 * 向资源处理器注册表中添加资源处理器,并根据配置文件和自定义函数设置资源处理器的属性
 */
private void addResourceHandler(ResourceHandlerRegistry registry,String pattern,
        Consumer<ResourceHandlerRegistration> customizer){
        // 判断是否已经存在了指定的pattern
        if(registry.hasMappingForPattern(pattern)){
        return;
        }
        // 创建一个资源处理器注册（ResourceHandlerRegistration）对象
        ResourceHandlerRegistration registration=registry.addResourceHandler(pattern);
        // 将刚才创建的资源处理器注册对象作为参数，用于自定义资源处理器的属性
        customizer.accept(registration);
        // 根据配置文件中的缓存时间设置资源处理器的缓存时间（cache period），以及缓存控制（cache control）策略
        registration.setCachePeriod(getSeconds(this.resourceProperties.getCache().getPeriod()));
        registration.setCacheControl(this.resourceProperties.getCache().getCachecontrol().toHttpCacheControl());
        registration.setUseLastModified(this.resourceProperties.getCache().isUseLastModified());
        // 进一步自定义资源处理器注册对象的属性
        customizeResourceHandlerRegistration(registration);
        }
```

小结一下:
静态资源访问规则如下:

- 规则一 访问： `/webjars/**`路径就去`classpath:/META-INF/resources/webjars/`下找资源.
- 规则二 访问： `/**`路径就去 静态资源默认的四个位置找资源
  - `classpath:/META-INF/resources/`
  - `classpath:/resources/`
  - `classpath:/static/`
  - `classpath:/public/`
- 规则三 静态资源默认都有**缓存规则**的设置
  - 所有缓存的设置, 直接通过配置文件:`spring.web`
  - cachePeriod: 缓存周期; 多久不用找服务器要新的, 默认没有，以秒为单位
  - cacheControl: **HTTP缓存控制**, [查看文档](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Caching)
  - useLastModified：是否使用最后一次修改, 配合HTTP Cache规则, 如果浏览器访问了一个静态资源

> 如果浏览器访问了一个静态资源 index.js，如果服务这个资源没有发生变化，下次访问的时候就可以直接让浏览器用自己缓存中的东西，而不用给服务器发请求



#### HTTP缓存实验

设置如下:

```yaml
#1、spring.web：
# 1.配置国际化的区域信息(locale)
# 2.静态资源策略(开启、处理链、缓存)

spring:
  web:
    resources:
      add-mappings: true # 开启静态资源映射规则,默认true
      cache:
        period: 3600 # 单位是 秒, 此为简要设置, 下面(cache-control)是详细配置,会覆盖period
        cache-control:
          max-age: 7200 # 浏览器第一次请求服务器，服务器告诉浏览器此资源缓存7200秒，7200秒以内的所有此资源访问不用发给服务器请求，7200秒以后发请求给服务器
        use-last-modified: true # 默认true, 使用资源 last-modified 时间，来对比服务器和浏览器的资源是否相同没有变化。相同返回 304
```

第一次请求:

![第一次请求](https://s2.loli.net/2023/06/23/LX86W9SCGMmNPEl.webp)

第二次请求:

![第二次请求](https://s2.loli.net/2023/06/23/M9q5A2UQWSv7l3E.webp)

### 5.自定义静态资源

大体分为两种方式:

- 配置文件的方式
- 代码的方式

#### 配置文件

```java

@Configuration(proxyBeanMethods = false)
@Import(EnableWebMvcConfiguration.class)
@EnableConfigurationProperties({WebMvcProperties.class, WebProperties.class})
@Order(0)
public static class WebMvcAutoConfigurationAdapter implements WebMvcConfigurer, ServletContextAware {
  // ...
}
```

与两个配置文件绑定`WebMvcProperties.class`和`WebProperties.class`,
即以`spring.web`和`spring.mvc`开头的配置

- `spring.web`:可配置locale(国际化)和resources(静态资源相关),具体可查看`WebProperties.class`
- `spring.mvc`:可配置内容很多,具体可查看`WebMvcProperties.class`

```yaml
spring:
  web:
    resources:
      add-mappings: true # 开启静态资源映射规则,默认true
      cache:
        period: 3600 # 单位是 秒, 此为简要设置, 下面(cache-control)是详细配置,会覆盖period
        cache-control:
          max-age: 7200 # 浏览器第一次请求服务器，服务器告诉浏览器此资源缓存7200秒，7200秒以内的所有此资源访问不用发给服务器请求，7200秒以后发请求给服务器
        use-last-modified: true # 默认true, 使用资源 last-modified 时间，来对比服务器和浏览器的资源是否相同没有变化。相同返回 304
      static-locations: classpath:/static/,classpath:/test/ # 自定义静态资源目录,按顺序访问
  mvc:
    webjars-path-pattern: /webjars/** # 自定义webjars路径前缀,默认:/webjars/**
    static-path-pattern: /static/** # 静态资源访问路径前缀,默认:/**
```

#### 代码方式

就是在容器中放置组件:`WebMvcConfigurer`,来配置底层.

注意:

- 默认配置仍有效
- 加上`@EnableWebMvc`会将默认配置失效

```java

@Configuration
public class MyConfig {
  @Bean
  public WebMvcConfigurer webMvcConfigurer() {// 与下面代码一样效果
    return new WebMvcConfigurer() {
      @Override
      public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("/static");
      }
    };
  }
}
```

```java

@Configuration
public class MyConfig implements WebMvcConfigurer { // 还可以上面写法,效果一样
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 保留默认配置
    WebMvcConfigurer.super.addResourceHandlers(registry);

    // 自定义配置
    registry.addResourceHandler("/static/**")// 设置静态资源访问前缀,同配置文件中的spring.mvc.static-path-pattern:
            .addResourceLocations("/static");// 设置静态资源获取路径,同配置文件中的spring.web.resources.static-locations: classpath:/static/

  }
}
```

<details> 
<summary><strong><span style="color: red; ">为什么容器中含有WebMvcConfigurer组件,就能配置底层行为???</span></strong></summary>

1. `WebMvcAutoConfiguration`是一个自动配置类, 它里面有一个`EnableWebMvcConfiguration`
2. `EnableWebMvcConfiguration`继承与`DelegatingWebMvcConfiguration`, 这两个都生效
3. `DelegatingWebMvcConfiguration`利用**DI**把容器中所有`WebMvcConfigurer`注入进来
4. 当调用`DelegatingWebMvcConfiguration`的方法配置底层规则, 而它调用所有`WebMvcConfigurer`的配置底层方法

</details>

## 2.路径匹配

**Spring5.3**之后加入了更多的**请求路径匹配**的实现策略,

以前只支持**AntPathMatcher**策略,现在提供了**PathPatternParser**策略,
并且可以指定使用哪种策略

默认使用**PathPatternParser**策略

### 1.AntPathMatcher策略

Ant 风格的路径模式语法具有以下规则:

- *：表示**任意数量**的**字符**,0~n
- ?：表示**任意一个**字符,
- \**：表示**任意数量**的**目录**
- {}：表示一个命名的模式**占位符**
- []：表示字符集合,例如[a-z]表示小写字母

例如:

- <strong>*.html</strong> 匹配任意名称,且扩展名为.html的文件
- <strong>/folder1/*/*.java</strong> 匹配在folder1目录下的任意两级目录下的.java文件
- <strong>/folder2/**/*.jsp</strong> 匹配在folder2目录下任意目录深度的.jsp文件
- <strong>/{type}/{id}.html</strong> 匹配任意文件名为{id}.html,在任意命名的{type}目录下的文件

```text
注意：Ant 风格的路径模式语法中的特殊字符需要转义,如:
1. 要匹配文件路径中的星号,则需要转义为\\*
2. 要匹配文件路径中的问号,则需要转义为\\?
```

代码测试:

```java

@Slf4j
@RestController
public class AntPathController {
  @GetMapping("/a*/b?/{p1:[a-f]+}")
  public String hello(HttpServletRequest request,
                      @PathVariable("p1") String path) {

    log.info("路径变量p1： {}", path);
    //获取请求路径
    return request.getRequestURI();
  }
}
```

访问:http://localhost:8080/ads/bd/adf

![AntPathMatcher策略](https://s2.loli.net/2023/06/23/4BUG8Jv7jnVcfR2.webp)

控制台打印:
> 路径变量p1： adf

### 2.PathPatternParser策略

> - 基准测试下,有**6~8**倍吞吐量提升,降低30%~40%空间分配率
>- 兼容**AntPathMatcher**语法,并支持更多类型的路径模式

```text
注意:"**" 多段匹配的支持仅允许在模式末尾使用
```

### 3.修改默认策略

- 配置文件:spring.mvc.pathmatch.matching-strategy=ant_path_matcher
- 代码修改:

```java
/**
 * 此方法可以修改路径匹配规则
 * 从spring5.3 开始,默认 PathPatternParser
 * 想要修改为 AntPathMatcher,则只需设置为空即可
 */
@Override
public void configurePathMatch(PathMatchConfigurer configurer){
        configurer.setPatternParser(null);
        }
```

### 小结一下

- 使用默认的路径匹配规则(`PathPatternParser`)即可,性能高,兼容Ant风格
- 如果中间需要双星(**),只能换回Ant风格

SpringBoot 底层匹配策略:
WebMvcAutoConfiguration.java

```java
@Override
public void configurePathMatch(PathMatchConfigurer configurer){
        // 只有 ANT_PATH_MATCHER 才条件成立,创建 new AntPathMatcher()
        if(this.mvcProperties.getPathmatch()
        .getMatchingStrategy()==WebMvcProperties.MatchingStrategy.ANT_PATH_MATCHER){
        configurer.setPathMatcher(new AntPathMatcher());
        this.dispatcherServletPath.ifAvailable((dispatcherPath)->{
        String servletUrlMapping=dispatcherPath.getServletUrlMapping();
        if(servletUrlMapping.equals("/")&&singleDispatcherServlet()){
        UrlPathHelper urlPathHelper=new UrlPathHelper();
        urlPathHelper.setAlwaysUseFullPath(true);
        configurer.setUrlPathHelper(urlPathHelper);
        }
        });
        }
        }
```

默认情况,WebMvcProperties.java:

```java
// 默认情况是:PATH_PATTERN_PARSER
private MatchingStrategy matchingStrategy=MatchingStrategy.PATH_PATTERN_PARSER;

/////////////////////////////
// MatchingStrategy 是枚举类
public enum MatchingStrategy {

  /**
   * Use the {@code AntPathMatcher} implementation.
   */
  ANT_PATH_MATCHER,

  /**
   * Use the {@code PathPatternParser} implementation.
   */
  PATH_PATTERN_PARSER

}
```

## 3.内容协商

### 1.HTTP内容协商

在**HTTP**协议中,**内容协商**是一种机制,用于为同一**URI**提供资源不同的表示形式,以帮助用户代理指定最适合用户的表示形式
例如,哪种文档语言,哪种图片格式或者哪种内容编码

内容协商通常有两种方式,服务端驱动型内容协商和代理驱动型内容协商

#### 服务端驱动型内容协商

在服务端驱动型内容协商或者主动内容协商中,浏览器（或者其他任何类型的用户代理）会随同 URL 发送一系列的 HTTP 标头.
这些标头描述了用户倾向的选择.服务器则以此为线索,通过内部算法来选择最佳方案提供给客户端.如果它不能提供一个合适的资源,
它可能使用 406（Not Acceptable）、415（Unsupported Media Type）进行响应并为其支持的媒体类型设置标头.
例如，分别对 POST 和 PATCH 请求使用 Accept-Post (en-US) 或 Accept-Patch 标头

<img src="https://s2.loli.net/2023/06/24/Mxv32C8Ag6DQKS7.webp" alt="服务端驱动型内容协商"/>

HTTP/1.1 规范指定了一系列的标准标头用于启动服务端驱动型内容协商（Accept、Accept-Charset、Accept-Encoding、Accept-Language）

| 请求头             | 请求头说明        | 响应头              | 响应头说明        |
|-----------------|--------------|------------------|--------------|
| Accept          | 告诉服务端需要的类型   | Content-Type     | 告诉客户端响应的媒体类型 |
| Accept-Language | 告诉服务端需要的语言   | Content-Language | 告诉客户端响应的语言   |
| Accept-Charset  | 告诉服务端需要的字符集  | Content-Charset  | 告诉客户端响应的字符集  |
| Accept-Encoding | 告诉服务端需要的压缩方式 | Content-Encoding | 告诉客户端响应的压缩方式 |

#### 代理驱动型内容协商

从 HTTP 协议制定之初，该协议就准许另外一种协商机制：代理驱动型内容协商，或称为响应式协商。
在这种协商机制中，当面临不明确的请求时，服务器会返回一个页面，其中包含了可供选择的资源的链接。
资源呈现给用户，由用户做出选择.但是HTTP 标准没有明确指定提供可选资源链接的页面的格式，这阻碍了该过程的无痛自动化。
除了退回至服务端驱动型内容协商外，这种自动化方法几乎无一例外都是通过脚本技术来完成的，
尤其是 JavaScript 重定向技术：在检测了协商的条件之后，脚本会触发重定向动作。
另外一个问题是，为了获得实际的资源，需要额外发送一次请求，减慢了将资源呈现给用户的速度

### 2.SpringMVC的内容协商

SpringMVC实现了HTTP内容协商的同时,又进行了扩展.
支持4种内容协商方式：HTTP首部Accept，扩展名，请求参数，或者固定类型

### 3.SpringBoot的内容协商

由于SpringBoot的`web场景启动器`整合了SpringMVC,因此SpringBoot引入`web场景启动器`后即可拥有内容协商功能

SpringBoot有两种方式:基于请求头和基于请求参数的实现

- 基于请求头内容协商:(默认开启)
  - 客户端向服务端发送请求，携带HTTP标准的**Accept请求头**
  - **Accept**: `application/json`、`text/xml`、`text/yaml`
  - 服务端根据客户端请求头期望的数据类型进行动态返回
- 基于请求参数内容协商:(需要开启)
  - 发送请求: GET /person?format=json
  - 匹配到 @GetMapping("/person")
  - 根据参数协商,优先返回**json**类型数据,(需要开启参数匹配设置)
  - 发送请求 GET /person?format=xml,优先返回**xml**类型数据

![SpringBoot内容协商](https://s2.loli.net/2023/06/24/UljfBkXywHdnAxr.webp)

### 4.测试

默认情况,返回JSON

<details> 
<summary><strong><span style="color: red; ">是因为???</span></strong></summary>
<strong>web场景</strong>依赖
<strong>spring-boot-starter-json</strong><br/>
<strong>spring-boot-starter-json</strong>依赖<strong>jackson-databind</strong><br/>
<strong>jackson-databind</strong>可以将对象转为JSON
</details>

效果:
![发请求](https://s2.loli.net/2023/06/24/2srgE9f1NoOjAwH.webp)
![请求结果](https://s2.loli.net/2023/06/24/G59OC74LfhFrb3H.webp)

修改为XML格式:

1. 引依赖

```xml

<dependency>
  <groupId>com.fasterxml.jackson.dataformat</groupId>
  <artifactId>jackson-dataformat-xml</artifactId>
</dependency>
```

2. 标注解

```java

@JacksonXmlRootElement  // 可以写出为xml文档
@Data
public class Person {
  private long id;
  private String name;
  private int age;
}
```

效果:

![内容协商-xml](https://s2.loli.net/2023/06/24/vG4JUTPusNzAWfl.webp)

参数演示:

需要开启**基于请求参数**的内容协商

代码版:

```java
/**
 * 内容协商的相关配置
 */
@Override
public void configureContentNegotiation(ContentNegotiationConfigurer configurer){
        // 开启基于请求参数的内容协商功能
        configurer.favorParameter(true);// 默认:false
        // 自定义内容协商时使用的参数名
        configurer.parameterName("type");// 默认:format
        }
```

配置文件版:

```properties
# 开启基于请求参数的内容协商功能。 默认参数名：format。 默认此功能不开启
spring.mvc.contentnegotiation.favor-parameter=true
# 指定内容协商时使用的参数名。默认是 format
spring.mvc.contentnegotiation.parameter-name=type
```

![](https://s2.loli.net/2023/06/24/1V7okFrjKUIRPY8.webp)

![](https://s2.loli.net/2023/06/24/QlobhemNGY6rCgd.webp)

![](https://s2.loli.net/2023/06/24/GcSY74PgelosNt3.webp)

![](https://s2.loli.net/2023/06/24/bjuMB2hftdnPcaI.webp)

### 5.内容协商原理浅析

> 其实就是
>- HttpMessageConverter 怎么工作？合适工作？
>- 定制 HttpMessageConverter 来实现多端内容协商
>- 编写WebMvcConfigurer提供的configureMessageConverters底层，修改底层的MessageConverter

#### 1.`@ResponseBody`由**HttpMessageConverter**处理

> 标注了`@ResponseBody`的返回值 将会由支持它的**HttpMessageConverter**写给浏览器

- 如果**controller**方法的返回值标注了`@ResponseBody`注解
  - 请求进来先来到**DispatcherServlet**的`doDispatch()`进行处理
  - 找到一个**HandlerAdapter**适配器。利用适配器执行目标方法
  - `RequestMappingHandlerAdapter`来执行，调用`invokeHandlerMethod()`来执行目标方法
  - 目标方法执行之前，准备好两个东西
    - HandlerMethodArgumentResolver：参数解析器，确定目标方法每个参数值
    - HandlerMethodReturnValueHandler：返回值处理器，确定目标方法的返回值改怎么处理
  - `RequestMappingHandlerAdapter`里面的`invokeAndHandle()`真正执行目标方法
  - 目标方法执行完成，会返回**返回值对象**
  - 找到一个合适的返回值处理器`HandlerMethodReturnValueHandler`
  - 最终找到`RequestResponseBodyMethodProcessor`能处理 标注了`@ResponseBody`注解的方法
  - `RequestResponseBodyMethodProcessor`调用`writeWithMessageConverters`,利用`MessageConverter`把返回值写出去

- `HttpMessageConverter`会先进行内容协商
  - 遍历所有的`MessageConverter`看谁支持这种内容类型的数据
  - 默认`MessageConverter`有以下:
  - ![](https://s2.loli.net/2023/06/25/bGrtcC3mV4du52F.webp)
  - 最终因为要`json`所以`MappingJackson2HttpMessageConverter`支持写出json
  - jackson用`ObjectMapper`把对象写出去

#### 2.WebMvcAutoConfiguration提供几种默认HttpMessageConverters

`EnableWebMvcConfiguration`通过`addDefaultHttpMessageConverters`添加了默认的`MessageConverter`;
如下:

- `ByteArrayHttpMessageConverter`： 支持字节数据读写
- `StringHttpMessageConverter`: 支持字符串读写
- `ResourceHttpMessageConverter`: 支持资源读写
- `ResourceRegionHttpMessageConverter`: 支持分区资源写出
- `AllEncompassingFormHttpMessageConverter`: 支持表单xml/json读写
- `MappingJackson2HttpMessageConverter`: 支持请求响应体Json读写

> 系统提供默认的MessageConverter 功能有限，仅用于json或者普通返回数据。
> 额外增加新的内容协商功能，必须增加新的HttpMessageConverter

## 4.模板引擎




























