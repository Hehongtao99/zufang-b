package com.zufang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息阅读状态实体类
 */
@Data
@TableName("zf_message_read_status")
public class MessageReadStatus {
    
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 消息ID
     */
    private Long messageId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 是否已读
     */
    private Boolean isRead;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 