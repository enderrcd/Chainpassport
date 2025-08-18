package com.web1.controller;

import com.web1.dto.ApiResponse;
import com.web1.service.ZKPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/public")
public class ZKPController {
    
    @Autowired
    private ZKPService zkpService;
    
    /**
     * 用户注册接口 - 生成密钥对并初始化ZKP认证流程
     */
    @PostMapping("/register")
    public ApiResponse<HttpSession> register(HttpSession session) {
        try {
            zkpService.initializeZKP(session);
            return ApiResponse.success(session);
        } catch (Exception e) {
            return ApiResponse.error("注册失败：" + e.getMessage());
        }
    }
    
    /**
     * 零知识证明认证按钮接口
     */
    @PostMapping("/login/button")
    public ApiResponse<String> zkpLogin(HttpSession session) {
        try {
            String token = zkpService.performZKPAuthentication(session);
            return ApiResponse.success(token);
        } catch (Exception e) {
            return ApiResponse.error("认证失败：" + e.getMessage());
        }
    }
}