package com.web1.service;

import com.web1.entity.User;
import com.web1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * 用户详情服务实现类
 * 简化版，仅提供基本的用户加载功能
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从数据库加载用户
        User user = userRepository.findByUserNameAndDelFlag(username, 0);
        
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        // 创建Spring Security的User对象
        // 注意：此处可以添加用户角色和权限
        return new org.springframework.security.core.userdetails.User(
            user.getUserName(),
            user.getPassword(),
            new ArrayList<>() // 空权限列表
        );
    }
}
