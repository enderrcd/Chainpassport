package com.web1.controller;

import com.web1.dto.ApiResponse;
import com.web1.dto.LoginRequest;
import com.web1.entity.User;
import com.web1.service.UserService;
import com.web1.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

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
    public ApiResponse<Object> logout(@RequestHeader("authentication") String authHeader) {
        try {
            // 从authentication头中提取token
            String token = authHeader.replace("Bearer ", "");
            
            // 从token中获取用户ID
            String userId = jwtUtil.getUserIdFromToken(token);
            
            // 登出用户
            if (userId != null) {
                userService.logout(userId);
            }
            
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error("登出失败：" + e.getMessage());
        }
    }
    
    /**
     * 用户注册接口 - ZKP身份认证
     */
    @PostMapping("/public/register")
    public ApiResponse<Object> register(@RequestBody LoginRequest registerRequest, HttpSession session) {
        try {
            // 调用用户服务进行ZKP注册流程
            Object sessionData = userService.zkpRegister(registerRequest, session);
            return ApiResponse.success(sessionData);
        } catch (Exception e) {
            return ApiResponse.error("注册失败：" + e.getMessage());
        }
    }
}
