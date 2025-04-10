package com.zufang.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 退租信息DTO
 */
@Data
public class OrderTerminateDTO {
    /**
     * 退租ID
     */
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

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
     * 租客ID
     */
    private Long userId;

    /**
     * 租客姓名
     */
    private String userName;

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
     * 房东电话
     */
    private String landlordPhone;

    /**
     * 原始开始日期
     */
    private LocalDate originalStartDate;

    /**
     * 原始结束日期
     */
    private LocalDate originalEndDate;

    /**
     * 月租金
     */
    private BigDecimal monthlyRent;

    /**
     * 押金
     */
    private BigDecimal deposit;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 申请时间
     */
    private LocalDateTime requestTime;

    /**
     * 申请原因
     */
    private String requestReason;

    /**
     * 期望退租日期
     */
    private LocalDate expectedTerminateDate;

    /**
     * 实际退租日期
     */
    private LocalDate actualTerminateDate;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 房东备注
     */
    private String landlordRemark;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 管理员备注
     */
    private String adminRemark;

    /**
     * 已租天数
     */
    private Integer daysRented;

    /**
     * 剩余天数
     */
    private Integer daysRemaining;

    /**
     * 总天数
     */
    private Integer totalDays;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 违约金
     */
    private BigDecimal penaltyAmount;

    /**
     * 是否已支付违约金
     */
    private Boolean isPenaltyPaid;

    /**
     * 违约金支付方式
     */
    private String penaltyPayMethod;

    /**
     * 状态
     */
    private String status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 