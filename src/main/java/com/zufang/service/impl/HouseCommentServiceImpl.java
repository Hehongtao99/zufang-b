package com.zufang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zufang.dto.HouseCommentDTO;
import com.zufang.entity.HouseComment;
import com.zufang.entity.User;
import com.zufang.mapper.HouseCommentMapper;
import com.zufang.service.HouseCommentService;
import com.zufang.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 房源评论服务实现类
 */
@Service
public class HouseCommentServiceImpl extends ServiceImpl<HouseCommentMapper, HouseComment> implements HouseCommentService {
    
    @Autowired
    private UserService userService;
    
    @Override
    @Transactional
    public boolean addComment(HouseComment houseComment) {
        // 设置为根评论
        houseComment.setRootId(null);
        houseComment.setParentId(null);
        houseComment.setReplyUserId(null);
        houseComment.setLikeCount(0);
        return save(houseComment);
    }
    
    @Override
    @Transactional
    public boolean replyComment(HouseComment houseComment) {
        // 如果是回复评论，需要设置根评论ID
        if (houseComment.getParentId() != null) {
            // 查询父评论
            HouseComment parentComment = getById(houseComment.getParentId());
            if (parentComment != null) {
                // 如果父评论是根评论，则设置根评论ID为父评论ID
                if (parentComment.getRootId() == null) {
                    houseComment.setRootId(parentComment.getId());
                } else {
                    // 否则沿用父评论的根评论ID
                    houseComment.setRootId(parentComment.getRootId());
                }
                // 设置回复用户ID
                houseComment.setReplyUserId(parentComment.getUserId());
            }
        }
        houseComment.setLikeCount(0);
        return save(houseComment);
    }
    
    @Override
    public List<HouseCommentDTO> getCommentsByHouseId(Long houseId) {
        // 查询所有评论
        LambdaQueryWrapper<HouseComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseComment::getHouseId, houseId)
               .orderByDesc(HouseComment::getCreateTime);
        List<HouseComment> comments = list(wrapper);
        
        if (comments.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取所有用户ID
        List<Long> userIds = comments.stream()
                .map(HouseComment::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        // 查询所有用户信息 - 改用多次查询单个用户的方式
        Map<Long, User> userMap = new HashMap<>();
        for (Long userId : userIds) {
            User user = userService.getById(userId);
            if (user != null) {
                userMap.put(userId, user);
            }
        }
        
        // 转换为DTO
        List<HouseCommentDTO> commentDTOs = comments.stream().map(comment -> {
            HouseCommentDTO dto = new HouseCommentDTO();
            BeanUtils.copyProperties(comment, dto);
            
            // 设置用户信息
            User user = userMap.get(comment.getUserId());
            if (user != null) {
                dto.setUsername(user.getUsername());
                dto.setNickname(user.getNickname());
                dto.setAvatar(user.getAvatar());
            }
            
            // 设置回复用户信息
            if (comment.getReplyUserId() != null) {
                User replyUser = userMap.get(comment.getReplyUserId());
                if (replyUser != null) {
                    dto.setReplyUsername(replyUser.getUsername());
                    dto.setReplyNickname(replyUser.getNickname());
                }
            }
            
            return dto;
        }).collect(Collectors.toList());
        
        // 构建评论树
        // 区分根评论和子评论
        List<HouseCommentDTO> rootComments = commentDTOs.stream()
                .filter(c -> c.getRootId() == null)
                .collect(Collectors.toList());
        
        Map<Long, List<HouseCommentDTO>> childrenMap = commentDTOs.stream()
                .filter(c -> c.getRootId() != null)
                .collect(Collectors.groupingBy(HouseCommentDTO::getRootId));
        
        // 填充子评论
        rootComments.forEach(root -> {
            List<HouseCommentDTO> children = childrenMap.get(root.getId());
            root.setChildren(children);
        });
        
        return rootComments;
    }
    
    @Override
    @Transactional
    public boolean deleteComment(Long commentId, Long userId) {
        // 查询评论
        HouseComment comment = getById(commentId);
        if (comment == null) {
            return false;
        }
        
        // 只有评论作者或管理员可以删除评论
        if (!comment.getUserId().equals(userId)) {
            // 获取用户信息
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getId, userId);
            User user = userService.getOne(queryWrapper);
            
            if (user == null || !"ADMIN".equals(user.getRole())) {
                return false;
            }
        }
        
        // 如果是根评论，删除所有子评论
        if (comment.getRootId() == null) {
            LambdaQueryWrapper<HouseComment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(HouseComment::getRootId, commentId);
            remove(wrapper);
        }
        
        // 删除评论
        return removeById(commentId);
    }
} 