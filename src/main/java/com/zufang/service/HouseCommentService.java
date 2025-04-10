package com.zufang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zufang.dto.HouseCommentDTO;
import com.zufang.entity.HouseComment;

import java.util.List;

/**
 * 房源评论服务接口
 */
public interface HouseCommentService extends IService<HouseComment> {
    
    /**
     * 添加评论
     *
     * @param houseComment 评论信息
     * @return 是否成功
     */
    boolean addComment(HouseComment houseComment);
    
    /**
     * 回复评论
     *
     * @param houseComment 评论信息
     * @return 是否成功
     */
    boolean replyComment(HouseComment houseComment);
    
    /**
     * 分页获取房源评论列表
     *
     * @param houseId 房源ID
     * @return 评论列表
     */
    List<HouseCommentDTO> getCommentsByHouseId(Long houseId);
    
    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @param userId 当前用户ID
     * @return 是否成功
     */
    boolean deleteComment(Long commentId, Long userId);
} 