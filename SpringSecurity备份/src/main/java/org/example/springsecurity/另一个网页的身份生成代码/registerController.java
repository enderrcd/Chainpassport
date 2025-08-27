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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
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
    public Result<String> register(HttpServletRequest  request, HttpServletResponse  response) throws NoSuchAlgorithmException, NoSuchProviderException, URISyntaxException, IOException {
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
        //redisCache.setCacheObject("loginPrivate:"+jwt, keyPair.getPrivate());
        //把令牌存在session

        HttpSession session = request.getSession();
        session.setAttribute("loginJwt", jwt);
        redisCache.setCacheObject("loginPrivate:"+jwt,Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
        //向另一个网页发送请求，把公钥和jwt发送过去
        CloseableHttpClient client= HttpClients.createDefault();
        URIBuilder uriBuilder = new URIBuilder("http://localhost:8080/public/login/ZKP/hello");

        uriBuilder.addParameter("jwt", jwt);
        URI uri = uriBuilder.build();
        HttpPost httpPost = new HttpPost(uri);
        //序列化
        String json = JSONObject.toJSONString(keyPair.getPublic());
        log.info("公钥序列化：{}", json);
        httpPost.setHeader("PublicSecret", json);
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
        //服务器通过 Set-Cookie 响应头，将 sessionId 发送给客户端
        response.setHeader("Set-Cookie", "JSESSIONID=" + session.getId() + "; Path=/; HttpOnly");
        return Result.success(session.getId());
    }
    @PostMapping("/login/ZKP")
    public Result<String> calculateResult(@RequestHeader String authentication) throws Exception {
        log.info("用户令牌：{}", authentication);
        Object cacheObject = redisCache.getCacheObject("loginPrivate:" + authentication);
        if(cacheObject == null){
            return Result.error("用户未注册");
        }
        log.info("私钥：{}", cacheObject.toString());
        //解码并且转成privateKey
        byte[] decode = Base64.getDecoder().decode(cacheObject.toString());
        KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");
        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decode));
        //转换成PrivateSecret类型
        byte[] sign = Ed25519Example.sign(authentication.getBytes("UTF-8"), privateKey);
        return Result.success(Base64.getEncoder().encodeToString(sign));
    }
}
