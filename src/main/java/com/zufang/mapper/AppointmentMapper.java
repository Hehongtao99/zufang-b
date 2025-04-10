package com.zufang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zufang.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预约Mapper接口
 */
@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {
} 