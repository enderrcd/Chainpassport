package com.web1.controller;

import com.web1.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/public")
public class GiteeController {
    
    @Value("${gitee.oauth2.client-id}")
    private String clientId;
    
    @Value("${gitee.oauth2.redirect-uri}")
    private String redirectUri;
    
    /**
     * 获取Gitee OAuth2配置信息
     */
    @PostMapping("/login/gitee/config")
    public ApiResponse<Map<String, String>> getGiteeConfig(@RequestHeader(value = "giteeId", required = false) String giteeId) {
        try {
            Map<String, String> config = new HashMap<>();
            config.put("client_id", clientId);
            config.put("redirect_uri", redirectUri);
            config.put("response_type", "code");
            config.put("scope", "user_info");
            
            return ApiResponse.success(config);
        } catch (Exception e) {
            return ApiResponse.error("获取配置失败：" + e.getMessage());
        }
    }
}