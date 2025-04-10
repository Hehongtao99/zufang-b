package com.zufang.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * WebSocket消息实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebSocketMessage {

    /**
     * 消息类型
     */
    private String type;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 发送时间
     */
    private LocalDateTime timestamp;

    /**
     * 关联ID（如预约ID）
     */
    private Long referenceId;

    /**
     * 附加数据
     */
    private Object data;
    
    /**
     * 创建一个预约通知消息
     */
    public static WebSocketMessage createAppointmentNotification(Long appointmentId, String title, String content, Object data) {
        return WebSocketMessage.builder()
                .type("APPOINTMENT_NOTIFICATION")
                .title(title)
                .content(content)
                .timestamp(LocalDateTime.now())
                .referenceId(appointmentId)
                .data(data)
                .build();
    }
    
    /**
     * 创建一个聊天消息
     * 
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param houseId 房源ID
     * @param content 消息内容
     * @param data 附加数据
     * @return 聊天消息对象
     */
    public static WebSocketMessage createChatMessage(Long senderId, Long receiverId, Long houseId, String content, Object data) {
        return WebSocketMessage.builder()
                .type("CHAT_MESSAGE")
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .timestamp(LocalDateTime.now())
                .referenceId(houseId) // 使用房源ID作为关联ID
                .data(data)
                .build();
    }
} 