package com.zufang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zufang.common.api.CommonResult;
import com.zufang.common.response.Result;
import com.zufang.dto.MessageSendRequest;
import com.zufang.entity.Message;
import com.zufang.service.MessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 消息控制器
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/messages")
@Api(tags = "消息管理")
public class MessageController {

    private final MessageService messageService;
    
    /**
     * 获取用户未读消息数量
     */
    @GetMapping("/user/messages/unread-count")
    public Result<Integer> getUnreadMessageCount(HttpServletRequest request) {
        // 从请求中获取当前登录的用户ID
        Long userId = (Long) request.getAttribute("userId");
        log.info("获取用户未读消息数量: userId={}", userId);
        
        try {
            int count = messageService.countUnreadMessages(userId);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取用户未读消息数量失败: {}", e.getMessage(), e);
            return Result.fail("获取未读消息数量失败");
        }
    }
    
    /**
     * 获取用户消息列表
     */
    @GetMapping("/user/messages")
    public Result<Page<Message>> getUserMessages(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        // 从请求中获取当前登录的用户ID
        Long userId = (Long) request.getAttribute("userId");
        log.info("获取用户消息列表: userId={}, page={}, size={}", userId, page, size);
        
        try {
            Page<Message> messages = messageService.getUserMessages(userId, page, size);
            return Result.success(messages);
        } catch (Exception e) {
            log.error("获取用户消息列表失败: {}", e.getMessage(), e);
            return Result.fail("获取消息列表失败");
        }
    }
    
    /**
     * 标记消息为已读
     */
    @PutMapping("/{messageId}/read")
    @ApiOperation("标记消息为已读")
    public Result<Boolean> markMessageAsRead(@PathVariable Long messageId, HttpServletRequest request) {
        // 从请求中获取当前登录的用户ID
        Long userId = (Long) request.getAttribute("userId");
        log.info("标记消息为已读: userId={}, messageId={}", userId, messageId);
        
        try {
            boolean success = messageService.markMessageAsRead(messageId, userId);
            return Result.success(success);
        } catch (Exception e) {
            log.error("标记消息为已读失败: {}", e.getMessage(), e);
            return Result.fail("标记消息为已读失败");
        }
    }
    
    /**
     * 标记所有消息为已读
     */
    @PostMapping("/user/messages/read-all")
    public Result<Boolean> markAllMessagesAsRead(HttpServletRequest request) {
        // 从请求中获取当前登录的用户ID
        Long userId = (Long) request.getAttribute("userId");
        log.info("标记所有消息为已读: userId={}", userId);
        
        try {
            boolean success = messageService.markAllMessagesAsRead(userId);
            return Result.success(success);
        } catch (Exception e) {
            log.error("标记所有消息为已读失败: {}", e.getMessage(), e);
            return Result.fail("标记所有消息为已读失败");
        }
    }
    
    /**
     * 删除消息
     */
    @DeleteMapping("/user/messages/{messageId}")
    public Result<Boolean> deleteMessage(@PathVariable Long messageId, HttpServletRequest request) {
        // 从请求中获取当前登录的用户ID
        Long userId = (Long) request.getAttribute("userId");
        log.info("删除消息: userId={}, messageId={}", userId, messageId);
        
        try {
            boolean success = messageService.deleteMessage(messageId, userId);
            return Result.success(success);
        } catch (Exception e) {
            log.error("删除消息失败: {}", e.getMessage(), e);
            return Result.fail("删除消息失败");
        }
    }
    
    /**
     * 获取房东消息列表
     */
    @GetMapping("/landlord/messages")
    public Result<Page<Message>> getLandlordMessages(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        // 从请求中获取当前登录的房东ID
        Long landlordId = (Long) request.getAttribute("userId");
        log.info("获取房东消息列表: landlordId={}, page={}, size={}", landlordId, page, size);
        
        try {
            Page<Message> messages = messageService.getUserMessages(landlordId, page, size);
            return Result.success(messages);
        } catch (Exception e) {
            log.error("获取房东消息列表失败: {}", e.getMessage(), e);
            return Result.fail("获取消息列表失败");
        }
    }

    @PostMapping("/broadcast")
    @ApiOperation("发送系统广播通知")
    public Result<Void> broadcastMessage(@RequestBody @Valid MessageSendRequest request) {
        messageService.broadcastSystemMessage(request);
        return Result.success(null);
    }

    @GetMapping("/system")
    @ApiOperation("获取系统通知列表")
    public Result<Page<Message>> getSystemMessages(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId) {
        return Result.success(messageService.getSystemMessages(pageNum, pageSize, userId));
    }
} 