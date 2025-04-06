# 简介

[官网](https://spring.io/projects/spring-security)

Spring Security是一个Java框架，用于保护应用程序的安全性。它提供了一套全面的安全解决方案，包括身份验证、授权、防止攻击等功能。Spring Security基于过滤器链的概念，可以轻松地集成到任何基于Spring的应用程序中。它支持多种身份验证选项和授权策略，开发人员可以根据需要选择适合的方式。此外，Spring Security还提供了一些附加功能，如集成第三方身份验证提供商和单点登录，以及会话管理和密码编码等。总之，Spring Security是一个强大且易于使用的框架，可以帮助开发人员提高应用程序的安全性和可靠性。

***主要功能：***
**1. 认证（Authentication）**

- **用户身份验证**：Spring Security 通过多种方式（如表单登录、HTTP Basic、OAuth2 等）验证用户身份，确保只有合法用户能够访问系统。
- **灵活的认证机制**：支持自定义认证流程和提供者，便于与各种身份验证方案集成。

**2. 授权（Authorization）**

- **访问控制**：在用户通过认证后，Spring Security 根据配置的权限规则（如角色、权限等）控制用户对资源的访问。
- **方法级安全**：通过注解（例如 `@PreAuthorize`、`@Secured` 等）可以在业务逻辑层面直接控制访问权限，实现细粒度的权限管理。

**3. 防御常见攻击**

- **CSRF 攻击防护**：内置跨站请求伪造（CSRF）防护机制，有效降低攻击风险。
- **会话管理**：提供会话固定攻击防护、并发登录控制等功能，保障会话安全。
- **安全头设置**：自动配置 HTTP 安全头（如 X-Frame-Options、X-XSS-Protection 等）来增强安全性。

**4. 可扩展性与定制化**

- **高度定制化**：可以根据项目需求定制安全策略，从认证流程到授权规则均可自定义。
- **与 Spring 生态系统无缝集成**：与 Spring Boot、Spring MVC 等其他 Spring 模块结合紧密，简化了安全配置和管理。

**5. 支持多种安全协议 **

- **OAuth2 与 OpenID Connect**：内置对 OAuth2 和 OpenID Connect 的支持，便于构建基于第三方身份验证的应用程序。
- **LDAP 集成**：支持 LDAP 作为用户信息和权限的存储方案，方便与企业级认证系统集成。

---



# 快速入门

在原有SpringBoot项目中引入依赖即可

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

再次访问时，会出现登录窗口

<img src="https://cdn.jsdelivr.net/gh/KNeegcyao/picdemo/img/image-20250404132135066.png" alt="image-20250404132135066" style="zoom:50%;" />

用户名为user，登录密码会打印在控制台

![image-20250404132159600](https://cdn.jsdelivr.net/gh/KNeegcyao/picdemo/img/image-20250404132159600.png)

登录后就可以访问原来接口的数据。

---



# 认证

## **登录校验流程**

![image-20250404134627378](https://cdn.jsdelivr.net/gh/KNeegcyao/picdemo/img/image-20250404134627378.png)

## 原理

Spring Security 主要通过 **一系列的过滤器（Filter）** 进行请求的拦截、认证和授权。

![【Spring Security系列】Spring Security 过滤器详解与基于JDBC的认证实现_springsecurity过滤器链 ...](https://cdn.jsdelivr.net/gh/KNeegcyao/picdemo/img/e820f60802784bd09fe7780d999a68e3.png)

常见的过滤器包括：

| 过滤器名称                             |                 作用                  |
| -------------------------------------- | :-----------------------------------: |
| `SecurityContextPersistenceFilter`     | 读取、存储 `SecurityContext` 认证信息 |
| `UsernamePasswordAuthenticationFilter` |  处理表单登录请求（用户名密码认证）   |
| `BasicAuthenticationFilter`            |            处理 Basic 认证            |
| `BearerTokenAuthenticationFilter`      |          解析 JWT Token 认证          |
| `ExceptionTranslationFilter`           |       处理认证或授权失败的异常        |
| `FilterSecurityInterceptor`            |         进行授权（权限判断）          |

⚠ 过滤器链的执行顺序决定了 Spring Security 处理请求的方式。

## 认证流程

![image-20250405125501291](https://cdn.jsdelivr.net/gh/KNeegcyao/picdemo/img/image-20250405125501291.png)

| 名称                                     | 类型       | 作用                                                         |
| ---------------------------------------- | ---------- | ------------------------------------------------------------ |
| **UsernamePasswordAuthenticationFilter** | 过滤器     | 拦截登录请求（通常是 `/login`），从 HTTP 请求中提取用户名和密码，封装成一个待认证的 `Authentication` 对象，并触发后续认证流程。 |
| **ProviderManager**                      | 认证管理器 | 实现了 `AuthenticationManager`，内部维护一组 `AuthenticationProvider`，负责将认证请求委派给能处理该类型令牌的 Provider。 |
| **DaoAuthenticationProvider**            | 认证提供者 | 实现了 `AuthenticationProvider`，调用 `UserDetailsService` 加载用户、校验密码，并在认证成功后构造包含用户权限的 `Authentication`。 |
| **InMemoryUserDetailsManager**           | 用户服务   | 实现了 `UserDetailsService`，负责根据用户名从内存（或数据库）中查询用户信息，并返回一个包含用户名、密码、权限等的 `UserDetails` |

### **时序步骤详解**

1.  **提交用户名和密码**
    - **Client → UsernamePasswordAuthenticationFilter**
      用户在登录页面输入用户名/密码，浏览器发起 POST `/login` 请求，`UsernamePasswordAuthenticationFilter` 拦截该请求。
2.  **封装 Authentication 对象**

    - **UsernamePasswordAuthenticationFilter**
      - 从请求中解析出用户名和密码
      - 封装成一个 `UsernamePasswordAuthenticationToken`（此时 `isAuthenticated()==false`，且内部只有用户名和密码，没有权限信息）
3.  **调用认证管理器**

    - **UsernamePasswordAuthenticationFilter → ProviderManager.authenticate(...)**
      将上一步的 `UsernamePasswordAuthenticationToken` 传给 `ProviderManager`（即 `AuthenticationManager`）进行认证。
4.  **委派给 DaoAuthenticationProvider**

    - **ProviderManager → DaoAuthenticationProvider.authenticate(...)**
      `ProviderManager` 遍历其持有的所有 `AuthenticationProvider`，找到支持 `UsernamePasswordAuthenticationToken` 的 `DaoAuthenticationProvider`，并调用它的 `authenticate()` 方法。
5.  **加载用户信息**

    - **DaoAuthenticationProvider → InMemoryUserDetailsManager.loadUserByUsername(username)**
      `DaoAuthenticationProvider` 调用 `UserDetailsService.loadUserByUsername`，查询对应的用户记录及其权限信息。

      - InMemoryUserDetailsManager（或其他实现）从内存/数据库中查到用户实体和该用户的角色/权限
      - 将查询到的信息封装到一个 `UserDetails`（此处是 `LoginUser` 或 Spring 提供的 `User`）对象中返回
6.  **返回 UserDetails 对象**

    - **InMemoryUserDetailsManager → DaoAuthenticationProvider**
      返回包含用户名、加密密码、权限列表等的 `UserDetails`。
7.  **密码校验**

    - **DaoAuthenticationProvider**
      - 使用注入的 `PasswordEncoder`（如 `BCryptPasswordEncoder`）将用户提交的密码加密后，与 `UserDetails.getPassword()`（数据库中已加密的密码）进行比对。
      - 如果不匹配，则抛出 `BadCredentialsException`，认证失败。
8.  **构造已认证的 Authentication**

    - **DaoAuthenticationProvider**
      - 如果密码校验通过，就基于原来的 `UsernamePasswordAuthenticationToken`，将其 `principal`（`UserDetails`）和 `authorities`（权限列表）填充进去，并将 `authenticated` 标志设为 `true`，形成一个完整的已认证令牌。
9.  **返回已认证的 Authentication**

    - **DaoAuthenticationProvider → ProviderManager → UsernamePasswordAuthenticationFilter**
      最终将这个已认证的 `Authentication` 对象一路返回给最初的过滤器。
10.  **保存到 SecurityContextHolder**

     - **UsernamePasswordAuthenticationFilter**
       - 调用 `SecurityContextHolder.getContext().setAuthentication(authentication)`，将认证结果存入当前线程的安全上下文中，后续同一请求的其他过滤器或业务代码都能通过 `SecurityContextHolder` 获取到当前用户信息。
       - 随后通常会跳转到登录成功页面或返回 JWT Token（前后端分离时）

### 形象解释

想象你要进入一家高级俱乐部，整个 Spring Security 认证流程就像你从门外走到俱乐部大堂，再到最终拿到 VIP 通行证的过程：

------

  1. 走到门口——**UsernamePasswordAuthenticationFilter（门卫）**

你来到俱乐部门口，门卫（过滤器）会先问：“请出示你的邀请函（用户名/密码）。”

- 如果你连邀请函都没带，他会拦下你，直接说“请先登录”。
- 如果你递上了邀请函，他就把它交给保安队长去进一步核实。

------

  2. 找保安队长——**ProviderManager（保安队长）**

门卫把邀请函交给保安队长，队长说：“好，我这里有好几位专门负责不同类型邀请函的保安（AuthenticationProvider），我来决定把你交给谁检查。”

- 队长看了下这是常规的“用户名+密码”邀请函，就交给负责这类的保安（`DaoAuthenticationProvider`）。

------

  3. 验证身份——**DaoAuthenticationProvider（专职核查保安）**

这位保安会带你去后台档案室（调用 `UserDetailsService`）找你的“会员档案”：

1. 去档案室查询：这就像他打开了会员数据库，找到了你的档案（`UserDetails`）。
2. 核对签名：保安拿着你手上的签名（密码），用他们的密钥（`PasswordEncoder`）进行比对。

- **比对不符**：保安会立刻说“这签名不对，你不是会员”，认证失败。
- **比对通过**：保安给你盖章，给你的邀请函加上“已认证”标记，告诉队长“他是真会员”。

------

  4. 盖上“已认证”印章——**返回 Authentication**

保安把“已盖章的邀请函”（已认证的 `Authentication` 对象，里面写着你的会员等级、特权列表）交还给保安队长，队长再转交给门卫。

------

  5. 登记通行证——**SecurityContextHolder（签到簿）**

门卫把你的“已认证邀请函”放进签到簿（`SecurityContext`），这样整个俱乐部其他区域的工作人员（后续的过滤器或业务逻辑）都能查到你的身份和权限。

------

  6. 发放 VIP 通行证——**生成 JWT & 缓存**

同时，俱乐部后台给你制作了一张带有你身份信息和到期时间的 VIP 通行证（JWT），并在门卫办公室（Redis）存了一份你的档案副本：

- **JWT**：就像一张加密的电子通行证，你离开后还可以凭它再次进入。
- **Redis 缓存**：就像门卫办公室里存了一份你的会员资料复印件，加快下次验证速度。

------

  7. 你拿着通行证入内

拿到通行证后，你就可以自由进出俱乐部的各个受保护区域（受保护的 API）。每次进门，门卫只要扫描你的通行证（解析 JWT），确认签名没问题，再从办公室（Redis）快速取出你的会员档案，就知道你有哪几种特权。

------

  8. 注销离场——**logout（销毁缓存）**

当你要离开时，门卫把你在办公室的会员档案复印件（Redis 缓存）销毁。这样即使有人捡到你的旧通行证，门卫扫描后也查不到你的档案，就会被拒绝入内。

## 具体实现

**流程**：前端→封装令牌→`AuthenticationManager`→`UserDetailsService`+`PasswordEncoder`→认证通过→生成 JWT→Redis 缓存→返回 Token

创建一个类实现UserDetailsService接口,自定义加载逻辑

- 默认情况下，Spring Security 并不知道你的用户数据存在哪里，也不知道你用的是哪张表、哪种 ORM。
- 通过自己实现 `loadUserByUsername`，你可以：
  - 从数据库（MyBatis、JPA、JDBC）、缓存（Redis）、外部系统（LDAP、微服务）中查询用户；
  - 查询用户的角色和权限，把它们封装到返回的 `UserDetails`（这里是 `LoginUser`）里；
  - 对不存在的用户抛出异常，让 Spring Security 知道该如何反馈“用户名不存在”或“密码错误”的信息。

```java
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //根据用户名查询用户信息
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserName,username);
        User user = userMapper.selectOne(wrapper);
        //如果查询不到数据就通过抛出异常来给出提示
        if(Objects.isNull(user)){
            throw new RuntimeException("用户名或密码错误");
        }
        //TODO 根据用户查询权限信息 添加到LoginUser中
  
        //封装成UserDetails对象返回 
        return new LoginUser(user);
    }
}
```

因为UserDetailsService方法的返回值是UserDetails类型，所以需要定义一个类，实现该接口，把用户信息封装在其中。

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements UserDetails {

    private User user;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

### 密码加密存储

我们一般使用SpringSecurity为我们提供的BCryptPasswordEncoder。

我们只需要使用把BCryptPasswordEncoder对象注入Spring容器中，SpringSecurity就会使用该PasswordEncoder来进行密码校验。

我们可以定义一个SpringSecurity的配置类



1.**基于 `WebSecurityConfigurerAdapter`（5.6 及更早版本）**

```java
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Autowired
    JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                //关闭csrf
                .csrf().disable()
                //不通过session获取SecurityContext
                .sessionManagement().disable()
                .authorizeRequests()
                // 对于登录接口 允许匿名访问
                .antMatchers("/user/login").anonymous()
                // 除上面外的所有请求全部需要鉴权认证
                .anyRequest().authenticated();

        //把token校验过滤器添加到过滤器链中
        //addFilterBefore表示在某某之前添加
        //这里表示我们的jwtAuthenticationTokenFilter过滤器在UsernamePasswordAuthenticationFilter之前执行
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
    }
    
    //在接口中我们通过AuthenticationManager的authenticate方法来进行用户认证,
    //所以需要在SecurityConfig中配置把AuthenticationManager注入容器。
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}

```

2.**Spring Security 5.7+** 推荐的 **无侵入式 Bean 配置**（不再继承 `WebSecurityConfigurerAdapter`）

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. 密码加密器
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. 核心安全过滤链
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtFilter) throws Exception {
        http
            .csrf().disable()                                // 关闭 CSRF
            .sessionManagement().sessionCreationPolicy(
                SessionCreationPolicy.STATELESS)             // 无状态会话
            .and()
            .authorizeHttpRequests(authz -> authz
                .antMatchers("/user/login").anonymous()     // 登录接口允许匿名
                .anyRequest().authenticated()               // 其他都要认证
            )
            // 在用户名/密码过滤器之前，先走 JWT 认证
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 3. 如果你有自定义的 UserDetailsService，也可以在这里注册
    // @Bean
    // public UserDetailsService userDetailsService(...) { ... }

    // 4. 如果你还需要暴露 AuthenticationManager（比如在 Controller 里手动调用）
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) 
            throws Exception {
        return authConfig.getAuthenticationManager();
    }
}

```

### 定义登录接口

```java
@RestController
public class LoginController {

    @Autowired
    private LoginServcie loginServcie;

    @PostMapping("/user/login")
    public ResponseResult login(@RequestBody User user){
        return loginServcie.login(user);
    }
}

```

```java
public class LoginServiceImpl implements LoginService {
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    RedisCache redisCache;
    @Override
    public ResponseResult login(User user){
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword());
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        if(Objects.isNull(authenticate)){
            throw new RuntimeException("用户名或密码错误");
        }
        //使用userid 生成token
        LoginUser loginUser =(LoginUser)authenticate.getPrincipal();
        String userId = loginUser.getUser().getId().toString();
        String jwt = JwtUtil.createJWT(userId);
        //authenticate存入redis
        redisCache.setCacheObject("login+"+userId,loginUser);
        //把token响应给前端
        HashMap<String,String> map = new HashMap<>();
        map.put("token",jwt);
        return new ResponseResult(200,"登录成功",map);
    }

    @Override
    public ResponseResult logout(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser)authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();
        redisCache.deleteObject("login:"+userId);
        return new ResponseResult(200,"注销成功");
    }
}

```

这段代码是 Spring Security + JWT 进行**登录认证**的 `LoginServiceImpl` 实现。它的主要作用是：

1. **验证用户名和密码**，通过 `AuthenticationManager` 进行认证。
2. **生成 JWT Token**，用于后续请求的身份认证。
3. **将用户信息存入 Redis**，方便后续的 Token 解析。
4. **返回 JWT Token 给前端**，前端在后续请求时携带 Token 进行身份认证。

------

 **🔍 详细解析代码**

```java
public class LoginServiceImpl implements LoginService {
```

这里 `LoginServiceImpl` **实现** 了 `LoginService` 接口，表示它是一个**用户登录的业务逻辑类**。

------

  **1️⃣ 注入依赖**

```java
@Autowired
AuthenticationManager authenticationManager;
@Autowired
RedisCache redisCache;
```

- `authenticationManager`：Spring Security 提供的认证管理器，**用来校验用户名和密码是否正确**。
- `redisCache`：用于**将用户信息存入 Redis**，以便后续使用。

------

 **2️⃣ 处理用户登录**

```java
@Override
public ResponseResult login(User user){
```

这个方法的作用是：
 **用户登录 -> 验证身份 -> 生成 Token -> 存入 Redis -> 返回 Token**

 **📌 2.1 构造 `AuthenticationToken` 进行认证**

```java
UsernamePasswordAuthenticationToken authenticationToken = 
    new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword());
```

- `UsernamePasswordAuthenticationToken` 是 Spring Security 内置的 **用户名+密码认证** 令牌。
- 这个对象**封装了用户提交的用户名和密码**，后续交给 `AuthenticationManager` 进行认证。

------

 **📌 2.2 进行身份认证**

```java
Authentication authenticate = authenticationManager.authenticate(authenticationToken);
```

- `authenticationManager.authenticate(authenticationToken)` 负责调用 **Spring Security 的认证流程**：
  - 通过 **`UserDetailsService` 查询用户信息**（通常是数据库查询）。
  - 使用 **`PasswordEncoder` 校验密码是否正确**。
  - 如果认证成功，返回一个 `Authentication` 对象，其中包含了用户的权限信息。
- 如果 `authenticate == null`，说明**用户名或密码错误**，抛出异常：

```java
if(Objects.isNull(authenticate)){
    throw new RuntimeException("用户名或密码错误");
}
```

------

 **📌 2.3 生成 JWT Token**

```java
LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
String userId = loginUser.getUser().getId().toString();
String jwt = JwtUtil.createJWT(userId);
```

- `authenticate.getPrincipal()` 返回的是 `UserDetails`（我们这里的 `LoginUser`）。
- `userId` 取出用户 ID（因为 JWT 需要一个唯一标识）。
- `JwtUtil.createJWT(userId)` 生成 JWT Token，后续请求都会携带这个 Token 进行身份认证。

------

 **📌 2.4 存入 Redis**

```java
redisCache.setCacheObject("login+"+userId, loginUser);
```

- 把 `LoginUser`（包括用户信息和权限）存入 Redis，键名是 `"login+userId"`。
- 后续用户请求时，系统会用 JWT 中的 `userId` 去 Redis 查找用户信息，**避免每次都查询数据库**。

------

 **📌 2.5 返回 Token**

```java
HashMap<String,String> map = new HashMap<>();
map.put("token", jwt);
return new ResponseResult(200, "登录成功", map);
```

- 创建 `HashMap<String, String>` 存放 `token`，返回给前端。
- 前端拿到 Token 后，**后续请求会在 `Authorization` 头部携带这个 Token 进行身份认证**。

------

 **🔎 代码执行流程**

1. **前端** 发送 `POST` 请求到 `/user/login`，携带用户名和密码。
2. **Spring Security 认证**
   - `authenticationManager.authenticate()` 调用 `UserDetailsService` 查询用户信息。
   - `PasswordEncoder` 进行密码校验。
   - 认证成功，返回 `Authentication` 对象。
3. **生成 JWT Token**，用于后续的请求身份认证。
4. **用户信息存入 Redis**，避免每次都查询数据库。
5. **返回 Token 给前端**，前端后续请求带上 Token。

------

 **📝 总结**

| 步骤              | 说明                                                         |
| ----------------- | ------------------------------------------------------------ |
| **1. 认证**       | `authenticationManager.authenticate(authenticationToken)` 进行用户认证 |
| **2. 生成 JWT**   | `JwtUtil.createJWT(userId)` 生成 Token                       |
| **3. 存入 Redis** | `redisCache.setCacheObject("login+"+userId, loginUser)` 缓存用户信息 |
| **4. 返回 Token** | 返回 JWT 给前端，前端存储并在请求中携带                      |

这段代码的目的是 **用 JWT 替代 Session 进行身份认证**，并结合 **Redis 进行缓存**，提升系统性能。🚀

### 认证过滤器

我们需要自定义一个过滤器，这个过滤器会去获取请求头中的token，对token进行解析取出其中的userid。

使用userid去redis中获取对应的LoginUser对象。

然后封装Authentication对象存入SecurityContextHolder

```java
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    @Autowired
    private RedisCache redisCache;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
                                    throws ServletException, IOException {
        // 1. 从请求头获取 token
        String token = request.getHeader("token");
        if (!StringUtils.hasText(token)) {
            // 如果没有携带 token，直接放行（让后续的过滤器或控制器决定是否需要登录）
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 解析 token，提取 userId
        String userId;
        try {
            Claims claims = JwtUtil.parseJWT(token);
            userId = claims.getSubject();      // JWT 的 subject 字段里存放的是用户 ID
        } catch (Exception e) {
            // 解析失败（过期、签名不匹配等），抛出异常或返回 401
            e.printStackTrace();
            throw new RuntimeException("token 非法或已过期");
        }

        // 3. 从 Redis 中加载用户信息
        String redisKey = "login:" + userId;
        LoginUser loginUser = redisCache.getCacheObject(redisKey);
        if (Objects.isNull(loginUser)) {
            // 如果缓存中没有用户信息，说明用户未登录或已注销
            throw new RuntimeException("用户未登录");
        }

        // 4. 构造 Authentication 并存入 SecurityContext
        //    TODO: 可以从 loginUser 中获取权限列表，填充到第三个参数 authorities
        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(
                loginUser,      // principal：当前登录用户详情
                null,           // credentials：因为已通过 JWT 认证，不需要密码
                null            // authorities：用户权限列表，后续可补充
            );
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // 5. 继续执行后续过滤器链
        filterChain.doFilter(request, response);
    }
}

```

### 注销

我们只需要定义一个注销接口，然后获取SecurityContextHolder中的认证信息，删除redis中对应的数据即可

```java
    @Override
    public ResponseResult logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userid = loginUser.getUser().getId();
        redisCache.deleteObject("login:"+userid);
        return new ResponseResult(200,"退出成功");
    }
```

---



# 授权

**对已经认证（登录）成功的用户，判断他是否有权限访问某些资源或执行某些操作,授权发生在认证之后。Spring Security 会根据用户的权限（`Authorities`）和访问的资源，决定是否放行。**

 🧠 一句话理解授权流程：

> 用户携带 Token 请求接口 → JWTFilter 提取用户权限 → Security 判断用户是否有权访问 URL（或方法）

## 🔐 Spring Security 授权的三种常见方式：

### ✅ 1. **基于 URL 的授权**（控制接口访问权限）

配置在 `SecurityFilterChain` 里的：

```java
http.authorizeHttpRequests()
    .antMatchers("/admin/**").hasRole("ADMIN")      // 访问 /admin 开头接口必须有 ADMIN 角色
    .antMatchers("/user/**").hasAnyAuthority("user:add", "user:update") // 具备任一权限可访问
    .anyRequest().authenticated();                  // 其他请求都要登录认证
```

**常用方法:**

| 方法                      | 说明                                      |
| ------------------------- | ----------------------------------------- |
| `.hasAuthority("权限名")` | 拥有某个具体权限                          |
| `.hasAnyAuthority(...)`   | 拥有任意一个权限即可                      |
| `.hasRole("角色名")`      | 拥有某个角色（底层其实是加前缀：`ROLE_`） |
| `.hasAnyRole(...)`        | 拥有任意一个角色                          |
| `.authenticated()`        | 已登录用户可访问                          |
| `.permitAll()`            | 所有人都可以访问                          |
| `.anonymous()`            | 匿名用户才能访问（未登录）                |

------

### ✅ 2. **基于方法的授权**（精细控制某个接口或业务方法）

使用注解，在 Controller 或 Service 方法上使用：

 **启用方法级安全：**

```java
@EnableMethodSecurity // Spring Security 6+ 推荐的（老版本用 @EnableGlobalMethodSecurity）
```

 **然后用注解控制权限：**

```java
@PreAuthorize("hasAuthority('user:add')")
@GetMapping("/user/add")
public String addUser() {
    return "添加用户";
}
```

**常用表达式：**

| 表达式                               | 说明                                             |
| ------------------------------------ | ------------------------------------------------ |
| `hasAuthority('xxx')`                | 拥有指定权限                                     |
| `hasRole('ADMIN')`                   | 拥有指定角色（会自动加 `ROLE_` 前缀）            |
| `hasAnyAuthority('a','b')`           | 拥有任一权限                                     |
| `#id == authentication.principal.id` | 当前用户只能操作自己的数据（比如只改自己的资料） |

------

### ✅ 3. **自定义权限校验逻辑**

如果你想写一个更加灵活的权限判断逻辑，可以自定义权限校验组件：

```java
@Component("myAuth")
public class MyAuthorizationService {
    public boolean checkPermission(Authentication auth, String permission) {
        // 判断 auth 中的权限列表是否包含 permission
        return auth.getAuthorities().stream()
                   .anyMatch(a -> a.getAuthority().equals(permission));
    }
}
```

然后这样用：

```java
@PreAuthorize("@myAuth.checkPermission(authentication, 'user:delete')")
```

## 具体实现

### **限制访问资源所需权限**

```java
@RestController
public class HelloController {

    @RequestMapping("/hello")
    @PreAuthorize("hasAuthority('test')")
    public String hello(){
        return "hello";
    }
}

```

### **封装权限信息**

封装权限信息到LoginUser

```java
@Data
@NoArgsConstructor
public class LoginUser implements UserDetails {

    private User user;
  
    //存储权限信息
    private List<String> permissions;
  
  
    public LoginUser(User user,List<String> permissions) {
        this.user = user;
        this.permissions = permissions;
    }


    //存储SpringSecurity所需要的权限信息的集合
    @JSONField(serialize = false)
    private List<GrantedAuthority> authorities;

    @Override
    public  Collection<? extends GrantedAuthority> getAuthorities() {
        if(authorities!=null){
            return authorities;
        }
        //把permissions中字符串类型的权限信息转换成GrantedAuthority对象存入authorities中
        authorities = permissions.stream().
                map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

```

在UserDetailsServiceImpl中去把权限信息封装到LoginUser中了

```java
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserName,username);
        User user = userMapper.selectOne(wrapper);
        if(Objects.isNull(user)){
            throw new RuntimeException("用户名或密码错误");
        }
        // 根据用户查询权限信息 添加到LoginUser中
        List<String> list = new ArrayList<>(Arrays.asList("test"));
        return new LoginUser(user,list);
    }
}
```

去JwtAuthenticationTokenFilter类中把用户中的authorities权限封装到UsernamePasswordAuthenticationToken中，然后交给SecurityContextHolder。

```java
// 存入SecurityContextHolder
// 获取权限信息封装到 setAuthentication 中
UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
        new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

```

### 从数据库查询权限信息

RBAC权限模型（Role-Based Access Control）即：基于角色的权限控制。这是目前最常被开发者使用也是相对易用、通用权限模型。

<img src="https://cdn.jsdelivr.net/gh/KNeegcyao/picdemo/img/2627855816.png" alt="img" style="zoom:50%;" />

<img src="https://cdn.jsdelivr.net/gh/KNeegcyao/picdemo/img/image-20250405231327498.png" alt="img" style="zoom:67%;" />

```java
/**
 * 菜单表(Menu)实体类
 *
 * @author makejava
 * @since 2021-11-24 15:30:08
 */
@TableName(value="sys_menu")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Menu implements Serializable {
    private static final long serialVersionUID = -54979041104113736L;
  
        @TableId
    private Long id;
    /**
    * 菜单名
    */
    private String menuName;
    /**
    * 路由地址
    */
    private String path;
    /**
    * 组件路径
    */
    private String component;
    /**
    * 菜单状态（0显示 1隐藏）
    */
    private String visible;
    /**
    * 菜单状态（0正常 1停用）
    */
    private String status;
    /**
    * 权限标识
    */
    private String perms;
    /**
    * 菜单图标
    */
    private String icon;
  
    private Long createBy;
  
    private Date createTime;
  
    private Long updateBy;
  
    private Date updateTime;
    /**
    * 是否删除（0未删除 1已删除）
    */
    private Integer delFlag;
    /**
    * 备注
    */
    private String remark;
}

```

```sql
create table sys_menu
(
    id          bigint auto_increment
        primary key,
    menu_name   varchar(64)  default 'NULL' not null comment '菜单名',
    path        varchar(200)                null comment '路由地址',
    component   varchar(255)                null comment '组件路径',
    visible     char         default '0'    null comment '菜单状态（0显示 1隐藏）',
    status      char         default '0'    null comment '菜单状态（0正常 1停用）',
    perms       varchar(100)                null comment '权限标识',
    icon        varchar(100) default '#'    null comment '菜单图标',
    create_by   bigint                      null,
    create_time datetime                    null,
    update_by   bigint                      null,
    update_time datetime                    null,
    del_flag    int          default 0      null comment '是否删除（0未删除 1已删除）',
    remark      varchar(500)                null comment '备注'
)
    comment '菜单表';

```

### 接口具体实现

mapper层实现

```java
public interface MenuMapper extends BaseMapper<Menu> {
    List<String> selectPermsByUserId(Long id);
}

```

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="com.example.mapper.MenuMapper">


    <select id="selectPermsByUserId" resultType="java.lang.String">
        SELECT
            DISTINCT m.`perms`
        FROM
            sys_user_role ur
            LEFT JOIN `sys_role` r ON ur.`role_id` = r.`id`
            LEFT JOIN `sys_role_menu` rm ON ur.`role_id` = rm.`role_id`
            LEFT JOIN `sys_menu` m ON m.`id` = rm.`menu_id`
        WHERE
            user_id = #{userid}
            AND r.`status` = 0
            AND m.`status` = 0
    </select>
</mapper>

```

完善UserServiceImpl

```java
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MenuMapper menuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserName,username);
        User user = userMapper.selectOne(wrapper);
        if(Objects.isNull(user)){
            throw new RuntimeException("用户名或密码错误");
        }
        List<String> permissionKeyList =  menuMapper.selectPermsByUserId(user.getId());
        return new LoginUser(user,permissionKeyList);
    }
}

```

---

# 自定义失败处理

## 异常拦截点：ExceptionTranslationFilter

- **职责**：在过滤器链中专门捕获认证或授权过程中抛出的异常。
- **工作流程**：
  1. 当认证（Authentication）或授权（Authorization）失败时，相关的过滤器（如 `UsernamePasswordAuthenticationFilter`、`FilterSecurityInterceptor`）会抛出异常。
  2. `ExceptionTranslationFilter` 捕获这些异常，判断是认证失败还是授权失败。
  3. 根据异常类型分别交给 **`AuthenticationEntryPoint`** 或 **`AccessDeniedHandler`** 去处理。

------

## 认证失败 vs 授权失败

| 异常类型                    | 场景                                                  | 默认处理                                                     |
| --------------------------- | ----------------------------------------------------- | ------------------------------------------------------------ |
| **AuthenticationException** | 用户未登录、登录凭证（用户名/密码、Token）不合法      | 调用 `AuthenticationEntryPoint.commence()`，默认重定向到登录页面或返回 401 |
| **AccessDeniedException**   | 用户已登录，但没有访问某个资源的权限（角色/权限不足） | 调用 `AccessDeniedHandler.handle()`，默认返回 403 页面或错误响应 |

------

## 自定义处理器

### 自定义 `AuthenticationEntryPoint`

当捕获到 `AuthenticationException` 时会走这里，通常用于“未登录”或“Token 无效”场景。

```java
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
                         throws IOException, ServletException {
        // 构造统一的 JSON 响应体
        ResponseResult result = new ResponseResult(
            HttpStatus.UNAUTHORIZED.value(), 
            "认证失败，请重新登录"
        );
        String json = JSON.toJSONString(result);
        // 直接将 JSON 写回响应
        WebUtils.renderString(response, json);
    }
}
```

- **`commence()` 方法**：
  - `response`：直接往 HTTP 响应里写状态码和 JSON，前端可以根据结构化数据统一处理。
  - `ResponseResult`：你的统一响应对象，包含 `code`、`msg`、`data` 等字段。
  - `WebUtils.renderString()`：工具方法，设置响应类型为 `application/json` 并写入字符串。

### 自定义 `AccessDeniedHandler`

当捕获到 `AccessDeniedException` 时会走这里，通常用于“权限不足”场景。

```java
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
                       throws IOException, ServletException {
        ResponseResult result = new ResponseResult(
            HttpStatus.FORBIDDEN.value(), 
            "权限不足"
        );
        String json = JSON.toJSONString(result);
        WebUtils.renderString(response, json);
    }
}
```

- **`handle()` 方法**：
  - 同样构造统一的 JSON 响应并写回，状态码使用 403（Forbidden）。

------

## 将自定义处理器注入到 Spring Security

在你的 `HttpSecurity` 配置中，调用 `exceptionHandling()` 方法将它们注册进去：

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private AuthenticationEntryPointImpl authenticationEntryPoint;
    @Autowired
    private AccessDeniedHandlerImpl accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
          // ... 其他配置 ...
          .exceptionHandling()
            .authenticationEntryPoint(authenticationEntryPoint)  // 认证失败处理
            .accessDeniedHandler(accessDeniedHandler)            // 授权失败处理
          // ... 继续你的过滤链配置 ...
        ;
        return http.build();
    }
}
```

- **`authenticationEntryPoint(...)`**：指定当出现 `AuthenticationException` 时调用哪个 Bean。
- **`accessDeniedHandler(...)`**：指定当出现 `AccessDeniedException` 时调用哪个 Bean。



# 跨域

## 什么是跨域（CORS）？

- **同源策略**：浏览器出于安全考虑，只有当 协议、域名、端口 三者都相同时，才能正常发起 AJAX 请求。
- **跨域场景**：前后端分离时，前端通常跑在 `http://localhost:3000`，后端在 `http://localhost:8080`，端口不同，就属于跨域。
- **CORS**（Cross‑Origin Resource Sharing）机制允许服务器声明：哪些域名、哪些方法、哪些请求头可以访问它的资源。

------

## Spring MVC 层面允许跨域

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")               // ① 对所有接口都允许跨域
                .allowedOriginPatterns("*")      // ② 允许任意域名发起跨域请求（也可以写具体域名列表）
                .allowCredentials(true)          // ③ 允许携带 Cookie（如果前端要发送或接收 cookie，必须开启）
                .allowedMethods("GET","POST","DELETE","PUT") // ④ 允许的方法
                .allowedHeaders("*")            // ⑤ 允许的请求头
                .maxAge(3600);                  // ⑥ 预检请求的缓存时间（秒），在这段时间内浏览器不再发第二次预检
    }
}
```

- **为什么要 MVC 配置？**
  Spring MVC 默认会注册一个 `CorsFilter`，它根据上面的规则响应浏览器的 **预检请求**（`OPTIONS`），并在实际请求中加上相应的 CORS 响应头。

------

## Spring Security 层面允许跨域

即使你在 MVC 层面配置了 CORS，Spring Security 默认也会拦截所有请求（包括 `OPTIONS` 预检），导致预检被拒绝。
 所以在你的安全配置里，还要**显式开启** CORS 支持：

```java
@Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                //关闭csrf
                .csrf().disable()
                //不通过Session获取SecurityContext
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // 对于登录接口 允许匿名访问
                .antMatchers("/user/login").anonymous()
                // 除上面外的所有请求全部需要鉴权认证
                .anyRequest().authenticated();

        //添加过滤器
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        //配置异常处理器
        http.exceptionHandling()
                //配置认证失败处理器
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler);

        //允许跨域
        http.cors();
    }

```

- **`http.cors()`**
  它会让 Spring Security 去查找 Spring MVC 中的 `CorsConfiguration`（即上面 `CorsConfig` 定义的规则），并在 SecurityFilterChain 的最前面插入一个 `CorsFilter`。
- **效果**：
  - **预检请求（OPTIONS）** 先经过 CORS 过滤器，返回允许的跨域响应头，浏览器才会继续发真正的请求。
  - **实际请求** 也会带上 `Access-Control-Allow-Origin`、`Access-Control-Allow-Credentials` 等头，浏览器才允许前端 JS 访问响应内容。

------

## 为什么两处都要配置？

1. **MVC 层**：负责 **定义** 哪些路径、哪些域名、哪些方法可以跨域。
2. **Security 层**：负责 **允许** Spring Security 过滤链里也执行这套跨域规则，否则所有跨域请求（包括预检）都会被 Security 拦截成 401/403。

---

# 自定义处理器

## 认证成功处理器

实际上在UsernamePasswordAuthenticationFilter进行登录认证的时候，如果登录成功了是会调用AuthenticationSuccessHandler的方法进行认证成功后的处理的。AuthenticationSuccessHandler就是登录成功处理器。

我们也可以自己去自定义成功处理器进行成功后的相应处理。

```java
@Component
public class SGSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        System.out.println("认证成功了");
    }
}
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthenticationSuccessHandler successHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin().successHandler(successHandler);

        http.authorizeRequests().anyRequest().authenticated();
    }
}
```

## 认证失败处理器

实际上在UsernamePasswordAuthenticationFilter进行登录认证的时候，如果认证失败了是会调用AuthenticationFailureHandler的方法进行认证失败后的处理的。AuthenticationFailureHandler就是登录失败处理器。

我们也可以自己去自定义失败处理器进行失败后的相应处理。

```java
@Component
public class SGFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        System.out.println("认证失败了");
    }
}
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthenticationSuccessHandler successHandler;

    @Autowired
    private AuthenticationFailureHandler failureHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin()
//                配置认证成功处理器
                .successHandler(successHandler)
//                配置认证失败处理器
                .failureHandler(failureHandler);

        http.authorizeRequests().anyRequest().authenticated();
    }
}
```

**⚠️注意：**这和之前的`AuthenticationEntryPoint`两个类虽然都是处理 **认证失败** 的情况，但它们的作用场景是完全不同的

**总结区别:**

| 比较项   | `AuthenticationEntryPoint`                               | `AuthenticationFailureHandler`                               |
| -------- | -------------------------------------------------------- | ------------------------------------------------------------ |
| 所在阶段 | 用户访问受保护资源但未登录                               | 用户登录时输入错误                                           |
| 出发原因 | 请求接口没带 token、token 无效                           | 登录接口用户名/密码错误                                      |
| 响应方式 | 通常返回 401（未认证）                                   | 通常返回 401（登录失败）或业务自定义状态码                   |
| 注册位置 | `http.exceptionHandling().authenticationEntryPoint(...)` | 登录过滤器 `UsernamePasswordAuthenticationFilter` 的 `setAuthenticationFailureHandler(...)` 方法 |

## 登出成功处理器

```java
@Component
public class SGLogoutSuccessHandler implements LogoutSuccessHandler {
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        System.out.println("注销成功");
    }
}
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthenticationSuccessHandler successHandler;

    @Autowired
    private AuthenticationFailureHandler failureHandler;

    @Autowired
    private LogoutSuccessHandler logoutSuccessHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin()
//                配置认证成功处理器
                .successHandler(successHandler)
//                配置认证失败处理器
                .failureHandler(failureHandler);

        http.logout()
                //配置注销成功处理器
                .logoutSuccessHandler(logoutSuccessHandler);

        http.authorizeRequests().anyRequest().authenticated();
    }
}
```
