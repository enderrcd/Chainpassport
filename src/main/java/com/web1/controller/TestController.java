package com.web1.controller;

import com.web1.dto.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器
 * 用于验证JWT认证是否正常工作
 */
@RestController
@RequestMapping("/test")
public class TestController {
    
    /**
     * 测试需要认证的接口
     */
    @GetMapping("/auth")
    public ApiResponse<String> testAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return ApiResponse.success("认证成功，用户ID: " + auth.getName());
        }
        return ApiResponse.error("认证失败");
    }
    
    /**
     * 测试公开接口
     */
    @GetMapping("/public")
    public ApiResponse<String> testPublic() {
        return ApiResponse.success("这是一个公开接口，无需认证");
    }
}