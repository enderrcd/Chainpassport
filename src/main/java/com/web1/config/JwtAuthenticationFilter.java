package com.web1.config;

import com.web1.util.JwtUtil;
import com.web1.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT认证过滤器
 * 用于验证每个请求中的JWT令牌
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // 获取请求头中的认证信息
        String authHeader = request.getHeader("authentication");
        
        if (authHeader == null || authHeader.isEmpty()) {
            // 也检查Authorization头（兼容性）
            authHeader = request.getHeader("Authorization");
        }
        
        String token = null;
        String userId = null;
        
        // 提取JWT令牌
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                userId = jwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                logger.warn("JWT令牌解析失败: " + e.getMessage());
            }
        }
        
        // 验证令牌并设置安全上下文
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // 检查Redis中是否存在用户登录信息
            Object userInfo = redisUtil.get("login:user:" + userId);
            
            if (userInfo != null && jwtUtil.validateToken(token, userId)) {
                // 创建认证对象
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
                
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // 设置安全上下文
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}