package com.zufang.service;

import com.zufang.dto.chat.ChatSessionDTO;
import com.zufang.entity.ChatMessage;

import java.util.List;

/**
 * 聊天消息服务接口
 */
public interface ChatMessageService {
    
    /**
     * 发送聊天消息
     * 
     * @param senderId     发送者ID
     * @param receiverId   接收者ID
     * @param houseId      房源ID
     * @param content      消息内容
     * @param senderAvatar 发送者头像
     * @param senderName   发送者昵称
     * @return 保存的消息对象
     */
    ChatMessage sendMessage(Long senderId, Long receiverId, Long houseId, String content, String senderAvatar, String senderName);
    
    /**
     * 获取聊天历史记录
     * 
     * @param userId     用户ID
     * @param landlordId 房东ID
     * @param houseId    房源ID
     * @return 聊天历史记录
     */
    List<ChatMessage> getChatHistory(Long userId, Long landlordId, Long houseId);
    
    /**
     * 获取未读消息数量
     * 
     * @param userId 用户ID
     * @return 未读消息数量
     */
    int countUnreadMessages(Long userId);
    
    /**
     * 将用户的所有未读消息标记为已读
     * 
     * @param userId 用户ID
     * @return 更新条数
     */
    int markAllAsRead(Long userId);
    
    /**
     * 将特定会话的所有未读消息标记为已读
     * 
     * @param receiverId 接收者ID
     * @param senderId   发送者ID
     * @param houseId    房源ID
     * @return 更新条数
     */
    int markConversationAsRead(Long receiverId, Long senderId, Long houseId);
    
    /**
     * 获取房东的所有聊天会话列表
     *
     * @param landlordId 房东ID
     * @return 会话列表
     */
    List<ChatSessionDTO> getLandlordSessions(Long landlordId);
    
    /**
     * 获取租户的所有聊天会话列表
     *
     * @param tenantId 租户ID
     * @return 会话列表
     */
    List<ChatSessionDTO> getTenantSessions(Long tenantId);
} 