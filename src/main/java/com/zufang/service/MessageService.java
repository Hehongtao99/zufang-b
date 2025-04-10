package com.zufang.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zufang.dto.MessageSendRequest;
import com.zufang.entity.Message;

/**
 * 消息服务接口
 */
public interface MessageService {
    
    /**
     * 创建消息
     * @param message 消息实体
     * @return 消息ID
     */
    Long createMessage(Message message);
    
    /**
     * 获取用户消息列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 消息分页结果
     */
    Page<Message> getUserMessages(Long userId, Integer page, Integer size);
    
    /**
     * 统计用户未读消息数量
     * @param userId 用户ID
     * @return 未读消息数量
     */
    int countUnreadMessages(Long userId);
    
    /**
     * 标记消息为已读
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markMessageAsRead(Long messageId, Long userId);
    
    /**
     * 标记用户所有消息为已读
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAllMessagesAsRead(Long userId);
    
    /**
     * 删除消息
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteMessage(Long messageId, Long userId);
    
    /**
     * 发送系统消息
     * @param userId 用户ID
     * @param title 消息标题
     * @param content 消息内容
     * @param type 消息类型
     * @param relatedId 关联ID
     * @return 是否发送成功
     */
    boolean sendSystemMessage(Long userId, String title, String content, String type, Long relatedId);
    
    /**
     * 发送消息
     * @param userId 用户ID
     * @param title 消息标题
     * @param content 消息内容
     * @return 是否发送成功
     */
    boolean sendMessage(Long userId, String title, String content);

    /**
     * 发送系统广播通知
     * @param request 发送请求
     */
    void broadcastSystemMessage(MessageSendRequest request);

    /**
     * 获取系统通知列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 系统通知分页结果
     */
    Page<Message> getSystemMessages(Integer pageNum, Integer pageSize);

    /**
     * 获取系统通知列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param userId 用户ID，如果提供则只返回该用户的消息
     * @return 系统通知分页结果
     */
    Page<Message> getSystemMessages(Integer pageNum, Integer pageSize, Long userId);

    /**
     * 标记消息为已读
     * @param messageId 消息ID
     */
    void markMessageAsRead(Long messageId);
    
    /**
     * 获取消息的阅读状态
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 是否已读
     */
    Boolean getMessageReadStatus(Long messageId, Long userId);
} 