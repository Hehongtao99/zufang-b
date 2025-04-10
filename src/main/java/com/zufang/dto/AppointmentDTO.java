package com.zufang.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预约DTO，用于封装预约信息展示
 */
@Data
public class AppointmentDTO implements Serializable {
    
    private Long id; // 预约ID
    
    private Long userId; // 用户ID
    
    private String userName; // 用户名称
    
    private String userAvatar; // 用户头像
    
    private Long houseId; // 房源ID
    
    private String houseTitle; // 房源标题
    
    private String houseCoverImage; // 房源封面图片
    
    private String houseImage; // 房源主图
    
    private String houseAddress; // 房源地址
    
    private BigDecimal houseRent; // 房源租金
    
    private Boolean houseDeleted; // 房源是否已删除
    
    private Long landlordId; // 房东ID
    
    private String landlordName; // 房东名称
    
    private String landlordAvatar; // 房东头像
    
    private String landlordPhone; // 房东电话
    
    private LocalDateTime appointmentTime; // 预约看房时间
    
    private String phone; // 联系电话
    
    private String remark; // 备注信息
    
    private String status; // 预约状态
    
    private Integer isRead; // 是否已读：0-未读，1-已读
    
    private LocalDateTime createTime; // 创建时间
    
    private LocalDateTime updateTime; // 更新时间
} 