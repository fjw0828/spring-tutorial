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

### 1.WebMvcAutoConfiguration原理浅析

#### 1.生效条件

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

#### 2.`HiddenHttpMethodFilter`: 过滤页面表单提交的Rest请求

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

#### 3.`FormContentFilter`:用于分析表单内容, 与前面的过滤器配合使用,

同样只针对`PUT`,`PATCH`,`DELETE`三种HTTP请求

#### 4.静态内部类`WebMvcAutoConfigurationAdapter`

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

##### 接口`WebMvcConfigurer`

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

##### 静态资源规则源码浅析

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

- 规则一 访问： /webjars/**路径就去 classpath:/META-INF/resources/webjars/下找资源.
- 规则二 访问： /**路径就去 静态资源默认的四个位置找资源
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

#### 5.静态内部类`EnableWebMvcConfiguration`

在`WebMvcAutoConfiguration`中有静态内部类`EnableWebMvcConfiguration`源码如下:

```java

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(WebProperties.class)
public static class EnableWebMvcConfiguration extends DelegatingWebMvcConfiguration implements ResourceLoaderAware {

}
```





































    

