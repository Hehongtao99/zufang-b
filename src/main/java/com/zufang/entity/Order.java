package com.zufang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@TableName("`order`") // 由于order是SQL关键字，需要加反引号
public class Order {

    @TableId(value = "id", type = IdType.AUTO)
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
     * 租客ID
     */
    private Long userId;

    /**
     * 房东ID
     */
    private Long landlordId;

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
     * 状态：UNPAID-待支付，PAID-已支付，CANCELLED-已取消，PAYMENT_CANCELLED-取消支付，
     * REFUNDING-退款中，REFUNDED-已退款，COMPLETED-已完成，TERMINATE_REQUESTED-申请退租，
     * TERMINATED-已退租
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
     * 取消原因
     */
    private String cancelReason;
    
    /**
     * 退租原因
     */
    private String terminateReason;
    
    /**
     * 退租申请时间
     */
    private LocalDateTime terminateRequestTime;
    
    /**
     * 期望退租日期
     */
    private LocalDate expectedTerminateDate;
    
    /**
     * 实际退租日期
     */
    private LocalDate actualTerminateDate;
    
    /**
     * 退租拒绝原因
     */
    private String terminateRejectReason;
    
    /**
     * 退租时间
     */
    private LocalDateTime terminateTime;
    
    /**
     * 违约金
     */
    private BigDecimal penaltyAmount;
    
    /**
     * 违约金是否已支付
     */
    private Boolean isPenaltyPaid;
    
    /**
     * 违约金支付时间
     */
    private LocalDateTime penaltyPayTime;

    /**
     * 违约金支付方式
     */
    private String penaltyPayMethod;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    private Boolean isDeleted;
    
    /**
     * 房源标题
     */
    @TableField(exist = false)
    private String houseTitle;
    
    /**
     * 退租状态
     */
    @TableField(exist = false)
    private String terminateStatus;
    
    /**
     * 获取房源标题
     */
    public String getHouseTitle() {
        return this.houseTitle;
    }
    
    /**
     * 设置房源标题
     */
    public void setHouseTitle(String houseTitle) {
        this.houseTitle = houseTitle;
    }
    
    /**
     * 设置退租状态
     */
    public void setTerminateStatus(String terminateStatus) {
        this.terminateStatus = terminateStatus;
    }
    
    /**
     * 获取退租状态
     */
    public String getTerminateStatus() {
        return this.terminateStatus;
    }
    
    /**
     * 设置实际结束日期（兼容setActualEndDate方法）
     */
    public void setActualEndDate(LocalDate actualEndDate) {
        this.actualTerminateDate = actualEndDate;
    }
}