package com.zufang.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * WebSocket消息服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 发送消息给指定用户
     *
     * @param userId  用户ID
     * @param destination 目的地（前缀由WebSocketConfig中的配置决定）
     * @param payload 消息内容
     */
    public void sendMessageToUser(Long userId, String destination, Object payload) {
        try {
            log.info("发送WebSocket消息给用户: {}, 目的地: {}, 内容: {}", userId, destination, payload);
            messagingTemplate.convertAndSendToUser(userId.toString(), destination, payload);
        } catch (Exception e) {
            log.error("发送WebSocket消息给用户失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送消息给所有用户
     *
     * @param destination 目的地
     * @param payload     消息内容
     */
    public void sendMessageToAll(String destination, Object payload) {
        try {
            log.info("发送WebSocket广播消息, 目的地: {}, 内容: {}", destination, payload);
            messagingTemplate.convertAndSend(destination, payload);
        } catch (Exception e) {
            log.error("发送WebSocket广播消息失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 发送消息给指定用户
     *
     * @param userId  用户ID
     * @param message 消息对象
     */
    public void sendMessage(String userId, WebSocketMessage message) {
        try {
            log.info("发送WebSocket消息给用户: {}, 类型: {}, 内容: {}", userId, message.getType(), message.getContent());
            messagingTemplate.convertAndSendToUser(userId, "/queue/messages", message);
        } catch (Exception e) {
            log.error("发送WebSocket消息给用户失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 发送聊天消息
     *
     * @param senderId   发送者ID
     * @param receiverId 接收者ID
     * @param houseId    房源ID
     * @param content    消息内容
     * @param data       附加数据
     */
    public void sendChatMessage(Long senderId, Long receiverId, Long houseId, String content, Object data) {
        try {
            WebSocketMessage message = WebSocketMessage.createChatMessage(senderId, receiverId, houseId, content, data);
            log.info("发送聊天消息: 发送者:{}, 接收者:{}, 房源:{}, 内容:{}", senderId, receiverId, houseId, content);
            messagingTemplate.convertAndSendToUser(receiverId.toString(), "/queue/chat", message);
        } catch (Exception e) {
            log.error("发送聊天消息失败: " + e.getMessage(), e);
        }
    }
} 