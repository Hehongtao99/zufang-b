package com.zufang.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 退租请求数据传输对象
 */
@Data
public class TerminateDTO {
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 退租原因
     */
    private String reason;
    
    /**
     * 期望退租日期
     */
    private String expectedDate;
    
    /**
     * 实际退租日期（由房东确认时设置）
     */
    private String actualDate;
    
    /**
     * 违约金金额
     */
    private Double penaltyAmount;
} 