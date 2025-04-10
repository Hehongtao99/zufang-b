package com.zufang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统设置实体类
 */
@Data
@TableName("zf_system_setting")
public class SystemSetting {
    
    /**
     * 设置ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 系统名称
     */
    private String systemName;
    
    /**
     * 系统描述
     */
    private String systemDescription;
    
    /**
     * 联系电话
     */
    private String contactPhone;
    
    /**
     * 联系邮箱
     */
    private String contactEmail;
    
    /**
     * 备案信息
     */
    private String icp;
    
    /**
     * 系统Logo URL
     */
    private String logoUrl;
    
    /**
     * 系统版本
     */
    private String version;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 