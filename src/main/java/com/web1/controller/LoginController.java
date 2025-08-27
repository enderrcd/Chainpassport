package com.web1.controller;

import com.web1.dto.ApiResponse;
import com.web1.dto.LoginRequest;
import com.web1.entity.User;
import com.web1.service.UserService;
import com.web1.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class LoginController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody LoginRequest loginRequest) {
        try {
            String token = userService.login(loginRequest);
            return ApiResponse.success(token);
        } catch (Exception e) {
            return ApiResponse.error("登录失败：" + e.getMessage());
        }
    }
    
    /**
     * 用户登出接口
     */
    @PostMapping("/logout/user")
    public ApiResponse<Object> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            // 从Authorization头中提取token
            String token = authHeader.replace("Bearer ", "");
            
            // 从token中获取用户ID
            String userId = jwtUtil.getUserIdFromToken(token);
            
            // 登出用户
            if (userId != null) {
                userService.logout(userId);
            }
            
            return ApiResponse.success("登出成功");
        } catch (Exception e) {
            return ApiResponse.error("登出失败：" + e.getMessage());
        }
    }
    
    /**
     * 用户注册接口
     */
    @PostMapping("/public/register")
    public ApiResponse<User> register(@RequestBody User user) {
        try {
            User registeredUser = userService.register(user);
            return ApiResponse.success(registeredUser);
        } catch (Exception e) {
            return ApiResponse.error("注册失败：" + e.getMessage());
        }
    }
}
