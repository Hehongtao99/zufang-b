package com.zufang.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zufang.dto.AppointmentDTO;
import com.zufang.dto.AppointmentUpdateDTO;
import com.zufang.entity.Appointment;

/**
 * 预约服务接口
 */
public interface AppointmentService extends IService<Appointment> {
    
    /**
     * 创建预约
     * @param appointmentDTO 预约信息DTO
     * @return 预约ID
     */
    Long createAppointment(AppointmentDTO appointmentDTO);
    
    /**
     * 查询房东预约列表
     * @param page       分页参数
     * @param landlordId 房东ID
     * @param status     预约状态
     * @return 预约列表
     */
    Page<AppointmentDTO> getLandlordAppointments(Page<AppointmentDTO> page, Long landlordId, String status);
    
    /**
     * 更新预约状态
     * @param appointmentUpdateDTO 预约更新DTO
     * @return 更新后的预约
     */
    AppointmentDTO updateAppointmentStatus(AppointmentUpdateDTO appointmentUpdateDTO);
    
    /**
     * 房东接受预约
     * @param appointmentId 预约ID
     * @param landlordId    房东ID
     * @return 预约DTO
     */
    AppointmentDTO acceptAppointment(Long appointmentId, Long landlordId);
    
    /**
     * 房东拒绝预约
     * @param appointmentId 预约ID
     * @param landlordId    房东ID
     * @param reason        拒绝原因
     * @return 预约DTO
     */
    AppointmentDTO rejectAppointment(Long appointmentId, Long landlordId, String reason);
    
    /**
     * 预约完成
     * @param appointmentId 预约ID
     * @param landlordId    房东ID
     * @return 预约DTO
     */
    AppointmentDTO completeAppointment(Long appointmentId, Long landlordId);
    
    /**
     * 获取房东未读预约数量
     * @param landlordId 房东ID
     * @return 未读数量
     */
    Integer getUnreadCount(Long landlordId);
    
    /**
     * 标记预约为已读
     * @param appointmentId 预约ID
     * @param landlordId    房东ID
     */
    void markAsRead(Long appointmentId, Long landlordId);
    
    /**
     * 给房东发送预约通知
     * @param appointmentDTO 预约DTO
     */
    void sendNotificationToLandlord(AppointmentDTO appointmentDTO);
    
    /**
     * 查询用户预约列表
     * @param page   分页参数
     * @param userId 用户ID
     * @param status 预约状态
     * @return 预约列表
     */
    Page<AppointmentDTO> getUserAppointments(Page<AppointmentDTO> page, Long userId, String status);
    
    /**
     * 用户取消预约
     * @param appointmentId 预约ID
     * @param userId        用户ID
     * @return 是否成功
     */
    void cancelAppointment(Long appointmentId, Long userId);
    
    /**
     * 统计房东待处理预约数量（状态为待确认的预约）
     * @param landlordId 房东ID
     * @return 待处理预约数量
     */
    int countLandlordPendingAppointments(Long landlordId);
} 