package com.zufang.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 预约更新DTO，用于更新预约状态
 */
@Data
public class AppointmentUpdateDTO implements Serializable {
    
    private Long appointmentId; // 预约ID
    
    private Long landlordId; // 房东ID
    
    private String status; // 预约状态
    
    private String remark; // 备注（拒绝原因等）
} 