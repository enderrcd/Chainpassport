package org.example.springsecurity.Service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.springsecurity.domain.LoginUser;
import org.example.springsecurity.domain.Result;
import org.example.springsecurity.domain.User;
import org.example.springsecurity.mapper.MenuMapper;
import org.example.springsecurity.mapper.UserMapper;
import org.example.springsecurity.utils.JwtClaimsConstant;
import org.example.springsecurity.utils.JwtProperties;
import org.example.springsecurity.utils.JwtUtil;
import org.example.springsecurity.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class UserServiceImpl implements UserDetailsService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private MenuMapper menuMapper;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserName, username);
        User user = userMapper.selectOne(queryWrapper);
        log.info("查询用户：{}" ,user);
        if(user == null){
            throw new UsernameNotFoundException("用户不存在");
        }
        List<String>list=menuMapper.selectPermsByUserId(user.getId());
        //把权限的集合添加到loginUser中
        return new LoginUser(user,list);
    }

    public Result login(User userDto) {
        //获取AuthenticationManagerBuilder对象，从Ioc容器中获取，，进行用户认证
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userDto.getUserName(), userDto.getPassword()));
        if(authenticate == null){
            throw new RuntimeException("用户名或密码错误");
        }
        LoginUser principal =(LoginUser) authenticate.getPrincipal();
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, principal.getUser().getId());
        String jwt = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        redisCache.setCacheObject("login:"+jwt, principal);
        return Result.success(jwt);

    }

    public void logout(String authentication) {
        //获取securityContextHolder中的用户id
        Authentication authentication1 = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication1.getPrincipal();
        Long userId = loginUser.getUser().getId();
        Map<String, Object>map=new HashMap<>();
        log.info("userId为{}", userId);
        map.put(JwtClaimsConstant.USER_ID, userId);
        String jwt = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), map);
        log.info("用户token：{}", jwt);
        boolean b = redisCache.deleteObject("login:" + authentication);
        log.info("用户退出登录，清空redis中的token：{}，删除的结果为{}", jwt, b);
    }

    public User selectUserByGiteeId(String id) {
      return  userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getGiteeId, id));
    }

    public void registerGitee(LoginUser oauth2User) {
        userMapper.insert(oauth2User.getUser());
    }
}

/*
总结：authenticate 的生命周期流程
步骤
        说明
1
        AuthenticationManager.authenticate() 被调用
2
        DaoAuthenticationProvider.authenticate() 被触发
3
        UserDetailsService.loadUserByUsername() 被调用，从数据库加载用户信息
4
如果认证成功，返回包含 UserDetails（即你的 LoginUser）的 Authentication 对象
所以，authenticate 中包含数据库用户信息，是因为你实现了 UserDetailsService 接口，并在其中加载了数据库中的用户数据。Spring Security 会在认证成功后将其放入 Authentication 对象中。
*/
