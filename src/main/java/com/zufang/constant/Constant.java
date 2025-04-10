package com.zufang.constant;

/**
 * 系统常量类
 */
public class Constant {
    
    /**
     * 用户ID属性名
     */
    public static final String USER_ID_ATTR = "userId";
    
    /**
     * 用户ID键名（在OrderTerminateController中使用）
     */
    public static final String USER_ID_KEY = "userId";

    /**
     * 用户类型：普通用户
     */
    public static final int USER_TYPE_USER = 1;

    /**
     * 用户类型：房东
     */
    public static final int USER_TYPE_LANDLORD = 2;

    /**
     * 用户类型：管理员
     */
    public static final int USER_TYPE_ADMIN = 3;

    /**
     * JWT令牌前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 用户角色：管理员
     */
    public static final String ROLE_ADMIN = "ADMIN";

    /**
     * 用户角色：房东
     */
    public static final String ROLE_LANDLORD = "LANDLORD";

    /**
     * 用户角色：普通用户
     */
    public static final String ROLE_USER = "USER";
} 