package com.zufang.common.enums;

/**
 * 订单状态枚举
 */
public enum OrderStatus {
    /**
     * 待支付
     */
    UNPAID,
    
    /**
     * 已支付
     */
    PAID,
    
    /**
     * 已取消
     */
    CANCELLED,
    
    /**
     * 取消支付
     */
    PAYMENT_CANCELLED,
    
    /**
     * 退款中
     */
    REFUNDING,
    
    /**
     * 已退款
     */
    REFUNDED,
    
    /**
     * 租赁中（活跃状态）
     */
    ACTIVE,
    
    /**
     * 已完成
     */
    COMPLETED,
    
    /**
     * 已申请退租
     */
    TERMINATE_REQUESTED,
    
    /**
     * 退租申请已批准
     */
    TERMINATE_APPROVED,
    
    /**
     * 已退租
     */
    TERMINATED,
    
    /**
     * 过期
     */
    EXPIRED
} 