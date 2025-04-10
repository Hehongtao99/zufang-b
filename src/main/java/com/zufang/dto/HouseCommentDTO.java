package com.zufang.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 房源评论数据传输对象
 */
@Data
public class HouseCommentDTO {
    
    private Long id; // 评论ID
    
    private Long houseId; // 房源ID
    
    private Long userId; // 评论用户ID
    
    private String username; // 用户名
    
    private String nickname; // 用户昵称
    
    private String avatar; // 用户头像
    
    private String content; // 评论内容
    
    private Long parentId; // 父评论ID
    
    private Long rootId; // 根评论ID
    
    private Long replyUserId; // 被回复用户ID
    
    private String replyUsername; // 被回复用户名
    
    private String replyNickname; // 被回复用户昵称
    
    private Integer likeCount; // 点赞数
    
    private LocalDateTime createTime; // 创建时间
    
    private List<HouseCommentDTO> children; // 子评论列表
} 