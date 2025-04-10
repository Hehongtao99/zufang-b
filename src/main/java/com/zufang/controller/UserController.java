package com.zufang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zufang.common.exception.BusinessException;
import com.zufang.common.util.JwtUtil;
import com.zufang.common.response.Result;
import com.zufang.dto.HouseInfoDTO;
import com.zufang.dto.UserInfoDTO;
import com.zufang.dto.UserLoginDTO;
import com.zufang.dto.UserRegisterDTO;
import com.zufang.dto.AppointmentDTO;
import com.zufang.service.OrderService;
import com.zufang.service.UserService;
import com.zufang.service.AppointmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private AppointmentService appointmentService;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid UserLoginDTO loginDTO) {
        try {
            log.info("用户登录: {}", loginDTO.getUsername());
            
            // 登录
            Map<String, Object> loginResult = userService.login(loginDTO);
            String token = (String) loginResult.get("token");
            Long userId = (Long) loginResult.get("userId");
            String role = (String) loginResult.get("role");
            
            // 返回登录信息
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            map.put("userId", userId);
            map.put("userRole", role);
            
            log.info("用户登录成功: {}, 角色: {}", loginDTO.getUsername(), role);
            return Result.success(map);
        } catch (Exception e) {
            log.error("用户登录失败: {}, 错误: {}", loginDTO.getUsername(), e.getMessage());
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Long> register(@RequestBody @Valid UserRegisterDTO registerDTO) {
        try {
            log.info("用户注册: {}, 角色: {}", registerDTO.getUsername(), registerDTO.getRole());
            
            // 注册
            Long userId = userService.register(registerDTO);
            
            log.info("用户注册成功: {}, ID: {}", registerDTO.getUsername(), userId);
            return Result.success(userId);
        } catch (Exception e) {
            log.error("用户注册失败: {}, 错误: {}", registerDTO.getUsername(), e.getMessage());
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    public Result<UserInfoDTO> getUserInfo(HttpServletRequest request) {
        try {
            log.info("开始获取用户信息");
            
            // 从request属性中获取userId
            Object userIdObj = request.getAttribute("userId");
            log.info("从request属性中获取userId: {}", userIdObj);
            
            if (userIdObj == null) {
                log.error("无法获取userId, token: {}", request.getHeader("token"));
                return Result.error("用户未登录");
            }
            
            Long userId = Long.valueOf(userIdObj.toString());
            log.info("使用request属性中的userId: {}", userId);
            
            // 获取用户信息
            UserInfoDTO userInfo = userService.getUserInfo(userId);
            log.info("获取用户信息成功: {}", userInfo);
            return Result.success(userInfo);
        } catch (BusinessException e) {
            log.error("获取用户信息失败, 错误: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("获取用户信息失败, 错误: {}", e.getMessage(), e);
            return Result.error("获取用户信息失败");
        }
    }

    /**
     * 获取用户已租房源列表
     */
    @GetMapping("/houses")
    public Result<Page<HouseInfoDTO>> getUserHouses(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        try {
            // 从request中获取用户ID
            Long userId = (Long) request.getAttribute("userId");
            log.info("获取用户已租房源列表, 用户ID: {}", userId);
            
            // 创建分页参数
            Page<HouseInfoDTO> page = new Page<>(pageNum, pageSize);
            
            // 获取用户已租房源列表
            page = orderService.getUserRentedHouses(page, userId);
            
            log.info("获取用户已租房源列表成功, 用户ID: {}, 房源数: {}", userId, page.getTotal());
            return Result.success(page);
        } catch (Exception e) {
            log.error("获取用户已租房源列表失败: {}", e.getMessage(), e);
            return Result.fail("获取房源列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户预约列表
     */
    @GetMapping("/appointments")
    public Result<Page<AppointmentDTO>> getUserAppointments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        try {
            // 从request中获取用户ID
            Long userId = (Long) request.getAttribute("userId");
            log.info("获取用户预约列表, 用户ID: {}, 页码: {}, 每页大小: {}, 状态过滤: {}", userId, page, size, status);
            
            if (userId == null) {
                log.error("获取用户预约列表失败: 用户ID为空");
                return Result.fail("用户未登录");
            }
            
            // 创建分页参数
            Page<AppointmentDTO> pageParam = new Page<>(page, size);
            
            // 获取用户预约列表
            pageParam = appointmentService.getUserAppointments(pageParam, userId, status);
            
            log.info("获取用户预约列表成功, 用户ID: {}, 总数: {}", userId, pageParam.getTotal());
            return Result.success(pageParam);
        } catch (Exception e) {
            log.error("获取用户预约列表失败: {}", e.getMessage(), e);
            return Result.fail("获取预约列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 取消预约
     */
    @PostMapping("/appointments/{id}/cancel")
    public Result<?> cancelAppointment(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 从request中获取用户ID
            Long userId = (Long) request.getAttribute("userId");
            log.info("取消预约, 用户ID: {}, 预约ID: {}", userId, id);
            
            if (userId == null) {
                log.error("取消预约失败: 用户ID为空");
                return Result.fail("用户未登录");
            }
            
            // 取消预约
            appointmentService.cancelAppointment(id, userId);
            
            log.info("取消预约成功, 用户ID: {}, 预约ID: {}", userId, id);
            return Result.success();
        } catch (BusinessException e) {
            log.error("取消预约业务异常: {}", e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("取消预约失败: {}", e.getMessage(), e);
            return Result.fail("取消预约失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    public Result<?> updateUserInfo(@RequestBody UserInfoDTO userInfoDTO, HttpServletRequest request) {
        try {
            // 从request中获取用户ID
            Long userId = (Long) request.getAttribute("userId");
            log.info("更新用户信息, 用户ID: {}", userId);
            
            if (userId == null) {
                // 尝试从DTO中获取userId
                userId = userInfoDTO.getId();
                if (userId == null) {
                    log.error("更新用户信息失败: 用户ID为空");
                    return Result.fail("用户未登录");
                }
            }
            
            // 确保设置了正确的用户ID
            userInfoDTO.setId(userId);
            
            // 更新用户信息
            userService.updateUserInfo(userInfoDTO);
            
            log.info("更新用户信息成功, 用户ID: {}", userId);
            return Result.success();
        } catch (BusinessException e) {
            log.error("更新用户信息业务异常: {}", e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("更新用户信息失败: {}", e.getMessage(), e);
            return Result.fail("更新用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<?> changePassword(@RequestBody Map<String, String> passwordMap, HttpServletRequest request) {
        try {
            // 从request中获取用户ID
            Long userId = (Long) request.getAttribute("userId");
            log.info("修改密码, 用户ID: {}", userId);
            
            if (userId == null) {
                log.error("修改密码失败: 用户ID为空");
                return Result.fail("用户未登录");
            }
            
            // 获取密码参数
            String oldPassword = passwordMap.get("oldPassword");
            String newPassword = passwordMap.get("newPassword");
            
            if (oldPassword == null || newPassword == null) {
                log.error("修改密码失败: 参数不完整");
                return Result.fail("请提供原密码和新密码");
            }
            
            // 修改密码
            userService.changePassword(userId, oldPassword, newPassword);
            
            log.info("修改密码成功, 用户ID: {}", userId);
            return Result.success();
        } catch (BusinessException e) {
            log.error("修改密码业务异常: {}", e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("修改密码失败: {}", e.getMessage(), e);
            return Result.fail("修改密码失败: " + e.getMessage());
        }
    }
} 