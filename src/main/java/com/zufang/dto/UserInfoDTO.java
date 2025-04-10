package com.zufang.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息DTO
 */
@Data
public class UserInfoDTO {
    
    private Long id; // 用户ID
    
    private String username; // 用户名
    
    private String nickname; // 昵称
    
    private String avatar; // 头像
    
    private String phone; // 手机号
    
    private String email; // 邮箱
    
    private String role; // 角色：ADMIN-管理员，LANDLORD-房东，USER-用户
    
    private String idCard; // 身份证号
    
    private String realName; // 真实姓名
    
    private String status; // 状态：ACTIVE-正常，LOCKED-锁定，INACTIVE-未激活
    
    private LocalDateTime createTime; // 创建时间
    
    private String description; // 个人简介
} 