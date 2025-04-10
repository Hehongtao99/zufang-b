package com.zufang.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 订房DTO（用户提交订房请求时的参数）
 */
@Data
public class BookingDTO {
    
    /**
     * 房源ID
     */
    private Long houseId;
    
    /**
     * 租期开始日期
     */
    private LocalDate startDate;
    
    /**
     * 租期结束日期
     */
    private LocalDate endDate;
    
    /**
     * 租期月数
     */
    private Integer leaseTerm;
    
    /**
     * 备注
     */
    private String remark;
} 