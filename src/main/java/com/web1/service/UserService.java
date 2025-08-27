package com.web1.service;

import com.web1.dto.LoginRequest;
import com.web1.entity.User;
import com.web1.repository.UserRepository;
import com.web1.util.JwtUtil;
import com.web1.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
     * 根据用户名查找用户
     */
    public User findByUserName(String userName) {
        return userRepository.findByUserNameAndDelFlag(userName, 0);
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
