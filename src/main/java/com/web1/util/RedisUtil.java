package com.web1.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类 - 内存模拟版本
 * 用于测试环境，避免Redis连接问题
 */
@Component
public class RedisUtil {
    
    // 使用内存Map模拟Redis
    private final ConcurrentHashMap<String, Object> memoryCache = new ConcurrentHashMap<>();
    
    /**
     * 设置缓存
     */
    public void set(String key, Object value) {
        memoryCache.put(key, value);
    }
    
    /**
     * 设置缓存并指定过期时间
     * 注意：这个简化版本不实现真正的过期机制
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        memoryCache.put(key, value);
        // 简化版本：不实现真正的过期，实际项目中需要定时清理
    }
    
    /**
     * 获取缓存
     */
    public Object get(String key) {
        return memoryCache.get(key);
    }
    
    /**
     * 删除缓存
     */
    public Boolean delete(String key) {
        return memoryCache.remove(key) != null;
    }
    
    /**
     * 判断key是否存在
     */
    public Boolean hasKey(String key) {
        return memoryCache.containsKey(key);
    }
    
    /**
     * 设置过期时间
     * 简化版本：直接返回true
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return true;
    }
}