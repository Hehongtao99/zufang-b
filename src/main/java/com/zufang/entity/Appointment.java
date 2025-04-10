package com.zufang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预约看房实体类
 */
@Data
@TableName("appointment")
public class Appointment implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id; // 预约ID
    
    private Long userId; // 用户ID（预约人）
    
    private Long houseId; // 房源ID
    
    private Long landlordId; // 房东ID
    
    private LocalDateTime appointmentTime; // 预约看房时间
    
    private String phone; // 联系电话
    
    private String remark; // 备注信息
    
    private String status; // 预约状态：PENDING-待处理，APPROVED-已同意，REJECTED-已拒绝，COMPLETED-已完成，CANCELED-已取消
    
    private Integer isRead; // 是否已读：0-未读，1-已读
    
    private LocalDateTime createTime; // 创建时间
    
    private LocalDateTime updateTime; // 更新时间
    
    @TableLogic
    private Integer isDeleted; // 是否删除：0-未删除，1-已删除
}