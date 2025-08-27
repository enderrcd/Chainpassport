package com.web1.controller;

import com.web1.dto.ApiResponse;
import com.web1.service.GiteeOAuth2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/public/login")
public class GiteeOAuth2Controller {
    
    @Autowired
    private GiteeOAuth2Service giteeOAuth2Service;
    
    /**
     * Gitee OAuth2配置接口
     */
    @PostMapping("/gitee/config")
    public ApiResponse<Map<String, String>> getGiteeConfig(
            @RequestHeader(value = "giteeId", required = false) String giteeId) {
        try {
            Map<String, String> config = giteeOAuth2Service.getOAuth2Config(giteeId);
            return ApiResponse.success(config);
        } catch (Exception e) {
            return ApiResponse.error("获取配置失败：" + e.getMessage());
        }
    }
    
    /**
     * 零知识证明认证按钮接口
     */
    @PostMapping("/button")
    public ApiResponse<String> zkpAuthentication(@RequestBody Map<String, String> zkpRequest, HttpSession session) {
        try {
            String token = giteeOAuth2Service.performZKPAuthentication(zkpRequest, session);
            return ApiResponse.success(token);
        } catch (Exception e) {
            return ApiResponse.error("认证失败：" + e.getMessage());
        }
    }
}