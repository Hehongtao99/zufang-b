package com.zufang.service.impl;

import com.zufang.dto.chat.ChatSessionDTO;
import com.zufang.entity.ChatMessage;
import com.zufang.mapper.ChatMessageMapper;
import com.zufang.service.ChatMessageService;
import com.zufang.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天消息服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {
    
    private final ChatMessageMapper chatMessageMapper;
    private final WebSocketService webSocketService;
    
    @Override
    @Transactional
    public ChatMessage sendMessage(Long senderId, Long receiverId, Long houseId, String content, String senderAvatar, String senderName) {
        log.info("发送聊天消息: 发送者:{}, 接收者:{}, 房源:{}, 内容:{}", senderId, receiverId, houseId, content);
        
        ChatMessage message = new ChatMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setHouseId(houseId);
        message.setContent(content);
        message.setSenderAvatar(senderAvatar);
        message.setSenderName(senderName);
        message.setIsRead(false);
        message.setIsDeleted(false);
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());
        
        chatMessageMapper.insert(message);
        
        // 发送WebSocket消息通知
        notifyMessageReceived(message);
        
        return message;
    }
    
    /**
     * 通过WebSocket发送消息通知
     *
     * @param message 消息对象
     */
    private void notifyMessageReceived(ChatMessage message) {
        // 准备附加数据
        Map<String, Object> data = new HashMap<>();
        data.put("messageId", message.getId());
        data.put("houseId", message.getHouseId());
        data.put("senderAvatar", message.getSenderAvatar());
        data.put("senderName", message.getSenderName());
        
        // 通过WebSocket发送实时消息
        webSocketService.sendChatMessage(
            message.getSenderId(),
            message.getReceiverId(),
            message.getHouseId(),
            message.getContent(),
            data
        );
    }
    
    @Override
    public List<ChatMessage> getChatHistory(Long userId, Long landlordId, Long houseId) {
        log.info("获取聊天历史记录: 用户:{}, 房东:{}, 房源:{}", userId, landlordId, houseId);
        return chatMessageMapper.getChatHistory(userId, landlordId, houseId);
    }
    
    @Override
    public int countUnreadMessages(Long userId) {
        return chatMessageMapper.countUnreadMessages(userId);
    }
    
    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        log.info("将用户所有未读消息标记为已读: 用户:{}", userId);
        return chatMessageMapper.markAllAsRead(userId);
    }
    
    @Override
    @Transactional
    public int markConversationAsRead(Long receiverId, Long senderId, Long houseId) {
        log.info("将特定会话的所有未读消息标记为已读: 接收者:{}, 发送者:{}, 房源:{}", receiverId, senderId, houseId);
        return chatMessageMapper.markConversationAsRead(receiverId, senderId, houseId);
    }
    
    @Override
    public List<ChatSessionDTO> getLandlordSessions(Long landlordId) {
        log.info("获取房东的聊天会话列表: 房东ID={}", landlordId);
        return chatMessageMapper.getLandlordSessions(landlordId);
    }

    @Override
    public List<ChatSessionDTO> getTenantSessions(Long tenantId) {
        log.info("获取租户的聊天会话列表: 租户ID={}", tenantId);
        return chatMessageMapper.getTenantSessions(tenantId);
    }
} 