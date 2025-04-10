package com.zufang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zufang.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息Mapper接口
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
} 