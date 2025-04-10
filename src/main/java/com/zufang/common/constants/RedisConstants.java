package com.zufang.common.constants;

/**
 * Redis常量类
 */
public class RedisConstants {
    
    /**
     * 用户Token前缀
     */
    public static final String USER_TOKEN_PREFIX = "user:token:";
    
    /**
     * 默认Token过期时间（24小时）
     */
    public static final long TOKEN_EXPIRE_TIME = 24 * 60 * 60;
    
    /**
     * 用户信息前缀
     */
    public static final String USER_INFO_PREFIX = "user:info:";
    
    /**
     * 用户信息过期时间（1小时）
     */
    public static final long USER_INFO_EXPIRE_TIME = 60 * 60;
    
    /**
     * 房源列表前缀
     */
    public static final String HOUSE_LIST_PREFIX = "house:list:";
    
    /**
     * 房源列表过期时间（10分钟）
     */
    public static final long HOUSE_LIST_EXPIRE_TIME = 10 * 60;
    
    /**
     * 房源详情前缀
     */
    public static final String HOUSE_DETAIL_PREFIX = "house:detail:";
    
    /**
     * 房源详情过期时间（10分钟）
     */
    public static final long HOUSE_DETAIL_EXPIRE_TIME = 10 * 60;
} 