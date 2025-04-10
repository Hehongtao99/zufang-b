package com.zufang.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 订单支付成功事件
 */
@Getter
public class OrderPaidEvent extends ApplicationEvent {
    
    private final Long orderId;
    
    public OrderPaidEvent(Long orderId) {
        super(orderId);
        this.orderId = orderId;
    }
} 