package com.zufang.controller;

import com.zufang.common.response.Result;
import com.zufang.dto.chat.ChatMessageDTO;
import com.zufang.dto.chat.ChatSessionDTO;
import com.zufang.entity.ChatMessage;
import com.zufang.entity.House;
import com.zufang.entity.User;
import com.zufang.service.ChatMessageService;
import com.zufang.service.HouseService;
import com.zufang.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 聊天控制器
 */
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    
    private final ChatMessageService chatMessageService;
    private final UserService userService;
    private final HouseService houseService;
    
    /**
     * 发送聊天消息
     *
     * @param messageDTO 消息DTO
     * @param request   HttpServletRequest 用于获取用户信息
     * @return 结果
     */
    @PostMapping("/send")
    public Result<ChatMessage> sendMessage(@RequestBody ChatMessageDTO messageDTO, HttpServletRequest request) {
        log.info("发送聊天消息: {}", messageDTO);
        
        // 获取当前登录用户信息
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("未登录，请先登录");
        }
        
        // 校验参数
        if (messageDTO.getReceiverId() == null || messageDTO.getHouseId() == null || messageDTO.getContent() == null) {
            return Result.error("参数不能为空");
        }
        
        // 设置发送者ID为当前登录用户
        messageDTO.setSenderId(userId);
        
        // 验证接收者是否存在
        User receiver = userService.getById(messageDTO.getReceiverId());
        if (receiver == null) {
            return Result.error("接收者不存在");
        }
        
        // 验证房源是否存在
        House house = houseService.getById(messageDTO.getHouseId());
        if (house == null) {
            return Result.error("房源不存在");
        }
        
        // 获取当前用户信息
        User currentUser = userService.getById(userId);
        
        // 设置发送者信息为当前登录用户信息
        messageDTO.setSenderAvatar(currentUser.getAvatar());
        messageDTO.setSenderName(currentUser.getNickname());
        
        // 发送消息
        ChatMessage message = chatMessageService.sendMessage(
                messageDTO.getSenderId(),
                messageDTO.getReceiverId(),
                messageDTO.getHouseId(),
                messageDTO.getContent(),
                messageDTO.getSenderAvatar(),
                messageDTO.getSenderName()
        );
        
        return Result.ok(message);
    }
    
    /**
     * 获取聊天历史记录
     *
     * @param userId     用户ID
     * @param landlordId 房东ID
     * @param houseId    房源ID
     * @return 聊天历史记录
     */
    @GetMapping("/history")
    public Result<List<ChatMessage>> getChatHistory(
            @RequestParam Long userId,
            @RequestParam Long landlordId,
            @RequestParam Long houseId,
            HttpServletRequest request) {
        log.info("获取聊天历史记录: 用户:{}, 房东:{}, 房源:{}", userId, landlordId, houseId);
        
        // 获取当前登录用户信息
        Long currentUserId = (Long) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("role");
        
        if (currentUserId == null || userRole == null) {
            return Result.error("未登录，请先登录");
        }
        
        List<ChatMessage> chatHistory = chatMessageService.getChatHistory(userId, landlordId, houseId);
        
        // 将会话中的发给当前用户的消息标记为已读
        chatMessageService.markConversationAsRead(currentUserId, 
            "USER".equalsIgnoreCase(userRole) ? landlordId : userId, 
            houseId);
        
        return Result.ok(chatHistory);
    }
    
    /**
     * 获取未读消息数量
     *
     * @param userId 用户ID
     * @return 未读消息数量
     */
    @GetMapping("/unread/count")
    public Result<Integer> countUnreadMessages(@RequestParam Long userId) {
        int count = chatMessageService.countUnreadMessages(userId);
        return Result.ok(count);
    }
    
    /**
     * 标记所有消息为已读
     *
     * @param userId 用户ID
     * @return 结果
     */
    @PostMapping("/mark-all-read")
    public Result<Integer> markAllAsRead(@RequestParam Long userId) {
        int count = chatMessageService.markAllAsRead(userId);
        return Result.ok(count);
    }
    
    /**
     * 标记特定会话消息为已读
     *
     * @param receiverId 接收者ID
     * @param senderId   发送者ID
     * @param houseId    房源ID
     * @return 结果
     */
    @PostMapping("/mark-conversation-read")
    public Result<Integer> markConversationAsRead(
            @RequestParam Long receiverId,
            @RequestParam Long senderId,
            @RequestParam Long houseId) {
        int count = chatMessageService.markConversationAsRead(receiverId, senderId, houseId);
        return Result.ok(count);
    }
    
    /**
     * 获取用户的聊天会话列表 (根据角色区分)
     *
     * @param request HttpServletRequest 用于获取用户ID和角色
     * @return 会话列表
     */
    @GetMapping("/sessions")
    public Result<List<ChatSessionDTO>> getUserSessions(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("role");
        
        // 添加日志打印获取到的属性值
        log.info("Attempting to retrieve user info from request attributes. Retrieved userId: {}, Retrieved userRole: {}", userId, userRole);
        
        if (userId == null || userRole == null) {
            log.error("Failed to retrieve userId or userRole from request attributes. userId={}, userRole={}", userId, userRole);
            return Result.error("无法获取用户信息,请重新登录");
        }
        
        log.info("获取用户的聊天会话列表: 用户ID={}, 角色={}", userId, userRole);
        
        List<ChatSessionDTO> sessions;
        if ("USER".equalsIgnoreCase(userRole)) {
            sessions = chatMessageService.getTenantSessions(userId);
        } else if ("LANDLORD".equalsIgnoreCase(userRole)) {
            sessions = chatMessageService.getLandlordSessions(userId);
        } else {
            log.warn("未知用户角色: {}", userRole);
            return Result.error("不支持的用户角色");
        }
        
        return Result.ok(sessions);
    }
} 