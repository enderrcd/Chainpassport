package com.web1.service;

import com.web1.dto.LoginRequest;
import com.web1.entity.User;
import com.web1.repository.UserRepository;
import com.web1.util.JwtUtil;
import com.web1.util.RedisUtil;
import com.web1.util.ZKPUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.security.KeyPair;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务类
 * 简化版，移除了复杂的认证逻辑
 */
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private ZKPUtil zkpUtil;
    
    /**
     * 用户登录
     */
    public String login(LoginRequest loginRequest) {
        // 查找用户
        User user = userRepository.findByUserNameAndDelFlag(loginRequest.getUserName(), 0);
        
        if (user != null && passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            // 生成JWT令牌
            String token = jwtUtil.generateToken(user.getUserId().toString());
            
            // 将用户信息存储到Redis缓存中，过期时间30分钟
            redisUtil.set("login:user:" + user.getUserId(), user, 30, TimeUnit.MINUTES);
            
            return token;
        }
        
        throw new RuntimeException("用户名或密码错误");
    }
    
    /**
     * 用户登出
     */
    public void logout(String userId) {
        // 从Redis中删除该用户的登录信息
        redisUtil.delete("login:user:" + userId);
    }
    
    /**
    /**
     * 根据用户名查找用户
     */
    public User findByUserName(String userName) {
        return userRepository.findByUserNameAndDelFlag(userName, 0);
    }
    
    /**
     * ZKP身份认证注册
     */
    public Object zkpRegister(LoginRequest registerRequest, HttpSession session) {
        try {
            // 1. 生成Ed25519密钥对
            KeyPair keyPair = zkpUtil.generateKeyPair();
            
            // 2. 生成唯一的用户UUID
            String userId = UUID.randomUUID().toString();
            
            // 3. 创建JWT令牌
            String jwt = jwtUtil.generateToken(userId);
            
            // 4. 将JWT存储在session中
            session.setAttribute("loginJwt", jwt);
            
            // 5. 将私钥存储到Redis缓存中
            String privateKeyStr = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
            redisUtil.set("loginPrivate:" + jwt, privateKeyStr, 30, TimeUnit.MINUTES);
            
            // 6. 将公钥存储到Redis缓存中
            String publicKeyStr = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            redisUtil.set("loginPublic:" + jwt, publicKeyStr, 30, TimeUnit.MINUTES);
            
            // 7. 向认证服务发送请求（这里预留接口，实际需要HTTP客户端调用）
            // 7. 向认证服务发送请求，传递公钥和JWT
            zkpUtil.sendToAuthService(publicKeyStr, jwt);
            
            // 8. 返回session信息
            // 8. 返回session信息 - 按接口文档要求返回字符串表示
            return session.toString();
            
        } catch (Exception e) {
            throw new RuntimeException("ZKP注册失败：" + e.getMessage());
        }
    }
    
    /**
     * 注册新用户
     * 注意：此处应添加密码加密逻辑
     */
    public User register(User user) {
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 保存用户
        return userRepository.save(user);
    }
}
