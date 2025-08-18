# Web1 登录后端架构

基于Spring Boot + MySQL + Redis的登录认证系统，支持传统用户名密码登录、Gitee OAuth2登录和零知识证明(ZKP)认证。

## 功能特性

- 用户名密码登录认证
- JWT令牌生成和验证
- Redis缓存用户信息
- Gitee OAuth2第三方登录
- Ed25519零知识证明认证
- Spring Security安全框架

## 技术栈

- Spring Boot 2.7.0
- Spring Security
- Spring Data JPA
- MySQL 8.0
- Redis
- JWT (JSON Web Token)
- Ed25519 数字签名算法

## 项目结构

```
src/main/java/com/web1/
├── Web1Application.java          # Spring Boot启动类
├── config/
│   ├── SecurityConfig.java       # Spring Security配置
│   └── RedisConfig.java          # Redis配置
├── controller/
│   ├── LoginController.java      # 登录相关接口
│   ├── GiteeController.java      # Gitee OAuth2接口
│   └── ZKPController.java        # 零知识证明接口
├── service/
│   ├── UserService.java          # 用户服务
│   ├── UserDetailsServiceImpl.java # Spring Security用户详情服务
│   └── ZKPService.java           # 零知识证明服务
├── entity/
│   └── User.java                 # 用户实体类
├── repository/
│   └── UserRepository.java      # 用户数据访问层
├── dto/
│   ├── LoginRequest.java         # 登录请求DTO
│   └── ApiResponse.java          # 统一响应DTO
└── util/
    ├── JwtUtil.java              # JWT工具类
    └── RedisUtil.java            # Redis工具类
```

## 接口说明

### 1. 用户登录
- **接口**: POST /login
- **功能**: 用户名密码登录，返回JWT令牌
- **参数**: userName, password

### 2. 用户登出
- **接口**: POST /logout/user
- **功能**: 清除Redis中的用户信息
- **请求头**: authentication (JWT令牌)

### 3. Gitee OAuth2配置
- **接口**: POST /public/login/gitee/config
- **功能**: 获取Gitee OAuth2配置信息
- **请求头**: giteeId (可选)

### 4. ZKP用户注册
- **接口**: POST /public/register
- **功能**: 生成Ed25519密钥对，初始化ZKP认证

### 5. ZKP身份认证
- **接口**: POST /public/login/button
- **功能**: 执行零知识证明认证

## 环境配置

### 1. 数据库配置
修改 `src/main/resources/application.yml` 中的数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/web1_db
    username: your_username
    password: your_password
```

### 2. Redis配置
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: your_redis_password
```

### 3. Gitee OAuth2配置
```yaml
gitee:
  oauth2:
    client-id: your_gitee_client_id
    client-secret: your_gitee_client_secret
    redirect-uri: http://localhost:8080/oauth2/register/gitee
```

## 部署步骤

1. **初始化数据库**
   ```bash
   mysql -u root -p < database/init.sql
   ```

2. **启动Redis服务**
   ```bash
   redis-server
   ```

3. **编译运行项目**
   ```bash
   mvn clean compile
   mvn spring-boot:run
   ```

4. **访问接口**
   - 服务地址: http://localhost:8080
   - 测试用户: admin/123456 或 test/123456

## 注意事项

1. JWT令牌过期时间为30分钟
2. Redis缓存用户信息过期时间为30分钟
3. GiteeId过期时间为1小时
4. 所有密码使用BCrypt加密存储
5. ZKP认证使用Ed25519数字签名算法

## 安全特性

- 使用Spring Security进行身份认证和授权
- JWT令牌签名验证
- 密码BCrypt加密
- Redis存储敏感信息设置过期时间
- CORS跨域支持
- CSRF防护禁用（适用于API服务）