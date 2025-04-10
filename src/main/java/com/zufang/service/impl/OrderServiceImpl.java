package com.zufang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zufang.common.enums.OrderStatus;
import com.zufang.common.exception.BusinessException;
import com.zufang.dto.*;
import com.zufang.entity.Contract;
import com.zufang.entity.House;
import com.zufang.entity.Order;
import com.zufang.event.OrderPaidEvent;
import com.zufang.mapper.OrderMapper;
import com.zufang.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 */
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private HouseService houseService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private ContractService contractService;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    
    /**
     * 创建订单
     * @param dto 订房信息
     * @param userId 用户ID
     * @return 创建的订单ID
     */
    @Override
    @Transactional
    public Long createOrder(BookingDTO dto, Long userId) {
        // 查询房源
        HouseInfoDTO houseInfo = houseService.getHouseInfo(dto.getHouseId());
        if (houseInfo == null) {
            log.error("创建订单失败，房源不存在：{}", dto.getHouseId());
            return null;
        }
        
        // 验证房源状态
        if (!"APPROVED".equals(houseInfo.getStatus())) {
            log.error("创建订单失败，房源状态不是已上架：{}", houseInfo.getStatus());
            return null;
        }
        
        // 验证最短租期
        int leaseTerm = dto.getLeaseTerm() != null ? dto.getLeaseTerm() : 0;
        if (leaseTerm < houseInfo.getMinLeaseTerm()) {
            log.error("创建订单失败，租期不满足最短要求：{} < {}", leaseTerm, houseInfo.getMinLeaseTerm());
            return null;
        }
        
        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setHouseId(houseInfo.getId());
        order.setUserId(userId);
        order.setLandlordId(houseInfo.getOwnerId());
        order.setStartDate(dto.getStartDate());
        order.setEndDate(dto.getEndDate());
        
        // 计算租期月数，如果没有提供，则根据开始和结束日期计算
        if (leaseTerm <= 0 && dto.getStartDate() != null && dto.getEndDate() != null) {
            // 计算月份差
            long monthsDiff = ChronoUnit.MONTHS.between(
                dto.getStartDate().withDayOfMonth(1), 
                dto.getEndDate().withDayOfMonth(1)
            );
            // 考虑天数差，如果结束日期的天数大于等于开始日期的天数，则增加一个月
            if (dto.getEndDate().getDayOfMonth() >= dto.getStartDate().getDayOfMonth()) {
                monthsDiff += 1;
            }
            leaseTerm = (int) monthsDiff;
        }
        
        // 确保租期至少为1个月
        leaseTerm = Math.max(1, leaseTerm);
        
        // 计算费用
        order.setMonthlyRent(houseInfo.getPrice());
        // 押金 = 月租金 * 押金月数
        order.setDeposit(houseInfo.getPrice().multiply(new BigDecimal(houseInfo.getDepositMonths())));
        // 服务费 = 月租金 * 2%
        order.setServiceFee(houseInfo.getPrice().multiply(new BigDecimal("0.02")));
        
        // 总金额 = 月租金 * 租期月数 + 押金 + 服务费
        BigDecimal totalRent = houseInfo.getPrice().multiply(new BigDecimal(leaseTerm));
        order.setTotalAmount(totalRent.add(order.getDeposit()).add(order.getServiceFee()));
        
        order.setStatus("UNPAID"); // 初始状态为待支付
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        order.setCreateTime(now);
        order.setUpdateTime(now);
        
        save(order);
        return order.getId();
    }
    
    /**
     * 支付订单
     * @param dto 支付信息
     * @param userId 用户ID
     * @return 是否支付成功
     */
    @Override
    @Transactional
    public boolean payOrder(PaymentDTO dto, Long userId) {
        Order order = getById(dto.getOrderId());
        if (order == null) {
            log.error("支付订单失败，订单不存在：{}", dto.getOrderId());
            return false;
        }
        
        if (!userId.equals(order.getUserId())) {
            log.error("支付订单失败，订单不属于当前用户：{} != {}", userId, order.getUserId());
            return false;
        }
        
        if (!"UNPAID".equals(order.getStatus())) {
            log.error("支付订单失败，订单状态不是待支付：{}", order.getStatus());
            return false;
        }
        
        // 更新订单状态
        order.setStatus("PAID");
        order.setPayTime(LocalDateTime.now());
        order.setPayMethod(dto.getPayMethod());
        order.setTransactionId(generateTransactionId());
        updateById(order);
        
        // 更新房源状态为已出租
        boolean houseUpdated = houseService.updateHouseStatus(order.getHouseId(), "RENTED");
        if (!houseUpdated) {
            log.warn("订单支付成功，但更新房源状态失败，房源ID：{}", order.getHouseId());
        }
        
        // 发布订单支付成功事件，让ContractService监听并处理合同创建
        eventPublisher.publishEvent(new OrderPaidEvent(order.getId()));
        
        return true;
    }
    
    /**
     * 取消订单
     * @param id 订单ID
     * @param userId 用户ID
     * @param reason 取消原因
     * @return 是否取消成功
     */
    @Override
    @Transactional
    public boolean cancelOrder(Long id, Long userId, String reason) {
        Order order = getById(id);
        if (order == null) {
            log.error("取消订单失败，订单不存在：{}", id);
            return false;
        }
        
        if (!userId.equals(order.getUserId())) {
            log.error("取消订单失败，订单不属于当前用户：{} != {}", userId, order.getUserId());
            return false;
        }
        
        // 修改逻辑：允许取消未支付、已支付和取消支付的订单
        if (!"UNPAID".equals(order.getStatus()) && !"PAID".equals(order.getStatus()) && !"PAYMENT_CANCELLED".equals(order.getStatus())) {
            log.error("取消订单失败，订单状态不允许取消：{}", order.getStatus());
            return false;
        }
        
        // 更新订单状态
        order.setStatus("CANCELLED");
        order.setCancelReason(reason);
        order.setUpdateTime(LocalDateTime.now());
        
        boolean updated = updateById(order);
        
        if (updated) {
            // 如果订单已支付，则需要取消相关联的合同
            if ("PAID".equals(order.getStatus())) {
                // 查询与订单关联的合同
                LambdaQueryWrapper<Contract> contractQueryWrapper = new LambdaQueryWrapper<>();
                contractQueryWrapper.eq(Contract::getOrderId, id);
                
                Contract contract = contractService.getOne(contractQueryWrapper);
                if (contract != null) {
                    // 更新合同状态为已取消
                    contract.setStatus("CANCELLED");
                    contract.setUpdateTime(LocalDateTime.now());
                    contractService.updateById(contract);
                    log.info("订单{}关联的合同{}已被标记为取消", id, contract.getId());
                }
                
                // 将房源状态恢复为可租
                houseService.updateHouseStatus(order.getHouseId(), "APPROVED");
                log.info("订单{}取消后，房源{}状态已恢复为可租", id, order.getHouseId());
            }
        }
        
        return updated;
    }
    
    /**
     * 取消支付
     * @param id 订单ID
     * @param userId 用户ID
     * @param reason 取消原因
     * @return 是否取消成功
     */
    @Override
    @Transactional
    public boolean cancelPayment(Long id, Long userId, String reason) {
        Order order = getById(id);
        if (order == null) {
            log.error("取消支付失败，订单不存在：{}", id);
            return false;
        }
        
        if (!userId.equals(order.getUserId())) {
            log.error("取消支付失败，订单不属于当前用户：{} != {}", userId, order.getUserId());
            return false;
        }
        
        // 只有未支付的订单可以取消支付
        if (!"UNPAID".equals(order.getStatus())) {
            log.error("取消支付失败，订单状态不是未支付：{}", order.getStatus());
            return false;
        }
        
        // 更新订单状态
        order.setStatus("PAYMENT_CANCELLED");
        order.setCancelReason(reason);
        order.setUpdateTime(LocalDateTime.now());
        
        boolean updated = updateById(order);
        
        if (updated) {
            log.info("订单{}已取消支付，原因：{}", id, reason);
        }
        
        return updated;
    }
    
    /**
     * 获取订单详情
     * @param id 订单ID
     * @return 订单详情
     */
    @Override
    public OrderDTO getOrder(Long id) {
        Order order = getById(id);
        if (order == null) {
            return null;
        }
        
        return convertToDTO(order);
    }
    
    /**
     * 管理员分页查询所有订单
     * @param page 分页参数
     * @param status 订单状态（可选）
     * @return 订单分页数据
     */
    @Override
    public Page<OrderDTO> pageOrders(Page<Order> page, String status) {
        log.info("分页查询订单: page={}, size={}, status={}", page.getCurrent(), page.getSize(), status);
        
        try {
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            
            // 如果指定了状态，则按状态筛选，使用更灵活的匹配方式
            if (StringUtils.hasText(status)) {
                // 实现对多种状态格式的兼容
                queryWrapper.and(wrapper -> wrapper
                    .eq(Order::getStatus, status)
                    .or()
                    .eq(Order::getStatus, status.toUpperCase())
                    .or()
                    .eq(Order::getStatus, status.toLowerCase())
                    .or()
                    .like(Order::getStatus, status));
            }
            
            queryWrapper.orderByDesc(Order::getCreateTime);
            
            // 查询前记录查询条件
            log.info("订单查询条件: status={}, queryWrapper={}", status, queryWrapper.getCustomSqlSegment());
            
            Page<Order> orderPage = page(page, queryWrapper);
            
            // 记录查询结果数量
            log.info("订单查询结果: 总数={}", orderPage.getTotal());
            
            // 如果结果为空，尝试进行无条件查询
            if (orderPage.getRecords().isEmpty() && StringUtils.hasText(status)) {
                log.info("指定状态查询结果为空，尝试查询所有订单");
                LambdaQueryWrapper<Order> allWrapper = new LambdaQueryWrapper<>();
                allWrapper.orderByDesc(Order::getCreateTime);
                orderPage = page(page, allWrapper);
                log.info("查询所有订单结果: 总数={}", orderPage.getTotal());
            }
            
            Page<OrderDTO> dtoPage = convertToOrderDTOPage(orderPage);
            
            // 打印前5条记录的状态，用于调试
            if (!dtoPage.getRecords().isEmpty()) {
                log.info("查询结果前5条记录状态:");
                dtoPage.getRecords().stream().limit(5).forEach(dto -> 
                    log.info("订单ID: {}, 状态: {}, 金额: {}", dto.getId(), dto.getStatus(), dto.getTotalAmount()));
            }
            
            return dtoPage;
        } catch (Exception e) {
            log.error("分页查询订单异常: error={}", e.getMessage(), e);
            // 返回空结果而不是抛出异常，避免前端崩溃
            Page<OrderDTO> emptyPage = new Page<>(page.getCurrent(), page.getSize());
            emptyPage.setRecords(new ArrayList<>());
            return emptyPage;
        }
    }
    
    /**
     * 房东分页查询自己的订单
     * @param page 分页参数
     * @param landlordId 房东ID
     * @param status 订单状态（可选）
     * @return 订单分页数据
     */
    @Override
    public Page<OrderDTO> pageLandlordOrders(Page<Order> page, Long landlordId, String status) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getLandlordId, landlordId);
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Order::getStatus, status);
        }
        queryWrapper.orderByDesc(Order::getCreateTime);
        
        Page<Order> orderPage = page(page, queryWrapper);
        return convertToOrderDTOPage(orderPage);
    }
    
    /**
     * 用户分页查询自己的订单
     * @param page 分页参数
     * @param userId 用户ID
     * @param status 订单状态（可选）
     * @return 订单分页数据
     */
    @Override
    public Page<OrderDTO> pageUserOrders(Page<Order> page, Long userId, String status) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getUserId, userId);
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Order::getStatus, status);
        }
        queryWrapper.orderByDesc(Order::getCreateTime);
        
        Page<Order> orderPage = page(page, queryWrapper);
        return convertToOrderDTOPage(orderPage);
    }
    
    /**
     * 生成订单编号
     * @return 订单编号
     */
    private String generateOrderNo() {
        return "ORD" + DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now()) 
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * 生成交易流水号
     * @return 交易流水号
     */
    private String generateTransactionId() {
        return "TRX" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()) 
                + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
    
    /**
     * 根据ID获取订单信息
     */
    @Override
    public OrderDTO getOrderById(Long id) {
        try {
            log.info("根据ID获取订单信息: id={}", id);
            Order order = getById(id);
            if (order == null) {
                log.warn("订单不存在: id={}", id);
                return null;
            }
            return convertToDTO(order);
        } catch (Exception e) {
            log.error("根据ID获取订单信息异常: id={}, error={}", id, e.getMessage(), e);
            throw new BusinessException("获取订单信息失败");
        }
    }
    
    /**
     * 将Order转换为OrderDTO
     */
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        BeanUtils.copyProperties(order, dto);
        
        // 格式化租期日期
        if (order.getStartDate() != null) {
            dto.setLeaseStartDate(order.getStartDate().toString());
        }
        
        if (order.getEndDate() != null) {
            dto.setLeaseEndDate(order.getEndDate().toString());
        }
        
        // 获取房源信息
        try {
            HouseInfoDTO houseInfo = houseService.getHouseInfo(order.getHouseId());
            if (houseInfo != null) {
                dto.setHouseTitle(houseInfo.getTitle());
                dto.setHouseCoverImage(houseInfo.getCoverImage());
                dto.setHouseImage(houseInfo.getCoverImage());
                dto.setHouseAddress(houseInfo.getCity() + houseInfo.getDistrict() + houseInfo.getAddress());
                dto.setIsHouseDeleted(false);
            } else {
                // 如果房源不存在，设置默认值并标记房源已删除
                dto.setHouseTitle("房源信息已删除");
                dto.setHouseCoverImage("");
                dto.setHouseImage("");
                dto.setHouseAddress("地址信息已删除");
                dto.setIsHouseDeleted(true);
            }
        } catch (Exception e) {
            log.error("获取房源信息异常: houseId={}, error={}", order.getHouseId(), e.getMessage(), e);
            dto.setHouseTitle("房源信息获取失败");
            dto.setHouseCoverImage("");
            dto.setHouseImage("");
            dto.setHouseAddress("地址信息获取失败");
            dto.setIsHouseDeleted(false);
        }
        
        // 获取用户信息
        try {
            UserInfoDTO userInfo = userService.getUserInfo(order.getUserId());
            if (userInfo != null) {
                dto.setUserName(userInfo.getNickname() != null ? userInfo.getNickname() : userInfo.getUsername());
                dto.setUserPhone(userInfo.getPhone());
                // 添加租客真实姓名
                dto.setUserRealName(userInfo.getRealName());
            } else {
                dto.setUserName("未知用户");
                dto.setUserPhone("");
                dto.setUserRealName("");
            }
        } catch (Exception e) {
            log.error("获取用户信息异常: userId={}, error={}", order.getUserId(), e.getMessage(), e);
            dto.setUserName("用户信息获取失败");
            dto.setUserPhone("");
            dto.setUserRealName("");
        }
        
        // 获取房东信息
        try {
            UserInfoDTO landlordInfo = userService.getUserInfo(order.getLandlordId());
            if (landlordInfo != null) {
                dto.setLandlordName(landlordInfo.getNickname() != null ? landlordInfo.getNickname() : landlordInfo.getUsername());
                // 添加房东真实姓名和电话
                dto.setLandlordRealName(landlordInfo.getRealName());
                dto.setLandlordPhone(landlordInfo.getPhone());
            } else {
                dto.setLandlordName("未知房东");
                dto.setLandlordRealName("");
                dto.setLandlordPhone("");
            }
        } catch (Exception e) {
            log.error("获取房东信息异常: landlordId={}, error={}", order.getLandlordId(), e.getMessage(), e);
            dto.setLandlordName("房东信息获取失败");
            dto.setLandlordRealName("");
            dto.setLandlordPhone("");
        }
        
        // 退租字段处理 - BeanUtils.copyProperties已复制基本字段，无需额外处理
        
        return dto;
    }
    
    /**
     * 转换订单分页数据为DTO分页数据
     * @param orderPage 订单分页数据
     * @return DTO分页数据
     */
    private Page<OrderDTO> convertToOrderDTOPage(Page<Order> orderPage) {
        List<OrderDTO> records = orderPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        Page<OrderDTO> dtoPage = new Page<>();
        dtoPage.setRecords(records);
        dtoPage.setCurrent(orderPage.getCurrent());
        dtoPage.setSize(orderPage.getSize());
        dtoPage.setTotal(orderPage.getTotal());
        dtoPage.setPages(orderPage.getPages());
        
        return dtoPage;
    }

    /**
     * 计算房东总收入
     */
    @Override
    public BigDecimal calculateLandlordTotalIncome(Long landlordId) {
        log.info("计算房东总收入: landlordId={}", landlordId);
        
        try {
            // 查询所有已支付的订单
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getLandlordId, landlordId)
                   .and(w -> w.eq(Order::getStatus, "PAID")
                          .or().eq(Order::getStatus, "COMPLETED")
                          .or().eq(Order::getStatus, "ACTIVE")
                          .or().eq(Order::getStatus, "TERMINATED"));
            
            // 查询SQL日志输出
            log.info("房东总收入SQL查询条件: {}", wrapper.getCustomSqlSegment());
            List<Order> orders = baseMapper.selectList(wrapper);
            log.info("房东总收入查询到订单数量: {}", orders.size());
            
            // 计算总收入
            BigDecimal totalIncome = BigDecimal.ZERO;
            for (Order order : orders) {
                if ("TERMINATED".equals(order.getStatus())) {
                    // 对于已终止的订单
                    if (Boolean.TRUE.equals(order.getIsPenaltyPaid()) && order.getPenaltyAmount() != null) {
                        // 如果有违约金并已支付，添加违约金收入
                        BigDecimal penaltyIncome = order.getPenaltyAmount();
                        totalIncome = totalIncome.add(penaltyIncome);
                        
                        // 计算已收租金（总金额减去押金）
                        BigDecimal rentCollected = BigDecimal.ZERO;
                        if (order.getTotalAmount() != null && order.getDeposit() != null) {
                            rentCollected = order.getTotalAmount().subtract(order.getDeposit());
                        }
                        
                        // 计算租赁天数比例
                        LocalDate startDate = order.getStartDate();
                        LocalDate endDate = order.getEndDate();
                        LocalDate terminateDate = order.getActualTerminateDate() != null ? 
                                                order.getActualTerminateDate() : LocalDate.now();
                        
                        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
                        long daysRented = ChronoUnit.DAYS.between(startDate, terminateDate);
                        
                        // 计算从实际退租日到租约结束还有多少天
                        long remainingDays = ChronoUnit.DAYS.between(terminateDate, endDate);
                        
                        // 确保计算安全
                        if (totalDays > 0 && remainingDays >= 0) {
                            // 计算每天的租金
                            BigDecimal dailyRent = rentCollected.divide(new BigDecimal(totalDays), 2, RoundingMode.HALF_UP);
                            // 计算实际应收租金 = 每天租金 × 已使用天数
                            BigDecimal actualRent = dailyRent.multiply(new BigDecimal(daysRented));
                            
                            totalIncome = totalIncome.add(actualRent);
                            
                            log.debug("终止订单[{}]计算: 违约金收入({}) + 实际租金收入({}) = 总收入({})", 
                                    order.getId(), penaltyIncome, actualRent, penaltyIncome.add(actualRent));
                        } else {
                            // 异常情况，直接加上租金收入
                            totalIncome = totalIncome.add(rentCollected);
                            log.debug("终止订单[{}]计算(异常日期): 违约金({}) + 租金收入({}) = 总收入({})", 
                                    order.getId(), penaltyIncome, rentCollected, penaltyIncome.add(rentCollected));
                        }
                    } else {
                        // 如果没有违约金或未支付，只计算实际租期的租金
                        BigDecimal rentCollected = BigDecimal.ZERO;
                        if (order.getTotalAmount() != null && order.getDeposit() != null) {
                            rentCollected = order.getTotalAmount().subtract(order.getDeposit());
                        } else if (order.getTotalAmount() != null) {
                            // 估算租金
                            rentCollected = order.getTotalAmount().multiply(new BigDecimal("0.8"));
                        }
                        
                        // 计算租赁天数比例
                        LocalDate startDate = order.getStartDate();
                        LocalDate endDate = order.getEndDate();
                        LocalDate terminateDate = order.getActualTerminateDate() != null ? 
                                                order.getActualTerminateDate() : LocalDate.now();
                        
                        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
                        long daysRented = ChronoUnit.DAYS.between(startDate, terminateDate);
                        
                        // 确保计算安全
                        if (totalDays > 0 && daysRented >= 0) {
                            // 计算租期内实际应收租金
                            BigDecimal actualRent = rentCollected.multiply(
                                new BigDecimal(daysRented)).divide(
                                new BigDecimal(totalDays), 2, RoundingMode.HALF_UP);
                            
                            totalIncome = totalIncome.add(actualRent);
                            
                            log.debug("终止订单[{}]无违约金计算: 实际租金收入({}) = 总收入({})", 
                                    order.getId(), actualRent, actualRent);
                        } else {
                            // 异常情况，直接加上租金收入
                            totalIncome = totalIncome.add(rentCollected);
                            log.debug("终止订单[{}]无违约金计算(异常日期): 租金收入({}) = 总收入({})", 
                                    order.getId(), rentCollected, rentCollected);
                        }
                    }
                } else {
                    // 正常订单收入计算（不包括押金）
                    if (order.getTotalAmount() != null && order.getDeposit() != null) {
                        BigDecimal orderIncome = order.getTotalAmount().subtract(order.getDeposit());
                        totalIncome = totalIncome.add(orderIncome);
                        
                        log.debug("正常订单[{}]收入: 总金额({}) - 押金({}) = 计入总收入({})", 
                                order.getId(), order.getTotalAmount(), order.getDeposit(), orderIncome);
                    } else if (order.getTotalAmount() != null) {
                        // 如果没有押金信息
                        log.debug("正常订单[{}]收入(无押金信息): 总金额({}) * 0.8 = 估算收入({})", 
                              order.getId(), order.getTotalAmount(), 
                              order.getTotalAmount().multiply(new BigDecimal("0.8")));
                        
                        // 估算收入为总金额的80%
                        totalIncome = totalIncome.add(order.getTotalAmount().multiply(new BigDecimal("0.8")));
                    }
                }
            }
            
            log.info("计算房东总收入成功: landlordId={}, totalIncome={}", landlordId, totalIncome);
            return totalIncome;
        } catch (Exception e) {
            log.error("计算房东总收入失败: landlordId={}, error={}", landlordId, e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 计算房东本月收入
     */
    @Override
    public BigDecimal calculateLandlordMonthlyIncome(Long landlordId) {
        log.info("计算房东本月收入: landlordId={}", landlordId);
        
        try {
            // 获取当月的起始日期和结束日期
            LocalDate now = LocalDate.now();
            LocalDate firstDayOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate lastDayOfMonth = now.with(TemporalAdjusters.lastDayOfMonth());
            
            return calculateLandlordIncomeInPeriod(landlordId, firstDayOfMonth, lastDayOfMonth);
        } catch (Exception e) {
            log.error("计算房东本月收入失败: landlordId={}, error={}", landlordId, e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 计算房东在指定时间段内的收入
     */
    @Override
    public BigDecimal calculateLandlordIncomeInPeriod(Long landlordId, LocalDate startDate, LocalDate endDate) {
        log.info("计算房东时间段内收入: landlordId={}, startDate={}, endDate={}", landlordId, startDate, endDate);
        
        try {
            // 转换为LocalDateTime，用于查询
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            
            // 查询指定时间段内的所有有效订单（扩大查询范围）
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            wrapper.eq("landlord_id", landlordId)
                   .and(w -> w.eq("status", "PAID")
                          .or().eq("status", "ACTIVE")
                          .or().eq("status", "COMPLETED")
                          .or().eq("status", "TERMINATED")
                          .or().eq("status", "TERMINATE_APPROVED")
                          .or().eq("status", "TERMINATE_REQUESTED"))
                   .and(w -> w.between("create_time", startDateTime, endDateTime)
                          .or().between("pay_time", startDateTime, endDateTime)
                          .or().between("actual_terminate_date", startDateTime, endDateTime));
            
            // 输出SQL日志
            log.info("房东时间段内收入SQL查询条件: {}", wrapper.getCustomSqlSegment());
            List<Order> orders = baseMapper.selectList(wrapper);
            log.info("房东时间段内收入查询到订单数量: {}", orders.size());
            
            // 计算时间段内的收入
            BigDecimal periodIncome = BigDecimal.ZERO;
            for (Order order : orders) {
                if ("TERMINATED".equals(order.getStatus())) {
                    // 对于已终止的订单，检查实际终止日期是否在查询的时间段内
                    if (order.getActualTerminateDate() != null) {
                        boolean terminatedInPeriod = !order.getActualTerminateDate().isBefore(startDate) 
                                                  && !order.getActualTerminateDate().isAfter(endDate);
                        
                        if (terminatedInPeriod) {
                            // 如果在当前时间段内终止，计入违约金
                            if (Boolean.TRUE.equals(order.getIsPenaltyPaid()) && order.getPenaltyAmount() != null) {
                                BigDecimal penaltyIncome = order.getPenaltyAmount();
                                periodIncome = periodIncome.add(penaltyIncome);
                                log.debug("时间段内终止订单[{}]违约金收入: {}", order.getId(), penaltyIncome);
                            }
                            
                            // 计算已收租金（总金额减去押金）按照实际租期比例
                            BigDecimal rentCollected = BigDecimal.ZERO;
                            if (order.getTotalAmount() != null && order.getDeposit() != null) {
                                rentCollected = order.getTotalAmount().subtract(order.getDeposit());
                            } else if (order.getTotalAmount() != null) {
                                // 估算租金
                                rentCollected = order.getTotalAmount().multiply(new BigDecimal("0.8"));
                            }
                            
                            // 计算租赁天数比例
                            LocalDate leaseStartDate = order.getStartDate();
                            LocalDate leaseEndDate = order.getEndDate();
                            LocalDate terminateDate = order.getActualTerminateDate();
                            
                            // 计算当前时间段内的租期（如果租期跨越时间段边界）
                            LocalDate periodStartDate = leaseStartDate.isBefore(startDate) ? startDate : leaseStartDate;
                            LocalDate periodEndDate = terminateDate;
                            
                            long totalLeaseDays = ChronoUnit.DAYS.between(leaseStartDate, leaseEndDate);
                            long periodDays = ChronoUnit.DAYS.between(periodStartDate, periodEndDate);
                            
                            // 确保计算安全
                            if (totalLeaseDays > 0 && periodDays > 0) {
                                // 计算当前时间段内的租金
                                BigDecimal periodRent = rentCollected.multiply(
                                    new BigDecimal(periodDays)).divide(
                                    new BigDecimal(totalLeaseDays), 2, RoundingMode.HALF_UP);
                                
                                periodIncome = periodIncome.add(periodRent);
                                log.debug("时间段内终止订单[{}]租金收入计算: 总租金({}) * 时间段天数({}) / 总租期({}) = 时间段租金({})", 
                                        order.getId(), rentCollected, periodDays, totalLeaseDays, periodRent);
                            }
                        }
                    }
                } else if ("PAID".equals(order.getStatus()) || "ACTIVE".equals(order.getStatus())) {
                    // 对于正在执行中的订单，计算时间段内的租金
                    if (order.getTotalAmount() != null && order.getDeposit() != null) {
                        BigDecimal rentTotal = order.getTotalAmount().subtract(order.getDeposit());
                        
                        // 计算租赁天数
                        LocalDate leaseStartDate = order.getStartDate();
                        LocalDate leaseEndDate = order.getEndDate();
                        
                        // 计算当前时间段内的租期（如果租期跨越时间段边界）
                        LocalDate periodStartDate = leaseStartDate.isBefore(startDate) ? startDate : leaseStartDate;
                        LocalDate periodEndDate = leaseEndDate.isAfter(endDate) ? endDate : leaseEndDate;
                        
                        long totalLeaseDays = ChronoUnit.DAYS.between(leaseStartDate, leaseEndDate);
                        long periodDays = ChronoUnit.DAYS.between(periodStartDate, periodEndDate);
                        
                        // 确保计算安全且有效
                        if (totalLeaseDays > 0 && periodDays > 0) {
                            // 计算当前时间段内的租金
                            BigDecimal periodRent = rentTotal.multiply(
                                new BigDecimal(periodDays)).divide(
                                new BigDecimal(totalLeaseDays), 2, RoundingMode.HALF_UP);
                            
                            periodIncome = periodIncome.add(periodRent);
                            log.debug("时间段内活跃订单[{}]租金收入计算: 总租金({}) * 时间段天数({}) / 总租期({}) = 时间段租金({})", 
                                    order.getId(), rentTotal, periodDays, totalLeaseDays, periodRent);
                        }
                    } else if (order.getTotalAmount() != null) {
                        // 如果没有押金信息，估算租金为总金额的80%
                        BigDecimal rentTotal = order.getTotalAmount().multiply(new BigDecimal("0.8"));
                        
                        // 计算租赁天数
                        LocalDate leaseStartDate = order.getStartDate();
                        LocalDate leaseEndDate = order.getEndDate();
                        
                        // 计算当前时间段内的租期（如果租期跨越时间段边界）
                        LocalDate periodStartDate = leaseStartDate.isBefore(startDate) ? startDate : leaseStartDate;
                        LocalDate periodEndDate = leaseEndDate.isAfter(endDate) ? endDate : leaseEndDate;
                        
                        long totalLeaseDays = ChronoUnit.DAYS.between(leaseStartDate, leaseEndDate);
                        long periodDays = ChronoUnit.DAYS.between(periodStartDate, periodEndDate);
                        
                        // 确保计算安全且有效
                        if (totalLeaseDays > 0 && periodDays > 0) {
                            // 计算当前时间段内的租金
                            BigDecimal periodRent = rentTotal.multiply(
                                new BigDecimal(periodDays)).divide(
                                new BigDecimal(totalLeaseDays), 2, RoundingMode.HALF_UP);
                            
                            periodIncome = periodIncome.add(periodRent);
                            log.debug("时间段内活跃订单[{}]租金收入计算(无押金): 估算租金({}) * 时间段天数({}) / 总租期({}) = 时间段租金({})", 
                                    order.getId(), rentTotal, periodDays, totalLeaseDays, periodRent);
                        }
                    }
                } else if ("COMPLETED".equals(order.getStatus())) {
                    // 正常完成的租约，添加租金收入记录
                    Map<String, Object> completedRentItem = new HashMap<>();
                    // 添加基本订单信息
                    completedRentItem.put("orderId", order.getId());
                    completedRentItem.put("orderNo", order.getOrderNo());
                    completedRentItem.put("houseId", order.getHouseId());
                    completedRentItem.put("incomeType", "RENT");
                    completedRentItem.put("incomeTypeDesc", "租金收入(已完成)");
                    
                    // 添加租期信息
                    completedRentItem.put("leaseStartDate", order.getStartDate());
                    completedRentItem.put("leaseEndDate", order.getEndDate());
                    
                    // 计算租金金额(总金额 - 押金)
                    BigDecimal rentAmount = order.getTotalAmount() != null && order.getDeposit() != null ? 
                        order.getTotalAmount().subtract(order.getDeposit()) : 
                        (order.getTotalAmount() != null ? order.getTotalAmount().multiply(new BigDecimal("0.8")) : BigDecimal.ZERO);
                    
                    completedRentItem.put("amount", rentAmount);
                    
                    // 计算租期总天数并添加租期信息
                    long totalDays = ChronoUnit.DAYS.between(order.getStartDate(), order.getEndDate());
                    completedRentItem.put("totalDays", totalDays);
                    completedRentItem.put("leaseTermInfo", String.format("租期: %tF 至 %tF (共%d天)", order.getStartDate(), order.getEndDate(), totalDays));
                    
                    // 创建收入记录集合（如果尚未创建）
                    List<Map<String, Object>> incomeRecords = new ArrayList<>();
                    incomeRecords.add(completedRentItem);
                }
            }
            
            log.info("计算房东时间段内收入成功: landlordId={}, startDate={}, endDate={}, periodIncome={}",
                    landlordId, startDate, endDate, periodIncome);
            return periodIncome;
        } catch (Exception e) {
            log.error("计算房东时间段内收入失败: landlordId={}, startDate={}, endDate={}, error={}",
                    landlordId, startDate, endDate, e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 统计房东订单总数
     */
    @Override
    public int countLandlordOrders(Long landlordId) {
        log.info("统计房东订单总数: landlordId={}", landlordId);
        
        try {
            // 查询所有相关状态的订单数量
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getLandlordId, landlordId)
                   .and(w -> w.eq(Order::getStatus, "PAID")
                          .or().eq(Order::getStatus, "ACTIVE")
                          .or().eq(Order::getStatus, "COMPLETED")
                          .or().eq(Order::getStatus, "TERMINATED")
                          .or().eq(Order::getStatus, "TERMINATE_APPROVED")
                          .or().eq(Order::getStatus, "TERMINATE_REQUESTED"));
            
            // 输出查询条件日志
            log.info("房东订单总数SQL查询条件: {}", wrapper.getCustomSqlSegment());
            
            // 将Long转换为int，确保安全转换
            Long count = baseMapper.selectCount(wrapper);
            log.info("统计到的订单数量: {}", count);
            
            // 检查是否超出int范围
            if (count > Integer.MAX_VALUE) {
                log.warn("订单数量超出Integer.MAX_VALUE范围: {}", count);
                return Integer.MAX_VALUE;
            }
            
            int intCount = count.intValue();
            
            log.info("统计房东订单总数成功: landlordId={}, count={}", landlordId, intCount);
            return intCount;
        } catch (Exception e) {
            log.error("统计房东订单总数失败: landlordId={}, error={}", landlordId, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 统计房东已出租房源数量
     */
    @Override
    public int countLandlordRentedHouses(Long landlordId) {
        log.info("统计房东已出租房源数量: landlordId={}", landlordId);
        
        try {
            // 查询当前有效的订单（ACTIVE状态，表示正在租赁中）
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            wrapper.select("DISTINCT house_id") // 只查询不重复的房源ID
                  .eq("landlord_id", landlordId)
                  .eq("status", "ACTIVE") // 只统计当前租赁中的订单
                  .ne("is_deleted", 1); // 排除已删除的订单
            
            // 输出SQL日志
            log.info("房东已出租房源统计SQL查询条件: {}", wrapper.getCustomSqlSegment());
            
            // 执行查询并计数
            Long count = baseMapper.selectCount(wrapper);
            log.info("统计房东已出租房源数量成功: landlordId={}, count={}", landlordId, count);
            
            if (count == null) {
                return 0;
            }
            
            if (count > Integer.MAX_VALUE) {
                log.warn("房源数量超出Integer.MAX_VALUE范围: {}", count);
                return Integer.MAX_VALUE;
            }
            
            return count.intValue();
        } catch (Exception e) {
            log.error("统计房东已出租房源数量失败: landlordId={}, error={}", landlordId, e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * 统计房东本月订单数
     */
    @Override
    public int countLandlordMonthlyOrders(Long landlordId) {
        log.info("统计房东本月订单数: landlordId={}", landlordId);
        
        try {
            // 获取当月的起始日期和结束日期
            LocalDate now = LocalDate.now();
            LocalDate firstDayOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate lastDayOfMonth = now.with(TemporalAdjusters.lastDayOfMonth());
            
            LocalDateTime startDateTime = firstDayOfMonth.atStartOfDay();
            LocalDateTime endDateTime = lastDayOfMonth.atTime(23, 59, 59);
            
            log.info("本月时间范围: {} 至 {}", startDateTime, endDateTime);
            
            // 查询指定时间段内创建或支付的订单
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getLandlordId, landlordId)
                   .and(w -> w.eq(Order::getStatus, "PAID")
                          .or().eq(Order::getStatus, "ACTIVE")
                          .or().eq(Order::getStatus, "COMPLETED")
                          .or().eq(Order::getStatus, "TERMINATED")
                          .or().eq(Order::getStatus, "TERMINATE_APPROVED")
                          .or().eq(Order::getStatus, "TERMINATE_REQUESTED"))
                   .and(w -> w.between(Order::getCreateTime, startDateTime, endDateTime)
                          .or().between(Order::getPayTime, startDateTime, endDateTime));
            
            log.info("房东本月订单SQL查询条件: {}", wrapper.getCustomSqlSegment());
            
            // 将Long转换为int，确保安全转换
            Long count = baseMapper.selectCount(wrapper);
            log.info("统计到的本月订单数量: {}", count);
            
            // 检查是否超出int范围
            if (count > Integer.MAX_VALUE) {
                log.warn("订单数量超出Integer.MAX_VALUE范围: {}", count);
                return Integer.MAX_VALUE;
            }
            
            int intCount = count.intValue();
            
            log.info("统计房东本月订单数成功: landlordId={}, count={}", landlordId, intCount);
            return intCount;
        } catch (Exception e) {
            log.error("统计房东本月订单数失败: landlordId={}, error={}", landlordId, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 获取房东收入明细列表
     */
    @Override
    public Page<Map<String, Object>> getLandlordIncomeList(Long landlordId, Integer page, Integer size, String startDateStr, String endDateStr) {
        log.info("获取房东收入明细列表: landlordId={}, page={}, size={}, startDate={}, endDate={}",
                landlordId, page, size, startDateStr, endDateStr);
        
        try {
            // 解析日期参数
            LocalDate startDate = null;
            LocalDate endDate = null;
            
            if (StringUtils.hasText(startDateStr)) {
                startDate = LocalDate.parse(startDateStr);
            }
            
            if (StringUtils.hasText(endDateStr)) {
                endDate = LocalDate.parse(endDateStr);
            }
            
            // 构建查询条件
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getLandlordId, landlordId);
            
            // 指定有效的订单状态
            wrapper.and(w -> w.eq(Order::getStatus, "PAID")
                          .or().eq(Order::getStatus, "COMPLETED")
                          .or().eq(Order::getStatus, "ACTIVE")
                          .or().eq(Order::getStatus, "TERMINATED"));
            
            // 添加日期范围过滤
            if (startDate != null) {
                wrapper.ge(Order::getCreateTime, startDate.atStartOfDay());
            }
            
            if (endDate != null) {
                wrapper.le(Order::getCreateTime, endDate.plusDays(1).atStartOfDay().minusSeconds(1));
            }
            
            // 按创建时间倒序排序
            wrapper.orderByDesc(Order::getCreateTime);
            
            // 查询订单
            Page<Order> orderPage = new Page<>(page, size);
            Page<Order> result = baseMapper.selectPage(orderPage, wrapper);
            
            // 转换结果
            List<Map<String, Object>> incomeList = new ArrayList<>();
            
            // 处理有效订单
            for (Order order : result.getRecords()) {
                // 1. 添加租金收入记录
                Map<String, Object> incomeRecord = new HashMap<>();
                incomeRecord.put("orderId", order.getId());
                incomeRecord.put("orderNo", order.getOrderNo());
                incomeRecord.put("houseId", order.getHouseId());
                incomeRecord.put("userId", order.getUserId());
                
                // 获取房源信息
                HouseInfoDTO houseInfo = houseService.getHouseInfo(order.getHouseId());
                if (houseInfo != null) {
                    incomeRecord.put("houseTitle", houseInfo.getTitle());
                } else {
                    incomeRecord.put("houseTitle", "未知房源");
                }
                
                // 获取租客信息
                UserInfoDTO userInfo = userService.getUserInfo(order.getUserId());
                if (userInfo != null) {
                    incomeRecord.put("userName", userInfo.getNickname() != null ? userInfo.getNickname() : userInfo.getUsername());
                    incomeRecord.put("userRealName", userInfo.getRealName());
                } else {
                    incomeRecord.put("userName", "未知用户");
                    incomeRecord.put("userRealName", "");
                }
                
                // 计算租金收入金额
                BigDecimal amount = BigDecimal.ZERO;
                if (order.getTotalAmount() != null && order.getDeposit() != null) {
                    amount = order.getTotalAmount().subtract(order.getDeposit());
                } else if (order.getTotalAmount() != null) {
                    amount = order.getTotalAmount().multiply(new BigDecimal("0.8")); // 估算
                }
                
                incomeRecord.put("amount", amount);
                incomeRecord.put("deposit", order.getDeposit() != null ? order.getDeposit() : BigDecimal.ZERO);
                incomeRecord.put("totalAmount", order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
                
                // 租期信息
                incomeRecord.put("startDate", order.getStartDate() != null ? order.getStartDate().toString() : "");
                incomeRecord.put("endDate", order.getEndDate() != null ? order.getEndDate().toString() : "");
                
                // 计算租期天数
                long totalDays = 0;
                if (order.getStartDate() != null && order.getEndDate() != null) {
                    totalDays = ChronoUnit.DAYS.between(order.getStartDate(), order.getEndDate());
                }
                incomeRecord.put("leaseDays", totalDays);
                
                // 交易时间
                incomeRecord.put("transactionTime", order.getPayTime() != null ? order.getPayTime().toString() : order.getCreateTime().toString());
                
                // 订单状态和收入类型
                incomeRecord.put("status", order.getStatus());
                
                // 设置收入类型为租金收入
                incomeRecord.put("incomeType", "RENT");
                incomeRecord.put("incomeTypeName", "租金收入");
                
                // 备注
                StringBuilder remarkBuilder = new StringBuilder();
                
                if ("TERMINATED".equals(order.getStatus())) {
                    // 计算实际租期
                    LocalDate terminateDate = order.getActualTerminateDate() != null ? order.getActualTerminateDate() : LocalDate.now();
                    
                    // 记录日志以便调试
                    log.debug("订单[{}]租期开始日期: {}, 实际退租日期: {}", order.getId(), order.getStartDate(), terminateDate);
                    
                    // 确保开始日期不为空
                    if (order.getStartDate() == null) {
                        log.warn("订单[{}]租期开始日期为空", order.getId());
                        long actualDays = 0;
                        remarkBuilder.append("实际租期: ").append(actualDays).append("/").append(totalDays).append(" 天, ");
                    } else {
                        // 计算实际租期 - 修正计算逻辑
                        long actualDays;
                        
                        if (order.getActualTerminateDate() != null) {
                            // 如果有实际终止日期，计算从开始到实际终止的天数
                            actualDays = ChronoUnit.DAYS.between(order.getStartDate(), order.getActualTerminateDate());
                            
                            // 输出计算结果，便于调试
                            log.debug("订单[{}]使用订单的实际终止日期计算租期: 从{}到{}, 共{}天", 
                                   order.getId(), order.getStartDate(), order.getActualTerminateDate(), actualDays);
                                   
                            // 特殊情况处理：如果计算结果小于等于0，至少为1天
                            if (actualDays <= 0) {
                                actualDays = 1;
                                log.warn("订单[{}]计算的实际租期<=0, 已调整为1天", order.getId());
                            }
                        } else {
                            // 如果没有实际终止日期，使用当前日期 - 这种情况应该很少发生
                            actualDays = ChronoUnit.DAYS.between(order.getStartDate(), LocalDate.now());
                            log.warn("订单[{}]没有实际终止日期，使用当前日期计算租期: {}天", order.getId(), actualDays);
                            
                            if (actualDays <= 0) {
                                actualDays = 1; 
                                log.warn("订单[{}]使用当前日期计算的租期<=0, 已调整为1天", order.getId());
                            }
                        }
                        
                        remarkBuilder.append("实际租期: ").append(actualDays).append("/").append(totalDays).append(" 天, ");
                    }
                    
                    remarkBuilder.append("已收全额租金");
                    remarkBuilder.append("租约已终止");
                } else if ("ACTIVE".equals(order.getStatus()) || "PAID".equals(order.getStatus())) {
                    remarkBuilder.append("已收全额租金");
                    remarkBuilder.append("租约已生效");
                } else if ("COMPLETED".equals(order.getStatus())) {
                    remarkBuilder.append("已收全额租金");
                    remarkBuilder.append("租约已完成");
                }
                
                incomeRecord.put("remark", remarkBuilder.toString());
                incomeList.add(incomeRecord);
                
                // 2. 如果是已终止的订单，添加退租记录（负收入）
                if ("TERMINATED".equals(order.getStatus())) {
                    Map<String, Object> terminateRecord = new HashMap<>(incomeRecord);
                    
                    // 设置为退租收入类型
                    terminateRecord.put("incomeType", "TERMINATE");
                    terminateRecord.put("incomeTypeName", "退租记录");
                    
                    // 计算退租金额（负值）
                    LocalDate terminateDate = order.getActualTerminateDate() != null ? order.getActualTerminateDate() : LocalDate.now();
                    LocalDate orderEndDate = order.getEndDate();
                    // 计算从实际退租日到租约结束还有多少天
                    long remainingDays = ChronoUnit.DAYS.between(terminateDate, orderEndDate);
                    
                    // 日志输出当前计算的剩余天数，便于调试
                    log.debug("订单[{}]计算退款: 退租日={}, 结束日={}, 剩余天数={}", 
                            order.getId(), terminateDate, orderEndDate, remainingDays);
                    
                    // 如果订单备注中已包含剩余天数信息，尝试解析并使用
                    if (order.getRemark() != null && order.getRemark().contains("剩余天数:")) {
                        try {
                            // 尝试从备注中提取剩余天数信息
                            String remark = order.getRemark();
                            int remainingDaysIndex = remark.indexOf("剩余天数:") + "剩余天数:".length();
                            int endIndex = remark.indexOf(" 天", remainingDaysIndex);
                            if (endIndex > remainingDaysIndex) {
                                String remainingDaysStr = remark.substring(remainingDaysIndex, endIndex).trim();
                                remainingDays = Long.parseLong(remainingDaysStr);
                                log.debug("从订单备注中提取到剩余天数: {}", remainingDays);
                            }
                        } catch (Exception e) {
                            log.warn("从备注中提取剩余天数失败，使用计算值: {}", e.getMessage());
                        }
                    }
                    
                    if (remainingDays > 0) {
                        // 计算每天的租金
                        BigDecimal dailyRent = amount.divide(new BigDecimal(totalDays), 2, RoundingMode.HALF_UP);
                        // 计算应退还的租金 = 每天租金 × 剩余天数
                        BigDecimal refundAmount = dailyRent.multiply(new BigDecimal(remainingDays));
                        // 设为负数，表示退款
                        refundAmount = refundAmount.negate();
                        terminateRecord.put("amount", refundAmount);
                        
                        // 记录日志
                        log.debug("订单[{}]退款计算: 每日租金={}, 剩余天数={}, 退款金额={}", 
                                order.getId(), dailyRent, remainingDays, refundAmount);
                    } else {
                        terminateRecord.put("amount", BigDecimal.ZERO);
                    }
                    
                    // 交易时间改为终止时间
                    terminateRecord.put("transactionTime", 
                        order.getTerminateTime() != null ? order.getTerminateTime().toString() : 
                        (order.getActualTerminateDate() != null ? order.getActualTerminateDate().toString() : order.getUpdateTime().toString()));
                    
                    // 使用从订单备注中提取的信息，生成更精确的终止备注
                    StringBuilder terminateRemarkBuilder = new StringBuilder();
                    if (order.getRemark() != null && order.getRemark().contains("实际租期:")) {
                        // 尝试从订单备注中提取租期信息
                        terminateRemarkBuilder.append(order.getRemark());
                        if (remainingDays > 0) {
                            terminateRemarkBuilder.append(", 已退还未使用租期租金");
                        }
                    } else {
                        // 使用计算的剩余天数
                        terminateRemarkBuilder.append("提前终止 ").append(remainingDays).append(" 天");
                        if (remainingDays > 0) {
                            terminateRemarkBuilder.append(", 已退还未使用租期租金");
                        }
                        terminateRemarkBuilder.append(", 租约已终止");
                    }
                    
                    terminateRecord.put("remark", terminateRemarkBuilder.toString());
                    incomeList.add(terminateRecord);
                    
                    // 3. 如果有违约金并已支付，添加违约金收入记录
                    if (Boolean.TRUE.equals(order.getIsPenaltyPaid()) && order.getPenaltyAmount() != null && order.getPenaltyAmount().compareTo(BigDecimal.ZERO) > 0) {
                        Map<String, Object> penaltyRecord = new HashMap<>(incomeRecord);
                        
                        // 设置为违约金收入类型
                        penaltyRecord.put("incomeType", "PENALTY");
                        penaltyRecord.put("incomeTypeName", "违约金收入");
                        
                        // 设置违约金金额
                        penaltyRecord.put("amount", order.getPenaltyAmount());
                        
                        // 交易时间改为违约金支付时间
                        penaltyRecord.put("transactionTime", 
                            order.getPenaltyPayTime() != null ? order.getPenaltyPayTime().toString() : 
                            (order.getTerminateTime() != null ? order.getTerminateTime().toString() : order.getUpdateTime().toString()));
                        
                        penaltyRecord.put("remark", "因提前终止租约收取的违约金");
                        incomeList.add(penaltyRecord);
                    }
                }
            }
            
            // 创建Page对象
            Page<Map<String, Object>> pageResult = new Page<>(page, size, result.getTotal());
            pageResult.setRecords(incomeList);
            
            log.info("获取房东收入明细列表成功: landlordId={}, 总记录数={}", landlordId, pageResult.getTotal());
            return pageResult;
        } catch (Exception e) {
            log.error("获取房东收入明细列表失败: landlordId={}, error={}", landlordId, e.getMessage(), e);
            return new Page<>(page, size, 0);
        }
    }

    /**
     * 获取用户已租房源列表
     */
    @Override
    public Page<HouseInfoDTO> getUserRentedHouses(Page<HouseInfoDTO> page, Long userId) {
        log.info("获取用户已租房源: userId={}", userId);
        
        try {
            // 查询用户已支付的订单
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getUserId, userId)
                   .eq(Order::getStatus, "PAID")
                   .select(Order::getHouseId)
                   .groupBy(Order::getHouseId);
            
            List<Order> orders = baseMapper.selectList(wrapper);
            
            // 如果没有订单，返回空页
            if (orders.isEmpty()) {
                return new Page<>(page.getCurrent(), page.getSize(), 0);
            }
            
            // 从订单中提取房源ID列表
            List<Long> houseIds = orders.stream()
                                        .map(Order::getHouseId)
                                        .collect(Collectors.toList());
            
            // 获取房源详情列表
            List<HouseInfoDTO> houses = new ArrayList<>();
            for (Long houseId : houseIds) {
                HouseInfoDTO house = houseService.getHouseInfo(houseId);
                if (house != null) {
                    houses.add(house);
                }
            }
            
            // 手动处理分页
            long total = houses.size();
            int fromIndex = (int) ((page.getCurrent() - 1) * page.getSize());
            if (fromIndex >= total) {
                return new Page<>(page.getCurrent(), page.getSize(), total);
            }
            
            int toIndex = (int) Math.min(page.getCurrent() * page.getSize(), total);
            List<HouseInfoDTO> pageList = houses.subList(fromIndex, toIndex);
            
            // 设置分页结果
            Page<HouseInfoDTO> result = new Page<>(page.getCurrent(), page.getSize(), total);
            result.setRecords(pageList);
            
            log.info("获取用户已租房源成功: userId={}, total={}", userId, total);
            return result;
        } catch (Exception e) {
            log.error("获取用户已租房源失败: userId={}, error={}", userId, e.getMessage(), e);
            return new Page<>(page.getCurrent(), page.getSize(), 0);
        }
    }

    /**
     * 统计所有已支付订单的数量
     */
    @Override
    public int countAllPaidOrders() {
        log.info("统计所有已支付订单的数量");
        
        try {
            // 查询所有有效状态的订单
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            wrapper.and(w -> w.eq("status", "PAID")
                    .or().eq("status", "COMPLETED")
                    .or().eq("status", "ACTIVE")
                    .or().eq("status", "TERMINATED")
                    .or().eq("status", "TERMINATE_APPROVED")
                    .or().eq("status", "TERMINATE_REQUESTED"));
            
            // 将Long转换为int，确保安全转换
            Long count = baseMapper.selectCount(wrapper);
            log.info("统计到的有效订单数量: {}", count);
            
            // 检查是否超出int范围
            if (count > Integer.MAX_VALUE) {
                log.warn("订单数量超出Integer.MAX_VALUE范围: {}", count);
                return Integer.MAX_VALUE;
            }
            
            int intCount = count.intValue();
            
            log.info("统计所有有效订单数量成功: count={}", intCount);
            return intCount;
        } catch (Exception e) {
            log.error("统计所有有效订单数量失败: error={}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 计算平台总收入
     */
    @Override
    public BigDecimal calculateTotalIncome() {
        log.info("计算平台总收入");
        
        try {
            // 查询所有已支付的订单 - 扩大状态范围
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.and(w -> w
                    .eq(Order::getStatus, "PAID")
                    .or().eq(Order::getStatus, "COMPLETED")
                    .or().eq(Order::getStatus, "ACTIVE")
                    .or().eq(Order::getStatus, "TERMINATED")
                    .or().eq(Order::getStatus, "paid")
                    .or().eq(Order::getStatus, "completed")
                    .or().eq(Order::getStatus, "active")
                    .or().like(Order::getStatus, "PAY")
                    .or().like(Order::getStatus, "COMPLETE")
                    .or().like(Order::getStatus, "ACTIVE"));
            
            List<Order> orders = baseMapper.selectList(wrapper);
            
            log.info("查询到的订单数量: {}", orders.size());
            if (!orders.isEmpty()) {
                // 打印前5个订单的状态，用于调试
                for (int i = 0; i < Math.min(5, orders.size()); i++) {
                    Order order = orders.get(i);
                    log.info("订单ID: {}, 状态: {}, 金额: {}", order.getId(), order.getStatus(), order.getTotalAmount());
                }
            }
            
            // 计算总收入
            BigDecimal totalIncome = BigDecimal.ZERO;
            for (Order order : orders) {
                if ("TERMINATED".equals(order.getStatus())) {
                    // 对于已终止的订单
                    if (Boolean.TRUE.equals(order.getIsPenaltyPaid()) && order.getPenaltyAmount() != null) {
                        // 如果有违约金并已支付，添加违约金收入
                        BigDecimal penaltyIncome = order.getPenaltyAmount();
                        totalIncome = totalIncome.add(penaltyIncome);
                        
                        // 计算已收租金（总金额减去押金）
                        BigDecimal rentCollected = BigDecimal.ZERO;
                        if (order.getTotalAmount() != null && order.getDeposit() != null) {
                            rentCollected = order.getTotalAmount().subtract(order.getDeposit());
                        }
                        
                        // 计算租赁天数比例
                        LocalDate startDate = order.getStartDate();
                        LocalDate endDate = order.getEndDate();
                        LocalDate terminateDate = order.getActualTerminateDate() != null ? 
                                                order.getActualTerminateDate() : LocalDate.now();
                        
                        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
                        long daysRented = ChronoUnit.DAYS.between(startDate, terminateDate);
                        
                        // 计算从实际退租日到租约结束还有多少天
                        long remainingDays = ChronoUnit.DAYS.between(terminateDate, endDate);
                        
                        // 确保计算安全
                        if (totalDays > 0 && remainingDays >= 0) {
                            // 计算每天的租金
                            BigDecimal dailyRent = rentCollected.divide(new BigDecimal(totalDays), 2, RoundingMode.HALF_UP);
                            // 计算实际应收租金 = 每天租金 × 已使用天数
                            BigDecimal actualRent = dailyRent.multiply(new BigDecimal(daysRented));
                            
                            totalIncome = totalIncome.add(actualRent);
                            
                            log.debug("终止订单[{}]计算: 违约金收入({}) + 实际租金收入({}) = 总收入({})", 
                                    order.getId(), penaltyIncome, actualRent, penaltyIncome.add(actualRent));
                        } else {
                            // 异常情况，直接加上租金收入
                            totalIncome = totalIncome.add(rentCollected);
                            log.debug("终止订单[{}]计算(异常日期): 违约金({}) + 租金收入({}) = 总收入({})", 
                                    order.getId(), penaltyIncome, rentCollected, penaltyIncome.add(rentCollected));
                        }
                    } else {
                        // 无违约金或未支付违约金的终止订单，只计算实际租期内的租金
                        if (order.getTotalAmount() != null && order.getDeposit() != null) {
                            BigDecimal rentCollected = order.getTotalAmount().subtract(order.getDeposit());
                            
                            // 计算租赁天数比例
                            LocalDate startDate = order.getStartDate();
                            LocalDate endDate = order.getEndDate();
                            LocalDate terminateDate = order.getActualTerminateDate() != null ? 
                                                    order.getActualTerminateDate() : LocalDate.now();
                            
                            long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
                            long daysRented = ChronoUnit.DAYS.between(startDate, terminateDate);
                            
                            // 计算从实际退租日到租约结束还有多少天
                            long remainingDays = ChronoUnit.DAYS.between(terminateDate, endDate);
                            
                            // 确保计算安全
                            if (totalDays > 0 && remainingDays >= 0) {
                                // 计算每天的租金
                                BigDecimal dailyRent = rentCollected.divide(new BigDecimal(totalDays), 2, RoundingMode.HALF_UP);
                                // 计算实际应收租金 = 每天租金 × 已使用天数
                                BigDecimal actualRent = dailyRent.multiply(new BigDecimal(daysRented));
                                
                                totalIncome = totalIncome.add(actualRent);
                                
                                log.debug("终止订单[{}]计算(无违约金): 实际租金收入({}) = 总收入({})", 
                                        order.getId(), actualRent, actualRent);
                            } else {
                                // 异常情况，直接加上租金收入
                                totalIncome = totalIncome.add(rentCollected);
                                log.debug("终止订单[{}]计算(异常日期,无违约金): 租金收入({}) = 总收入({})", 
                                        order.getId(), rentCollected, rentCollected);
                            }
                        }
                    }
                } else {
                    // 正常订单收入计算（不包括押金）
                    if (order.getTotalAmount() != null && order.getDeposit() != null) {
                        // 从总金额中减去押金，得到实际收入
                        BigDecimal orderIncome = order.getTotalAmount().subtract(order.getDeposit());
                        totalIncome = totalIncome.add(orderIncome);
                        
                        log.debug("正常订单[{}]收入计算: 总金额({}) - 押金({}) = 实际收入({})", 
                                order.getId(), order.getTotalAmount(), order.getDeposit(), orderIncome);
                    } else if (order.getTotalAmount() != null) {
                        // 如果没有押金信息，按照总金额的80%估算实际收入（假设押金通常是一个月租金）
                        BigDecimal estimatedIncome = order.getTotalAmount().multiply(new BigDecimal("0.8"));
                        totalIncome = totalIncome.add(estimatedIncome);
                        
                        log.debug("正常订单[{}]收入估算(无押金信息): 总金额({}) * 0.8 = 估算收入({})", 
                                order.getId(), order.getTotalAmount(), estimatedIncome);
                    }
                }
            }
            
            log.info("计算平台总收入成功: totalIncome={}", totalIncome);
            return totalIncome;
        } catch (Exception e) {
            log.error("计算平台总收入失败: error={}", e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 计算平台本月收入
     */
    @Override
    public BigDecimal calculateMonthlyIncome() {
        log.info("计算平台本月收入");
        
        try {
            // 获取当月的起始日期和结束日期
            LocalDate now = LocalDate.now();
            LocalDate firstDayOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate lastDayOfMonth = now.with(TemporalAdjusters.lastDayOfMonth());
            
            LocalDateTime startDateTime = firstDayOfMonth.atStartOfDay();
            LocalDateTime endDateTime = lastDayOfMonth.atTime(23, 59, 59);
            
            // 查询本月内的所有有效订单（扩大查询范围）
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            wrapper.and(w -> w.eq("status", "PAID")
                    .or().eq("status", "COMPLETED")
                    .or().eq("status", "ACTIVE")
                    .or().eq("status", "TERMINATED")
                    .or().eq("status", "TERMINATE_APPROVED")
                    .or().eq("status", "TERMINATE_REQUESTED"))
                .and(w -> w.between("create_time", startDateTime, endDateTime)
                    .or().between("pay_time", startDateTime, endDateTime));
            
            List<Order> orders = baseMapper.selectList(wrapper);
            
            log.info("本月查询到的订单数量: {}", orders.size());
            if (!orders.isEmpty()) {
                // 打印前5个订单的状态，用于调试
                for (int i = 0; i < Math.min(5, orders.size()); i++) {
                    Order order = orders.get(i);
                    log.info("本月订单ID: {}, 状态: {}, 金额: {}", order.getId(), order.getStatus(), order.getTotalAmount());
                }
            }
            
            // 计算本月收入
            BigDecimal monthlyIncome = BigDecimal.ZERO;
            for (Order order : orders) {
                if ("TERMINATED".equals(order.getStatus())) {
                    // 对于已终止的订单
                    if (Boolean.TRUE.equals(order.getIsPenaltyPaid()) && order.getPenaltyAmount() != null) {
                        // 如果有违约金并已支付，添加违约金收入
                        BigDecimal penaltyIncome = order.getPenaltyAmount();
                        monthlyIncome = monthlyIncome.add(penaltyIncome);
                        
                        // 计算已收租金（总金额减去押金）
                        BigDecimal rentCollected = BigDecimal.ZERO;
                        if (order.getTotalAmount() != null && order.getDeposit() != null) {
                            rentCollected = order.getTotalAmount().subtract(order.getDeposit());
                        }
                        
                        // 计算租赁天数比例
                        LocalDate startDate = order.getStartDate();
                        LocalDate endDate = order.getEndDate();
                        LocalDate terminateDate = order.getActualTerminateDate() != null ? 
                                                order.getActualTerminateDate() : LocalDate.now();
                        
                        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
                        long daysRented = ChronoUnit.DAYS.between(startDate, terminateDate);
                        
                        // 计算从实际退租日到租约结束还有多少天
                        long remainingDays = ChronoUnit.DAYS.between(terminateDate, endDate);
                        
                        // 确保计算安全
                        if (totalDays > 0 && remainingDays >= 0) {
                            // 计算每天的租金
                            BigDecimal dailyRent = rentCollected.divide(new BigDecimal(totalDays), 2, RoundingMode.HALF_UP);
                            // 计算实际应收租金 = 每天租金 × 已使用天数
                            BigDecimal actualRent = dailyRent.multiply(new BigDecimal(daysRented));
                            
                            monthlyIncome = monthlyIncome.add(actualRent);
                            
                            log.debug("本月终止订单[{}]计算: 违约金收入({}) + 实际租金收入({}) = 总收入({})", 
                                    order.getId(), penaltyIncome, actualRent, penaltyIncome.add(actualRent));
                        } else {
                            // 异常情况，直接加上租金收入
                            monthlyIncome = monthlyIncome.add(rentCollected);
                            log.debug("本月终止订单[{}]计算(异常日期): 违约金({}) + 租金收入({}) = 总收入({})", 
                                    order.getId(), penaltyIncome, rentCollected, penaltyIncome.add(rentCollected));
                        }
                    } else {
                        // 无违约金或未支付违约金的终止订单，只计算实际租期内的租金
                        if (order.getTotalAmount() != null && order.getDeposit() != null) {
                            BigDecimal rentCollected = order.getTotalAmount().subtract(order.getDeposit());
                            
                            // 计算租赁天数比例
                            LocalDate startDate = order.getStartDate();
                            LocalDate endDate = order.getEndDate();
                            LocalDate terminateDate = order.getActualTerminateDate() != null ? 
                                                    order.getActualTerminateDate() : LocalDate.now();
                            
                            long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
                            long daysRented = ChronoUnit.DAYS.between(startDate, terminateDate);
                            
                            // 计算从实际退租日到租约结束还有多少天
                            long remainingDays = ChronoUnit.DAYS.between(terminateDate, endDate);
                            
                            // 确保计算安全
                            if (totalDays > 0 && remainingDays >= 0) {
                                // 计算每天的租金
                                BigDecimal dailyRent = rentCollected.divide(new BigDecimal(totalDays), 2, RoundingMode.HALF_UP);
                                // 计算实际应收租金 = 每天租金 × 已使用天数
                                BigDecimal actualRent = dailyRent.multiply(new BigDecimal(daysRented));
                                
                                monthlyIncome = monthlyIncome.add(actualRent);
                                
                                log.debug("本月终止订单[{}]计算(无违约金): 实际租金收入({}) = 总收入({})", 
                                        order.getId(), actualRent, actualRent);
                            } else {
                                // 异常情况，直接加上租金收入
                                monthlyIncome = monthlyIncome.add(rentCollected);
                                log.debug("本月终止订单[{}]计算(异常日期,无违约金): 租金收入({}) = 总收入({})", 
                                        order.getId(), rentCollected, rentCollected);
                            }
                        }
                    }
                } else {
                    // 正常订单收入计算（不包括押金）
                    if (order.getTotalAmount() != null && order.getDeposit() != null) {
                        BigDecimal orderIncome = order.getTotalAmount().subtract(order.getDeposit());
                        monthlyIncome = monthlyIncome.add(orderIncome);
                        
                        log.debug("本月正常订单[{}]收入计算: 总金额({}) - 押金({}) = 实际收入({})", 
                                order.getId(), order.getTotalAmount(), order.getDeposit(), orderIncome);
                    } else if (order.getTotalAmount() != null) {
                        // 如果没有押金信息，按照总金额的80%估算实际收入
                        BigDecimal estimatedIncome = order.getTotalAmount().multiply(new BigDecimal("0.8"));
                        monthlyIncome = monthlyIncome.add(estimatedIncome);
                        
                        log.debug("本月正常订单[{}]收入估算(无押金信息): 总金额({}) * 0.8 = 估算收入({})", 
                                order.getId(), order.getTotalAmount(), estimatedIncome);
                    }
                }
            }
            
            log.info("计算平台本月收入成功: monthlyIncome={}", monthlyIncome);
            return monthlyIncome;
        } catch (Exception e) {
            log.error("计算平台本月收入失败: error={}", e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 获取月度收入数据（用于图表显示）
     */
    @Override
    public List<Map<String, Object>> getMonthlyIncomeData() {
        log.info("获取月度收入数据");
        
        try {
            List<Map<String, Object>> monthlyData = new ArrayList<>();
            LocalDate now = LocalDate.now();
            
            // 计算近12个月的收入数据
            for (int i = 11; i >= 0; i--) {
                LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
                LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
                
                LocalDateTime startDateTime = monthStart.atStartOfDay();
                LocalDateTime endDateTime = monthEnd.atTime(23, 59, 59);
                
                // 查询指定月份内的所有有效订单
                QueryWrapper<Order> wrapper = new QueryWrapper<>();
                wrapper.and(w -> w.eq("status", "PAID")
                        .or().eq("status", "COMPLETED")
                        .or().eq("status", "ACTIVE")
                        .or().eq("status", "TERMINATED")
                        .or().eq("status", "TERMINATE_APPROVED")
                        .or().eq("status", "TERMINATE_REQUESTED"))
                    .and(w -> w.between("create_time", startDateTime, endDateTime)
                        .or().between("pay_time", startDateTime, endDateTime));
                
                List<Order> orders = baseMapper.selectList(wrapper);
                
                // 计算月收入
                BigDecimal monthIncome = BigDecimal.ZERO;
                for (Order order : orders) {
                    BigDecimal orderIncome = order.getTotalAmount();
                    // 排除押金
                    if (order.getDeposit() != null && order.getDeposit().compareTo(BigDecimal.ZERO) > 0) {
                        orderIncome = orderIncome.subtract(order.getDeposit());
                    }
                    monthIncome = monthIncome.add(orderIncome);
                }
                
                // 格式化月份为 "yyyy-MM" 格式
                String monthLabel = monthStart.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                
                Map<String, Object> monthData = new HashMap<>();
                monthData.put("month", monthLabel);
                monthData.put("income", monthIncome);
                monthlyData.add(monthData);
            }
            
            log.info("获取月度收入数据成功: dataSize={}", monthlyData.size());
            return monthlyData;
        } catch (Exception e) {
            log.error("获取月度收入数据失败: error={}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 用户申请退租
     * @param dto 退租信息
     * @param userId 用户ID
     * @return 是否申请成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyTerminate(TerminateDTO dto, Long userId) {
        log.info("用户申请退租: userId={}, orderDto={}", userId, dto);
        
        // 查询订单
        Order order = getById(dto.getOrderId());
        if (order == null) {
            log.error("订单不存在: orderId={}", dto.getOrderId());
            throw new BusinessException("订单不存在");
        }
        
        // 打印订单详情
        log.info("订单详情: id={}, status={}, 完整订单信息: {}", order.getId(), order.getStatus(), order);
        
        // 验证订单归属
        if (!order.getUserId().equals(userId)) {
            log.error("无权操作该订单: orderId={}, userId={}, orderUserId={}", 
                    dto.getOrderId(), userId, order.getUserId());
            throw new BusinessException("您无权操作该订单");
        }
        
        // 检查退租申请次数限制
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getHouseId, order.getHouseId())
                   .eq(Order::getUserId, userId)
                   .in(Order::getStatus, Arrays.asList("TERMINATE_REQUESTED", "TERMINATE_APPROVED", "ACTIVE"))
                   .isNotNull(Order::getTerminateRequestTime);
        
        long requestCount = count(queryWrapper);
        log.info("用户退租申请次数检查：houseId={}, userId={}, 已申请次数={}", order.getHouseId(), userId, requestCount);
        
        // 如果已经达到3次申请限制，则拒绝
        if (requestCount >= 3) {
            log.error("用户退租申请次数已达上限(3次): userId={}, houseId={}, requestCount={}", 
                    userId, order.getHouseId(), requestCount);
            throw new BusinessException("您已超过对该房源的退租申请次数限制(最多3次)，无法再次申请退租");
        }
        
        // 验证订单状态 - 添加特殊调试信息
        String orderStatus = order.getStatus();
        log.info("状态验证详细信息: ");
        log.info("- 当前订单状态: '{}'", orderStatus);
        log.info("- PAID状态比较: '{}' == '{}' 结果={}", orderStatus, "PAID", "PAID".equals(orderStatus));
        log.info("- ACTIVE状态比较: '{}' == '{}' 结果={}", orderStatus, "ACTIVE", "ACTIVE".equals(orderStatus));
        log.info("- 状态字符串长度: {}", orderStatus.length());
        log.info("- 状态字符串字节: {}", orderStatus.getBytes());
        log.info("- 使用trim比较结果: PAID={}, ACTIVE={}", 
                "PAID".equalsIgnoreCase(orderStatus.trim()), 
                "ACTIVE".equalsIgnoreCase(orderStatus.trim()));

        // 检查每个字符的ASCII码
        StringBuilder sb = new StringBuilder("- 状态字符串ASCII码: ");
        for (int i = 0; i < orderStatus.length(); i++) {
            char c = orderStatus.charAt(i);
            sb.append((int)c).append(" ");
        }
        log.info(sb.toString());

        // 使用更宽松的方式检查状态，忽略大小写和空格
        boolean isPaidOrActive = "PAID".equalsIgnoreCase(orderStatus.trim()) || 
                                 "ACTIVE".equalsIgnoreCase(orderStatus.trim());

        if (!isPaidOrActive) {
            log.error("订单状态不允许申请退租: orderId={}, status='{}', 只有已支付(PAID)或租赁中(ACTIVE)的订单可以申请退租", 
                    dto.getOrderId(), orderStatus);
            throw new BusinessException("当前订单状态不允许申请退租，只有已支付或租赁中的订单可以申请退租");
        }
        
        log.info("订单状态验证通过，继续处理退租申请");
        
        // 期望退租日期处理
        LocalDate expectedDate = null;
        try {
            expectedDate = LocalDate.parse(dto.getExpectedDate(), DateTimeFormatter.ISO_DATE);
        } catch (Exception e) {
            log.error("期望退租日期格式错误: {}", dto.getExpectedDate(), e);
            throw new BusinessException("期望退租日期格式错误，请使用YYYY-MM-DD格式");
        }
        
        // 期望退租日期不能早于当前日期
        LocalDate today = LocalDate.now();
        if (expectedDate.isBefore(today)) {
            log.error("期望退租日期不能早于当前日期: expected={}, today={}", expectedDate, today);
            throw new BusinessException("期望退租日期不能早于当前日期");
        }
        
        // 计算违约金
        BigDecimal penaltyAmount = calculateTerminationPenalty(dto.getOrderId());
        
        // 更新订单状态为申请退租
        order.setStatus("TERMINATE_REQUESTED");
        order.setTerminateReason(dto.getReason());
        order.setExpectedTerminateDate(expectedDate);
        order.setPenaltyAmount(penaltyAmount);
        order.setTerminateRequestTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        
        boolean updated = updateById(order);
        
        // 添加退租记录（可选，如果有单独的退租记录表）
        // terminateRecordMapper.insert(new TerminateRecord(...));
        
        // 发送通知给房东
        try {
            // 获取房源和房东信息
            HouseInfoDTO houseInfo = houseService.getHouseInfo(order.getHouseId());
            UserInfoDTO landlordInfo = userService.getUserInfo(order.getLandlordId());
            UserInfoDTO tenantInfo = userService.getUserInfo(order.getUserId());
            
            if (houseInfo != null && tenantInfo != null) {
                // 构建消息内容 - 使用StringBuilder替代String.format避免引号问题
                StringBuilder messageSb = new StringBuilder();
                messageSb.append("租客 ").append(tenantInfo.getNickname())
                          .append(" 申请退租房源\"").append(houseInfo.getTitle())
                          .append("\"，退租原因：").append(dto.getReason())
                          .append("，期望退租日期：").append(dto.getExpectedDate())
                          .append("，请及时处理。");
                String messageContent = messageSb.toString();
                
                // 发送系统消息给房东
                messageService.sendSystemMessage(order.getLandlordId(), "退租申请通知", messageContent, "TERMINATE", order.getId());
            }
        } catch (Exception e) {
            log.error("发送退租通知失败: {}", e.getMessage(), e);
            // 通知发送失败不影响主流程
        }
        
        return updated;
    }

    /**
     * 房东处理退租申请
     * @param orderId 订单ID
     * @param landlordId 房东ID
     * @param approved 是否同意退租
     * @param rejectReason 拒绝原因（如果拒绝）
     * @return 处理结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleTerminateRequest(Long orderId, Long landlordId, boolean approved, String rejectReason) {
        log.info("房东处理退租申请: landlordId={}, orderId={}, approved={}", landlordId, orderId, approved);
        
        // 查询订单
        Order order = getById(orderId);
        if (order == null) {
            log.error("订单不存在: orderId={}", orderId);
            throw new BusinessException("订单不存在");
        }
        
        // 验证房东身份
        if (!order.getLandlordId().equals(landlordId)) {
            log.error("无权处理该订单: orderId={}, landlordId={}, orderLandlordId={}", 
                    orderId, landlordId, order.getLandlordId());
            throw new BusinessException("您无权处理该订单");
        }
        
        // 验证订单状态
        if (!"TERMINATE_REQUESTED".equals(order.getStatus())) {
            log.error("订单状态不允许处理退租申请: orderId={}, status={}, 只有处于退租申请中(TERMINATE_REQUESTED)的订单可以处理", 
                    orderId, order.getStatus());
            throw new BusinessException("当前订单状态不允许处理退租申请，订单必须处于退租申请中状态");
        }
        
        if (approved) {
            // 同意退租
            order.setStatus("TERMINATE_APPROVED");
            
            // 移除此处更新房源状态的逻辑，应在 confirmTermination 中处理
            // houseService.updateHouseStatus(order.getHouseId(), "APPROVED"); 
        } else {
            // 拒绝退租
            order.setStatus("ACTIVE"); // 恢复为租赁中状态
            order.setTerminateRejectReason(rejectReason);
        }
        
        order.setUpdateTime(LocalDateTime.now());
        boolean updated = updateById(order);
        
        // 发送通知给租客
        try {
            UserInfoDTO tenantInfo = userService.getUserInfo(order.getUserId());
            HouseInfoDTO houseInfo = houseService.getHouseInfo(order.getHouseId());
            
            if (houseInfo != null && tenantInfo != null) {
                StringBuilder sb = new StringBuilder();
                if (approved) {
                    // 使用字符串拼接避免引号问题
                    sb.append("您申请退租的房源\"").append(houseInfo.getTitle())
                      .append("\"已获房东同意，请按约定日期办理退租手续。");
                } else {
                    // 使用字符串拼接避免引号问题
                    sb.append("您申请退租的房源\"").append(houseInfo.getTitle())
                      .append("\"被房东拒绝，原因：").append(rejectReason);
                }
                String messageContent = sb.toString();
                
                // 发送系统消息给租客
                messageService.sendSystemMessage(order.getUserId(), approved ? "退租申请已同意" : "退租申请被拒绝", messageContent, "TERMINATE", order.getId());
            }
        } catch (Exception e) {
            log.error("发送退租处理通知失败: {}", e.getMessage(), e);
            // 通知发送失败不影响主流程
        }
        
        return updated;
    }

    /**
     * 房东确认退租完成
     * @param orderId 订单ID
     * @param landlordId 房东ID
     * @return 是否确认成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmTermination(Long orderId, Long landlordId) {
        log.info("房东确认退租完成: landlordId={}, orderId={}", landlordId, orderId);
        
        // 查询订单
        Order order = getById(orderId);
        if (order == null) {
            log.error("订单不存在: orderId={}", orderId);
            throw new BusinessException("订单不存在");
        }
        
        // 验证房东身份
        if (!order.getLandlordId().equals(landlordId)) {
            log.error("无权操作该订单: orderId={}, landlordId={}, orderLandlordId={}", 
                    orderId, landlordId, order.getLandlordId());
            throw new BusinessException("您无权操作该订单");
        }
        
        // 验证订单状态
        if (!"TERMINATE_APPROVED".equals(order.getStatus())) {
            log.error("订单状态不允许确认退租: orderId={}, status={}, 只有已同意退租(TERMINATE_APPROVED)的订单可以确认完成", 
                    orderId, order.getStatus());
            throw new BusinessException("当前订单状态不允许确认退租，订单必须是已同意退租状态");
        }
        
        // 更新订单状态为 TERMINATED
        order.setStatus("TERMINATED");
        order.setActualTerminateDate(LocalDate.now());
        order.setUpdateTime(LocalDateTime.now());
        
        boolean orderUpdated = updateById(order);
        if (!orderUpdated) {
            log.error("更新订单状态为TERMINATED失败: orderId={}", orderId);
            // 即使订单更新失败，也尝试更新房源状态，但记录错误
            // 理论上事务应回滚，但这里增加健壮性
        }
        
        // 更新房源状态为 APPROVED (可出租)
        try {
            log.info("尝试将房源状态更新为APPROVED: houseId={}", order.getHouseId());
            boolean houseUpdated = houseService.updateHouseStatus(order.getHouseId(), "APPROVED");
            if (houseUpdated) {
                log.info("退租完成后，房源状态已成功更新为APPROVED: houseId={}", order.getHouseId());
            } else {
                // 如果更新失败，记录警告，可能需要手动处理或后台任务修复
                log.warn("退租完成后，尝试更新房源状态为APPROVED失败: houseId={}", order.getHouseId());
                // 可以考虑抛出异常让事务回滚，或者记录下来后续处理
                // throw new BusinessException("更新房源状态失败"); 
            }
        } catch (Exception e) {
            log.error("退租完成后，更新房源状态为APPROVED时发生异常: houseId={}, error={}", 
                    order.getHouseId(), e.getMessage(), e);
            // 同样，可以考虑抛出异常让事务回滚
            // throw new BusinessException("更新房源状态时发生异常");
        }
        
        // 发送通知给租客 (无论房源状态是否更新成功都发送)
        try {
            UserInfoDTO tenantInfo = userService.getUserInfo(order.getUserId());
            HouseInfoDTO houseInfo = houseService.getHouseInfo(order.getHouseId());
            
            if (houseInfo != null && tenantInfo != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("您的房源\"").append(houseInfo.getTitle())
                  .append("\"退租手续已完成，感谢您的使用。");
                String messageContent = sb.toString();
                messageService.sendSystemMessage(order.getUserId(), "退租完成通知", messageContent, "TERMINATE", order.getId());
            }
        } catch (Exception e) {
            log.error("发送退租完成通知失败: {}", e.getMessage(), e);
            // 通知发送失败不影响主流程
        }
        
        // 返回订单是否成功更新为TERMINATED状态
        return orderUpdated;
    }

    /**
     * 计算退租违约金
     * @param orderId 订单ID
     * @return 违约金金额
     */
    @Override
    public BigDecimal calculateTerminationPenalty(Long orderId) {
        // 查询订单
        Order order = getById(orderId);
        if (order == null) {
            log.error("订单不存在，无法计算违约金: orderId={}", orderId);
            throw new BusinessException("订单不存在");
        }
        
        // 获取合同开始和结束日期
        LocalDate startDate = order.getStartDate();
        LocalDate endDate = order.getEndDate();
        LocalDate today = LocalDate.now();
        
        // 计算已租赁天数
        long daysRented = ChronoUnit.DAYS.between(startDate, today);
        
        // 计算合同总天数
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        
        // 根据合同约定计算违约金（假设是剩余租金的30%）
        if (daysRented >= totalDays) {
            // 已超过合同期限，无需支付违约金
            return BigDecimal.ZERO;
        }
        
        // 剩余天数
        long remainingDays = totalDays - daysRented;
        
        // 计算剩余租金
        BigDecimal monthlyRent = order.getMonthlyRent();
        BigDecimal dailyRent = monthlyRent.divide(new BigDecimal(30), 2, RoundingMode.HALF_UP);
        BigDecimal remainingRent = dailyRent.multiply(new BigDecimal(remainingDays));
        
        // 违约金为剩余租金的30%
        BigDecimal penalty = remainingRent.multiply(new BigDecimal("0.3")).setScale(2, RoundingMode.HALF_UP);
        
        return penalty;
    }

    /**
     * 计算房东违约金收入
     */
    @Override
    public BigDecimal calculateLandlordPenaltyIncome(Long landlordId) {
        log.info("计算房东违约金收入: landlordId={}", landlordId);
        
        try {
            // 查询该房东有违约金的订单，包括所有可能的状态
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getLandlordId, landlordId)
                   .and(w -> w.eq(Order::getStatus, "TERMINATED")
                          .or().eq(Order::getStatus, "TERMINATE_APPROVED")
                          .or().eq(Order::getStatus, "ACTIVE")
                          .or().eq(Order::getStatus, "COMPLETED"))
                   .isNotNull(Order::getPenaltyAmount)
                   .gt(Order::getPenaltyAmount, BigDecimal.ZERO);
            
            // 输出SQL日志
            log.info("房东违约金收入SQL查询条件: {}", wrapper.getCustomSqlSegment());
            List<Order> orders = baseMapper.selectList(wrapper);
            log.info("房东违约金收入查询到订单数量: {}", orders.size());
            
            // 计算总违约金收入
            BigDecimal totalPenalty = BigDecimal.ZERO;
            for (Order order : orders) {
                if (order.getPenaltyAmount() != null) {
                    totalPenalty = totalPenalty.add(order.getPenaltyAmount());
                    log.debug("订单[{}]违约金: {}", order.getId(), order.getPenaltyAmount());
                }
            }
            
            log.info("计算房东违约金收入成功: landlordId={}, totalPenalty={}", landlordId, totalPenalty);
            return totalPenalty;
        } catch (Exception e) {
            log.error("计算房东违约金收入失败: landlordId={}, error={}", landlordId, e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * 计算房东当前收到的押金总额
     */
    @Override
    public BigDecimal calculateLandlordTotalDeposit(Long landlordId) {
        log.info("计算房东押金总额: landlordId={}", landlordId);
        
        try {
            // 查询该房东活跃的订单
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getLandlordId, landlordId)
                   .and(w -> w.eq(Order::getStatus, "PAID")
                          .or().eq(Order::getStatus, "ACTIVE"));
            
            List<Order> orders = baseMapper.selectList(wrapper);
            
            // 计算总押金
            BigDecimal totalDeposit = BigDecimal.ZERO;
            for (Order order : orders) {
                if (order.getDeposit() != null) {
                    totalDeposit = totalDeposit.add(order.getDeposit());
                }
            }
            
            log.info("计算房东押金总额成功: landlordId={}, totalDeposit={}", landlordId, totalDeposit);
            return totalDeposit;
        } catch (Exception e) {
            log.error("计算房东押金总额失败: landlordId={}, error={}", landlordId, e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 统计房东待处理的退租申请数量
     */
    @Override
    public int countLandlordTerminateRequests(Long landlordId) {
        log.info("统计房东待处理的退租申请数量: landlordId={}", landlordId);
        
        try {
            // 检查OrderStatus枚举的值
            String terminateRequestStatus = "TERMINATE_REQUESTED";
            try {
                terminateRequestStatus = OrderStatus.TERMINATE_REQUESTED.name();
                log.info("使用OrderStatus枚举值: {}", terminateRequestStatus);
            } catch (Exception e) {
                log.warn("获取OrderStatus枚举值失败，使用字符串常量: {}, 错误: {}", terminateRequestStatus, e.getMessage());
            }
            
            // 查询该房东待处理的退租申请
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getLandlordId, landlordId)
                   .eq(Order::getStatus, terminateRequestStatus);
            
            // 输出SQL查询条件
            log.info("房东退租申请SQL查询条件: {}", wrapper.getCustomSqlSegment());
            
            // 使用selectCount返回的是Long，需要转换为int
            Long count = baseMapper.selectCount(wrapper);
            
            // 检查是否超出int范围
            if (count > Integer.MAX_VALUE) {
                log.warn("退租申请数量超出Integer.MAX_VALUE范围: {}", count);
                return Integer.MAX_VALUE;
            }
            
            int intCount = count.intValue();
            
            log.info("统计房东待处理的退租申请数量成功: landlordId={}, count={}", landlordId, intCount);
            return intCount;
        } catch (Exception e) {
            log.error("统计房东待处理的退租申请数量失败: landlordId={}, error={}", landlordId, e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public boolean handleTerminateRequest(Long orderId, Long landlordId, boolean approved, String rejectReason, 
                                         LocalDate actualTerminateDate, BigDecimal penaltyAmount, String remark) {
        // 查询订单
        Order order = getById(orderId);
        if (order == null) {
            log.error("订单不存在, orderId: {}", orderId);
            throw new BusinessException("订单不存在");
        }

        // 检查房东身份
        if (!order.getLandlordId().equals(landlordId)) {
            log.error("非法操作，不是该订单的房东, orderId: {}, landlordId: {}", orderId, landlordId);
            throw new BusinessException("非法操作，您不是该订单的房东");
        }

        // 检查订单状态
        if (!"TERMINATE_REQUESTED".equals(order.getStatus())) {
            log.error("订单状态不是'TERMINATE_REQUESTED', orderId: {}, orderStatus: {}", orderId, order.getStatus());
            throw new BusinessException("订单状态不允许此操作");
        }

        // 处理退租申请
        if (approved) {
            // 同意退租
            order.setStatus("TERMINATE_APPROVED");
            // 设置实际退租日期
            if (actualTerminateDate != null) {
                order.setActualTerminateDate(actualTerminateDate);
                
                // 计算并记录相关信息 - 计算剩余天数和总天数
                LocalDate startDate = order.getStartDate();
                LocalDate endDate = order.getEndDate();
                
                if (startDate != null && endDate != null) {
                    long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
                    long usedDays = ChronoUnit.DAYS.between(startDate, actualTerminateDate);
                    long remainingDays = totalDays - usedDays;
                    
                    if (usedDays < 0) usedDays = 0;
                    if (remainingDays < 0) remainingDays = 0;
                    
                    // 记录实际使用天数和剩余天数到订单备注
                    StringBuilder terminateInfoBuilder = new StringBuilder();
                    if (StringUtils.hasText(order.getRemark())) {
                        terminateInfoBuilder.append(order.getRemark()).append(" | ");
                    }
                    terminateInfoBuilder.append("实际租期: ").append(usedDays).append("/").append(totalDays)
                        .append(" 天, 剩余天数: ").append(remainingDays).append(" 天");
                    
                    // 如果有用户提供的备注，添加到终止信息后面
                    if (StringUtils.hasText(remark)) {
                        terminateInfoBuilder.append(", ").append(remark);
                    }
                    
                    order.setRemark(terminateInfoBuilder.toString());
                    
                    log.info("计算退租信息: orderId={}, 总天数={}, 已使用={}, 剩余={}", 
                          orderId, totalDays, usedDays, remainingDays);
                }
            } else {
                // 如果没有提供实际退租日期，使用预期退租日期
                order.setActualTerminateDate(order.getExpectedTerminateDate());
            }
            
            // 设置违约金金额
            if (penaltyAmount != null) {
                order.setPenaltyAmount(penaltyAmount);
            }
            
            // 设置终止时间
            order.setTerminateTime(LocalDateTime.now());
        } else {
            // 拒绝退租
            order.setStatus("ACTIVE");
            order.setTerminateRejectReason(rejectReason);
        }

        // 记录日志
        log.info("处理退租申请, orderId: {}, landlordId: {}, approved: {}, rejectReason: {}, actualTerminateDate: {}, penaltyAmount: {}, remark: {}", 
                orderId, landlordId, approved, rejectReason, actualTerminateDate, penaltyAmount, remark);

        // 更新订单
        boolean updated = updateById(order);
        return updated;
    }

    /**
     * 根据多个状态查询订单
     */
    @Override
    public Page<OrderDTO> pageOrdersByStatuses(Page<Order> page, List<String> statuses) {
        log.info("按多个状态查询订单: page={}, size={}, statuses={}", page.getCurrent(), page.getSize(), statuses);
        
        try {
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            
            // 如果指定了状态，则按状态筛选，使用更灵活的匹配方式
            if (statuses != null && !statuses.isEmpty()) {
                wrapper.and(w -> {
                    for (String status : statuses) {
                        // 为每个状态添加多种匹配方式
                        w.or(sw -> sw
                            .eq(Order::getStatus, status)
                            .or()
                            .eq(Order::getStatus, status.toUpperCase())
                            .or()
                            .eq(Order::getStatus, status.toLowerCase())
                            .or()
                            .like(Order::getStatus, status)
                        );
                    }
                });
            }
            
            // 按创建时间降序排序
            wrapper.orderByDesc(Order::getCreateTime);
            
            // 记录查询条件
            log.info("订单查询条件: statuses={}, queryWrapper={}", statuses, wrapper.getCustomSqlSegment());
            
            // 执行分页查询
            Page<Order> result = baseMapper.selectPage(page, wrapper);
            
            // 记录查询结果数量
            log.info("订单查询结果: 总数={}", result.getTotal());
            
            // 如果结果为空，尝试进行无条件查询
            if (result.getRecords().isEmpty() && statuses != null && !statuses.isEmpty()) {
                log.info("指定状态查询结果为空，尝试查询所有订单");
                LambdaQueryWrapper<Order> allWrapper = new LambdaQueryWrapper<>();
                allWrapper.orderByDesc(Order::getCreateTime);
                result = baseMapper.selectPage(page, allWrapper);
                log.info("查询所有订单结果: 总数={}", result.getTotal());
            }
            
            // 转换为DTO
            Page<OrderDTO> dtoPage = convertToOrderDTOPage(result);
            
            // 打印前5条记录的状态，用于调试
            if (!dtoPage.getRecords().isEmpty()) {
                log.info("查询结果前5条记录状态:");
                dtoPage.getRecords().stream().limit(5).forEach(dto -> 
                    log.info("订单ID: {}, 状态: {}, 金额: {}", dto.getId(), dto.getStatus(), dto.getTotalAmount()));
            }
            
            log.info("按多个状态查询订单成功: statuses={}, 总数={}", statuses, dtoPage.getTotal());
            return dtoPage;
        } catch (Exception e) {
            log.error("按多个状态查询订单失败: error={}", e.getMessage(), e);
            // 返回空结果而不是抛出异常，避免前端崩溃
            Page<OrderDTO> emptyPage = new Page<>(page.getCurrent(), page.getSize());
            emptyPage.setRecords(new ArrayList<>());
            return emptyPage;
        }
    }

    /**
     * 获取所有订单列表
     */
    @Override
    public List<Order> list() {
        log.info("获取所有订单列表");
        
        try {
            // 使用父类的list方法获取所有订单
            List<Order> orders = super.list();
            log.info("获取所有订单列表成功, 总数: {}", orders.size());
            return orders;
        } catch (Exception e) {
            log.error("获取所有订单列表失败: error={}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 统计房东的进行中订单数量（已支付且未完成的订单）
     * @param landlordId 房东ID
     * @return 进行中订单数量
     */
    @Override
    public int countLandlordActiveOrders(Long landlordId) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        
        // 房东ID匹配
        queryWrapper.eq("landlord_id", landlordId);
        
        // 状态为已支付，未退租
        queryWrapper.eq("status", "PAID");
        
        return Math.toIntExact(count(queryWrapper));
    }

    /**
     * 用户支付违约金
     * @param orderId 订单ID
     * @param userId 用户ID
     * @param payMethod 支付方式
     * @return 是否支付成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrderPenalty(Long orderId, Long userId, String payMethod) {
        log.info("用户支付违约金: orderId={}, userId={}, payMethod={}", orderId, userId, payMethod);
        
        try {
            // 查询订单
            Order order = getById(orderId);
            if (order == null) {
                log.error("订单不存在: orderId={}", orderId);
                throw new BusinessException("订单不存在");
            }
            
            // 验证用户所有权
            if (!order.getUserId().equals(userId)) {
                log.error("非法操作，不是该订单的租客: orderId={}, userId={}, orderUserId={}", 
                        orderId, userId, order.getUserId());
                throw new BusinessException("非法操作，您不是该订单的租客");
            }
            
            // 验证订单状态
            if (!"TERMINATE_APPROVED".equals(order.getStatus()) && !"TERMINATE_REQUESTED".equals(order.getStatus())) {
                log.error("订单状态不允许支付违约金: orderId={}, status={}", orderId, order.getStatus());
                throw new BusinessException("当前订单状态不允许支付违约金");
            }
            
            // 检查是否已支付违约金
            if (order.getIsPenaltyPaid() != null && order.getIsPenaltyPaid()) {
                log.warn("违约金已支付，无需重复支付: orderId={}", orderId);
                
                // 即使已支付，也再次检查并确保房源状态已更新为APPROVED并且订单状态为TERMINATED
                try {
                    // 将订单状态更新为TERMINATED（如果尚未更新）
                    if (!"TERMINATED".equals(order.getStatus())) {
                        order.setStatus("TERMINATED");
                        order.setActualTerminateDate(LocalDate.now());
                        order.setUpdateTime(LocalDateTime.now());
                        boolean orderUpdated = updateById(order);
                        log.info("更新订单状态为TERMINATED: orderId={}, 结果={}", orderId, orderUpdated ? "成功" : "失败");
                    }
                    
                    // 强制更新房源状态为APPROVED（三种方式尝试）
                    updateHouseStatusToApproved(order.getHouseId());
                } catch (Exception e) {
                    log.error("确保状态更新时发生异常: {}", e.getMessage(), e);
                    // 这里不抛出异常，因为违约金已支付成功，只是状态更新失败
                }
                
                return true;
            }
            
            // 检查是否设置了违约金金额
            if (order.getPenaltyAmount() == null || order.getPenaltyAmount().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("未设置违约金或金额为0，无需支付: orderId={}", orderId);
                
                // 自动设置为已支付
                order.setIsPenaltyPaid(true);
                // 更新订单状态为TERMINATED
                order.setStatus("TERMINATED");
                order.setActualTerminateDate(LocalDate.now());
                order.setUpdateTime(LocalDateTime.now());
                
                boolean orderUpdated = updateById(order);
                if (orderUpdated) {
                    log.info("订单已更新为已终止状态: orderId={}", orderId);
                }
                
                // 强制更新房源状态为APPROVED
                updateHouseStatusToApproved(order.getHouseId());
                
                return true;
            }
            
            // 更新违约金支付状态
            order.setIsPenaltyPaid(true);
            
            // 设置支付方式
            if (StringUtils.hasText(payMethod)) {
                order.setPenaltyPayMethod(payMethod);
            } else {
                order.setPenaltyPayMethod("ONLINE");
            }
            
            // 设置支付时间
            order.setPenaltyPayTime(LocalDateTime.now());
            
            // 更新订单状态为TERMINATED
            order.setStatus("TERMINATED");
            order.setActualTerminateDate(LocalDate.now());
            
            // 更新订单
            boolean result = updateById(order);
            if (result) {
                log.info("违约金支付成功，订单已更新为已终止: orderId={}, amount={}", orderId, order.getPenaltyAmount());
                
                // 强制更新房源状态为APPROVED
                updateHouseStatusToApproved(order.getHouseId());
                
                // 发送消息通知
                try {
                    // 向房东发送消息
                    messageService.sendMessage(
                            order.getLandlordId(),
                            "租客已支付违约金",
                            "订单 #" + order.getId() + " 的租客已支付违约金，金额：" + order.getPenaltyAmount() + " 元。退租流程已完成。"
                    );

                    // 向租客发送消息
                    messageService.sendMessage(
                            order.getUserId(),
                            "违约金支付成功",
                            "您已成功支付订单 #" + order.getId() + " 的违约金，金额：" + order.getPenaltyAmount() + " 元。退租流程已完成。"
                    );
                } catch (Exception e) {
                    log.error("发送违约金支付消息失败: {}", e.getMessage(), e);
                    // 消息发送失败不影响主流程
                }
                
                return true;
            } else {
                log.error("违约金支付失败，数据库更新失败: orderId={}", orderId);
                throw new BusinessException("违约金支付失败，请稍后重试");
            }
        } catch (BusinessException e) {
            log.error("违约金支付业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("违约金支付系统异常: {}", e.getMessage(), e);
            throw new BusinessException("系统异常，违约金支付失败");
        }
    }
    
    /**
     * 强制更新房源状态为APPROVED
     * @param houseId 房源ID
     */
    private void updateHouseStatusToApproved(Long houseId) {
        log.info("强制更新房源状态为APPROVED: houseId={}", houseId);
        // 使用新的executeDirectSql方法进行多重尝试
        String updateSql = "UPDATE house SET status = 'APPROVED', update_time = NOW() WHERE id = " + houseId;
        executeDirectSql(updateSql, houseId);
    }

    /**
     * 执行直接SQL更新房源状态
     * @param sql SQL语句
     * @param houseId 房源ID
     */
    private void executeDirectSql(String sql, Long houseId) {
        log.info("开始更新房源状态为APPROVED: houseId={}", houseId);
        
        // 先检查当前房源状态
        try {
            House house = houseService.getById(houseId);
            if (house != null && "APPROVED".equals(house.getStatus())) {
                log.info("房源状态已经是APPROVED，无需更新: houseId={}", houseId);
                return;
            }
        } catch (Exception e) {
            log.warn("检查房源当前状态时发生异常: {}", e.getMessage());
            // 继续执行，尝试更新状态
        }
        
        // 第一次尝试：直接SQL更新
        SqlSession sqlSession = null;
        boolean updateSuccess = false;
        try {
            // 使用新的方式获取SqlSession，替代过时的SqlHelper.sqlSession
            sqlSession = sqlSessionFactory.openSession();
            Connection connection = sqlSession.getConnection();
            try (Statement stmt = connection.createStatement()) {
                int rowsAffected = stmt.executeUpdate(sql);
                if (rowsAffected > 0) {
                    log.info("SQL直接更新房源状态成功: houseId={}, 影响行数={}", houseId, rowsAffected);
                    updateSuccess = true;
                } else {
                    log.warn("SQL直接更新房源状态未影响任何行: houseId={}", houseId);
                }
            }
        } catch (Exception e) {
            log.error("SQL直接更新房源状态失败: {}", e.getMessage(), e);
        } finally {
            // 安全关闭SqlSession
            if (sqlSession != null) {
                try {
                    sqlSession.close();
                } catch (Exception e) {
                    log.error("关闭SqlSession失败: {}", e.getMessage());
                }
            }
        }
        
        // 第二次尝试：使用houseService
        if (!updateSuccess) {
            try {
                boolean result = houseService.updateHouseStatus(houseId, "APPROVED");
                if (result) {
                    log.info("通过houseService更新房源状态成功: houseId={}", houseId);
                    updateSuccess = true;
                } else {
                    log.warn("通过houseService更新房源状态失败: houseId={}", houseId);
                }
            } catch (Exception e) {
                log.error("通过houseService更新房源状态出现异常: {}", e.getMessage(), e);
            }
        }
        
        // 第三次尝试：直接更新House实体
        if (!updateSuccess) {
            try {
                House house = houseService.getById(houseId);
                if (house != null) {
                    house.setStatus("APPROVED");
                    house.setUpdateTime(LocalDateTime.now());
                    boolean result = houseService.updateById(house);
                    if (result) {
                        log.info("通过直接更新House实体成功: houseId={}", houseId);
                        updateSuccess = true;
                    } else {
                        log.warn("通过直接更新House实体失败: houseId={}", houseId);
                    }
                }
            } catch (Exception e) {
                log.error("通过直接更新House实体出现异常: {}", e.getMessage(), e);
            }
        }
        
        // 如果所有尝试都失败，记录严重错误
        if (!updateSuccess) {
            log.error("所有更新房源状态的尝试都失败，需手动处理: houseId={}", houseId);
            // 可以考虑发送系统告警或将待处理任务保存到数据库，后续由定时任务处理
        }
    }

    /**
     * 检查并修复状态不一致的订单和房源
     * 主要处理已终止订单但房源仍处于出租状态的情况
     * 建议使用定时任务调用此方法
     * @return 修复的记录数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int checkAndFixInconsistentStatus() {
        log.info("开始检查并修复状态不一致的订单和房源...");
        int fixedCount = 0;
        
        try {
            // 查询所有处于TERMINATED状态但关联房源仍为RENTED状态的订单
            String sql = "SELECT o.id as order_id, o.house_id FROM `order` o " +
                        "JOIN house h ON o.house_id = h.id " +
                        "WHERE o.status = 'TERMINATED' " +
                        "AND h.status = 'RENTED' " +
                        "AND o.is_deleted = 0 " +
                        "AND h.is_deleted = 0";
            
            List<Map<String, Object>> inconsistentRecords = new ArrayList<>();
            
            // 执行查询
            SqlSession sqlSession = null;
            try {
                sqlSession = sqlSessionFactory.openSession();
                Connection connection = sqlSession.getConnection();
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    
                    while (rs.next()) {
                        Map<String, Object> record = new HashMap<>();
                        record.put("orderId", rs.getLong("order_id"));
                        record.put("houseId", rs.getLong("house_id"));
                        inconsistentRecords.add(record);
                    }
                }
            } catch (Exception e) {
                log.error("查询状态不一致订单失败: {}", e.getMessage(), e);
                return 0;
            } finally {
                if (sqlSession != null) {
                    sqlSession.close();
                }
            }
            
            log.info("发现{}条状态不一致的记录需要修复", inconsistentRecords.size());
            
            // 修复每一条不一致的记录
            for (Map<String, Object> record : inconsistentRecords) {
                Long houseId = (Long) record.get("houseId");
                Long orderId = (Long) record.get("orderId");
                
                log.info("修复状态不一致记录: orderId={}, houseId={}", orderId, houseId);
                
                try {
                    // 更新房源状态为APPROVED
                    updateHouseStatusToApproved(houseId);
                    fixedCount++;
                    
                    // 记录日志
                    log.info("成功修复订单和房源状态: orderId={}, houseId={}", orderId, houseId);
                } catch (Exception e) {
                    log.error("修复状态不一致记录失败: orderId={}, houseId={}, error={}", 
                            orderId, houseId, e.getMessage(), e);
                }
            }
            
            log.info("状态不一致修复完成，成功修复{}条记录", fixedCount);
            return fixedCount;
        } catch (Exception e) {
            log.error("检查并修复状态不一致过程中发生异常: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 计算总违约金收入
     */
    @Override
    public BigDecimal calculateTotalPenaltyIncome() {
        log.info("计算平台总违约金收入");
        
        try {
            // 查询所有已支付违约金的订单
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getIsPenaltyPaid, true)
                   .isNotNull(Order::getPenaltyAmount)
                   .gt(Order::getPenaltyAmount, BigDecimal.ZERO);
            
            List<Order> orders = baseMapper.selectList(wrapper);
            log.info("查询到已支付违约金的订单数量: {}", orders.size());
            
            // 计算总违约金收入
            BigDecimal totalPenaltyIncome = BigDecimal.ZERO;
            for (Order order : orders) {
                if (order.getPenaltyAmount() != null) {
                    totalPenaltyIncome = totalPenaltyIncome.add(order.getPenaltyAmount());
                }
            }
            
            log.info("计算平台总违约金收入成功: {}", totalPenaltyIncome);
            return totalPenaltyIncome;
        } catch (Exception e) {
            log.error("计算平台总违约金收入失败: {}", e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }
} 