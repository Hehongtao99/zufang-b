package com.zufang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 功能开关实体类
 */
@Data
@TableName("zf_feature_toggle")
public class FeatureToggle {
    
    /**
     * 功能开关ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 功能键名
     */
    private String featureKey;
    
    /**
     * 功能名称
     */
    private String featureName;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 功能描述
     */
    private String description;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 