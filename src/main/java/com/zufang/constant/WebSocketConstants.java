package com.zufang.constant;

/**
 * WebSocket相关常量
 */
public class WebSocketConstants {

    /**
     * WebSocket消息类型
     */
    public static class MessageType {
        /**
         * 系统通知
         */
        public static final String SYSTEM_NOTIFICATION = "SYSTEM_NOTIFICATION";
        
        /**
         * 预约通知
         */
        public static final String APPOINTMENT_NOTIFICATION = "APPOINTMENT_NOTIFICATION";
        
        /**
         * 预约状态变更通知
         */
        public static final String APPOINTMENT_STATUS_CHANGE = "APPOINTMENT_STATUS_CHANGE";
        
        /**
         * 预约取消通知
         */
        public static final String APPOINTMENT_CANCELED = "APPOINTMENT_CANCELED";
        
        /**
         * 聊天消息
         */
        public static final String CHAT_MESSAGE = "CHAT_MESSAGE";
    }
    
    /**
     * WebSocket目标路径
     */
    public static class Destination {
        /**
         * 用户消息队列
         */
        public static final String USER_QUEUE = "/queue/messages";
        
        /**
         * 用户通知队列
         */
        public static final String USER_NOTIFICATION = "/queue/notifications";
        
        /**
         * 聊天消息队列
         */
        public static final String CHAT_QUEUE = "/queue/chat";
    }
} 