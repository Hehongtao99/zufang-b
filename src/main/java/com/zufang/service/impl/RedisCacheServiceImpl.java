package com.zufang.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zufang.service.RedisCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存服务实现类
 * Redis功能已禁用，所有方法都是空操作
 */
@Slf4j
@Service
@SuppressWarnings("unchecked")
public class RedisCacheServiceImpl implements RedisCacheService {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public <T> void set(String key, T value) {
        // Redis功能已禁用，不执行任何操作
        log.debug("Redis功能已禁用，跳过设置缓存操作, key: {}", key);
    }

    @Override
    public <T> void set(String key, T value, long timeout, TimeUnit unit) {
        // Redis功能已禁用，不执行任何操作
        log.debug("Redis功能已禁用，跳过设置缓存操作, key: {}, 过期时间: {} {}", key, timeout, unit);
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        // Redis功能已禁用，直接返回null
        log.debug("Redis功能已禁用，跳过获取缓存操作, key: {}", key);
        return null;
    }

    @Override
    public <T> List<T> getList(String key, Class<T> clazz) {
        // Redis功能已禁用，直接返回空列表
        log.debug("Redis功能已禁用，跳过获取列表缓存操作, key: {}", key);
        return new ArrayList<>();
    }

    @Override
    public Boolean delete(String key) {
        // Redis功能已禁用，直接返回true表示操作成功
        log.debug("Redis功能已禁用，跳过删除缓存操作, key: {}", key);
        return true;
    }

    @Override
    public Boolean hasKey(String key) {
        // Redis功能已禁用，直接返回false表示key不存在
        log.debug("Redis功能已禁用，跳过判断key存在操作, key: {}", key);
        return false;
    }

    @Override
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        // Redis功能已禁用，直接返回true表示操作成功
        log.debug("Redis功能已禁用，跳过设置过期时间操作, key: {}, 过期时间: {} {}", key, timeout, unit);
        return true;
    }
} 