package com.zufang.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zufang.dto.BookingDTO;
import com.zufang.dto.OrderDTO;
import com.zufang.dto.PaymentDTO;
import com.zufang.dto.TerminateDTO;
import com.zufang.entity.Order;
import com.zufang.dto.HouseInfoDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 */
public interface OrderService {
    
    /**
     * 创建订单
     * @param dto 订房信息
     * @param userId 用户ID
     * @return 创建的订单ID
     */
    Long createOrder(BookingDTO dto, Long userId);
    
    /**
     * 支付订单
     * @param dto 支付信息
     * @param userId 用户ID
     * @return 是否支付成功
     */
    boolean payOrder(PaymentDTO dto, Long userId);
    
    /**
     * 取消订单
     * @param id 订单ID
     * @param userId 用户ID
     * @param reason 取消原因
     * @return 是否取消成功
     */
    boolean cancelOrder(Long id, Long userId, String reason);
    
    /**
     * 取消支付
     * @param id 订单ID
     * @param userId 用户ID
     * @param reason 取消原因
     * @return 是否取消成功
     */
    boolean cancelPayment(Long id, Long userId, String reason);
    
    /**
     * 获取订单详情
     * @param id 订单ID
     * @return 订单详情
     */
    OrderDTO getOrder(Long id);
    
    /**
     * 根据ID获取订单信息
     * @param id 订单ID
     * @return 订单详情DTO
     */
    OrderDTO getOrderById(Long id);
    
    /**
     * 管理员分页查询所有订单
     * @param page 分页参数
     * @param status 订单状态（可选）
     * @return 订单分页数据
     */
    Page<OrderDTO> pageOrders(Page<Order> page, String status);
    
    /**
     * 房东分页查询自己的订单
     * @param page 分页参数
     * @param landlordId 房东ID
     * @param status 订单状态（可选）
     * @return 订单分页数据
     */
    Page<OrderDTO> pageLandlordOrders(Page<Order> page, Long landlordId, String status);
    
    /**
     * 用户分页查询自己的订单
     * @param page 分页参数
     * @param userId 用户ID
     * @param status 订单状态（可选）
     * @return 订单分页数据
     */
    Page<OrderDTO> pageUserOrders(Page<Order> page, Long userId, String status);

    /**
     * 计算房东总收入
     * @param landlordId 房东ID
     * @return 总收入
     */
    BigDecimal calculateLandlordTotalIncome(Long landlordId);
    
    /**
     * 计算房东本月收入
     * @param landlordId 房东ID
     * @return 本月收入
     */
    BigDecimal calculateLandlordMonthlyIncome(Long landlordId);
    
    /**
     * 计算房东违约金收入
     * @param landlordId 房东ID
     * @return 违约金收入
     */
    BigDecimal calculateLandlordPenaltyIncome(Long landlordId);
    
    /**
     * 计算房东当前收到的押金总额
     * @param landlordId 房东ID
     * @return 押金总额
     */
    BigDecimal calculateLandlordTotalDeposit(Long landlordId);
    
    /**
     * 计算房东在指定时间段内的收入
     * @param landlordId 房东ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 时间段内的收入
     */
    BigDecimal calculateLandlordIncomeInPeriod(Long landlordId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 统计房东订单总数
     * @param landlordId 房东ID
     * @return 订单总数
     */
    int countLandlordOrders(Long landlordId);
    
    /**
     * 统计房东本月订单数
     * @param landlordId 房东ID
     * @return 本月订单数
     */
    int countLandlordMonthlyOrders(Long landlordId);
    
    /**
     * 统计房东已出租房源数量
     * @param landlordId 房东ID
     * @return 已出租房源数量
     */
    int countLandlordRentedHouses(Long landlordId);
    
    /**
     * 统计房东待处理的退租申请数量
     * @param landlordId 房东ID
     * @return 退租申请数量
     */
    int countLandlordTerminateRequests(Long landlordId);
    
    /**
     * 获取房东收入明细列表
     * @param landlordId 房东ID
     * @param page 页码
     * @param size 每页条数
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 收入明细分页数据
     */
    Page<Map<String, Object>> getLandlordIncomeList(Long landlordId, Integer page, Integer size, String startDate, String endDate);

    /**
     * 获取用户已租房源列表
     * @param page 分页对象
     * @param userId 用户ID
     * @return 分页结果
     */
    Page<HouseInfoDTO> getUserRentedHouses(Page<HouseInfoDTO> page, Long userId);

    /**
     * 统计所有已支付订单的数量
     * @return 订单数量
     */
    int countAllPaidOrders();

    /**
     * 计算平台总收入
     * @return 总收入
     */
    BigDecimal calculateTotalIncome();

    /**
     * 计算平台本月收入
     * @return 本月收入
     */
    BigDecimal calculateMonthlyIncome();

    /**
     * 计算总违约金收入
     * @return 违约金收入总额
     */
    BigDecimal calculateTotalPenaltyIncome();

    /**
     * 获取月度收入数据
     * @return 月度收入数据
     */
    List<Map<String, Object>> getMonthlyIncomeData();

    /**
     * 根据多个状态查询订单
     * @param page 分页对象
     * @param statuses 状态列表
     * @return 订单DTO分页对象
     */
    Page<OrderDTO> pageOrdersByStatuses(Page<Order> page, List<String> statuses);

    /**
     * 用户申请退租
     * @param dto 退租信息
     * @param userId 用户ID
     * @return 是否申请成功
     */
    boolean applyTerminate(TerminateDTO dto, Long userId);
    
    /**
     * 房东处理退租申请
     * @param orderId 订单ID
     * @param landlordId 房东ID
     * @param approved 是否同意退租
     * @param rejectReason 拒绝原因（如果拒绝）
     * @return 处理结果
     */
    boolean handleTerminateRequest(Long orderId, Long landlordId, boolean approved, String rejectReason);
    
    /**
     * 房东处理退租申请（带实际退租日期和违约金）
     * @param orderId 订单ID
     * @param landlordId 房东ID
     * @param approved 是否同意退租
     * @param rejectReason 拒绝原因（如果拒绝）
     * @param actualTerminateDate 实际退租日期
     * @param penaltyAmount 违约金金额
     * @param remark 备注
     * @return 处理结果
     */
    boolean handleTerminateRequest(Long orderId, Long landlordId, boolean approved, String rejectReason, 
                                  LocalDate actualTerminateDate, BigDecimal penaltyAmount, String remark);
    
    /**
     * 房东确认退租完成
     * @param orderId 订单ID
     * @param landlordId 房东ID
     * @return 是否确认成功
     */
    boolean confirmTermination(Long orderId, Long landlordId);
    
    /**
     * 计算退租违约金
     * @param orderId 订单ID
     * @return 违约金金额
     */
    BigDecimal calculateTerminationPenalty(Long orderId);
    
    /**
     * 获取所有订单列表
     * @return 所有订单列表
     */
    List<Order> list();

    /**
     * 统计房东的进行中订单数量（已支付且未完成的订单）
     * @param landlordId 房东ID
     * @return 进行中订单数量
     */
    int countLandlordActiveOrders(Long landlordId);

    /**
     * 用户支付违约金
     * @param orderId 订单ID
     * @param userId 用户ID
     * @param payMethod 支付方式
     * @return 是否支付成功
     */
    boolean payOrderPenalty(Long orderId, Long userId, String payMethod);

    /**
     * 检查并修复状态不一致的订单和房源
     * 主要处理已终止订单但房源仍处于出租状态的情况
     * @return 修复的记录数
     */
    int checkAndFixInconsistentStatus();
} 