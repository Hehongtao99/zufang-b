package com.zufang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zufang.entity.HouseComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 房源评论Mapper接口
 */
@Mapper
public interface HouseCommentMapper extends BaseMapper<HouseComment> {
} 