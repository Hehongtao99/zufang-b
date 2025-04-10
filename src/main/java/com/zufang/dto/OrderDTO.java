package com.zufang.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单DTO
 */
@Data
public class OrderDTO {
    
    private Long id;
    
    /**
     * 订单编号
     */
    private String orderNo;
    
    /**
     * 房源ID
     */
    private Long houseId;
    
    /**
     * 房源标题
     */
    private String houseTitle;
    
    /**
     * 房源图片
     */
    private String houseCoverImage;
    
    /**
     * 房源图片（前端使用）
     */
    private String houseImage;
    
    /**
     * 房源地址
     */
    private String houseAddress;
    
    /**
     * 房源是否已删除
     */
    private Boolean isHouseDeleted;
    
    /**
     * 租客ID
     */
    private Long userId;
    
    /**
     * 租客姓名
     */
    private String userName;
    
    /**
     * 租客真实姓名
     */
    private String userRealName;
    
    /**
     * 租客电话
     */
    private String userPhone;
    
    /**
     * 房东ID
     */
    private Long landlordId;
    
    /**
     * 房东姓名
     */
    private String landlordName;
    
    /**
     * 房东真实姓名
     */
    private String landlordRealName;
    
    /**
     * 房东电话
     */
    private String landlordPhone;
    
    /**
     * 租期开始日期
     */
    private LocalDate startDate;
    
    /**
     * 租期结束日期
     */
    private LocalDate endDate;
    
    /**
     * 月租金
     */
    private BigDecimal monthlyRent;
    
    /**
     * 押金
     */
    private BigDecimal deposit;
    
    /**
     * 服务费
     */
    private BigDecimal serviceFee;
    
    /**
     * 总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 状态：UNPAID-待支付，PAID-已支付，CANCELLED-已取消，PAYMENT_CANCELLED-取消支付，REFUNDING-退款中，REFUNDED-已退款，COMPLETED-已完成
     */
    private String status;
    
    /**
     * 支付时间
     */
    private LocalDateTime payTime;
    
    /**
     * 支付方式
     */
    private String payMethod;
    
    /**
     * 交易流水号
     */
    private String transactionId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 取消原因
     */
    private String cancelReason;
    
    /**
     * 租期开始日期（格式化后）
     */
    private String leaseStartDate;
    
    /**
     * 租期结束日期（格式化后）
     */
    private String leaseEndDate;
    
    /**
     * 退租原因
     */
    private String terminateReason;
    
    /**
     * 退租拒绝原因
     */
    private String terminateRejectReason;
    
    /**
     * 期望退租日期
     */
    private LocalDate expectedTerminateDate;
    
    /**
     * 实际退租日期
     */
    private LocalDate actualTerminateDate;
    
    /**
     * 违约金
     */
    private BigDecimal penaltyAmount;
    
    /**
     * 违约金是否已支付
     */
    private Boolean isPenaltyPaid;
} 