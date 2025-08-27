package org.example.springsecurity;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;

import org.example.springsecurity.domain.User;
import org.example.springsecurity.mapper.MenuMapper;
import org.example.springsecurity.mapper.UserMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class SpringSecurityApplicationTests {

    @Autowired
    private UserMapper userMapper; // 变量名与接口名一致（userMapper，而非 userMapping）
    @Autowired
    private MenuMapper menuMapper;

    @Test
    void testContextLoads() {
        // 使用 Wrapper 避免 null（更安全）
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("del_flag", 0); // 示例：查询未删除的用户

        List<User> users = userMapper.selectList(wrapper);
        System.out.println("查询结果: " + users);
        Assertions.assertFalse(users.isEmpty(), "用户列表为空");
    }
    @Test
    void testPassEncoder(){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode("123456");
        System.out.println("加密结果：" + encode);
    }

    @Test
    void testSelectPermsByUserId(){
        List<String> perms = menuMapper.selectPermsByUserId(2L);
        System.out.println("查询结果: " + perms);
        assertNotNull(perms);
    }

}

/*
在 Spring Security 中，过滤器链是实现安全机制的核心组件。当请求进入应用时，会依次经过一系列过滤器，每个过滤器负责特定的安全功能。下面详细介绍 Spring Security 中的关键过滤器及其主要作用：
        1. ChannelProcessingFilter
作用：负责处理 HTTP 通道安全，例如强制使用 HTTPS。
原理：检查请求是否符合指定的通道要求（如 HTTP 或 HTTPS），如果不符合，则进行重定向。
        2. SecurityContextPersistenceFilter
作用：在请求开始时从HttpSession中获取或创建SecurityContext，并在请求结束时将其保存回HttpSession。
原理：确保每个请求都能获取到安全上下文，实现用户身份的持久化。
        3. ConcurrentSessionFilter
作用：管理用户的并发会话，防止同一用户多次登录导致的安全问题。
原理：检查当前用户的会话是否已过期或被其他会话挤掉。
        4. UsernamePasswordAuthenticationFilter
作用：处理表单登录认证，从请求中提取用户名和密码。
原理：将用户名和密码封装为UsernamePasswordAuthenticationToken，并提交给AuthenticationManager进行验证。
        5. DefaultLoginPageGeneratingFilter
作用：生成默认的登录页面。
原理：当用户未自定义登录页面时，自动生成一个简单的登录表单。
        6. BasicAuthenticationFilter
作用：处理 HTTP Basic 认证。
原理：从请求头中提取Authorization字段，解析出用户名和密码进行验证。
        7. RequestCacheAwareFilter
作用：处理被安全机制拦截的请求，在用户登录成功后重定向到原始请求的 URL。
原理：使用RequestCache存储被拦截的请求，登录成功后恢复该请求。
        8. SecurityContextHolderAwareRequestFilter
作用：将原始的HttpServletRequest包装为SecurityContextHolderAwareRequestWrapper，提供额外的安全相关方法。
原理：增强请求对象，使其能够获取当前认证用户的信息。
        9. AnonymousAuthenticationFilter
作用：为未认证的请求创建匿名身份（AnonymousAuthenticationToken）。
原理：确保每个请求都有一个Authentication对象，即使是未登录的用户。
        10. SessionManagementFilter
作用：管理用户会话，例如处理会话超时、会话固定攻击防护等。
原理：在用户登录时创建新会话，防止会话固定攻击，并处理会话超时逻辑。
        11. ExceptionTranslationFilter
作用：处理安全异常，例如将AccessDeniedException转换为 HTTP 403 响应，或将AuthenticationException转换为登录页面重定向。
原理：捕获安全过滤器链中抛出的异常，并进行相应的处理。
        12. FilterSecurityInterceptor
作用：基于 URL、方法等规则进行权限控制，决定是否允许访问。
原理：检查当前用户是否具有访问资源所需的权限，通过AccessDecisionManager进行决策。




Spring Security 过滤器与 Spring MVC 拦截器均属于 AOP（面向切面编程）思想的具体实现，不过它们的职责和生效时机存在差异。下面将详细剖析两者的联系与区别，以及它们发挥作用的先后顺序。
一、核心差异
对比维度	   Spring Security 过滤器	              Spring MVC 拦截器
所属技术栈	属于 Spring Security 框架，主要负责安全相关处理	属于 Spring MVC 框架，主要用于请求预处理
拦截范围	能够拦截所有进入容器的 HTTP 请求 	仅能拦截 DispatcherServlet 处理的请求
接口规范	实现javax.servlet.Filter接口	 实现HandlerInterceptor接口
典型应用场景	身份验证、权限校验、会话管理等安全功能	 日志记录、参数预处理、性能监控等非安全功能
执行优先级	优先级更高，会先于 MVC 拦截器执行	  在 Security 过滤器之后执行
二、执行流程关系
当一个 HTTP 请求到达应用时，会按照以下顺序依次经过各个处理环节：

plaintext
客户端请求 → 容器过滤器 → Spring Security过滤器链 → DispatcherServlet → Spring MVC拦截器 → 控制器*/



/*
一、企业中的典型选择策略
场景	技术选择	原因
身份认证与授权	Spring Security	提供专业的认证机制（如 OAuth2、JWT）和强大的授权表达式（如hasRole）。
防止 CSRF/XSS 等安全漏洞	Spring Security	内置 CSRF 防护、安全头设置等功能，无需手动编写复杂的安全代码。
会话管理	Spring Security	支持并发会话控制、会话固定攻击防护，集成 Servlet Session 更深入。
业务参数校验 / 日志记录	Spring MVC 拦截器	轻量级，适合处理与业务相关的通用逻辑，如参数转换、性能监控。
请求链路追踪	Spring MVC 拦截器	可在preHandle和afterCompletion中添加日志，不影响安全核心逻辑。
多租户处理	两者结合	Security 获取租户身份，MVC 拦截器根据租户路由请求。
二、企业级架构中的协作模式
1. 分层拦截模型
        plaintext
┌───────────────────────────────────────────────────────────┐
        │                   客户端请求                                │
        └─────────────────────────┬───────────────────────────────────┘
        │
        ▼
        ┌───────────────────────────────────────────────────────────┐
        │                Spring Security过滤器链                      │
        │  ┌───────────────────┐  ┌───────────────────┐  ┌───────────┐  │
        │  │ SecurityContext   │  │ Authentication    │  │ Authorization │  │
        │  │ PersistenceFilter │  │ Filters           │  │ Filter        │  │
        │  └───────────────────┘  └───────────────────┘  └───────────┘  │
        └─────────────────────────┬───────────────────────────────────┘
        │
        ▼
        ┌───────────────────────────────────────────────────────────┐
        │                 DispatcherServlet                         │
        └─────────────────────────┬───────────────────────────────────┘
        │
        ▼
        ┌───────────────────────────────────────────────────────────┐
        │                  Spring MVC拦截器                          │
        │  ┌───────────────────┐  ┌───────────────────┐  ┌───────────┐  │
        │  │ LoggingInterceptor│  │ ParameterChecker  │  │ CacheInterceptor│
        │  └───────────────────┘  └───────────────────┘  └───────────┘  │
        └─────────────────────────┬───────────────────────────────────┘
        │
        ▼
        ┌───────────────────────────────────────────────────────────┐
        │                     Controller                              │
        └───────────────────────────────────────────────────────────┘
        2. 协作示例
        java
// SecurityConfig.java - 安全配置
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
                .and()
                .oauth2ResourceServer()
                .jwt(); // JWT认证
    }
}

// WebMvcConfig.java - MVC配置
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new PerformanceInterceptor())
                .addPathPatterns("/api/**");
    }
}

// PerformanceInterceptor.java - 性能监控拦截器
public class PerformanceInterceptor implements HandlerInterceptor {
    private final ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        startTime.set(System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        long duration = System.currentTimeMillis() - startTime.get();
        logger.info("请求 {} 耗时 {}ms", request.getRequestURI(), duration);
        startTime.remove();
    }
}


三、企业最佳实践
安全核心逻辑使用 Security
所有与认证、授权相关的逻辑必须通过 Spring Security 实现。
避免在 MVC 拦截器中处理用户身份验证，防止安全漏洞。
业务增强使用 MVC 拦截器
参数校验、日志记录、性能监控等与业务相关的功能使用 MVC 拦截器。
避免在 Security 过滤器中添加业务逻辑，保持安全模块的独立性。
数据传递机制
Security 通过SecurityContextHolder.getContext().getAuthentication()获取用户信息。
MVC 拦截器通过request.setAttribute()传递数据给后续组件。
异常处理分离
Security 异常（如认证失败）由ExceptionTranslationFilter统一处理。
MVC 拦截器异常（如参数校验失败）在 ControllerAdvice 中处理。*/



/*是的，在企业级开发中，权限校验和 Token 校验通常由 Spring Security 处理，而非 MVC 拦截器。B 站等教程中的做法（放在拦截器）属于简化方案，在实际企业项目中存在安全风险和维护成本。以下是详细分析：
一、为什么 B 站项目常把 Token 校验放拦截器？
简化教学：Spring Security 的配置较为复杂，新手难以快速上手。
降低依赖：避免引入过多框架，让代码更 “轻量”。
快速实现：拦截器的代码更直观，适合快速演示功能。

示例代码（B 站常见做法）：

java
// 错误示范：在MVC拦截器中校验Token
public class TokenInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        String token = request.getHeader("Authorization");
        if (token == null || !JwtUtil.validate(token)) {
            response.setStatus(401);
            return false;
        }
        User user = JwtUtil.parseToken(token);
        request.setAttribute("user", user);
        return true;
    }
}
二、企业级项目的正确做法
1. 使用 Spring Security 处理 Token 校验
Spring Security 提供了专门的JwtAuthenticationFilter，能够：

自动解析 Token 并验证签名
        将用户信息存入SecurityContext
统一处理认证异常（如 Token 过期）

示例配置：

java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/public/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint((req, res, ex) ->
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token"));
    }

    @Bean
    public JwtAuthenticationFilter jwtFilter() {
        return new JwtAuthenticationFilter();
    }
}
2. 权限校验使用表达式
Spring Security 支持基于表达式的权限控制，例如：

java
        http
    .authorizeRequests()
        .antMatchers("/admin/**").hasRole("ADMIN")
        .antMatchers("/user/**").access("hasRole('USER') or hasRole('ADMIN')");
三、拦截器方案的潜在问题
        安全漏洞
Filter 链前的请求未被拦截：静态资源、错误页面等可能绕过拦截器。
CSRF 防护缺失：拦截器不会自动处理 CSRF 令牌。
安全头缺失：如X-Frame-Options、Content-Security-Policy等。
功能缺失
会话管理薄弱：难以实现并发登录控制、会话固定攻击防护。
异常处理不一致：认证失败和权限不足的响应格式可能不统一。
集成困难：与 OAuth2、SAML 等标准协议集成复杂。
维护成本高
重复造轮子：每次都要手动实现 Token 解析、用户信息存储等逻辑。
配置分散：安全逻辑分散在多个拦截器中，难以统一管理。*/
