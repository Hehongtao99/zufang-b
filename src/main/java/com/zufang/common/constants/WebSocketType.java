package com.zufang.common.constants;

/**
 * WebSocket消息类型枚举
 */
public enum WebSocketType {
    /**
     * 预约通知
     */
    APPOINTMENT_NOTIFICATION,
    
    /**
     * 预约状态变更通知
     */
    APPOINTMENT_STATUS_CHANGE,
    
    /**
     * 预约取消通知
     */
    APPOINTMENT_CANCELED,
    
    /**
     * 聊天消息
     */
    CHAT_MESSAGE,
    
    /**
     * 系统通知
     */
    SYSTEM_NOTIFICATION
} 