package org.example.springsecurity.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.example.springsecurity.Service.impl.UserServiceImpl;
import org.example.springsecurity.domain.*;
import org.example.springsecurity.utils.*;
import org.example.springsecurity.另一个网页的身份生成代码.Ed25519Example;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
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
    public Result<Oauth2VO> registerGiteeHelp(@RequestHeader String giteeId) {
        if(StringUtils.hasText(giteeId)){
            return Result.success(Oauth2VO.builder()
                    .client_id(oauth2Properties.getClient_id())
                    .redirect_uri(oauth2Properties.getRedirect_uri())
                    .response_type(oauth2Properties.getResponse_type())
                    .build());
        }
        User user = userService.selectUserByGiteeId(giteeId);
        //把这个jsonobject转换成loginuser

        if(user== null){
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
                    .scope(user.getScope())
                    .build());
        }
        //前端最初的界面为http://localhost:8080/oauth2/register/gitee
       //前端收到这个数据后经过拼接后跳转到gitee授权页面，在这个页面进行 授权（这个页面是官方的)  不是拼接在上面那个界面上，而是https://gitee.com/oauth/authorize?client_id=fd645dd7bd720cf48d78fd5d379219147ac4863de9cc035aac22bec899305eac&redirect_uri=http://localhost:8080/home&response_type=code&scope=user_info
        //用户授权完毕之后，gitee会重定向到 redirect_uri（这就是回到我们自己的页面了），并携带code参数
    }

    @PostMapping("/public/login/ZKP/hello")
    public Result registerZKP(@RequestHeader String PublicSecret, @RequestParam String jwt) {
          //把公钥存储起来，并且设置过期时间
        //把字符串转换成对象
        PublicKey publicKey = JSONObject.parseObject(PublicSecret, PublicKey.class);
        log.debug("公钥{}",publicKey);
        redisCache.setCacheObject("loginPublic:"+jwt,Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        redisCache.expire("loginPublic:"+jwt,jwtProperties.getUserTtl(), TimeUnit.SECONDS);
        //JWTHolder.set(jwt);
        return Result.success();
    }

    @PostMapping("/public/login/button")
    public Result<String> registerButton(String sessionId, HttpServletRequest request, HttpServletResponse response) throws URISyntaxException, IOException {
        //根据sessionId获取session对象
        HttpSession session = request.getSession(false);
        String jwt = session.getAttribute("loginJwt").toString();
        if(jwt == null){
            return Result.error("请先去A认证");
        }
        log.info("我到这里了哈哈哈哈");
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost();
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.addHeader("Accept", "application/json");
        httpPost.addHeader("authentication", jwt);
        URIBuilder uriBuilder = new URIBuilder("http://localhost:8080/public/login/ZKP");
        httpPost.setURI(uriBuilder.build());
        log.info("我到这里了");
        CloseableHttpResponse execute = httpClient.execute(httpPost);
        try{
            if(execute.getStatusLine().getStatusCode() == 200){
                String result = EntityUtils.toString(execute.getEntity());
                JSONObject jsonObject = JSON.parseObject(result);
                String baseJwt = jsonObject.getString("data");
                if (baseJwt == null) {
                    log.info("result{}", result);
                    log.info("jsonObject{}",jsonObject);
                    return Result.error("认证失败，无法获取JWT数据");
                }
                byte[] decode = Base64.getDecoder().decode(baseJwt);
                String sign=new String(decode);
                Object cacheObject = redisCache.getCacheObject("loginPublic:" + jwt);
                byte[] decode1 = Base64.getDecoder().decode(cacheObject.toString());
                KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");
                PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(decode1));
                if(Ed25519Example.verify(jwt.getBytes("UTF-8"), decode,publicKey)){
                    //把jwt返回前端并存储起来，并且设置过期时间，每次访问有这个jwt就直接跳过登陆界面，当然是这个jwt是A认证的，我们构造一个假的loginuser，存入redis中
                    //之后可以顺利通过过滤器的校验等过程，soga，这下子逻辑也算是清楚了，这把redis上打分!!!!!
                    //生成jwt令牌，作为跳过登陆的标识，嘻嘻
                    return Result.success("认证成功，确实是A区的认证");
                }else{
                    return Result.error("认证失败，请重新认证11111");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.error("错误：{}，{}", execute.getStatusLine().getReasonPhrase(), execute.getStatusLine().getStatusCode());
        return Result.error("认证失败，请重新认证");
    }
    //重定向之后前端自动提取出来code并且发出post请求传递给我code
    @GetMapping("/home")
    public void registerGitee(@RequestParam String code, HttpServletResponse  response) throws URISyntaxException, IOException {

            log.info("code: {}", code);
            //拿到这个需要的code，然后去gitee获取用户信息
            CloseableHttpClient httpClient = HttpClients.createDefault();
        // 2. 构造POST请求（Gitee的token端点）
        HttpPost httpPost = new HttpPost("https://gitee.com/oauth/token");
            httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.addHeader("Accept", "application/json");
        // 3. 设置表单参数（必须用application/x-www-form-urlencoded格式）
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("grant_type", "authorization_code")); // 必需参数
        params.add(new BasicNameValuePair("client_id", oauth2Properties.getClient_id()));
        params.add(new BasicNameValuePair("client_secret",oauth2Properties.getClient_secret()));
        params.add(new BasicNameValuePair("code", code)); // 从回调中获取的code
        params.add(new BasicNameValuePair("redirect_uri", oauth2Properties.getRedirect_uri())); // 必须与Gitee配置一致
        // 4. 设置请求体和Content-Type
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
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
                    response.sendRedirect("http://localhost:8080/main?token="+jwt+"&giteeId="+id);
                    log.info("用户认证成功");
                    return;
                }
            }
            response.sendRedirect("http://localhost:8080/error");
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
                httpClient.close();
                execute.close();
            }
    }
}
