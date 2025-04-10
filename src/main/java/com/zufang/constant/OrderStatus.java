package com.zufang.constant;

/**
 * 订单状态常量
 */
public class OrderStatus {
    /**
     * 待支付
     */
    public static final String UNPAID = "UNPAID";

    /**
     * 已支付
     */
    public static final String PAID = "PAID";

    /**
     * 已取消
     */
    public static final String CANCELLED = "CANCELLED";

    /**
     * 取消支付
     */
    public static final String PAYMENT_CANCELLED = "PAYMENT_CANCELLED";

    /**
     * 退款中
     */
    public static final String REFUNDING = "REFUNDING";

    /**
     * 已退款
     */
    public static final String REFUNDED = "REFUNDED";

    /**
     * 已完成
     */
    public static final String COMPLETED = "COMPLETED";

    /**
     * 申请退租
     */
    public static final String TERMINATE_REQUESTED = "TERMINATE_REQUESTED";

    /**
     * 已退租
     */
    public static final String TERMINATED = "TERMINATED";

    /**
     * 已入住
     */
    public static final String CHECKED_IN = "CHECKED_IN";
} 