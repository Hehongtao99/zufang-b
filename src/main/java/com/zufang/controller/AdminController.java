package com.zufang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zufang.common.response.Result;
import com.zufang.dto.HouseApproveDTO;
import com.zufang.dto.HouseInfoDTO;
import com.zufang.dto.HouseEditDTO;
import com.zufang.dto.OrderDTO;
import com.zufang.dto.UserInfoDTO;
import com.zufang.dto.UserRegisterDTO;
import com.zufang.entity.Order;
import com.zufang.entity.User;
import com.zufang.service.HouseService;
import com.zufang.service.OrderService;
import com.zufang.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Autowired
    private HouseService houseService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    /**
     * 管理员获取所有房源列表
     */
    @GetMapping("/houses")
    public Result<Page<HouseInfoDTO>> getAllHouses(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        log.info("管理员获取房源列表请求: pageNum={}, pageSize={}, status={}", pageNum, pageSize, status);
        
        try {
            // 获取当前登录用户信息 - 简化处理，假设已通过拦截器验证了权限
            log.info("开始查询管理员房源列表");
            
            // 获取所有房源列表
            Page<HouseInfoDTO> housePage;
            if (status != null && !status.isEmpty()) {
                // 如果指定了状态，则查询指定状态的房源
                housePage = houseService.getHousesByStatus(status, pageNum, pageSize);
            } else {
                // 否则查询所有房源
                housePage = houseService.getAllHouses(pageNum, pageSize);
            }
            
            log.info("查询成功，总记录数：{}", housePage.getTotal());
            return Result.success(housePage);
        } catch (Exception e) {
            log.error("管理员获取房源列表失败: {}", e.getMessage(), e);
            return Result.fail("获取房源列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 管理员审核房源
     */
    @PostMapping("/houses/approve")
    public Result<Void> approveHouse(@RequestBody HouseApproveDTO approveDTO) {
        try {
            log.info("管理员审核房源, 房源ID: {}, 审核结果: {}", 
                    approveDTO.getHouseId(), approveDTO.getApproved());
            
            // 执行审核操作 - 简化处理，假设已通过拦截器验证了权限
            boolean success = houseService.approveHouse(
                    approveDTO.getHouseId(), 
                    approveDTO.getApproved(), 
                    approveDTO.getReason()
            );
            
            if (success) {
                log.info("审核房源成功, 房源ID: {}", approveDTO.getHouseId());
                return Result.success();
            } else {
                log.warn("审核房源失败, 房源ID: {}", approveDTO.getHouseId());
                return Result.fail("审核失败，房源不存在或状态异常");
            }
        } catch (Exception e) {
            log.error("审核房源失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 管理员仪表盘统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        log.info("获取管理员仪表盘统计数据");
        
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 获取用户总数
            long userCount = 0;
            try {
                userCount = userService.count();
                stats.put("userCount", userCount);
            } catch (Exception e) {
                log.error("获取用户总数失败", e);
                stats.put("userCount", 0);
            }
            
            // 获取房源总数
            long houseCount = 0;
            try {
                houseCount = houseService.count();
                stats.put("houseCount", houseCount);
            } catch (Exception e) {
                log.error("获取房源总数失败", e);
                stats.put("houseCount", 0);
            }
            
            // 获取订单总数和收入统计
            try {
                // 计算所有有效的订单数
                int orderCount = orderService.countAllPaidOrders();
                stats.put("orderCount", orderCount);
                
                // 计算总收入
                BigDecimal totalIncome = orderService.calculateTotalIncome();
                stats.put("income", totalIncome);
                
                // 计算本月收入
                BigDecimal monthlyIncome = orderService.calculateMonthlyIncome();
                stats.put("monthlyIncome", monthlyIncome);
                
                // 计算违约金收入
                BigDecimal penaltyIncome = orderService.calculateTotalPenaltyIncome();
                stats.put("penaltyIncome", penaltyIncome);
                
                // 添加月度收入数据
                List<Map<String, Object>> monthlyData = orderService.getMonthlyIncomeData();
                stats.put("monthlyIncomeData", monthlyData);
            } catch (Exception e) {
                log.error("获取订单统计失败", e);
                stats.put("orderCount", 0);
                stats.put("income", 0);
                stats.put("monthlyIncome", 0);
                stats.put("penaltyIncome", 0);
                stats.put("monthlyIncomeData", new ArrayList<>());
            }
            
            log.info("获取管理员仪表盘统计数据成功");
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取管理员仪表盘统计数据失败", e);
            return Result.fail("获取统计数据失败");
        }
    }
    
    /**
     * 获取指定状态的房源列表
     */
    @GetMapping("/houses/status/{status}")
    public Result<Page<HouseInfoDTO>> getHousesByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取指定状态房源列表, 状态: {}, 页码: {}, 每页数量: {}", status, pageNum, pageSize);
        
        try {
            // 将字符串状态转换为数字状态（如果需要）
            String queryStatus = status;
            if ("PENDING".equals(status)) {
                queryStatus = "0";
            } else if ("APPROVED".equals(status)) {
                queryStatus = "1";
            } else if ("REJECTED".equals(status)) {
                queryStatus = "2";
            } else if ("ONLINE".equals(status)) {
                queryStatus = "3";
            } else if ("OFFLINE".equals(status)) {
                queryStatus = "4";
            } else if ("DELETED".equals(status)) {
                queryStatus = "DELETED";
            }
            
            Page<HouseInfoDTO> housePage = houseService.getHousesByStatus(queryStatus, pageNum, pageSize);
            
            log.info("获取指定状态房源列表成功, 状态: {}, 总数: {}", status, housePage.getTotal());
            return Result.success(housePage);
        } catch (Exception e) {
            log.error("获取指定状态房源列表失败", e);
            return Result.fail("获取房源列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试管理员API
     */
    @GetMapping("/test")
    public Result<Map<String, Object>> testApi() {
        log.info("测试接口被调用");
        Map<String, Object> data = new HashMap<>();
        data.put("message", "管理员API测试成功");
        data.put("timestamp", new Date());
        return Result.success(data);
    }
    
    /**
     * 上架房源
     */
    @PostMapping("/houses/{houseId}/online")
    public Result<Void> onlineHouse(@PathVariable Long houseId) {
        log.info("管理员上架房源, 房源ID: {}", houseId);
        
        try {
            // 简化处理，假设已通过拦截器验证了权限
            
            // 上架房源 - 管理员上架，传null作为用户ID，表示管理员操作
            houseService.onlineHouse(houseId, null);
            
            log.info("上架房源成功, 房源ID: {}", houseId);
            return Result.success();
        } catch (Exception e) {
            log.error("上架房源失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 下架房源
     */
    @PostMapping("/houses/{houseId}/offline")
    public Result<Void> offlineHouse(@PathVariable Long houseId) {
        log.info("管理员下架房源, 房源ID: {}", houseId);
        
        try {
            // 简化处理，假设已通过拦截器验证了权限
            
            // 下架房源 - 管理员下架，传null作为用户ID，表示管理员操作
            houseService.offlineHouse(houseId, null);
            
            log.info("下架房源成功, 房源ID: {}", houseId);
            return Result.success();
        } catch (Exception e) {
            log.error("下架房源失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    public Result<Page<UserInfoDTO>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        log.info("管理员获取用户列表请求, 页码: {}, 每页数量: {}, 搜索词: {}, 角色: {}, 状态: {}", 
                page, size, query, role, status);
        
        try {
            Page<UserInfoDTO> userPage = userService.getUserList(page, size, query, role, status);
            log.info("获取用户列表成功, 总数: {}", userPage.getTotal());
            return Result.success(userPage);
        } catch (Exception e) {
            log.error("获取用户列表失败: {}", e.getMessage(), e);
            return Result.fail("获取用户列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/users/{userId}")
    public Result<UserInfoDTO> getUserDetail(@PathVariable Long userId) {
        log.info("获取用户详情, 用户ID: {}", userId);
        
        try {
            UserInfoDTO userInfo = userService.getUserById(userId);
            log.info("获取用户详情成功, 用户ID: {}", userId);
            return Result.success(userInfo);
        } catch (Exception e) {
            log.error("获取用户详情失败: {}", e.getMessage(), e);
            return Result.fail("获取用户详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 新增用户
     */
    @PostMapping("/users")
    public Result<Long> addUser(@RequestBody User user) {
        log.info("新增用户请求, 用户名: {}", user.getUsername());
        
        try {
            // 使用现有的注册接口
            UserRegisterDTO registerDTO = new UserRegisterDTO();
            registerDTO.setUsername(user.getUsername());
            registerDTO.setPassword(user.getPassword());
            registerDTO.setConfirmPassword(user.getPassword()); // 管理员添加用户，密码默认一致
            registerDTO.setNickname(user.getNickname());
            registerDTO.setRealName(user.getRealName());
            registerDTO.setPhone(user.getPhone());
            registerDTO.setEmail(user.getEmail());
            registerDTO.setRole(user.getRole());
            
            Long userId = userService.register(registerDTO);
            
            // 如果注册成功后需要更新状态
            if (user.getStatus() != null && !"ACTIVE".equals(user.getStatus())) {
                userService.updateUserStatus(userId, user.getStatus());
            }
            
            log.info("新增用户成功, 用户ID: {}", userId);
            return Result.success(userId);
        } catch (Exception e) {
            log.error("新增用户失败: {}", e.getMessage(), e);
            return Result.fail("新增用户失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/users/{userId}")
    public Result<Void> updateUser(@PathVariable Long userId, @RequestBody User user) {
        log.info("更新用户信息, 用户ID: {}", userId);
        
        try {
            // 获取原用户信息
            UserInfoDTO oldUser = userService.getUserById(userId);
            if (oldUser == null) {
                return Result.fail("用户不存在");
            }
            
            // 设置用户ID
            user.setId(userId);
            
            // 将 User 对象转换为 UserInfoDTO
            UserInfoDTO userInfoDTO = new UserInfoDTO();
            userInfoDTO.setId(userId);
            userInfoDTO.setUsername(user.getUsername());
            userInfoDTO.setNickname(user.getNickname());
            userInfoDTO.setPhone(user.getPhone());
            userInfoDTO.setEmail(user.getEmail());
            userInfoDTO.setAvatar(user.getAvatar());
            userInfoDTO.setRole(user.getRole());
            userInfoDTO.setRealName(user.getRealName());
            userInfoDTO.setIdCard(user.getIdCard());
            userInfoDTO.setDescription(user.getDescription());
            
            // 更新用户基本信息
            boolean updateSuccess = userService.updateUserInfo(userInfoDTO);
            if (!updateSuccess) {
                return Result.fail("更新用户信息失败");
            }
            
            // 如果状态发生变化，更新状态
            if (user.getStatus() != null && !user.getStatus().equals(oldUser.getStatus())) {
                boolean statusSuccess = userService.updateUserStatus(userId, user.getStatus());
                if (!statusSuccess) {
                    return Result.fail("更新用户状态失败");
                }
            }
            
            log.info("更新用户信息成功, 用户ID: {}", userId);
            return Result.success();
        } catch (Exception e) {
            log.error("更新用户信息失败: {}", e.getMessage(), e);
            return Result.fail("更新用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新用户状态
     */
    @PutMapping("/users/{userId}/status")
    public Result<Void> updateUserStatus(
            @PathVariable Long userId, 
            @RequestBody Map<String, String> statusMap) {
        String status = statusMap.get("status");
        log.info("更新用户状态, 用户ID: {}, 新状态: {}", userId, status);
        
        try {
            boolean success = userService.updateUserStatus(userId, status);
            
            if (success) {
                log.info("更新用户状态成功, 用户ID: {}", userId);
                return Result.success();
            } else {
                log.warn("更新用户状态失败, 用户ID: {}", userId);
                return Result.fail("更新用户状态失败");
            }
        } catch (Exception e) {
            log.error("更新用户状态失败: {}", e.getMessage(), e);
            return Result.fail("更新用户状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/users/{userId}")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        log.info("删除用户, 用户ID: {}", userId);
        
        try {
            boolean success = userService.deleteUser(userId);
            
            if (success) {
                log.info("删除用户成功, 用户ID: {}", userId);
                return Result.success();
            } else {
                log.warn("删除用户失败, 用户ID: {}", userId);
                return Result.fail("删除用户失败");
            }
        } catch (Exception e) {
            log.error("删除用户失败: {}", e.getMessage(), e);
            return Result.fail("删除用户失败: " + e.getMessage());
        }
    }
    
    /**
     * 重置用户密码
     */
    @PostMapping("/users/{userId}/reset-password")
    public Result<Void> resetPassword(
            @PathVariable Long userId, 
            @RequestBody Map<String, String> passwordMap) {
        String password = passwordMap.get("password");
        log.info("重置用户密码, 用户ID: {}", userId);
        
        try {
            boolean success = userService.resetPassword(userId, password);
            
            if (success) {
                log.info("重置用户密码成功, 用户ID: {}", userId);
                return Result.success();
            } else {
                log.warn("重置用户密码失败, 用户ID: {}", userId);
                return Result.fail("重置用户密码失败");
            }
        } catch (Exception e) {
            log.error("重置用户密码失败: {}", e.getMessage(), e);
            return Result.fail("重置用户密码失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取订单列表
     */
    @GetMapping("/orders")
    public Result<Page<OrderDTO>> getOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        log.info("管理员获取订单列表请求, 页码: {}, 每页数量: {}, 状态: {}", page, size, status);
        
        try {
            // 创建分页对象
            Page<Order> orderPage = new Page<>(page, size);
            
            // 调用服务获取订单分页数据
            Page<OrderDTO> result;
            if (status != null && !status.isEmpty()) {
                // 支持多状态查询，以逗号分隔
                String[] statuses = status.split(",");
                result = orderService.pageOrdersByStatuses(orderPage, Arrays.asList(statuses));
            } else {
                result = orderService.pageOrders(orderPage, null);
            }
            
            log.info("获取订单列表成功, 总数: {}", result.getTotal());
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取订单列表失败: {}", e.getMessage(), e);
            return Result.fail("获取订单列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/orders/{orderId}")
    public Result<OrderDTO> getOrderDetail(@PathVariable Long orderId) {
        log.info("管理员获取订单详情请求, 订单ID: {}", orderId);
        
        try {
            OrderDTO orderDTO = orderService.getOrderById(orderId);
            
            if (orderDTO != null) {
                log.info("获取订单详情成功, 订单ID: {}", orderId);
                return Result.success(orderDTO);
            } else {
                log.warn("获取订单详情失败, 订单不存在, 订单ID: {}", orderId);
                return Result.fail("订单不存在");
            }
        } catch (Exception e) {
            log.error("获取订单详情失败: {}", e.getMessage(), e);
            return Result.fail("获取订单详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取订单状态统计
     */
    @GetMapping("/orders/status-stats")
    public Result<Map<String, Integer>> getOrderStatusStats() {
        log.info("获取订单状态统计");
        
        try {
            // 查询所有订单
            List<Order> allOrders = orderService.list();
            
            Map<String, Integer> statusStats = new HashMap<>();
            Map<String, BigDecimal> statusAmounts = new HashMap<>();
            
            // 统计每种状态的订单数量和金额总和
            for (Order order : allOrders) {
                String status = order.getStatus();
                if (status == null) status = "NULL";
                
                // 更新数量统计
                statusStats.put(status, statusStats.getOrDefault(status, 0) + 1);
                
                // 更新金额统计
                BigDecimal currentAmount = statusAmounts.getOrDefault(status, BigDecimal.ZERO);
                statusAmounts.put(status, currentAmount.add(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO));
            }
            
            // 记录日志以便查看
            log.info("订单状态统计结果:");
            for (Map.Entry<String, Integer> entry : statusStats.entrySet()) {
                log.info("状态: '{}', 数量: {}, 金额总和: {}", 
                        entry.getKey(), entry.getValue(), statusAmounts.get(entry.getKey()));
            }
            
            log.info("获取订单状态统计成功");
            return Result.success(statusStats);
        } catch (Exception e) {
            log.error("获取订单状态统计失败: {}", e.getMessage(), e);
            return Result.fail("获取订单状态统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 修复订单和房源状态不一致的问题
     */
    @PostMapping("/admin/fix-status")
    public Result<Map<String, Object>> fixInconsistentStatus(HttpServletRequest request) {
        try {
            // 获取当前管理员ID
            Long adminId = (Long) request.getAttribute("userId");
            String role = (String) request.getAttribute("role");
            
            // 验证是否为管理员
            if (!"ADMIN".equals(role)) {
                log.warn("非管理员尝试执行状态修复: userId={}, role={}", adminId, role);
                return Result.fail("只有管理员才能执行此操作");
            }
            
            log.info("管理员执行状态不一致修复: adminId={}", adminId);
            
            // 调用服务执行修复
            int fixedCount = orderService.checkAndFixInconsistentStatus();
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("fixedCount", fixedCount);
            result.put("success", true);
            result.put("timestamp", LocalDateTime.now().toString());
            
            log.info("状态不一致修复完成: adminId={}, fixedCount={}", adminId, fixedCount);
            return Result.success(result);
        } catch (Exception e) {
            log.error("修复状态不一致失败: {}", e.getMessage(), e);
            return Result.fail("修复状态不一致失败: " + e.getMessage());
        }
    }
    
    /**
     * 管理员编辑房源
     */
    @PostMapping("/houses/edit")
    public Result<Boolean> editHouse(@RequestPart(value = "data") HouseEditDTO editDTO,
                                    @RequestPart(value = "coverImage", required = false) MultipartFile coverImageFile,
                                    @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles) {
        try {
            log.info("管理员编辑房源, 房源ID: {}", editDTO.getId());
            
            // 设置文件
            if (coverImageFile != null && !coverImageFile.isEmpty()) {
                editDTO.setCoverImageFile(coverImageFile);
            }
            
            if (imageFiles != null && !imageFiles.isEmpty()) {
                editDTO.setImageFiles(imageFiles);
            }
            
            // 编辑房源
            boolean success = houseService.adminEditHouse(editDTO);
            
            if (success) {
                log.info("管理员编辑房源成功, 房源ID: {}", editDTO.getId());
                return Result.success(true);
            } else {
                log.warn("管理员编辑房源失败, 房源ID: {}", editDTO.getId());
                return Result.fail("编辑房源失败，房源可能不存在");
            }
        } catch (Exception e) {
            log.error("管理员编辑房源失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 获取房源图片列表
     */
    @GetMapping("/houses/{houseId}/images")
    public Result<List<String>> getHouseImages(@PathVariable Long houseId) {
        try {
            log.info("管理员获取房源图片列表, 房源ID: {}", houseId);
            
            // 获取房源图片列表
            List<String> images = houseService.getHouseImages(houseId);
            
            log.info("管理员获取房源图片列表成功, 房源ID: {}, 图片数量: {}", houseId, images.size());
            return Result.success(images);
        } catch (Exception e) {
            log.error("管理员获取房源图片列表失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 管理员删除房源
     */
    @DeleteMapping("/houses/{houseId}")
    public Result<Void> deleteHouse(@PathVariable Long houseId) {
        try {
            log.info("管理员删除房源, 房源ID: {}", houseId);
            
            // 管理员删除房源，传null作为用户ID，表示管理员操作可以绕过权限检查
            houseService.deleteHouse(houseId, null);
            
            log.info("管理员删除房源成功, 房源ID: {}", houseId);
            return Result.success();
        } catch (Exception e) {
            log.error("管理员删除房源失败: {}, 房源ID: {}", e.getMessage(), houseId);
            return Result.fail(e.getMessage());
        }
    }
} 