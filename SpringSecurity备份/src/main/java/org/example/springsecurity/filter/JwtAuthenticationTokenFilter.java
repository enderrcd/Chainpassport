package org.example.springsecurity.filter;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.example.springsecurity.domain.LoginUser;
import org.example.springsecurity.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
            byte[] decode = Base64.getDecoder().decode(cacheObject1.toString());
            try {
                KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");
                PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(decode));
                //帮助他授权并且放行
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(publicKey, null, List.of(new GrantedAuthority() {
                    @Override
                    public String getAuthority() {
                        return "ROLE_USER";
                    }
                }));
                //authenticationToken.setAuthenticated(true);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                filterChain.doFilter(httpServletRequest, httpServletResponse);
                return;
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }

        }
        //把这个jsonobject转换成loginuser
        LoginUser cacheObject = JSONObject.parseObject(loginuser.toString(), LoginUser.class);
        if(cacheObject == null){
            httpServletResponse.setStatus(401);
            //重定向到主页面
            httpServletResponse.sendRedirect("http://localhost:8080/home");
            //filterChain.doFilter(httpServletRequest, httpServletResponse);
            throw new RuntimeException("用户未登录");
        }else{
            //存入ContextHolder
            //TODO 获取权限信息封装到Authentication中
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(cacheObject, null, cacheObject.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            logger.info("用户认证成功,准备放行");
            //刷新过期时间
            redisCache.setCacheObject("login:"+authentication,cacheObject,30, TimeUnit.MINUTES);
            log.info("获取的用户信息{}",cacheObject);
            filterChain.doFilter(httpServletRequest, httpServletResponse);

        }
    }
}
