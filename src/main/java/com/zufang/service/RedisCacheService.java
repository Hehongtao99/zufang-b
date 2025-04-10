package com.zufang.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存服务接口
 */
public interface RedisCacheService {

    /**
     * 设置缓存
     *
     * @param key   缓存键
     * @param value 缓存值
     * @param <T>   值类型
     */
    <T> void set(String key, T value);

    /**
     * 设置缓存并指定过期时间
     *
     * @param key     缓存键
     * @param value   缓存值
     * @param timeout 过期时间
     * @param unit    时间单位
     * @param <T>     值类型
     */
    <T> void set(String key, T value, long timeout, TimeUnit unit);

    /**
     * 获取缓存
     *
     * @param key   缓存键
     * @param clazz 返回类型
     * @param <T>   值类型
     * @return 缓存值
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 获取列表缓存
     *
     * @param key   缓存键
     * @param clazz 列表元素类型
     * @param <T>   值类型
     * @return 缓存列表
     */
    <T> List<T> getList(String key, Class<T> clazz);

    /**
     * 删除缓存
     *
     * @param key 缓存键
     * @return 是否删除成功
     */
    Boolean delete(String key);

    /**
     * 判断键是否存在
     *
     * @param key 缓存键
     * @return 是否存在
     */
    Boolean hasKey(String key);

    /**
     * 设置过期时间
     *
     * @param key     缓存键
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 是否设置成功
     */
    Boolean expire(String key, long timeout, TimeUnit unit);
} 