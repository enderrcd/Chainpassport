package org.example.springsecurity.filter;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.example.springsecurity.domain.LoginUser;
import org.example.springsecurity.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.management.remote.JMXServerErrorException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.PublicKey;

@Component
@Slf4j
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    @Autowired
    private RedisCache redisCache;
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        String authentication = httpServletRequest.getHeader("authentication");
        if(!StringUtils.hasText(authentication)){
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }
        logger.info("用户认证成功"+ authentication);
        Object loginuser  = redisCache.getCacheObject("login:" + authentication);
        //先判断是不是字符串类型的可以看出来是否为privatesecret
        Object cacheObject1 = redisCache.getCacheObject("loginPublic:" + authentication);
        if(cacheObject1 != null){
            logger.info("用户公钥认证成功");
            PublicKey publicKey = JSONObject.parseObject(cacheObject1.toString(), PublicKey.class);
            //帮助他授权并且放行
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(publicKey, null,null);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }
        //把这个jsonobject转换成loginuser
        LoginUser cacheObject = JSONObject.parseObject(loginuser.toString(), LoginUser.class);
        if(cacheObject == null){
            httpServletResponse.setStatus(401);
            //filterChain.doFilter(httpServletRequest, httpServletResponse);
            throw new RuntimeException("用户未登录");
        }else{
            //存入ContextHolder
            //TODO 获取权限信息封装到Authentication中
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(cacheObject, null, cacheObject.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            logger.info("用户认证成功,准备放行");
            log.info("获取的用户信息{}",cacheObject);
            filterChain.doFilter(httpServletRequest, httpServletResponse);

        }
    }
}
