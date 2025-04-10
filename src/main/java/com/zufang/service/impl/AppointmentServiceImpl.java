package com.zufang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zufang.common.constants.AppointmentStatus;
import com.zufang.common.constants.WebSocketType;
import com.zufang.common.exception.BusinessException;
import com.zufang.dto.AppointmentDTO;
import com.zufang.dto.AppointmentUpdateDTO;
import com.zufang.websocket.WebSocketMessage;
import com.zufang.entity.Appointment;
import com.zufang.entity.House;
import com.zufang.entity.User;
import com.zufang.mapper.AppointmentMapper;
import com.zufang.mapper.HouseMapper;
import com.zufang.mapper.UserMapper;
import com.zufang.service.AppointmentService;
import com.zufang.websocket.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 预约服务实现类
 */
@Slf4j
@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    @Autowired
    private AppointmentMapper appointmentMapper;
    
    @Autowired
    private HouseMapper houseMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private WebSocketService webSocketService;
    
    @Override
    @Transactional
    public Long createAppointment(AppointmentDTO appointmentDTO) {
        log.info("创建预约: {}", appointmentDTO);
        
        // 验证house是否存在
        House house = houseMapper.selectById(appointmentDTO.getHouseId());
        if (house == null) {
            log.error("创建预约失败: 房源不存在, houseId: {}", appointmentDTO.getHouseId());
            throw new BusinessException("房源不存在");
        }
        
        // 验证用户是否存在
        User user = userMapper.selectById(appointmentDTO.getUserId());
        if (user == null) {
            log.error("创建预约失败: 用户不存在, userId: {}", appointmentDTO.getUserId());
            throw new BusinessException("用户不存在");
        }
        
        // 设置landlordId
        appointmentDTO.setLandlordId(house.getLandlordId());
        
        // 创建预约记录
        Appointment appointment = new Appointment();
        BeanUtils.copyProperties(appointmentDTO, appointment);
        
        // 设置状态为等待确认
        appointment.setStatus(AppointmentStatus.PENDING.name());
        appointment.setIsRead(0);
        appointment.setCreateTime(LocalDateTime.now());
        appointment.setUpdateTime(LocalDateTime.now());
        appointment.setIsDeleted(0);
        
        // 保存预约记录
        int result = appointmentMapper.insert(appointment);
        if (result <= 0) {
            log.error("创建预约失败: 数据库插入失败, appointment: {}", appointment);
            throw new BusinessException("创建预约失败");
        }
        
        log.info("创建预约成功: {}", appointment);
        
        // 发送WebSocket通知给房东
        appointmentDTO.setId(appointment.getId());
        sendNotificationToLandlord(appointmentDTO);
        
        return appointment.getId();
    }
    
    @Override
    public Page<AppointmentDTO> getLandlordAppointments(Page<AppointmentDTO> page, Long landlordId, String status) {
        log.info("获取房东预约列表: landlordId={}, page={}, size={}, status={}", 
                landlordId, page.getCurrent(), page.getSize(), status);
        
        // 创建查询条件
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getLandlordId, landlordId)
               .eq(Appointment::getIsDeleted, 0);
        
        // 根据状态过滤
        if (StringUtils.hasText(status)) {
            wrapper.eq(Appointment::getStatus, status);
        }
        
        // 按创建时间倒序排序
        wrapper.orderByDesc(Appointment::getCreateTime);
        
        // 查询总数
        Long total = appointmentMapper.selectCount(wrapper);
        
        // 查询当前页数据
        Page<Appointment> appointmentPage = new Page<>(page.getCurrent(), page.getSize());
        appointmentPage = appointmentMapper.selectPage(appointmentPage, wrapper);
        
        // 转换为DTO
        List<AppointmentDTO> appointmentDTOList = appointmentPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 设置分页结果
        page.setRecords(appointmentDTOList);
        page.setTotal(total);
        
        log.info("获取房东预约列表成功: 总数={}", total);
        return page;
    }
    
    @Override
    @Transactional
    public AppointmentDTO updateAppointmentStatus(AppointmentUpdateDTO appointmentUpdateDTO) {
        log.info("更新预约状态: {}", appointmentUpdateDTO);
        
        // 获取预约记录
        Appointment appointment = appointmentMapper.selectById(appointmentUpdateDTO.getAppointmentId());
        if (appointment == null) {
            log.error("更新预约状态失败: 预约不存在, appointmentId: {}", appointmentUpdateDTO.getAppointmentId());
            throw new BusinessException("预约不存在");
        }
        
        // 验证是否为房东
        if (!appointment.getLandlordId().equals(appointmentUpdateDTO.getLandlordId())) {
            log.error("更新预约状态失败: 无权操作, appointmentId: {}, landlordId: {}", 
                    appointmentUpdateDTO.getAppointmentId(), appointmentUpdateDTO.getLandlordId());
            throw new BusinessException("无权操作此预约");
        }
        
        // 更新状态
        appointment.setStatus(appointmentUpdateDTO.getStatus());
        appointment.setRemark(appointmentUpdateDTO.getRemark());
        appointment.setUpdateTime(LocalDateTime.now());
        
        int result = appointmentMapper.updateById(appointment);
        if (result <= 0) {
            log.error("更新预约状态失败: 数据库更新失败, appointment: {}", appointment);
            throw new BusinessException("更新预约状态失败");
        }
        
        log.info("更新预约状态成功: {}", appointment);
        
        // 返回更新后的预约DTO
        return convertToDTO(appointment);
    }
    
    @Override
    @Transactional
    public AppointmentDTO acceptAppointment(Long appointmentId, Long landlordId) {
        log.info("接受预约: appointmentId={}, landlordId={}", appointmentId, landlordId);
        
        AppointmentUpdateDTO updateDTO = new AppointmentUpdateDTO();
        updateDTO.setAppointmentId(appointmentId);
        updateDTO.setLandlordId(landlordId);
        updateDTO.setStatus(AppointmentStatus.APPROVED.name());
        
        return updateAppointmentStatus(updateDTO);
    }
    
    @Override
    @Transactional
    public AppointmentDTO rejectAppointment(Long appointmentId, Long landlordId, String reason) {
        log.info("拒绝预约: appointmentId={}, landlordId={}, reason={}", appointmentId, landlordId, reason);
        
        AppointmentUpdateDTO updateDTO = new AppointmentUpdateDTO();
        updateDTO.setAppointmentId(appointmentId);
        updateDTO.setLandlordId(landlordId);
        updateDTO.setStatus(AppointmentStatus.REJECTED.name());
        updateDTO.setRemark(reason);
        
        return updateAppointmentStatus(updateDTO);
    }
    
    @Override
    @Transactional
    public AppointmentDTO completeAppointment(Long appointmentId, Long landlordId) {
        log.info("完成预约: appointmentId={}, landlordId={}", appointmentId, landlordId);
        
        AppointmentUpdateDTO updateDTO = new AppointmentUpdateDTO();
        updateDTO.setAppointmentId(appointmentId);
        updateDTO.setLandlordId(landlordId);
        updateDTO.setStatus(AppointmentStatus.COMPLETED.name());
        
        return updateAppointmentStatus(updateDTO);
    }
    
    @Override
    public Integer getUnreadCount(Long landlordId) {
        log.info("获取房东未读预约数: landlordId={}", landlordId);
        
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getLandlordId, landlordId)
               .eq(Appointment::getIsRead, 0)
               .eq(Appointment::getIsDeleted, 0);
        
        Integer count = Math.toIntExact(appointmentMapper.selectCount(wrapper));
        
        log.info("获取房东未读预约数成功: landlordId={}, count={}", landlordId, count);
        return count;
    }
    
    @Override
    @Transactional
    public void markAsRead(Long appointmentId, Long landlordId) {
        log.info("标记预约为已读: appointmentId={}, landlordId={}", appointmentId, landlordId);
        
        // 获取预约记录
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            log.error("标记预约为已读失败: 预约不存在, appointmentId: {}", appointmentId);
            throw new BusinessException("预约不存在");
        }
        
        // 验证是否为房东
        if (!appointment.getLandlordId().equals(landlordId)) {
            log.error("标记预约为已读失败: 无权操作, appointmentId: {}, landlordId: {}", appointmentId, landlordId);
            throw new BusinessException("无权操作此预约");
        }
        
        // 标记为已读
        LambdaUpdateWrapper<Appointment> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Appointment::getId, appointmentId)
                    .set(Appointment::getIsRead, 1)
                    .set(Appointment::getUpdateTime, LocalDateTime.now());
        
        int result = appointmentMapper.update(null, updateWrapper);
        if (result <= 0) {
            log.error("标记预约为已读失败: 数据库更新失败, appointmentId: {}", appointmentId);
            throw new BusinessException("标记预约为已读失败");
        }
        
        log.info("标记预约为已读成功: appointmentId={}", appointmentId);
    }
    
    @Override
    public void sendNotificationToLandlord(AppointmentDTO appointmentDTO) {
        log.info("发送预约通知给房东: {}", appointmentDTO);
        
        try {
            // 查询房源信息
            House house = houseMapper.selectById(appointmentDTO.getHouseId());
            if (house == null) {
                log.error("发送预约通知失败: 房源不存在, houseId: {}", appointmentDTO.getHouseId());
                return;
            }
            
            // 查询用户信息
            User user = userMapper.selectById(appointmentDTO.getUserId());
            if (user == null) {
                log.error("发送预约通知失败: 用户不存在, userId: {}", appointmentDTO.getUserId());
                return;
            }
            
            // 设置用户名和头像
            appointmentDTO.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
            appointmentDTO.setUserAvatar(user.getAvatar());
            
            // 设置房源信息
            appointmentDTO.setHouseTitle(house.getTitle());
            appointmentDTO.setHouseCoverImage(house.getCoverImage());
            appointmentDTO.setHouseImage(house.getCoverImage());
            appointmentDTO.setHouseAddress(house.getAddress());
            appointmentDTO.setHouseRent(house.getPrice());
            
            // 构建WebSocket消息
            WebSocketMessage message = WebSocketMessage.builder()
                    .type(WebSocketType.APPOINTMENT_NOTIFICATION.name())
                    .senderId(appointmentDTO.getUserId())
                    .receiverId(appointmentDTO.getLandlordId())
                    .content("有新的预约请求")
                    .data(appointmentDTO)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            // 发送WebSocket消息
            webSocketService.sendMessage(appointmentDTO.getLandlordId().toString(), message);
            
            log.info("发送预约通知成功: landlordId={}", appointmentDTO.getLandlordId());
        } catch (Exception e) {
            log.error("发送预约通知失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 获取用户预约列表
     */
    @Override
    public Page<AppointmentDTO> getUserAppointments(Page<AppointmentDTO> page, Long userId, String status) {
        log.info("获取用户预约列表: userId={}, page={}, size={}, status={}", 
                userId, page.getCurrent(), page.getSize(), status);
        
        // 创建查询条件
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getUserId, userId)
               .eq(Appointment::getIsDeleted, 0);
        
        // 根据状态过滤
        if (StringUtils.hasText(status)) {
            wrapper.eq(Appointment::getStatus, status);
        }
        
        // 按创建时间倒序排序
        wrapper.orderByDesc(Appointment::getCreateTime);
        
        // 查询总数
        Long total = appointmentMapper.selectCount(wrapper);
        
        // 查询当前页数据
        Page<Appointment> appointmentPage = new Page<>(page.getCurrent(), page.getSize());
        appointmentPage = appointmentMapper.selectPage(appointmentPage, wrapper);
        
        // 转换为DTO
        List<AppointmentDTO> appointmentDTOList = appointmentPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 设置分页结果
        page.setRecords(appointmentDTOList);
        page.setTotal(total);
        
        log.info("获取用户预约列表成功: 总数={}", total);
        return page;
    }
    
    /**
     * 用户取消预约
     */
    @Override
    @Transactional
    public void cancelAppointment(Long appointmentId, Long userId) {
        log.info("用户取消预约: appointmentId={}, userId={}", appointmentId, userId);
        
        // 获取预约记录
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            log.error("取消预约失败: 预约不存在, appointmentId: {}", appointmentId);
            throw new BusinessException("预约不存在");
        }
        
        // 验证是否为预约用户
        if (!appointment.getUserId().equals(userId)) {
            log.error("取消预约失败: 无权操作, appointmentId: {}, userId: {}", appointmentId, userId);
            throw new BusinessException("无权操作此预约");
        }
        
        // 验证预约状态是否可以取消
        if (AppointmentStatus.COMPLETED.name().equals(appointment.getStatus())) {
            log.error("取消预约失败: 已完成的预约不能取消, appointmentId: {}", appointmentId);
            throw new BusinessException("已完成的预约不能取消");
        }
        
        // 更新状态为已取消
        appointment.setStatus(AppointmentStatus.CANCELED.name());
        appointment.setUpdateTime(LocalDateTime.now());
        
        int result = appointmentMapper.updateById(appointment);
        if (result <= 0) {
            log.error("取消预约失败: 数据库更新失败, appointment: {}", appointment);
            throw new BusinessException("取消预约失败");
        }
        
        log.info("取消预约成功: appointmentId={}", appointmentId);
        
        // 发送通知给房东
        try {
            // 构建WebSocket消息
            WebSocketMessage message = WebSocketMessage.builder()
                    .type(WebSocketType.APPOINTMENT_CANCELED.name())
                    .senderId(userId)
                    .receiverId(appointment.getLandlordId())
                    .content("用户取消了预约")
                    .data(convertToDTO(appointment))
                    .timestamp(LocalDateTime.now())
                    .build();
            
            // 发送WebSocket消息
            webSocketService.sendMessage(appointment.getLandlordId().toString(), message);
            
            log.info("发送预约取消通知成功: landlordId={}", appointment.getLandlordId());
        } catch (Exception e) {
            log.error("发送预约取消通知失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 将实体转换为DTO
     */
    private AppointmentDTO convertToDTO(Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        
        AppointmentDTO dto = new AppointmentDTO();
        BeanUtils.copyProperties(appointment, dto);
        
        // 获取房源信息
        House house = houseMapper.selectById(appointment.getHouseId());
        if (house != null) {
            dto.setHouseTitle(house.getTitle());
            dto.setHouseCoverImage(house.getCoverImage());
            dto.setHouseImage(house.getCoverImage());
            dto.setHouseAddress(house.getAddress());
            dto.setHouseRent(house.getPrice());
            dto.setHouseDeleted(false);
        } else {
            // 房源不存在或已删除
            dto.setHouseDeleted(true);
        }
        
        // 获取用户信息
        User user = userMapper.selectById(appointment.getUserId());
        if (user != null) {
            dto.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
            dto.setUserAvatar(user.getAvatar());
        }
        
        // 获取房东信息
        User landlord = userMapper.selectById(appointment.getLandlordId());
        if (landlord != null) {
            dto.setLandlordName(landlord.getNickname() != null ? landlord.getNickname() : landlord.getUsername());
            dto.setLandlordAvatar(landlord.getAvatar());
            dto.setLandlordPhone(landlord.getPhone());
        }
        
        return dto;
    }

    /**
     * 统计房东待处理预约数量（状态为待确认的预约）
     * @param landlordId 房东ID
     * @return 待处理预约数量
     */
    @Override
    public int countLandlordPendingAppointments(Long landlordId) {
        QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();
        
        queryWrapper.eq("landlord_id", landlordId)
                   .eq("status", "PENDING")
                   .eq("is_deleted", 0);
        
        return Math.toIntExact(count(queryWrapper));
    }
} 