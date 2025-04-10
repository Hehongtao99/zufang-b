package com.zufang.dto;

import lombok.Data;

/**
 * 房源审核DTO
 */
@Data
public class HouseApproveDTO {
    
    /**
     * 房源ID
     */
    private Long houseId;
    
    /**
     * 是否通过审核
     */
    private Boolean approved;
    
    /**
     * 拒绝原因
     */
    private String reason;
    
    /**
     * 状态 - 兼容旧接口 @deprecated 使用approved代替
     */
    private String status;
    
    /**
     * 拒绝原因 - 兼容旧接口 @deprecated 使用reason代替
     */
    private String rejectReason;
} 