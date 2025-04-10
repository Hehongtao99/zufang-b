package com.zufang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("user")
public class User implements Serializable {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id; // 用户ID
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 昵称
     */
    @TableField(value = "nickname")
    private String nickname;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 用户类型：USER-普通用户，LANDLORD-房东，ADMIN-管理员
     */
    @TableField(value = "role")
    private String role;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 身份证号
     */
    private String idCard;
    
    /**
     * 用户状态
     */
    private String status;
    
    /**
     * 用户描述
     */
    private String description;
    
    /**
     * 最后登录时间
     */
    @TableField(exist = false)
    private LocalDateTime lastLoginTime;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 是否删除
     */
    @TableLogic
    private Boolean isDeleted;
    
    /**
     * 获取昵称，兼容getNickname()调用
     */
    public String getNickname() {
        return this.nickname;
    }
    
    /**
     * 设置昵称，兼容setNickname()调用
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    /**
     * 获取用户角色
     */
    public String getRole() {
        return this.role;
    }
    
    /**
     * 设置用户角色
     */
    public void setRole(String role) {
        this.role = role;
    }
    
    /**
     * 获取用户状态
     */
    public String getStatus() {
        return this.status;
    }
    
    /**
     * 设置用户状态
     */
    public void setStatus(String status) {
        this.status = status;
    }
} 