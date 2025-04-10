package com.zufang.dto;

import lombok.Data;

/**
 * 功能开关DTO
 */
@Data
public class FeatureToggleDTO {
    
    /**
     * 是否启用用户注册
     */
    private Boolean enableRegister;
    
    /**
     * 是否启用房源发布
     */
    private Boolean enableHousePublish;
    
    /**
     * 是否启用房源预约
     */
    private Boolean enableAppointment;
    
    /**
     * 是否启用订单功能
     */
    private Boolean enableOrder;
    
    /**
     * 是否启用合同功能
     */
    private Boolean enableContract;
    
    /**
     * 是否启用消息通知
     */
    private Boolean enableNotification;
} 