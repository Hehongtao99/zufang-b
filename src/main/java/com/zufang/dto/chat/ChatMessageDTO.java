package com.zufang.dto.chat;

import lombok.Data;

/**
 * 聊天消息DTO
 */
@Data
public class ChatMessageDTO {
    
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
} 