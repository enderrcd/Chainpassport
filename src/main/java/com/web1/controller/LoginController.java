package com.web1.controller;

import com.web1.dto.ApiResponse;
import com.web1.dto.LoginRequest;
import com.web1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
public class LoginController {
    
    @Autowired
    private UserService userService;
    
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
    public ApiResponse<Object> logout(@RequestHeader("authentication") String token) {
        try {
            userService.logout();
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error("登出失败：" + e.getMessage());
        }
    }
}