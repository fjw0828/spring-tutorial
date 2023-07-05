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

## 1.自动配置原理浅析

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

<img src="https://image.fu-jw.com/img/2023/07/01/649f85725fe98.webp" alt="WebMvcConfigurer"/>

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

<img src="https://image.fu-jw.com/img/2023/07/01/649f850bd24ec.webp" alt="第一次请求"/>

第二次请求:

<img src="https://image.fu-jw.com/img/2023/07/01/649f85350e56a.webp" alt="第二次请求"/>

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

<img src="https://image.fu-jw.com/img/2023/07/01/649f847e483f9.webp" alt="AntPathMatcher策略"/>

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

<img src="https://image.fu-jw.com/img/2023/07/01/649f8446e65eb.webp" alt="服务端驱动型内容协商"/>

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

<img src="https://image.fu-jw.com/img/2023/07/01/649f841eacfa9.webp"/>

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
<img src="https://image.fu-jw.com/img/2023/07/01/649f83f97a750.webp"/>
<img src="https://image.fu-jw.com/img/2023/07/01/649f83db815e5.webp"/>

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

<img src="https://image.fu-jw.com/img/2023/07/01/649f83a2b0c4e.webp"/>

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

<img src="https://image.fu-jw.com/img/2023/07/01/649f8378aac51.webp"/>

<img src="https://image.fu-jw.com/img/2023/07/01/649f835b383d5.webp"/>

<img src="https://image.fu-jw.com/img/2023/07/01/649f8333bc364.webp"/>

<img src="https://image.fu-jw.com/img/2023/07/01/649f83010fe7d.webp"/>

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
  - <img src="https://image.fu-jw.com/img/2023/07/01/649f825276881.webp"/>
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

> 除了RESTWeb服务，您还可以使用SpringMVC来提供动态HTML内容。
> SpringMVC支持多种模板技术，包括Thymelaf、FreeMarker和JSP。
> 此外，许多其他模板引擎也包含了它们自己的SpringMVC集成。

SpringBoot自动配置支持的模板引擎有:

- [FreeMarker](https://freemarker.apache.org/docs/)
- [Groovy](https://docs.groovy-lang.org/docs/next/html/documentation/template-engines.html#_the_markuptemplateengine)
- [Thymeleaf](https://www.thymeleaf.org/)
- [Mustache](https://mustache.github.io/)

### 整合Thymeleaf

```xml

<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

由自动配置原理知:

1. 开启了`org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration`自动配置
2. 属性绑定在**ThymeleafProperties**中，对应配置文件`spring.thymeleaf`内容
3. 默认情况,模板页面在`classpath:/templates`文件夹下
1. 默认情况,模板页面前缀`classpath:/templates/`
2. 默认情况,模板页面后缀`.html`
3. 达到效果: `classpath:/templates/` + 页面名 + `.html`

## 5.错误处理

### 默认机制

SpringBoot在**web场景**下,当应用程序发生错误或异常时,SpringBoot会自动应用`ErrorMvcAutoConfiguration`进行配置.

<img src="https://image.fu-jw.com/img/2023/07/01/649f81ec7ae0a.webp"/>

```java
// Load before the main WebMvcAutoConfiguration so that the error View is available
// 在WebMvcAutoConfiguration自动装配之前
@AutoConfiguration(before = WebMvcAutoConfiguration.class)
// 条件:普通的servlet web类型
@ConditionalOnWebApplication(type = Type.SERVLET)
// 条件:有Servlet和DispatcherServlet类
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
// 绑定配置文件:server.*和spring.mvc.*
@EnableConfigurationProperties({ServerProperties.class, WebMvcProperties.class})
public class ErrorMvcAutoConfiguration {
  //...
}
```

两大处理机制:
机制一: **SpringBoot**会自适应处理错误,响应页面或**JSON**数据(内容协商)

<img alt="同一请求-浏览器返回白页" src="https://image.fu-jw.com/img/2023/07/01/649ff3f00a98a.webp"/>
<img alt="同一请求-客户端返回JSON" src="https://image.fu-jw.com/img/2023/07/01/649ff431580c1.webp"/>
<img alt="同一请求-客户端也可设置返回XML" src="https://image.fu-jw.com/img/2023/07/01/649ff45dc52e3.webp"/>

机制二: **SpringMVC**的错误处理机制依然保留,MVC处理不了,才会交给boot进行处理

<img alt="SpringBoot错误处理机制" src="https://image.fu-jw.com/img/2023/07/03/64a22e201bcbb.webp"/>

### SpringMVC处理错误

```java

@Controller
public class ErrorController {

  /**
   * 测试MVC的错误处理机制
   * 默认情况下--不处理错误:
   * 浏览器返回白页,因为请求头中: Accept:text/html
   * 移动端postman返回JSON.因为请求头中: (Accept:* 所有类型,优先JSON)
   * 自己处理错误: handleException()
   */
  @GetMapping("testError")
  public String testError() {

    // 错误出现
    int i = 12 / 0;

    return "testError";
  }

  /**
   * 自定义处理所有错误
   * @ExceptionHandler 可以标识一个方法, 默认只能处理这个类发生的指定错误
   * @ControllerAdvice AOP思想, 可以统一处理所有方法, 如 GlobalExceptionHandler.java
   */
  @ResponseBody
  @ExceptionHandler(Exception.class)
  public String handleException(Exception e) {

    return "错误已发生,原因:" + e.getMessage();
  }
}
```

<img alt="mvc处理错误" src="https://image.fu-jw.com/img/2023/07/03/64a232aac8fa8.png"/>

统一错误处理:

```java

@ControllerAdvice // 统一处理所有Controller
public class GlobalExceptionHandler {

  /**
   * 自定义处理所有错误
   * @ExceptionHandler 可以标识一个方法, 默认只能处理这个类发生的指定错误
   * @ControllerAdvice AOP思想, 可以统一处理所有方法
   */
  @ResponseBody
  @ExceptionHandler(Exception.class)
  public String handleException(Exception e) {

    return "统一处理,错误已发生,原因:" + e.getMessage();
  }
}
```

<img alt="mvc统一处理错误" src="https://image.fu-jw.com/img/2023/07/03/64a232c9bbac2.png"/>

### SpringBoot错误原理浅析

自动配置类`ErrorMvcAutoConfiguration`, 主要包含以下功能:

#### 注册组件: BasicErrorController

这是一个默认的错误处理控制器,用于处理一般的错误请求.

可以在配置文件中配置:`server.error.path=/error`(默认值)
当发生错误以后,将SpringMVC不能处理的错误请求转发给`/error`进行处理

```java

@Controller
// 可以处理配置文件中:server.error.path 的映射
// 或者处理配置文件中:error.path 的映射
// 以上都没配置,就会将请求映射到: /error
@RequestMapping("${server.error.path:${error.path:/error}}")
public class BasicErrorController extends AbstractErrorController {
  //...
}
```

它会根据请求的`Accept`头部信息返回对应的错误响应,比如**JSON**,**XML**或**HTML**格式.
**内容协商机制**

```java
// "text/html"
// 返回html页面
@RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
public ModelAndView errorHtml(HttpServletRequest request,HttpServletResponse response){
        // 获取请求的状态码
        HttpStatus status=getStatus(request);
        Map<String, Object> model=Collections
        .unmodifiableMap(getErrorAttributes(request,getErrorAttributeOptions(request,MediaType.TEXT_HTML)));
        response.setStatus(status.value());
        // 得到解析的错误视图
        ModelAndView modelAndView=resolveErrorView(request,response,status,model);
        // 返回上面解析的视图,或者新建一个error视图(SpringBoot默认有一个error页面,状态码999)
        return(modelAndView!=null)?modelAndView:new ModelAndView("error",model);
        }

// 返回 ResponseEntity,即JSON数据
@RequestMapping
public ResponseEntity<Map<String, Object>>error(HttpServletRequest request){
        HttpStatus status=getStatus(request);
        if(status==HttpStatus.NO_CONTENT){
        return new ResponseEntity<>(status);
        }
        Map<String, Object> body=getErrorAttributes(request,getErrorAttributeOptions(request,MediaType.ALL));
        return new ResponseEntity<>(body,status);
        }
```

**错误视图解析:**

```java
//1、解析错误的自定义视图地址
ModelAndView modelAndView=resolveErrorView(request,response,status,model);
//2、如果解析不到错误页面的地址，默认的错误页就是 error
        return(modelAndView!=null)?modelAndView:new ModelAndView("error",model);
```

**1.解析错误视图:**

```java
protected ModelAndView resolveErrorView(HttpServletRequest request,HttpServletResponse response,HttpStatus status,
        Map<String, Object> model){
        // 遍历错误视图解析器:errorViewResolvers
        for(ErrorViewResolver resolver:this.errorViewResolvers){
        ModelAndView modelAndView=resolver.resolveErrorView(request,status,model);
        if(modelAndView!=null){
        return modelAndView;
        }
        }
        return null;
        }
```

在自动配置类,会将**默认的错误视图解析器**放在容器中

```java

@Configuration(proxyBeanMethods = false)
// 绑定配置文件中 web.* 和 web.mvc.*
@EnableConfigurationProperties({WebProperties.class, WebMvcProperties.class})
static class DefaultErrorViewResolverConfiguration {

  private final ApplicationContext applicationContext;

  private final Resources resources;

  DefaultErrorViewResolverConfiguration(ApplicationContext applicationContext, WebProperties webProperties) {
    this.applicationContext = applicationContext;
    this.resources = webProperties.getResources();
  }

  @Bean
  @ConditionalOnBean(DispatcherServlet.class)
  @ConditionalOnMissingBean(ErrorViewResolver.class)
  DefaultErrorViewResolver conventionErrorViewResolver() {
    // 在容器中放入默认错误视图解析器
    return new DefaultErrorViewResolver(this.applicationContext, this.resources);
  }

}
```

**默认的错误视图解析过程:**

```java
@Override
public ModelAndView resolveErrorView(HttpServletRequest request,HttpStatus status,Map<String, Object> model){
        // 1. 获取状态码
        // 2. 根据状态码解析错误视图(如: 404 500 等)    
        ModelAndView modelAndView=resolve(String.valueOf(status.value()),model);
        // 3. 状态码没有精确匹配,则模糊匹配(如:4xx 5xx, 注意只有这俩)
        if(modelAndView==null&&SERIES_VIEWS.containsKey(status.series())){
        modelAndView=resolve(SERIES_VIEWS.get(status.series()),model);
        }
        return modelAndView;
        }
// 具体的解析过程
private ModelAndView resolve(String viewName,Map<String, Object> model){
        // 错误视图名: error/404 或 error/4xx    
        String errorViewName="error/"+viewName;
        TemplateAvailabilityProvider provider=this.templateAvailabilityProviders.getProvider(errorViewName,
        this.applicationContext);
        if(provider!=null){
        // 有就返回
        return new ModelAndView(errorViewName,model);
        }
        // 没有, 继续
        return resolveResource(errorViewName,model);
        }

// 继续解析错误视图, 在静态资源目录下查找
private ModelAndView resolveResource(String viewName,Map<String, Object> model){
        // 遍历四个静态资源目录:classpath:/META-INF/resources/","classpath:/resources/",
        // "classpath:/static/", "classpath:/public/    
        for(String location:this.resources.getStaticLocations()){
        try{
        Resource resource=this.applicationContext.getResource(location);
        resource=resource.createRelative(viewName+".html");
        if(resource.exists()){
        return new ModelAndView(new HtmlResourceView(resource),model);
        }
        }
        catch(Exception ex){
        }
        }
        return null;
        }
```

**2.解析不到错误视图:**
精确状态码以及模糊状态码都没有匹配时,则映射到error视图

在template目录下创建`error.html`就会返回(注意:将上面统一错误处理注释)

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Title</title>
</head>
<body>
模板: error 页
</body>
</html>
```

效果:

<img alt="error页" src="https://image.fu-jw.com/img/2023/07/03/64a259a289e8c.png"/>

**如果error视图页没有:**

自动配置类`ErrorMvcAutoConfiguration`,在容器中放入了`error`组件,提供了默认白页功能:

```java

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "server.error.whitelabel", name = "enabled", matchIfMissing = true)
@Conditional(ErrorTemplateMissingCondition.class)
protected static class WhitelabelErrorViewConfiguration {

  private final StaticView defaultErrorView = new StaticView();

  @Bean(name = "error")
  @ConditionalOnMissingBean(name = "error")
  public View defaultErrorView() {
    return this.defaultErrorView;
  }

  // If the user adds @EnableWebMvc then the bean name view resolver from
  // WebMvcAutoConfiguration disappears, so add it back in to avoid disappointment.
  @Bean
  @ConditionalOnMissingBean
  public BeanNameViewResolver beanNameViewResolver() {
    BeanNameViewResolver resolver = new BeanNameViewResolver();
    resolver.setOrder(Ordered.LOWEST_PRECEDENCE - 10);
    return resolver;
  }

}
```

创建白页:

```java
private static class StaticView implements View {
  private static final MediaType TEXT_HTML_UTF8 = new MediaType("text", "html", StandardCharsets.UTF_8);
  private static final Log logger = LogFactory.getLog(StaticView.class);

  @Override
  public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
          throws Exception {
    if (response.isCommitted()) {
      String message = getMessage(model);
      logger.error(message);
      return;
    }
    response.setContentType(TEXT_HTML_UTF8.toString());
    StringBuilder builder = new StringBuilder();
    Object timestamp = model.get("timestamp");
    Object message = model.get("message");
    Object trace = model.get("trace");
    if (response.getContentType() == null) {
      response.setContentType(getContentType());
    }
    builder.append("<html><body><h1>Whitelabel Error Page</h1>")
            .append("<p>This application has no explicit mapping for /error, so you are seeing this as a fallback.</p>")
            .append("<div id='created'>")
            .append(timestamp)
            .append("</div>")
            .append("<div>There was an unexpected error (type=")
            .append(htmlEscape(model.get("error")))
            .append(", status=")
            .append(htmlEscape(model.get("status")))
            .append(").</div>");
    if (message != null) {
      builder.append("<div>").append(htmlEscape(message)).append("</div>");
    }
    if (trace != null) {
      builder.append("<div style='white-space:pre-wrap;'>").append(htmlEscape(trace)).append("</div>");
    }
    builder.append("</body></html>");
    response.getWriter().append(builder.toString());
  }
```

### 小结一下

先尝试解析错误页, 解析失败则在静态资源目录下查找

1. **解析**一个错误页
  - 如果发生了500、404、503、403 这些错误
    - 如果有模板引擎，默认在`classpath:/templates/error/精确码.html`
    - 如果没有模板引擎，在静态资源文件夹下找`精确码.html`
  - 如果匹配不到`精确码.html`这些精确的错误页，就去找`5xx.html`, `4xx.html`**模糊匹配**
    - 如果有模板引擎，默认在`classpath:/templates/error/5xx.html`
    - 如果没有模板引擎，在静态资源文件夹下找`5xx.html`
2. 如果模板引擎路径`templates`下有`error.html`页面, 就直接渲染

### 自定义错误响应

- 自定义json响应
  - 使用@ControllerAdvice + @ExceptionHandler 进行统一异常处理
- 自定义页面响应
  - 根据boot的错误页面规则，自定义页面模板

### 最佳实践

- 前后分离
  - 后台发生的所有错误, `@ControllerAdvice` + `@ExceptionHandler`进行统一异常处理
- 服务端页面渲染
  - 不可预知的错误,HTTP码表示的服务器端或客户端错误
    - 给`classpath:/templates/error/`下面，放常用精确的错误码页面。`500.html`，`404.html`
    - 给`classpath:/templates/error/`下面，放通用模糊匹配的错误码页面。 `5xx.html`，`4xx.html`
  - 发生业务错误
    - **核心业务**, 每一种错误, 都应该代码控制, 跳转到自己**定制的错误页**
    - **通用业务**, `classpath:/templates/error.html`页面, 显示错误信息

无论是返回页面或者JSON数据, 可用的**Model**数据都一样, 如下:
<img alt="model" src="https://image.fu-jw.com/img/2023/07/03/64a2742fd9f6c.png"/>

## 6.嵌入式容器

Servlet容器：管理、运行Servlet组件的环境，一般指服务器

Servlet三大组件:

- Servlet, 处理请求
- Filter, 过滤请求
- Listener, 监听请求

### 自动配置原理浅析

先从自动配置类开始

```java

@AutoConfiguration(after = SslAutoConfiguration.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
// 生效条件:实现ServletRequest接口的Servlet请求
@ConditionalOnClass(ServletRequest.class)
// 生效条件:是SERVLET类型的程序
@ConditionalOnWebApplication(type = Type.SERVLET)
// 绑定配置文件, server.*
@EnableConfigurationProperties(ServerProperties.class)
// 批量导入一些嵌入式服务器类
@Import({ServletWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar.class,
        ServletWebServerFactoryConfiguration.EmbeddedTomcat.class,
        ServletWebServerFactoryConfiguration.EmbeddedJetty.class,
        ServletWebServerFactoryConfiguration.EmbeddedUndertow.class})
public class ServletWebServerFactoryAutoConfiguration {
  // ...
}
```

嵌入式的三大服务器:`Tomcat`、`Jetty`、`Undertow`

- 导入`Tomcat`、`Jetty`、`Undertow`都有条件注解(系统中有对应的类才行,导包即可)
- 默认`Tomcat`配置生效.**web场景**默认导入`spring-boot-starter-tomcat`
- `Tomcat`配置生效后,会在容器中放入`TomcatServletWebServerFactory`组件,用于创建容器的工厂类

```java

@Configuration(proxyBeanMethods = false)
class ServletWebServerFactoryConfiguration {

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass({Servlet.class, Tomcat.class, UpgradeProtocol.class})
  @ConditionalOnMissingBean(value = ServletWebServerFactory.class, search = SearchStrategy.CURRENT)
  static class EmbeddedTomcat {

    @Bean
    TomcatServletWebServerFactory tomcatServletWebServerFactory(
            ObjectProvider<TomcatConnectorCustomizer> connectorCustomizers,
            ObjectProvider<TomcatContextCustomizer> contextCustomizers,
            ObjectProvider<TomcatProtocolHandlerCustomizer<?>> protocolHandlerCustomizers) {
      TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
      factory.getTomcatConnectorCustomizers().addAll(connectorCustomizers.orderedStream().toList());
      factory.getTomcatContextCustomizers().addAll(contextCustomizers.orderedStream().toList());
      factory.getTomcatProtocolHandlerCustomizers().addAll(protocolHandlerCustomizers.orderedStream().toList());
      return factory;
    }

  }
  // 其他容器...
}
```

- `TomcatServletWebServerFactory`工厂类中,具体的创建方法:

```java
@Override
// 获取web服务器
public WebServer getWebServer(ServletContextInitializer...initializers){
        if(this.disableMBeanRegistry){
        Registry.disableRegistry();
        }
        // 创建Tomcat容器
        Tomcat tomcat=new Tomcat();
        File baseDir=(this.baseDirectory!=null)?this.baseDirectory:createTempDir("tomcat");
        tomcat.setBaseDir(baseDir.getAbsolutePath());
        for(LifecycleListener listener:this.serverLifecycleListeners){
        tomcat.getServer().addLifecycleListener(listener);
        }
        Connector connector=new Connector(this.protocol);
        connector.setThrowOnFailure(true);
        tomcat.getService().addConnector(connector);
        customizeConnector(connector);
        tomcat.setConnector(connector);
        tomcat.getHost().setAutoDeploy(false);
        configureEngine(tomcat.getEngine());
        for(Connector additionalConnector:this.additionalTomcatConnectors){
        tomcat.getService().addConnector(additionalConnector);
        }
        prepareContext(tomcat.getHost(),initializers);
        return getTomcatWebServer(tomcat);
        }
```

### Tomcat容器创建时机

SpringApplication.java

```java
public ConfigurableApplicationContext run(String...args){
        // ...
        refreshContext(context); // 刷新上下文
        afterRefresh(context,applicationArguments);
        // ...    
        }
// ...

// 刷新上下文
private void refreshContext(ConfigurableApplicationContext context){
        if(this.registerShutdownHook){
        shutdownHook.registerApplicationContext(context);
        }
// 刷新
        refresh(context);
        }

protected void refresh(ConfigurableApplicationContext applicationContext){
        applicationContext.refresh();
        }
```

在**web场景**下,
ServletWebServerApplicationContext.java

```java
@Override
public final void refresh()throws BeansException,IllegalStateException{
        try{
        // 刷新
        super.refresh();
        }
        catch(RuntimeException ex){
        WebServer webServer=this.webServer;
        if(webServer!=null){
        webServer.stop();
        }
        throw ex;
        }
        }
```

AbstractApplicationContext.java

```java
@Override
// 创建容器十二步
public void refresh()throws BeansException,IllegalStateException{
synchronized (this.startupShutdownMonitor){
        StartupStep contextRefresh=this.applicationStartup.start("spring.context.refresh");

        // 1.准备上下文内容
        // Prepare this context for refreshing.
        prepareRefresh();

        // 2.获取刷新的bean工厂
        // Tell the subclass to refresh the internal bean factory.
        ConfigurableListableBeanFactory beanFactory=obtainFreshBeanFactory();

        // 3.准备上下文用到的bean工厂,如:类加载器,后置处理器
        // Prepare the bean factory for use in this context.
        prepareBeanFactory(beanFactory);

        try{
        // 4.通过编程方式修改BeanFactory的配置，比如添加自定义的BeanDefinition，修改属性值，注册BeanPostProcessor等等。
        // 通过这种方式，开发人员可以对Spring容器进行更灵活和定制化的配置
        // Allows post-processing of the bean factory in context subclasses.
        postProcessBeanFactory(beanFactory);

        // 5.执行所有已注册的BeanFactoryPostProcessor的postProcessBeanFactory方法
        StartupStep beanPostProcess=this.applicationStartup.start("spring.context.beans.post-process");
        // Invoke factory processors registered as beans in the context.
        invokeBeanFactoryPostProcessors(beanFactory);

        // 6.注册所有的BeanPostProcessor实例.
        // 允许开发人员在bean实例化和依赖注入的过程中对bean进行增强或定制
        // Register bean processors that intercept bean creation.
        registerBeanPostProcessors(beanFactory);
        beanPostProcess.end();

        // 7.初始化上下文的消息资源
        // 消息资源用于国际化和本地化的目的，它可以根据不同的语言和区域设置，提供相应的文本消息
        // Initialize message source for this context.
        initMessageSource();

        // 8.初始化应用程序事件的多播器,使得应用程序能够对事件进行发布和监听
        // 事件机制是一种通信机制，用于在不同的组件之间传递消息和触发相应的处理逻辑
        // Initialize event multicaster for this context.
        initApplicationEventMulticaster();

        // 9.容器刷新过程中的一个回调方法，用于提供一个扩展点，让开发人员可以在容器刷新完成后执行一些自定义的逻辑
        // 可以在onRefresh方法中执行一些额外的初始化操作、启动定时任务、注册额外的bean等
        // Initialize other special beans in specific context subclasses.
        onRefresh();

        // 10.向应用程序上下文注册事件监听器
        // Check for listener beans and register them.
        registerListeners();

        // 11.在Bean工厂初始化的最后阶段，完成所有注册的Bean的初始化过程
        // Bean工厂中所有已注册的Bean的名称 -->
        // 遍历所有的Bean名称获取对应的Bean定义 -->
        // 根据Bean定义的信息，进行实例化、依赖注入、初始化等操作 -->
        // 若Bean定义中有初始化方法（例如通过@PostConstruct注解标记的方法），则调用该方法进行额外的初始化逻辑
        // 若Bean定义中有销毁方法（例如通过@PreDestroy注解标记的方法），则在容器关闭时调用该方法进行资源释放等操作
        // Instantiate all remaining (non-lazy-init) singletons.
        finishBeanFactoryInitialization(beanFactory);

        // 12.容器刷新的最后阶段，执行一些额外的逻辑以完成刷新过程
        // 清理初始化过程中一系列操作使用到的资源缓存
        // 初始化LifecycleProcessor
        // 启动所有实现了Lifecycle接口的bean
        // 发布ContextRefreshedEvent事件
        // Last step: publish corresponding event.
        finishRefresh();
        }

        catch(BeansException ex){
        if(logger.isWarnEnabled()){
        logger.warn("Exception encountered during context initialization - "+
        "cancelling refresh attempt: "+ex);
        }

        // Destroy already created singletons to avoid dangling resources.
        destroyBeans();

        // Reset 'active' flag.
        cancelRefresh(ex);

        // Propagate exception to caller.
        throw ex;
        }

        finally{
        // Reset common introspection caches in Spring's core, since we
        // might not ever need metadata for singleton beans anymore...
        resetCommonCaches();
        contextRefresh.end();
        }
        }
        }
```

### 切换服务器

```xml

<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
  <exclusions>
    <!-- Exclude the Tomcat dependency -->
    <exclusion>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-tomcat</artifactId>
    </exclusion>
  </exclusions>
</dependency>
        <!-- Use Jetty instead -->
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-jetty</artifactId>
</dependency>
```





