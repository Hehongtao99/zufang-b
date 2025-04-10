package com.zufang.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类 - 已禁用
 * 所有Redis操作都被禁用，返回模拟值，不再连接Redis服务器
 */
@Slf4j
@Component
public class RedisUtil {
    
    /**
     * 设置缓存
     * @param key 键
     * @param value 值
     */
    public void set(String key, Object value) {
        log.debug("Redis功能已禁用，跳过设置缓存操作, key: {}", key);
    }
    
    /**
     * 设置缓存，并设置过期时间
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        log.debug("Redis功能已禁用，跳过设置缓存操作, key: {}, 过期时间: {} {}", key, timeout, unit);
    }
    
    /**
     * 获取缓存
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        log.debug("Redis功能已禁用，跳过获取缓存操作, key: {}", key);
        return null;
    }
    
    /**
     * 删除缓存
     * @param key 键
     */
    public void delete(String key) {
        log.debug("Redis功能已禁用，跳过删除缓存操作, key: {}", key);
    }
    
    /**
     * 判断key是否存在
     * @param key 键
     * @return 是否存在
     */
    public Boolean hasKey(String key) {
        log.debug("Redis功能已禁用，跳过判断key存在操作, key: {}", key);
        return false;
    }
    
    /**
     * 设置过期时间
     * @param key 键
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 是否成功
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        log.debug("Redis功能已禁用，跳过设置过期时间操作, key: {}, 过期时间: {} {}", key, timeout, unit);
        return true;
    }
    
    /**
     * 获取过期时间
     * @param key 键
     * @return 过期时间（-1表示永不过期，-2表示key不存在）
     */
    public Long getExpire(String key) {
        log.debug("Redis功能已禁用，跳过获取过期时间操作, key: {}", key);
        return -2L;
    }
    
    /**
     * 递增
     * @param key 键
     * @param delta 递增因子
     * @return 递增后的值
     */
    public Long increment(String key, long delta) {
        log.debug("Redis功能已禁用，跳过递增操作, key: {}, delta: {}", key, delta);
        return 0L;
    }
    
    /**
     * 递减
     * @param key 键
     * @param delta 递减因子
     * @return 递减后的值
     */
    public Long decrement(String key, long delta) {
        log.debug("Redis功能已禁用，跳过递减操作, key: {}, delta: {}", key, delta);
        return 0L;
    }
    
    /**
     * 删除匹配模式的所有key
     * @param pattern 模式字符串，例如 "prefix:*"
     */
    public void deleteKeysWithPattern(String pattern) {
        log.debug("Redis功能已禁用，跳过删除模式匹配key操作, 模式: {}", pattern);
    }
} 