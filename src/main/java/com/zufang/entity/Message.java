package com.zufang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 消息实体类
 */
@Data
@Accessors(chain = true)
@TableName("zf_message")
public class Message {
    
    /**
     * 消息ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 消息标题
     */
    private String title;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息类型（SYSTEM：系统消息；APPOINTMENT：预约消息；ORDER：订单消息）
     */
    private String type;
    
    /**
     * 是否已读
     */
    private Boolean isRead;
    
    /**
     * 关联ID（比如订单ID，预约ID等）
     */
    private Long referenceId;
    
    /**
     * 是否为全局消息（系统广播）
     */
    private Boolean isGlobal;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 