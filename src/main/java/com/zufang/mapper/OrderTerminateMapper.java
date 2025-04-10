package com.zufang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zufang.entity.OrderTerminate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 退租记录Mapper接口
 */
@Mapper
public interface OrderTerminateMapper extends BaseMapper<OrderTerminate> {
    
    /**
     * 根据订单ID查询退租记录
     * @param orderId 订单ID
     * @return 退租记录列表
     */
    @Select("SELECT * FROM order_terminate WHERE order_id = #{orderId} AND is_deleted = 0")
    List<OrderTerminate> selectByOrderId(Long orderId);
} 