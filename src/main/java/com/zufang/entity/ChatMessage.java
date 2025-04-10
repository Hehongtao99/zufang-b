package com.zufang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 */
@Data
@Accessors(chain = true)
@TableName("chat_message")
public class ChatMessage {
    
    /**
     * 消息ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 接收者ID
     */
    private Long receiverId;
    
    /**
     * 房源ID
     */
    private Long houseId;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 发送者头像
     */
    private String senderAvatar;
    
    /**
     * 发送者昵称
     */
    private String senderName;
    
    /**
     * 是否已读
     */
    private Boolean isRead;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 是否删除
     */
    @TableLogic
    private Boolean isDeleted;
} 