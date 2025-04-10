package com.zufang.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天会话DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionDTO {
    
    /**
     * 会话唯一标识
     */
    private String sessionKey;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名称
     */
    private String userName;
    
    /**
     * 用户头像
     */
    private String userAvatar;
    
    /**
     * 房东ID
     */
    private Long landlordId;
    
    /**
     * 房东名称
     */
    private String landlordName;
    
    /**
     * 房东头像
     */
    private String landlordAvatar;
    
    /**
     * 房源ID
     */
    private Long houseId;
    
    /**
     * 房源名称
     */
    private String houseName;
    
    /**
     * 最后一条消息
     */
    private String lastMessage;
    
    /**
     * 最后一条消息时间
     */
    private LocalDateTime lastMessageTime;
    
    /**
     * 未读消息数量
     */
    private Integer unreadCount;
} 