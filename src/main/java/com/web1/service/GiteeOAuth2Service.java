package com.web1.service;

import com.web1.util.JwtUtil;
import com.web1.util.RedisUtil;
import com.web1.util.ZKPUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class GiteeOAuth2Service {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private ZKPUtil zkpUtil;
    
    @Value("${gitee.oauth2.client-id}")
    private String clientId;
    
    @Value("${gitee.oauth2.redirect-uri}")
    private String redirectUri;
    
    /**
     * 获取Gitee OAuth2配置信息
     */
    public Map<String, String> getOAuth2Config(String giteeId) {
        Map<String, String> config = new HashMap<>();
        
        // 检查giteeId是否有效且未过期
        if (giteeId != null && !giteeId.equals("null")) {
            // 从Redis检查giteeId是否存在且未过期
            Object cachedData = redisUtil.get("giteeId:" + giteeId);
            if (cachedData != null) {
                // giteeId有效，可以跳过授权重新生成JWT
                String newJwt = jwtUtil.generateToken(giteeId);
                config.put("skip_auth", "true");
                config.put("new_jwt", newJwt);
                return config;
            }
        }
        
        // 返回OAuth2配置信息
        config.put("client_id", clientId);
        config.put("redirect_uri", redirectUri);
        config.put("response_type", "code");
        config.put("scope", "user_info");
        
        return config;
    }
    
    /**
     * 执行零知识证明认证
     */
    public String performZKPAuthentication(Map<String, String> zkpRequest, HttpSession session) {
        // 从session中获取JWT令牌
        String jwt = (String) session.getAttribute("loginJwt");
        
        if (jwt == null || jwt.isEmpty()) {
            throw new RuntimeException("请先去A认证");
        }
        
        try {
            // 从Redis获取公钥
            String publicKey = (String) redisUtil.get("loginPublic:" + jwt);
            if (publicKey == null) {
                throw new RuntimeException("公钥不存在，请重新认证");
            }
            
            // 调用ZKP认证服务
            // 调用ZKP认证服务 - 向http://localhost:8080/login/ZKP发送请求
            String signature = zkpUtil.callZKPService(jwt);
            
            // 验证签名
            boolean isValid = zkpUtil.verifySignature(jwt, signature, publicKey);
            
            if (isValid) {
                // 认证成功，生成新的token
                String token = jwtUtil.generateToken(UUID.randomUUID().toString());
                return token;
            } else {
                throw new RuntimeException("认证失败，请重新认证");
            }
            
        } catch (Exception e) {
            throw new RuntimeException("认证过程出错：" + e.getMessage());
        }
    }
}