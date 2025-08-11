package org.example.springsecurity.另一个网页的身份生成代码;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.math.ec.rfc8032.Ed25519;
import org.example.springsecurity.domain.Result;
import org.example.springsecurity.domain.UserZKPVo;
import org.example.springsecurity.utils.JwtClaimsConstant;
import org.example.springsecurity.utils.JwtProperties;
import org.example.springsecurity.utils.JwtUtil;
import org.example.springsecurity.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.*;
import java.util.*;

@RestController
@RequestMapping("/public")
@Slf4j
public class registerController {
    @Autowired
    private Ed25519Example ed25519Example;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private RedisCache redisCache;
    @PostMapping("/register")
    public Result<UserZKPVo> register() throws NoSuchAlgorithmException, NoSuchProviderException, URISyntaxException, IOException {
        //根据Ed25519Example.java生成密钥对
        //然后返回密钥对
        KeyPair keyPair = Ed25519Example.generateKeyPair();
        //私钥自己保存下来，公钥发送给当前这个网页
        log.info("私钥：{}", keyPair.getPrivate().toString());

        log.info("公钥：{}", keyPair.getPublic().toString());
        UUID uuid = UUID.randomUUID();
        log.info("用户id：{}", uuid.toString());
        //生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, uuid.toString());
        String jwt = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(),claims);
        //回来把jwt令牌这个独立验证逻辑补充一下，在过滤器中
        redisCache.setCacheObject("loginPrivate:"+jwt, keyPair.getPrivate());
        //向另一个网页发送请求，把公钥和jwt发送过去
        CloseableHttpClient client= HttpClients.createDefault();
        URIBuilder uriBuilder = new URIBuilder("http://localhost:8080/public/login/ZKP");
        uriBuilder.addParameter("PublicSecret", keyPair.getPublic().toString());
        uriBuilder.addParameter("jwt", jwt);
        URI uri = uriBuilder.build();
        HttpPost httpPost = new HttpPost(uri);
        CloseableHttpResponse execute = client.execute(httpPost);
        try {
            if(execute.getStatusLine().getStatusCode() == 200){
                log.info("返回结果：{}", EntityUtils.toString(execute.getEntity()));
            }
        }catch (Exception e){
            log.error("错误：{}", e.getMessage());
            throw new RuntimeException(e);
        }
        finally {
            client.close();
            execute.close();
        }
        UserZKPVo userZKPVo = new UserZKPVo(keyPair.getPrivate(), jwt);
        return Result.success(userZKPVo);
    }
    @PostMapping("/login/ZKP")
    public Result<String> calculateResult(@RequestHeader String authentication) throws Exception {
        Object cacheObject = redisCache.getCacheObject("loginPrivate:" + authentication);
        if(cacheObject == null){
            return Result.error("用户未注册");
        }
        PrivateKey privateKey = JSONObject.parseObject(cacheObject.toString(), PrivateKey.class);
        //转换成PrivateSecret类型
        byte[] sign = Ed25519Example.sign(authentication.getBytes("UTF-8"), privateKey);
        return Result.success(Base64.getEncoder().encodeToString(sign));
    }
}
