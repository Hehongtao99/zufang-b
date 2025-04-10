package com.zufang.dto;

import lombok.Data;

/**
 * 支付DTO
 */
@Data
public class PaymentDTO {
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 支付方式：ALIPAY-支付宝，WECHAT-微信，BANK-银行卡
     */
    private String payMethod;
} 