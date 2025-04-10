package com.zufang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zufang.dto.chat.ChatSessionDTO;
import com.zufang.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 聊天消息Mapper接口 (纯注解实现，使用MyBatis-Plus)
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    
    /**
     * 获取聊天历史记录
     *
     * @param userId     用户ID
     * @param landlordId 房东ID
     * @param houseId    房源ID
     * @return 聊天记录列表
     */
    @Select({"SELECT ", 
            "    cm.*, ", 
            "    u.nickname as sender_name, ", 
            "    u.avatar as sender_avatar ", 
            "FROM chat_message cm ", 
            "LEFT JOIN user u ON cm.sender_id = u.id ", 
            "WHERE ((cm.sender_id = #{userId} AND cm.receiver_id = #{landlordId}) ", 
            "    OR (cm.sender_id = #{landlordId} AND cm.receiver_id = #{userId})) ", 
            "    AND cm.house_id = #{houseId} ", 
            "    AND cm.is_deleted = 0 ", 
            "ORDER BY cm.create_time ASC"})
    List<ChatMessage> getChatHistory(@Param("userId") Long userId,
                                   @Param("landlordId") Long landlordId,
                                   @Param("houseId") Long houseId);
    
    /**
     * 标记消息为已读
     *
     * @param receiverId 接收者ID
     * @param senderId   发送者ID
     * @param houseId    房源ID
     * @return 影响行数
     */
    @Update("UPDATE chat_message SET is_read = 1 " +
            "WHERE receiver_id = #{receiverId} " +
            "AND sender_id = #{senderId} " +
            "AND house_id = #{houseId} " +
            "AND is_read = 0")
    int markConversationAsRead(@Param("receiverId") Long receiverId,
                             @Param("senderId") Long senderId,
                             @Param("houseId") Long houseId);
    
    /**
     * 获取用户的未读消息数量
     *
     * @param userId 用户ID
     * @return 未读消息数量
     */
    @Select("SELECT COUNT(*) FROM chat_message " +
            "WHERE receiver_id = #{userId} " +
            "AND is_read = 0 " +
            "AND is_deleted = 0")
    int countUnreadMessages(@Param("userId") Long userId);
    
    /**
     * 标记用户的所有消息为已读
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update("UPDATE chat_message SET is_read = 1 " +
            "WHERE receiver_id = #{userId} " +
            "AND is_read = 0")
    int markAllAsRead(@Param("userId") Long userId);
    
    /**
     * 获取房东的所有聊天会话列表 (原 getUserSessions)
     * 
     * @param landlordId 房东ID
     * @return 会话列表
     */
    @Select("WITH latest_messages AS (\n" +
            "    SELECT\n" +
            "        sender_id,\n" +
            "        receiver_id,\n" +
            "        house_id,\n" +
            "        content as last_message,\n" +
            "        create_time as last_message_time,\n" +
            "        ROW_NUMBER() OVER(PARTITION BY \n" +
            "            CASE WHEN sender_id = #{landlordId} THEN receiver_id ELSE sender_id END,\n" +
            "            house_id\n" +
            "            ORDER BY create_time DESC\n" +
            "        ) as rn\n" +
            "    FROM chat_message\n" +
            "    WHERE (sender_id = #{landlordId} OR receiver_id = #{landlordId})\n" +
            "    AND is_deleted = 0\n" +
            ")\n" +
            "SELECT\n" +
            "    CONCAT(CASE WHEN lm.sender_id = #{landlordId} THEN lm.receiver_id ELSE lm.sender_id END, '_', lm.house_id) as session_key,\n" +
            "    CASE WHEN lm.sender_id = #{landlordId} THEN lm.receiver_id ELSE lm.sender_id END as user_id,\n" +
            "    u.nickname as user_name,\n" +
            "    u.avatar as user_avatar,\n" +
            "    #{landlordId} as landlord_id,\n" +
            "    l.nickname as landlord_name,\n" +
            "    l.avatar as landlord_avatar,\n" +
            "    lm.house_id,\n" +
            "    h.title as house_name,\n" +
            "    lm.last_message,\n" +
            "    lm.last_message_time,\n" +
            "    (SELECT COUNT(*) FROM chat_message cm \n" +
            "     WHERE cm.sender_id = CASE WHEN lm.sender_id = #{landlordId} THEN lm.receiver_id ELSE lm.sender_id END \n" +
            "     AND cm.receiver_id = #{landlordId} \n" +
            "     AND cm.house_id = lm.house_id \n" +
            "     AND cm.is_read = 0 \n" +
            "     AND cm.is_deleted = 0) as unread_count\n" +
            "FROM latest_messages lm\n" +
            "JOIN user u ON u.id = CASE WHEN lm.sender_id = #{landlordId} THEN lm.receiver_id ELSE lm.sender_id END\n" +
            "JOIN user l ON l.id = #{landlordId}\n" +
            "JOIN house h ON h.id = lm.house_id\n" +
            "WHERE lm.rn = 1\n" +
            "ORDER BY lm.last_message_time DESC")
    List<ChatSessionDTO> getLandlordSessions(@Param("landlordId") Long landlordId);
    
    /**
     * 获取租户的所有聊天会话列表
     *
     * @param tenantId 租户ID
     * @return 会话列表
     */
    @Select("WITH latest_messages AS (\n" +
            "    SELECT\n" +
            "        sender_id,\n" +
            "        receiver_id,\n" +
            "        house_id,\n" +
            "        content as last_message,\n" +
            "        create_time as last_message_time,\n" +
            "        ROW_NUMBER() OVER(PARTITION BY \n" +
            "            CASE WHEN sender_id = #{tenantId} THEN receiver_id ELSE sender_id END,\n" +
            "            house_id\n" +
            "            ORDER BY create_time DESC\n" +
            "        ) as rn\n" +
            "    FROM chat_message\n" +
            "    WHERE (sender_id = #{tenantId} OR receiver_id = #{tenantId})\n" +
            "    AND is_deleted = 0\n" +
            ")\n" +
            "SELECT\n" +
            "    CONCAT(CASE WHEN lm.sender_id = #{tenantId} THEN lm.receiver_id ELSE lm.sender_id END, '_', lm.house_id) as session_key,\n" +
            "    #{tenantId} as user_id, \n" +
            "    t.nickname as user_name, \n" +
            "    t.avatar as user_avatar, \n" +
            "    CASE WHEN lm.sender_id = #{tenantId} THEN lm.receiver_id ELSE lm.sender_id END as landlord_id,\n" +
            "    l.nickname as landlord_name,\n" +
            "    l.avatar as landlord_avatar,\n" +
            "    lm.house_id,\n" +
            "    h.title as house_name,\n" +
            "    lm.last_message,\n" +
            "    lm.last_message_time,\n" +
            "    (SELECT COUNT(*) FROM chat_message cm \n" +
            "     WHERE cm.sender_id = CASE WHEN lm.sender_id = #{tenantId} THEN lm.receiver_id ELSE lm.sender_id END \n" +
            "     AND cm.receiver_id = #{tenantId} \n" +
            "     AND cm.house_id = lm.house_id \n" +
            "     AND cm.is_read = 0 \n" +
            "     AND cm.is_deleted = 0) as unread_count\n" +
            "FROM latest_messages lm\n" +
            "JOIN user l ON l.id = CASE WHEN lm.sender_id = #{tenantId} THEN lm.receiver_id ELSE lm.sender_id END\n" +
            "JOIN user t ON t.id = #{tenantId}\n" +
            "JOIN house h ON h.id = lm.house_id\n" +
            "WHERE lm.rn = 1\n" +
            "ORDER BY lm.last_message_time DESC")
    List<ChatSessionDTO> getTenantSessions(@Param("tenantId") Long tenantId);
} 