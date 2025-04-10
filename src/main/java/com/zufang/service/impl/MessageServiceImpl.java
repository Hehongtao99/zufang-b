package com.zufang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zufang.dto.MessageSendRequest;
import com.zufang.entity.Message;
import com.zufang.entity.MessageReadStatus;
import com.zufang.entity.User;
import com.zufang.mapper.MessageMapper;
import com.zufang.mapper.MessageReadStatusMapper;
import com.zufang.mapper.UserMapper;
import com.zufang.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息服务实现类
 */
@Service
@Slf4j
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private MessageReadStatusMapper messageReadStatusMapper;

    /**
     * 创建消息
     */
    @Override
    @Transactional
    public Long createMessage(Message message) {
        log.info("创建消息: userId={}, title={}", message.getUserId(), message.getTitle());
        
        // 设置初始值
        message.setIsRead(false);
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());
        
        // 保存消息
        save(message);
        
        log.info("创建消息成功: id={}", message.getId());
        return message.getId();
    }
    
    /**
     * 获取用户消息列表
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Message> getUserMessages(Long userId, Integer page, Integer size) {
        log.info("获取用户消息列表: userId={}, page={}, size={}", userId, page, size);
        
        // 构建查询条件
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                // 查询用户特定消息
                .eq(Message::getUserId, userId)
                // 或者查询全局消息
                .or()
                .eq(Message::getIsGlobal, true)
            )
            .orderByDesc(Message::getCreateTime);
        
        // 执行分页查询
        Page<Message> pageParam = new Page<>(page, size);
        Page<Message> result = page(pageParam, wrapper);
        
        // 处理消息的已读状态
        result.getRecords().forEach(message -> {
            if (message.getIsGlobal() != null && message.getIsGlobal()) {
                // 对于全局消息，从消息阅读状态表中获取已读状态
                MessageReadStatus readStatus = messageReadStatusMapper.selectOne(
                    new LambdaQueryWrapper<MessageReadStatus>()
                        .eq(MessageReadStatus::getMessageId, message.getId())
                        .eq(MessageReadStatus::getUserId, userId)
                );
                message.setIsRead(readStatus != null && readStatus.getIsRead());
            }
        });
        
        log.info("获取用户消息列表成功: userId={}, total={}", userId, result.getTotal());
        return result;
    }
    
    /**
     * 统计用户未读消息数量
     */
    @Override
    public int countUnreadMessages(Long userId) {
        log.info("统计用户未读消息数量: userId={}", userId);
        
        // 统计用户特定的未读消息
        LambdaQueryWrapper<Message> userMessageWrapper = new LambdaQueryWrapper<>();
        userMessageWrapper.eq(Message::getUserId, userId)
                         .eq(Message::getIsRead, false);
        long userMessageCount = count(userMessageWrapper);
        
        // 统计全局未读消息
        // 1. 获取所有全局消息
        LambdaQueryWrapper<Message> globalMessageWrapper = new LambdaQueryWrapper<>();
        globalMessageWrapper.eq(Message::getIsGlobal, true);
        List<Message> globalMessages = list(globalMessageWrapper);
        
        // 2. 统计未读的全局消息
        long globalUnreadCount = globalMessages.stream()
            .filter(message -> {
                MessageReadStatus readStatus = messageReadStatusMapper.selectOne(
                    new LambdaQueryWrapper<MessageReadStatus>()
                        .eq(MessageReadStatus::getMessageId, message.getId())
                        .eq(MessageReadStatus::getUserId, userId)
                );
                return readStatus == null || !readStatus.getIsRead();
            })
            .count();
        
        // 合计未读消息数量
        long totalCount = userMessageCount + globalUnreadCount;
        
        // 确保安全转换为int
        int intCount;
        if (totalCount > Integer.MAX_VALUE) {
            log.warn("未读消息数量超出Integer.MAX_VALUE范围: {}, 将使用最大值", totalCount);
            intCount = Integer.MAX_VALUE;
        } else {
            intCount = (int) totalCount;
        }
        
        log.info("统计用户未读消息数量成功: userId={}, userMessageCount={}, globalUnreadCount={}, totalCount={}", 
                userId, userMessageCount, globalUnreadCount, intCount);
        return intCount;
    }
    
    /**
     * 标记消息为已读
     */
    @Override
    @Transactional
    public boolean markMessageAsRead(Long messageId, Long userId) {
        log.info("标记消息为已读: messageId={}, userId={}", messageId, userId);
        
        // 验证消息是否存在
        Message message = getById(messageId);
        if (message == null) {
            log.warn("标记消息为已读失败，消息不存在: messageId={}", messageId);
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        if (message.getIsGlobal() != null && message.getIsGlobal()) {
            // 全局消息 - 创建或更新用户的阅读状态
            MessageReadStatus readStatus = messageReadStatusMapper.selectOne(
                new LambdaQueryWrapper<MessageReadStatus>()
                    .eq(MessageReadStatus::getMessageId, messageId)
                    .eq(MessageReadStatus::getUserId, userId)
            );
            
            if (readStatus == null) {
                // 创建新的阅读状态记录
                readStatus = new MessageReadStatus();
                readStatus.setMessageId(messageId);
                readStatus.setUserId(userId);
                readStatus.setIsRead(true);
                readStatus.setCreateTime(now);
                boolean success = messageReadStatusMapper.insert(readStatus) > 0;
                
                // 更新消息的更新时间
                message.setUpdateTime(now);
                updateById(message);
                
                return success;
            } else if (!readStatus.getIsRead()) {
                // 更新已有记录
                readStatus.setIsRead(true);
                boolean success = messageReadStatusMapper.updateById(readStatus) > 0;
                
                // 更新消息的更新时间
                message.setUpdateTime(now);
                updateById(message);
                
                return success;
            } else {
                // 已经是已读状态，无需更新
                return true;
            }
        } else {
            // 用户特定消息 - 验证所有者并更新
            if (!userId.equals(message.getUserId())) {
                log.warn("标记消息为已读失败，消息不属于当前用户: messageId={}, userId={}, messageUserId={}", 
                        messageId, userId, message.getUserId());
                return false;
            }
            
            // 如果已经是已读状态，直接返回成功
            if (message.getIsRead()) {
                log.info("消息已经是已读状态，无需更新: messageId={}", messageId);
                return true;
            }
            
            // 更新消息为已读状态
            message.setIsRead(true);
            message.setUpdateTime(now);
            boolean success = updateById(message);
            
            log.info("标记消息为已读{}成功: messageId={}", success ? "" : "不", messageId);
            return success;
        }
    }
    
    /**
     * 标记用户所有消息为已读
     */
    @Override
    @Transactional
    public boolean markAllMessagesAsRead(Long userId) {
        log.info("标记用户所有消息为已读: userId={}", userId);
        
        // 标记用户特定消息为已读
        LambdaUpdateWrapper<Message> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Message::getUserId, userId)
               .eq(Message::getIsRead, false)
               .set(Message::getIsRead, true)
               .set(Message::getUpdateTime, LocalDateTime.now());
        
        // 执行批量更新
        boolean success = update(wrapper);
        
        // 获取所有未读的全局消息
        List<Message> globalMessages = list(
            new LambdaQueryWrapper<Message>()
                .eq(Message::getIsGlobal, true)
                .eq(Message::getType, "SYSTEM")
        );
        
        // 标记所有全局消息为已读
        for (Message message : globalMessages) {
            // 检查用户是否已阅读该消息
            MessageReadStatus readStatus = messageReadStatusMapper.selectOne(
                new LambdaQueryWrapper<MessageReadStatus>()
                    .eq(MessageReadStatus::getMessageId, message.getId())
                    .eq(MessageReadStatus::getUserId, userId)
            );
            
            if (readStatus == null) {
                // 创建新的阅读状态记录
                readStatus = new MessageReadStatus();
                readStatus.setMessageId(message.getId());
                readStatus.setUserId(userId);
                readStatus.setIsRead(true);
                readStatus.setCreateTime(LocalDateTime.now());
                messageReadStatusMapper.insert(readStatus);
            } else if (!readStatus.getIsRead()) {
                // 更新已有记录
                readStatus.setIsRead(true);
                messageReadStatusMapper.updateById(readStatus);
            }
        }
        
        log.info("标记用户所有消息为已读{}成功: userId={}", success ? "" : "不", userId);
        return true;
    }
    
    /**
     * 删除消息
     */
    @Override
    @Transactional
    public boolean deleteMessage(Long messageId, Long userId) {
        log.info("删除消息: messageId={}, userId={}", messageId, userId);
        
        // 验证消息是否属于当前用户
        Message message = getById(messageId);
        if (message == null) {
            log.warn("删除消息失败，消息不存在: messageId={}", messageId);
            return false;
        }
        
        if (message.getIsGlobal() != null && message.getIsGlobal()) {
            // 全局消息 - 删除用户的阅读状态记录
            int deleted = messageReadStatusMapper.delete(
                new LambdaQueryWrapper<MessageReadStatus>()
                    .eq(MessageReadStatus::getMessageId, messageId)
                    .eq(MessageReadStatus::getUserId, userId)
            );
            
            // 创建一个"已删除"的阅读状态记录
            MessageReadStatus deletedStatus = new MessageReadStatus();
            deletedStatus.setMessageId(messageId);
            deletedStatus.setUserId(userId);
            deletedStatus.setIsRead(true); // 标记为已读
            deletedStatus.setCreateTime(LocalDateTime.now());
            messageReadStatusMapper.insert(deletedStatus);
            
            log.info("标记全局消息为已删除状态: messageId={}, userId={}", messageId, userId);
            return true;
        } else {
            // 用户特定消息 - 检查所有权并删除
            if (!userId.equals(message.getUserId())) {
                log.warn("删除消息失败，消息不属于当前用户: messageId={}, userId={}, messageUserId={}", 
                        messageId, userId, message.getUserId());
                return false;
            }
            
            // 删除消息
            boolean success = removeById(messageId);
            
            log.info("删除消息{}成功: messageId={}", success ? "" : "不", messageId);
            return success;
        }
    }

    /**
     * 发送系统消息
     */
    @Override
    @Transactional
    public boolean sendSystemMessage(Long userId, String title, String content, String type, Long relatedId) {
        log.info("发送系统消息: userId={}, title={}, type={}, relatedId={}", userId, title, type, relatedId);
        
        try {
            // 创建消息对象
            Message message = new Message();
            message.setUserId(userId);
            message.setTitle(title);
            message.setContent(content);
            message.setType(type);
            message.setReferenceId(relatedId);
            message.setIsRead(false);
            message.setIsGlobal(false); // 非全局消息
            message.setCreateTime(LocalDateTime.now());
            message.setUpdateTime(LocalDateTime.now());
            
            // 保存消息
            boolean saved = save(message);
            
            log.info("发送系统消息{}成功: userId={}, messageId={}", saved ? "" : "不", userId, message.getId());
            return saved;
        } catch (Exception e) {
            log.error("发送系统消息异常: userId={}, error={}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 发送普通消息
     */
    @Override
    @Transactional
    public boolean sendMessage(Long userId, String title, String content) {
        log.info("发送消息: userId={}, title={}", userId, title);
        
        try {
            // 创建消息对象
            Message message = new Message();
            message.setUserId(userId);
            message.setTitle(title);
            message.setContent(content);
            message.setType("SYSTEM"); // 默认类型为系统消息
            message.setIsRead(false);
            message.setIsGlobal(false); // 非全局消息
            message.setCreateTime(LocalDateTime.now());
            message.setUpdateTime(LocalDateTime.now());
            
            // 保存消息
            boolean saved = save(message);
            
            log.info("发送消息{}成功: userId={}, messageId={}", saved ? "" : "不", userId, message.getId());
            return saved;
        } catch (Exception e) {
            log.error("发送消息异常: userId={}, error={}", userId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void broadcastSystemMessage(MessageSendRequest request) {
        log.info("发送系统广播通知: title={}, receiverType={}", request.getTitle(), request.getReceiverType());
        
        // 创建一条全局系统消息
        Message message = new Message();
        message.setUserId(0L); // 系统用户ID为0
        message.setTitle(request.getTitle());
        message.setContent(request.getContent());
        message.setType("SYSTEM");
        message.setIsGlobal(true); // 设置为全局消息
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());
        save(message);
        
        log.info("创建全局系统消息成功: messageId={}", message.getId());
    }

    @Override
    public Page<Message> getSystemMessages(Integer pageNum, Integer pageSize) {
        // 管理员视图 - 只返回系统发送的广播消息
        return page(
            new Page<>(pageNum, pageSize),
            new LambdaQueryWrapper<Message>()
                .eq(Message::getType, "SYSTEM")
                .eq(Message::getIsGlobal, true)
                .orderByDesc(Message::getCreateTime)
        );
    }

    @Override
    public Page<Message> getSystemMessages(Integer pageNum, Integer pageSize, Long userId) {
        if (userId == null) {
            // 管理员视图 - 只返回系统发送的广播消息
            return getSystemMessages(pageNum, pageSize);
        } else {
            // 用户视图 - 返回全局消息和用户特定消息
            Page<Message> messagePage = page(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Message>()
                    .eq(Message::getType, "SYSTEM")
                    .and(wrapper -> wrapper
                        .eq(Message::getIsGlobal, true)
                        .or()
                        .eq(Message::getUserId, userId)
                    )
                    .orderByDesc(Message::getCreateTime)
            );
            
            // 填充全局消息的已读状态
            for (Message message : messagePage.getRecords()) {
                if (message.getIsGlobal() != null && message.getIsGlobal()) {
                    // 检查用户的阅读状态
                    Boolean readStatus = getMessageReadStatus(message.getId(), userId);
                    message.setIsRead(readStatus);
                }
            }
            
            return messagePage;
        }
    }

    @Override
    @Transactional
    public void markMessageAsRead(Long messageId) {
        // 获取当前消息
        Message message = getById(messageId);
        if (message == null) {
            log.warn("标记消息为已读失败，消息不存在: messageId={}", messageId);
            return;
        }
        
        // 对于非全局消息，直接更新消息表
        if (message.getIsGlobal() == null || !message.getIsGlobal()) {
            message.setIsRead(true);
            message.setUpdateTime(LocalDateTime.now());
            updateById(message);
            return;
        }
        
        // 对于全局消息，记录错误日志，因为应该调用双参数版本的方法来处理全局消息
        // 全局消息需要知道是哪个用户已读，不能直接将消息标记为已读
        log.error("全局消息不能使用单参数markMessageAsRead方法标记已读，请使用双参数版本: messageId={}", messageId);
    }
    
    @Override
    public Boolean getMessageReadStatus(Long messageId, Long userId) {
        // 检查消息是否为全局消息
        Message message = getById(messageId);
        if (message == null) {
            return false;
        }
        
        if (message.getIsGlobal() != null && message.getIsGlobal()) {
            // 全局消息 - 查询阅读状态记录
            MessageReadStatus readStatus = messageReadStatusMapper.selectOne(
                new LambdaQueryWrapper<MessageReadStatus>()
                    .eq(MessageReadStatus::getMessageId, messageId)
                    .eq(MessageReadStatus::getUserId, userId)
            );
            return readStatus != null && readStatus.getIsRead();
        } else {
            // 用户特定消息 - 直接返回消息的已读状态
            return message.getIsRead();
        }
    }
} 