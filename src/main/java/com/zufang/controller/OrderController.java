package com.zufang.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zufang.common.exception.BusinessException;
import com.zufang.common.response.Result;
import com.zufang.dto.BookingDTO;
import com.zufang.dto.OrderDTO;
import com.zufang.dto.PaymentDTO;
import com.zufang.dto.TerminateDTO;
import com.zufang.entity.Order;
import com.zufang.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单控制器
 */
@RestController
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;
    
    /**
     * 用户创建订单（订房）
     */
    @PostMapping("/user/orders/book")
    public Result<Long> createOrder(@RequestBody BookingDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("用户创建订单: userId={}, houseId={}", userId, dto.getHouseId());
        Long orderId = orderService.createOrder(dto, userId);
        return Result.success(orderId);
    }
    
    /**
     * 用户支付订单
     */
    @PostMapping("/user/orders/{id}/pay")
    public Result<Boolean> payOrder(
            @PathVariable Long id,
            @RequestBody PaymentDTO dto, 
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("用户支付订单: userId={}, orderId={}", userId, id);
        
        // 设置订单ID
        dto.setOrderId(id);
        
        boolean result = orderService.payOrder(dto, userId);
        return Result.success(result);
    }
    
    /**
     * 用户取消订单
     */
    @PostMapping("/user/orders/{id}/cancel")
    public Result<Boolean> cancelOrder(
            @PathVariable Long id, 
            @RequestParam(required = false) String reason,
            @RequestBody(required = false) Map<String, String> requestBody,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        
        // 从RequestParam或RequestBody中获取reason
        String cancelReason = reason;
        if (cancelReason == null && requestBody != null && requestBody.containsKey("reason")) {
            cancelReason = requestBody.get("reason");
        }
        
        // 如果仍然没有获取到reason，则使用默认值
        if (cancelReason == null) {
            cancelReason = "用户主动取消订单";
        }
        
        log.info("用户取消订单: userId={}, orderId={}, reason={}", userId, id, cancelReason);
        boolean result = orderService.cancelOrder(id, userId, cancelReason);
        return Result.success(result);
    }
    
    /**
     * 取消支付
     */
    @PostMapping("/user/orders/{id}/cancelPayment")
    public Result<Boolean> cancelPayment(
            @PathVariable Long id, 
            @RequestParam(required = false) String reason,
            @RequestBody(required = false) Map<String, String> requestBody,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        
        // 从RequestParam或RequestBody中获取reason
        String cancelReason = reason;
        if (cancelReason == null && requestBody != null && requestBody.containsKey("reason")) {
            cancelReason = requestBody.get("reason");
        }
        
        // 如果仍然没有获取到reason，则使用默认值
        if (cancelReason == null) {
            cancelReason = "用户取消支付";
        }
        
        log.info("用户取消支付: userId={}, orderId={}, reason={}", userId, id, cancelReason);
        boolean result = orderService.cancelPayment(id, userId, cancelReason);
        return Result.success(result);
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/orders/{id}")
    public Result<OrderDTO> getOrder(@PathVariable Long id) {
        log.info("获取订单详情: {}", id);
        OrderDTO dto = orderService.getOrder(id);
        return Result.success(dto);
    }
    
    /**
     * 用户获取单个订单详情
     */
    @GetMapping("/user/orders/{id}")
    public Result<OrderDTO> getUserOrder(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("用户获取订单详情: userId={}, orderId={}", userId, id);
        OrderDTO dto = orderService.getOrder(id);
        // 验证订单归属
        if (dto != null && !dto.getUserId().equals(userId)) {
            return Result.error("无权查看该订单");
        }
        return Result.success(dto);
    }
    
    /**
     * 管理员获取所有订单
     */
    @GetMapping("/admin/orders/list")
    public Result<Page<OrderDTO>> adminPageOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        log.info("管理员分页查询订单: page={}, size={}, status={}", page, size, status);
        Page<Order> pageParam = new Page<>(page, size);
        Page<OrderDTO> result = orderService.pageOrders(pageParam, status);
        return Result.success(result);
    }
    
    /**
     * 房东获取自己的订单
     */
    @GetMapping("/landlord/orders")
    public Result<Page<OrderDTO>> landlordPageOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        // 从请求中获取当前登录的房东ID
        Long landlordId = (Long) request.getAttribute("userId");
        log.info("房东分页查询订单: landlordId={}, page={}, size={}, status={}", landlordId, page, size, status);
        
        Page<Order> pageParam = new Page<>(page, size);
        Page<OrderDTO> result = orderService.pageLandlordOrders(pageParam, landlordId, status);
        return Result.success(result);
    }
    
    /**
     * 用户获取自己的订单
     */
    @GetMapping("/user/orders")
    public Result<Page<OrderDTO>> userPageOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        // 从请求中获取当前登录的用户ID
        Long userId = (Long) request.getAttribute("userId");
        log.info("用户分页查询订单: userId={}, page={}, size={}, status={}", userId, page, size, status);
        
        Page<Order> pageParam = new Page<>(page, size);
        Page<OrderDTO> result = orderService.pageUserOrders(pageParam, userId, status);
        return Result.success(result);
    }
    
    /**
     * 房东查看订单详情
     */
    @GetMapping("/landlord/orders/{id}")
    public Result<OrderDTO> getLandlordOrderDetail(@PathVariable Long id, HttpServletRequest request) {
        try {
            log.info("查询房东订单详情: orderId={}", id);
            
            // 获取当前登录的房东ID
            Object landlordIdObj = request.getAttribute("userId");
            if (landlordIdObj == null) {
                log.warn("房东未登录，无法查看订单详情");
                return Result.error("请先登录");
            }
            
            Long landlordId;
            try {
                landlordId = Long.parseLong(landlordIdObj.toString());
            } catch (NumberFormatException e) {
                log.error("房东ID类型转换错误: {}", e.getMessage());
                return Result.error("用户身份异常");
            }
            
            log.info("房东查询订单详情: orderId={}, landlordId={}", id, landlordId);
            
            // 获取订单详情
            OrderDTO orderDTO = orderService.getOrderById(id);
            if (orderDTO == null) {
                log.warn("订单不存在: orderId={}", id);
                return Result.error("订单不存在");
            }
            
            // 验证订单是否属于当前房东
            if (!landlordId.equals(orderDTO.getLandlordId())) {
                log.warn("订单不属于当前房东: orderId={}, requestLandlordId={}, orderLandlordId={}", 
                        id, landlordId, orderDTO.getLandlordId());
                return Result.error("您无权查看此订单");
            }
            
            return Result.success(orderDTO);
        } catch (Exception e) {
            log.error("查询房东订单详情异常: orderId={}, error={}", id, e.getMessage(), e);
            return Result.error("获取订单详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户申请退租
     */
    @PostMapping("/user/orders/{id}/terminate")
    public Result<Boolean> terminateRental(
            @PathVariable Long id,
            @RequestBody TerminateDTO dto,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("用户申请退租: userId={}, orderId={}, 传入的DTO={}, 参数ID={}", 
                userId, id, dto, id);
        
        // 确保DTO中的订单ID与路径参数一致
        dto.setOrderId(id);
        
        try {
            boolean result = orderService.applyTerminate(dto, userId);
            return Result.success(result);
        } catch (BusinessException e) {
            log.error("业务异常: {}", e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("系统异常: {}", e.getMessage(), e);
            return Result.fail("申请退租失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户支付违约金
     */
    @PostMapping("/user/orders/{id}/payPenalty")
    public Result<Boolean> payPenalty(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> requestBody,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("用户支付违约金: userId={}, orderId={}", userId, id);
        
        // 获取支付方式
        String payMethod = null;
        if (requestBody != null && requestBody.containsKey("payMethod")) {
            payMethod = (String) requestBody.get("payMethod");
        }
        
        try {
            // 调用服务层支付违约金方法
            boolean result = orderService.payOrderPenalty(id, userId, payMethod);
            return Result.success(result);
        } catch (BusinessException e) {
            log.error("业务异常: {}", e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("系统异常: {}", e.getMessage(), e);
            return Result.fail("支付违约金失败: " + e.getMessage());
        }
    }
    
    /**
     * 房东处理退租申请
     */
    @PostMapping("/landlord/orders/handleTerminate")
    public Result<Boolean> handleTerminateRequest(
            @RequestBody Map<String, Object> requestParams,
            HttpServletRequest request) {
        try {
            Long landlordId = (Long) request.getAttribute("userId");
            if (landlordId == null) {
                log.error("房东ID为空");
                return Result.fail("请先登录");
            }

            // 从请求体中获取参数
            if (requestParams == null || !requestParams.containsKey("orderId")) {
                log.error("请求参数中缺少orderId");
                return Result.fail("请求参数不完整：缺少订单ID");
            }
            
            Object orderIdObj = requestParams.get("orderId");
            if (orderIdObj == null) {
                log.error("orderId参数为null");
                return Result.fail("订单ID不能为空");
            }
            
            Long orderId;
            try {
                orderId = Long.valueOf(orderIdObj.toString());
            } catch (NumberFormatException e) {
                log.error("订单ID格式错误: {}", orderIdObj);
                return Result.fail("订单ID格式错误");
            }
            
            Boolean approved = (Boolean) requestParams.get("approved");
            if (approved == null) {
                log.error("approved参数为null");
                return Result.fail("请指定是否批准退租");
            }
            
            String rejectReason = requestParams.get("rejectReason") != null ? requestParams.get("rejectReason").toString() : null;
            String actualTerminateDateStr = requestParams.get("actualTerminateDateStr") != null ? requestParams.get("actualTerminateDateStr").toString() : null;
            Object penaltyAmountObj = requestParams.get("penaltyAmount");
            String remark = requestParams.get("remark") != null ? requestParams.get("remark").toString() : null;

            // 记录详细的参数信息用于调试
            log.info("处理退租请求参数: orderId={}, landlordId={}, approved={}, rejectReason={}, actualTerminateDateStr={}, penaltyAmountObj={}, remark={}",
                    orderId, landlordId, approved, rejectReason, actualTerminateDateStr, penaltyAmountObj, remark);

            LocalDate actualTerminateDate = null;
            if (StringUtils.hasText(actualTerminateDateStr)) {
                try {
                    actualTerminateDate = LocalDate.parse(actualTerminateDateStr);
                } catch (Exception e) {
                    log.error("实际退租日期格式错误: {}", actualTerminateDateStr, e);
                    return Result.fail("实际退租日期格式错误");
                }
            }

            // 转换违约金金额
            BigDecimal penaltyAmount = null;
            if (penaltyAmountObj != null) {
                if (penaltyAmountObj instanceof Number) {
                    penaltyAmount = new BigDecimal(penaltyAmountObj.toString());
                } else if (penaltyAmountObj instanceof String) {
                    penaltyAmount = new BigDecimal((String) penaltyAmountObj);
                }
            }

            boolean result;
            if (approved && (actualTerminateDate != null || penaltyAmount != null || StringUtils.hasText(remark))) {
                // 使用带详细参数的方法
                result = orderService.handleTerminateRequest(
                        orderId, landlordId, approved, rejectReason,
                        actualTerminateDate, penaltyAmount, remark);
            } else {
                // 使用原有方法
                result = orderService.handleTerminateRequest(orderId, landlordId, approved, rejectReason);
            }

            if (result) {
                return Result.ok(true);
            } else {
                return Result.fail("处理退租请求失败");
            }
        } catch (BusinessException e) {
            log.error("处理退租请求业务异常: {}", e.getMessage());
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("处理退租请求异常", e);
            return Result.fail("处理退租请求失败");
        }
    }
    
    /**
     * 确认退租完成
     */
    @PostMapping("/landlord/orders/{id}/confirmTermination")
    public Result<Boolean> confirmTermination(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long landlordId = (Long) request.getAttribute("userId");
        log.info("房东确认退租: landlordId={}, orderId={}", landlordId, id);
        
        boolean result = orderService.confirmTermination(id, landlordId);
        return Result.success(result);
    }
    
    /**
     * 获取房东退租申请列表
     */
    @GetMapping("/landlord/terminates")
    public Result<Map<String, Object>> getLandlordTerminates(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        // 从请求中获取当前登录的房东ID
        Long landlordId = (Long) request.getAttribute("userId");
        log.info("获取房东退租申请列表: landlordId={}, page={}, size={}", landlordId, page, size);
        
        try {
            // 构建分页对象
            Page<Order> orderPage = new Page<>(page, size);
            
            // 获取退租申请列表，使用字符串常量表示状态
            Page<OrderDTO> dtoPage = orderService.pageLandlordOrders(orderPage, landlordId, "TERMINATE_REQUESTED");
            
            // 封装返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("records", dtoPage.getRecords());
            result.put("total", dtoPage.getTotal());
            result.put("size", dtoPage.getSize());
            result.put("current", dtoPage.getCurrent());
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取房东退租申请列表失败: landlordId={}, error={}", landlordId, e.getMessage(), e);
            return Result.fail("获取退租申请列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取单个退租申请详情
     * 用于房东查看特定退租申请的详细信息
     */
    @GetMapping("/landlord/terminates/{id}")
    public Result<OrderDTO> getTerminateDetail(@PathVariable Long id, HttpServletRequest request) {
        try {
            log.info("查询退租申请详情: orderId={}", id);
            
            // 获取当前登录的房东ID
            Object landlordIdObj = request.getAttribute("userId");
            if (landlordIdObj == null) {
                log.warn("房东未登录，无法查看退租申请详情");
                return Result.error("请先登录");
            }
            
            Long landlordId;
            try {
                landlordId = Long.parseLong(landlordIdObj.toString());
            } catch (NumberFormatException e) {
                log.error("房东ID类型转换错误: {}", e.getMessage());
                return Result.error("用户身份异常");
            }
            
            log.info("房东查询退租申请详情: orderId={}, landlordId={}", id, landlordId);
            
            // 获取订单详情
            OrderDTO orderDTO = orderService.getOrderById(id);
            if (orderDTO == null) {
                log.warn("订单不存在: orderId={}", id);
                return Result.error("订单不存在");
            }
            
            // 验证订单是否属于当前房东
            if (!landlordId.equals(orderDTO.getLandlordId())) {
                log.warn("订单不属于当前房东: orderId={}, requestLandlordId={}, orderLandlordId={}", 
                        id, landlordId, orderDTO.getLandlordId());
                return Result.error("您无权查看此订单");
            }
            
            // 验证是否是退租申请
            if (!"TERMINATE_REQUESTED".equals(orderDTO.getStatus()) && 
                !"TERMINATE_APPROVED".equals(orderDTO.getStatus()) && 
                !"TERMINATE_REJECTED".equals(orderDTO.getStatus()) && 
                !"TERMINATED".equals(orderDTO.getStatus())) {
                
                log.warn("订单不是退租申请: orderId={}, status={}", id, orderDTO.getStatus());
                return Result.fail("此订单不是退租申请");
            }
            
            return Result.success(orderDTO);
        } catch (Exception e) {
            log.error("查询退租申请详情异常: orderId={}, error={}", id, e.getMessage(), e);
            return Result.error("获取退租申请详情失败: " + e.getMessage());
        }
    }
} 