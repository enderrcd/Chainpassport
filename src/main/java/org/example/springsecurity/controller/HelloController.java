package org.example.springsecurity.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.example.springsecurity.Service.impl.UserServiceImpl;
import org.example.springsecurity.domain.*;
import org.example.springsecurity.utils.*;
import org.example.springsecurity.另一个网页的身份生成代码.Ed25519Example;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping
public class HelloController {
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private Oauth2Properties oauth2Properties;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private JwtProperties jwtProperties;

    @GetMapping("/h1")
    @PreAuthorize("hasAuthority('system:test:list')")
    public Result hello() {
        Result result = Result.success();
        result.setMsg("hello world");
        return result;
    }

    @PostMapping("/login")
    public Result login(@RequestBody User userDto) {
        return userService.login(userDto);
    }
    @PostMapping("/logout/user")
    public Result logout(@RequestHeader String authentication) {
        log.info("logout");
        userService.logout(authentication);
        return Result.success();
    }
    //千万记得给jwt设置保存时间，这样子可以避免重复授权或者登录。。。。。。。。。。。。
    @PostMapping("/public/login/gitee/config")
    public Result<Oauth2VO> registerGiteeHelp(@RequestHeader String authentication) {
        Object loginuser  = redisCache.getCacheObject("login:" + authentication);
        //把这个jsonobject转换成loginuser
        LoginUser cacheObject = JSONObject.parseObject(loginuser.toString(), LoginUser.class);
        if(cacheObject == null){
            return Result.success(Oauth2VO.builder()
                    .client_id(oauth2Properties.getClient_id())
                    .redirect_uri(oauth2Properties.getRedirect_uri())
                    .response_type(oauth2Properties.getResponse_type())
                    .build());
        }else{
            return Result.success(Oauth2VO.builder()
                    .client_id(oauth2Properties.getClient_id())
                    .redirect_uri(oauth2Properties.getRedirect_uri())
                    .response_type(oauth2Properties.getResponse_type())
                    .scope(cacheObject.getUser().getScope())
                    .build());
        }
        //前端最初的界面为http://localhost:8080/oauth2/register/gitee
       //前端收到这个数据后经过拼接后跳转到gitee授权页面，在这个页面进行 授权（这个页面是官方的)
        //用户授权完毕之后，gitee会重定向到 redirect_uri（这就是回到我们自己的页面了），并携带code参数
    }

    @PostMapping("/public/login/ZKP")
    public Result registerZKP(@RequestParam PublicKey PublicSecret, @RequestParam String jwt) {
          //把公钥存储起来，并且设置过期时间
        redisCache.setCacheObject("loginPublic:"+jwt,PublicSecret);
        redisCache.expire("loginPublic:"+jwt,jwtProperties.getUserTtl(), TimeUnit.SECONDS);
        JWTHolder.set(jwt);
        return Result.success();
    }

    @PostMapping("/public/login/button")
    public Result<String> registerButton() throws URISyntaxException, IOException {
        String jwt = JWTHolder.get();
        if(jwt == null){
            return Result.error("请先去A认证");
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost();
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.addHeader("Accept", "application/json");
        httpPost.addHeader("authentication", jwt);
        URIBuilder uriBuilder = new URIBuilder("http://localhost:8080/login/ZKP");
        httpPost.setURI(uriBuilder.build());
        CloseableHttpResponse execute = httpClient.execute(httpPost);
        try{
            if(execute.getStatusLine().getStatusCode() == 200){
                String result = EntityUtils.toString(execute.getEntity());
                JSONObject jsonObject = JSON.parseObject(result);
                String baseJwt = jsonObject.getString("data");
                byte[] decode = Base64.getDecoder().decode(baseJwt);
                String sign=new String(decode);
                Object cacheObject = redisCache.getCacheObject("loginPublic:" + jwt);
                PublicKey publicKey = JSONObject.parseObject(cacheObject.toString(), PublicKey.class);
                if(Ed25519Example.verify(jwt.getBytes("UTF-8"), decode,publicKey)){
                    JWTHolder.remove();
                    return Result.success("认证成功，确实是A区的认证");
                }else{
                    return Result.error("认证失败，请重新认证");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Result.error("认证失败，请重新认证");
    }
    //重定向之后前端自动提取出来code并且发出post请求传递给我code
    @PostMapping("/home")
    public Result<String> registerGitee(@RequestParam String code) throws URISyntaxException, IOException {

            log.info("code: {}", code);
            //拿到这个需要的code，然后去gitee获取用户信息
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost();
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Accept", "application/json");
            URIBuilder uriBuilder = new URIBuilder("https://gitee.com/oauth/token");
            uriBuilder.setParameter("client_id", oauth2Properties.getClient_id());
            uriBuilder.setParameter("redirect_uri", oauth2Properties.getRedirect_uri());
            uriBuilder.setParameter("client_secret", oauth2Properties.getClient_secret());
            uriBuilder.setParameter("code", code);
            URI uri = uriBuilder.build();
            httpPost.setURI(uri);
            CloseableHttpResponse execute = httpClient.execute(httpPost);
            try{
            if(execute.getStatusLine().getStatusCode() == 200){
                String result = EntityUtils.toString(execute.getEntity());
                JSONObject jsonObject = JSON.parseObject(result);
                String access_token = jsonObject.getString("access_token");
                String scope = jsonObject.getString("scope");
                HttpGet httpGet = new HttpGet();
                URIBuilder uriBuilder1 = new URIBuilder("https://gitee.com/api/v5/user");
                uriBuilder1.setParameter("access_token", access_token);
                httpGet.setURI(uriBuilder1.build());
                CloseableHttpResponse execute1 = httpClient.execute(httpGet);
                if(execute1.getStatusLine().getStatusCode() == 200){
                    String result1 = EntityUtils.toString(execute1.getEntity());
                    JSONObject jsonObject1 = JSON.parseObject(result1);
                    String login = jsonObject1.getString("login");
                    String name = jsonObject1.getString("name");
                    String avatar_url = jsonObject1.getString("avatar_url");
                    String email = jsonObject1.getString("email");
                    String id= jsonObject1.getString("id");
                    //为了验证在允许时间内是否验证过，我们先把这些信息存入到redis中
                    User user=User.builder()
                            .userName(login)
                            .nickName(name)
                            .avatar(avatar_url)
                            .email(email)
                            .giteeId(id)
                            .delFlag(0)
                            .status(String.valueOf(0L))
                            .scope(scope)
                            .build();
                    LoginUser oauth2User=new LoginUser(user, Arrays.asList("test","admin"));
                    //把用户信息插入到数据库，前提是先判断数据库中是否存在
                    User user1 = userService.selectUserByGiteeId(id);
                    if(user1==null) {
                        userService.registerGitee(oauth2User);
                    }
                    Map<String,Object>map=new HashMap<>();
                    map.put(JwtClaimsConstant.USER_ID, id);
                    String jwt = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), map);
                    redisCache.setCacheObject("login:"+jwt, oauth2User,30, TimeUnit.MINUTES);
                    return Result.success(jwt);
                }
            }
            return Result.error("获取用户信息失败"+execute.getStatusLine().getStatusCode());
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
                httpClient.close();
                execute.close();
            }
    }
}
