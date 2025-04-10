package com.zufang.controller;

import com.zufang.common.R;
import com.zufang.dto.HouseCommentDTO;
import com.zufang.entity.HouseComment;
import com.zufang.service.HouseCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 房源评论控制器
 */
@RestController
@RequestMapping("/house/comment")
public class HouseCommentController {
    
    @Autowired
    private HouseCommentService houseCommentService;
    
    /**
     * 获取房源评论列表
     *
     * @param houseId 房源ID
     * @return 评论列表
     */
    @GetMapping("/list/{houseId}")
    public R<List<HouseCommentDTO>> getCommentList(@PathVariable Long houseId) {
        List<HouseCommentDTO> list = houseCommentService.getCommentsByHouseId(houseId);
        return R.success(list);
    }
    
    /**
     * 添加评论
     *
     * @param houseComment 评论信息
     * @param request HTTP请求
     * @return 是否成功
     */
    @PostMapping("/add")
    public R<String> addComment(@RequestBody HouseComment houseComment, HttpServletRequest request) {
        // 首先检查评论信息是否完整
        if (houseComment.getHouseId() == null) {
            return R.error("房源ID不能为空");
        }
        
        if (houseComment.getContent() == null || houseComment.getContent().trim().isEmpty()) {
            return R.error("评论内容不能为空");
        }
        
        // 获取当前用户ID，优先使用前端传过来的userId
        Long userId = houseComment.getUserId();
        
        // 如果前端没传userId，尝试从session获取
        if (userId == null) {
            userId = (Long) request.getSession().getAttribute("userId");
            if (userId == null) {
                return R.error("请先登录");
            }
            houseComment.setUserId(userId);
        }
        
        boolean success = houseCommentService.addComment(houseComment);
        
        return success ? R.success("评论成功") : R.error("评论失败");
    }
    
    /**
     * 回复评论
     *
     * @param houseComment 评论信息
     * @param request HTTP请求
     * @return 是否成功
     */
    @PostMapping("/reply")
    public R<String> replyComment(@RequestBody HouseComment houseComment, HttpServletRequest request) {
        // 首先检查评论信息是否完整
        if (houseComment.getHouseId() == null) {
            return R.error("房源ID不能为空");
        }
        
        if (houseComment.getContent() == null || houseComment.getContent().trim().isEmpty()) {
            return R.error("回复内容不能为空");
        }
        
        if (houseComment.getParentId() == null) {
            return R.error("回复评论ID不能为空");
        }
        
        // 获取当前用户ID，优先使用前端传过来的userId
        Long userId = houseComment.getUserId();
        
        // 如果前端没传userId，尝试从session获取
        if (userId == null) {
            userId = (Long) request.getSession().getAttribute("userId");
            if (userId == null) {
                return R.error("请先登录");
            }
            houseComment.setUserId(userId);
        }
        
        boolean success = houseCommentService.replyComment(houseComment);
        
        return success ? R.success("回复成功") : R.error("回复失败");
    }
    
    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @param request HTTP请求
     * @return 是否成功
     */
    @DeleteMapping("/delete/{commentId}")
    public R<String> deleteComment(@PathVariable Long commentId, HttpServletRequest request) {
        // 获取当前用户ID
        Long userId = (Long) request.getSession().getAttribute("userId");
        if (userId == null) {
            return R.error("请先登录");
        }
        
        boolean success = houseCommentService.deleteComment(commentId, userId);
        
        return success ? R.success("删除成功") : R.error("删除失败");
    }
} 