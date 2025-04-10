package com.zufang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 房源评论实体类
 */
@Data
@TableName("house_comment")
public class HouseComment implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id; // 评论ID
    
    private Long houseId; // 房源ID
    
    private Long userId; // 评论用户ID
    
    private String content; // 评论内容
    
    private Long parentId; // 父评论ID，如果是回复其他评论则填写
    
    private Long rootId; // 根评论ID，用于标识评论树
    
    private Long replyUserId; // 被回复用户ID
    
    private Integer likeCount; // 点赞数
    
    private LocalDateTime createTime; // 创建时间
    
    private LocalDateTime updateTime; // 更新时间
    
    @TableLogic
    private Integer isDeleted; // 是否删除：0-未删除，1-已删除
} 