package com.zufang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zufang.common.response.Result;
import com.zufang.dto.AppointmentDTO;
import com.zufang.dto.AppointmentUpdateDTO;
import com.zufang.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 预约控制器
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {
    
    private final AppointmentService appointmentService;
    
    // 定义日期时间格式化器，支持多种常见格式
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    /**
     * 创建预约
     */
    @PostMapping("/appointment/create")
    public Result<Long> createAppointment(@RequestBody Map<String, Object> params, @RequestAttribute Long userId) {
        try {
            // 创建DTO对象
            AppointmentDTO appointmentDTO = new AppointmentDTO();
            appointmentDTO.setUserId(userId);
            appointmentDTO.setHouseId(Long.valueOf(params.get("houseId").toString()));
            
            // 处理日期解析
            String appointmentTimeStr = params.get("appointmentTime").toString();
            LocalDateTime appointmentTime;
            
            try {
                // 尝试解析 ISO 格式 (2025-04-16T00:00:00)
                if (appointmentTimeStr.contains("T")) {
                    appointmentTime = LocalDateTime.parse(appointmentTimeStr, ISO_FORMATTER);
                } else {
                    // 解析标准格式 (2025-04-16 00:00:00)
                    appointmentTime = LocalDateTime.parse(appointmentTimeStr, FORMATTER);
                }
                
                appointmentDTO.setAppointmentTime(appointmentTime);
            } catch (Exception e) {
                log.error("日期解析错误: {}", appointmentTimeStr, e);
                return Result.fail("预约时间格式错误，请使用正确的日期时间格式");
            }
            
            appointmentDTO.setPhone(params.get("phone").toString());
            appointmentDTO.setRemark(params.get("remark") != null ? params.get("remark").toString() : null);
            
            // 调用服务创建预约
            Long appointmentId = appointmentService.createAppointment(appointmentDTO);
            return Result.success(appointmentId);
        } catch (Exception e) {
            log.error("创建预约失败: {}", e.getMessage(), e);
            return Result.fail("创建预约失败: " + e.getMessage());
        }
    }
    
    /**
     * 房东获取预约列表
     */
    @GetMapping("/landlord/appointments")
    public Result<Page<AppointmentDTO>> getLandlordAppointments(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestAttribute Long userId
    ) {
        try {
            // 创建分页对象
            Page<AppointmentDTO> page = new Page<>(pageNum, pageSize);
            // 调用服务获取预约列表
            page = appointmentService.getLandlordAppointments(page, userId, status);
            return Result.success(page);
        } catch (Exception e) {
            log.error("获取房东预约列表失败: {}", e.getMessage(), e);
            return Result.fail("获取预约列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新预约状态
     */
    @PostMapping("/landlord/appointments/update-status")
    public Result<AppointmentDTO> updateAppointmentStatus(@RequestBody Map<String, Object> params, @RequestAttribute Long userId) {
        try {
            Long id = Long.parseLong(params.get("id").toString());
            String status = params.get("status").toString();
            String remark = params.get("remark") != null ? params.get("remark").toString() : null;
            
            // 创建更新DTO对象
            AppointmentUpdateDTO updateDTO = new AppointmentUpdateDTO();
            updateDTO.setAppointmentId(id);
            updateDTO.setLandlordId(userId);
            updateDTO.setStatus(status);
            updateDTO.setRemark(remark);
            
            // 调用服务更新状态
            AppointmentDTO updatedAppointment = appointmentService.updateAppointmentStatus(updateDTO);
            return Result.success(updatedAppointment);
        } catch (Exception e) {
            log.error("更新预约状态失败: {}", e.getMessage(), e);
            return Result.fail("更新预约状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 拒绝预约
     */
    @PostMapping("/landlord/appointments/{id}/reject")
    public Result<AppointmentDTO> rejectAppointment(@PathVariable Long id, @RequestBody Map<String, Object> params, @RequestAttribute Long userId) {
        try {
            String reason = params.get("reason").toString();
            // 调用拒绝预约方法
            AppointmentDTO updatedAppointment = appointmentService.rejectAppointment(id, userId, reason);
            return Result.success(updatedAppointment);
        } catch (Exception e) {
            log.error("拒绝预约失败: {}", e.getMessage(), e);
            return Result.fail("拒绝预约失败: " + e.getMessage());
        }
    }
    
    /**
     * 接受预约
     */
    @PostMapping("/landlord/appointments/{id}/accept")
    public Result<AppointmentDTO> acceptAppointment(@PathVariable Long id, @RequestAttribute Long userId) {
        try {
            // 调用接受预约方法
            AppointmentDTO updatedAppointment = appointmentService.acceptAppointment(id, userId);
            return Result.success(updatedAppointment);
        } catch (Exception e) {
            log.error("接受预约失败: {}", e.getMessage(), e);
            return Result.fail("接受预约失败: " + e.getMessage());
        }
    }
    
    /**
     * 完成预约
     */
    @PostMapping("/landlord/appointments/{id}/complete")
    public Result<AppointmentDTO> completeAppointment(@PathVariable Long id, @RequestAttribute Long userId) {
        try {
            // 调用完成预约方法
            AppointmentDTO updatedAppointment = appointmentService.completeAppointment(id, userId);
            return Result.success(updatedAppointment);
        } catch (Exception e) {
            log.error("标记预约为完成失败: {}", e.getMessage(), e);
            return Result.fail("标记预约为完成失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取未读预约数量
     */
    @GetMapping("/landlord/appointments/unread-count")
    public Result<Integer> getUnreadCount(@RequestAttribute Long userId) {
        try {
            Integer count = appointmentService.getUnreadCount(userId);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取未读预约数量失败: {}", e.getMessage(), e);
            return Result.fail("获取未读预约数量失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取未读预约数量 (POST方法)
     */
    @PostMapping("/landlord/appointments/unread-count")
    public Result<Integer> getUnreadCountPost(@RequestAttribute Long userId) {
        return getUnreadCount(userId);
    }
    
    /**
     * 标记预约为已读
     */
    @PostMapping("/landlord/appointments/mark-read/{id}")
    public Result<Void> markAsRead(@PathVariable Long id, @RequestAttribute Long userId) {
        try {
            appointmentService.markAsRead(id, userId);
            return Result.success();
        } catch (Exception e) {
            log.error("标记已读失败: {}", e.getMessage(), e);
            return Result.fail("标记已读失败: " + e.getMessage());
        }
    }
} 