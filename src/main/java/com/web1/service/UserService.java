package com.web1.service;

import com.web1.dto.LoginRequest;
import com.web1.entity.User;
import com.web1.repository.UserRepository;
import com.web1.util.JwtUtil;
import com.web1.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
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
        // 使用Spring Security进行身份认证
        UsernamePasswordAuthenticationToken authenticationToken = 
            new UsernamePasswordAuthenticationToken(loginRequest.getUserName(), loginRequest.getPassword());
        
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        
        if (authentication.isAuthenticated()) {
            // 获取用户信息
            User user = userRepository.findByUserNameAndDelFlag(loginRequest.getUserName(), 0);
            if (user != null) {
                // 生成JWT令牌
                String token = jwtUtil.generateToken(user.getUserId().toString());
                
                // 将用户信息存储到Redis缓存中，过期时间30分钟
                redisUtil.set("login:user:" + user.getUserId(), user, 30, TimeUnit.MINUTES);
                
                return token;
            }
        }
        
        throw new RuntimeException("用户名或密码错误");
    }
    
    /**
     * 用户登出
     */
    public void logout() {
        // 从SecurityContextHolder获取当前认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            String userId = (String) authentication.getPrincipal();
            
            // 从Redis中删除该用户的登录信息
            redisUtil.delete("login:user:" + userId);
        }
    }
    
    /**
     * 根据用户名查找用户
     */
    public User findByUserName(String userName) {
        return userRepository.findByUserNameAndDelFlag(userName, 0);
    }
}