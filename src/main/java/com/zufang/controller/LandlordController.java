package com.zufang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zufang.common.response.Result;
import com.zufang.service.AppointmentService;
import com.zufang.service.HouseService;
import com.zufang.service.OrderService;
import com.zufang.dto.HouseEditDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 房东控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class LandlordController {
    
    private final OrderService orderService;
    private final HouseService houseService;
    private final AppointmentService appointmentService;

    /**
     * 获取房东首页统计数据
     */
    @GetMapping("/landlord/dashboard/statistics")
    public Result<Map<String, Object>> getDashboardStatistics(HttpServletRequest request) {
        // 从请求中获取当前登录的房东ID
        Long landlordId = (Long) request.getAttribute("userId");
        log.info("获取房东首页统计: landlordId={}", landlordId);
        
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            // 1. 统计房东的房源数量
            try {
                int houseCount = houseService.countLandlordHouses(landlordId);
                statistics.put("houseCount", houseCount);
            } catch (Exception e) {
                log.error("获取房东房源数量失败: {}", e.getMessage());
                statistics.put("houseCount", 0);
            }
            
            // 2. 统计待处理预约数量
            try {
                int pendingAppointmentCount = appointmentService.countLandlordPendingAppointments(landlordId);
                statistics.put("pendingAppointmentCount", pendingAppointmentCount);
            } catch (Exception e) {
                log.error("获取房东待处理预约数量失败: {}", e.getMessage());
                statistics.put("pendingAppointmentCount", 0);
            }
            
            // 3. 统计进行中订单数量
            try {
                int activeOrderCount = orderService.countLandlordActiveOrders(landlordId);
                statistics.put("activeOrderCount", activeOrderCount);
            } catch (Exception e) {
                log.error("获取房东进行中订单数量失败: {}", e.getMessage());
                statistics.put("activeOrderCount", 0);
            }
            
            // 4. 统计本月收入
            try {
                BigDecimal monthlyIncome = orderService.calculateLandlordMonthlyIncome(landlordId);
                statistics.put("monthlyIncome", monthlyIncome);
            } catch (Exception e) {
                log.error("获取房东本月收入失败: {}", e.getMessage());
                statistics.put("monthlyIncome", new BigDecimal("0.00"));
            }
            
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取房东首页统计失败: {}", e.getMessage(), e);
            // 即使整体失败，也返回默认的统计数据
            if (statistics.isEmpty()) {
                statistics.put("houseCount", 0);
                statistics.put("pendingAppointmentCount", 0);
                statistics.put("activeOrderCount", 0);
                statistics.put("monthlyIncome", new BigDecimal("0.00"));
            }
            return Result.success(statistics);
        }
    }

    /**
     * 获取房东财务统计
     */
    @GetMapping("/landlord/finance/statistics")
    public Result<Map<String, Object>> getLandlordFinanceStatistics(HttpServletRequest request) {
        // 从请求中获取当前登录的房东ID
        Long landlordId = (Long) request.getAttribute("userId");
        log.info("获取房东财务统计: landlordId={}", landlordId);
        
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 1. 计算房东总收入（包括所有历史收入，含租金和违约金）
            BigDecimal totalIncome = orderService.calculateLandlordTotalIncome(landlordId);
            statistics.put("totalIncome", totalIncome);
            statistics.put("totalIncomeDesc", "所有历史订单的累计收入（包含租金和违约金）");
            
            // 2. 计算房东本月收入（当月租金和违约金收入）
            BigDecimal monthlyIncome = orderService.calculateLandlordMonthlyIncome(landlordId);
            statistics.put("monthlyIncome", monthlyIncome);
            statistics.put("monthlyIncomeDesc", "本月产生的租金和违约金收入");
            
            // 3. 计算违约金收入（所有历史违约金累计）
            BigDecimal penaltyIncome = orderService.calculateLandlordPenaltyIncome(landlordId);
            statistics.put("penaltyIncome", penaltyIncome);
            statistics.put("penaltyIncomeDesc", "所有历史订单中的违约金收入");
            
            // 4. 计算总押金额（当前活跃订单的押金总额）
            BigDecimal totalDeposit = orderService.calculateLandlordTotalDeposit(landlordId);
            statistics.put("totalDeposit", totalDeposit);
            statistics.put("totalDepositDesc", "当前活跃订单的押金总额");
            
            // 5. 计算总租金收入(总收入减去违约金)
            BigDecimal totalRentIncome = totalIncome.subtract(penaltyIncome);
            statistics.put("totalRentIncome", totalRentIncome);
            statistics.put("totalRentIncomeDesc", "所有历史订单的租金收入（不含违约金）");
            
            // 6. 统计订单总数（所有历史订单）
            int totalOrders = orderService.countLandlordOrders(landlordId);
            statistics.put("totalOrders", totalOrders);
            statistics.put("totalOrdersDesc", "所有历史订单总数");
            
            // 7. 统计本月订单数（本月新增的订单）
            int monthlyOrders = orderService.countLandlordMonthlyOrders(landlordId);
            statistics.put("monthlyOrders", monthlyOrders);
            statistics.put("monthlyOrdersDesc", "本月新增订单数量");
            
            // 8. 统计已出租房源数量（当前仍在租赁中的房源，不包括已退租）
            int rentedHouses = orderService.countLandlordRentedHouses(landlordId);
            statistics.put("rentedHouses", rentedHouses);
            statistics.put("rentedHousesDesc", "当前仍在租赁中的房源数量，不包括已退租的房源");
            
            // 9. 统计退租申请数量（待处理的退租申请）
            int terminateRequests = orderService.countLandlordTerminateRequests(landlordId);
            statistics.put("terminateRequests", terminateRequests);
            statistics.put("terminateRequestsDesc", "当前待处理的退租申请数量");
            
            // 10. 添加月度收入数据
            List<Map<String, Object>> monthlyIncomeData = new ArrayList<>();
            LocalDate now = LocalDate.now();
            
            for (int i = 5; i >= 0; i--) {
                LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
                LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
                
                // 格式化月份为 "yyyy-MM" 格式
                String monthLabel = monthStart.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                
                // 获取该月收入
                BigDecimal income = orderService.calculateLandlordIncomeInPeriod(
                    landlordId, 
                    monthStart, 
                    monthEnd
                );
                
                Map<String, Object> monthData = new HashMap<>();
                monthData.put("month", monthLabel);
                monthData.put("income", income);
                monthlyIncomeData.add(monthData);
            }
            
            statistics.put("monthlyIncomeData", monthlyIncomeData);
            statistics.put("monthlyIncomeDataDesc", "近6个月的月度收入数据");
            
            // 11. 添加收入分类和说明，便于前端展示
            Map<String, String> incomeTypes = new HashMap<>();
            incomeTypes.put("totalIncome", "总收入");
            incomeTypes.put("monthlyIncome", "本月收入");
            incomeTypes.put("totalRentIncome", "总租金收入");
            incomeTypes.put("penaltyIncome", "违约金收入");
            statistics.put("incomeTypes", incomeTypes);
            
            // 12. 添加统计区块分类信息，便于前端展示
            Map<String, Object> blockInfo = new HashMap<>();
            
            // 收入区块
            Map<String, Object> incomeBlock = new HashMap<>();
            incomeBlock.put("title", "收入统计");
            incomeBlock.put("items", Arrays.asList(
                "totalIncome", "monthlyIncome", "totalRentIncome", "penaltyIncome"
            ));
            
            // 订单区块
            Map<String, Object> orderBlock = new HashMap<>();
            orderBlock.put("title", "订单统计");
            orderBlock.put("items", Arrays.asList(
                "totalOrders", "monthlyOrders", "rentedHouses", "terminateRequests"
            ));
            
            // 押金区块
            Map<String, Object> depositBlock = new HashMap<>();
            depositBlock.put("title", "押金统计");
            depositBlock.put("items", Arrays.asList("totalDeposit"));
            
            blockInfo.put("blocks", Arrays.asList(incomeBlock, orderBlock, depositBlock));
            statistics.put("blockInfo", blockInfo);
            
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取房东财务统计失败: {}", e.getMessage(), e);
            return Result.fail("获取财务统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取房东收入明细
     */
    @GetMapping("/landlord/finance/income")
    public Result<Map<String, Object>> getIncomeList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String type,
            HttpServletRequest request) {
        // 从请求中获取当前登录的房东ID
        Long landlordId = (Long) request.getAttribute("userId");
        log.info("获取房东收入明细: landlordId={}, page={}, size={}, startDate={}, endDate={}, type={}",
                landlordId, page, size, startDate, endDate, type);
        
        try {
            // 1. 查询收入明细
            Page<Map<String, Object>> pageResult = orderService.getLandlordIncomeList(
                landlordId, page, size, startDate, endDate);
            
            // 如果指定了收入类型筛选
            if (StringUtils.hasText(type)) {
                List<Map<String, Object>> filteredRecords = pageResult.getRecords().stream()
                    .filter(record -> {
                        String incomeType = (String) record.get("incomeType");
                        if (incomeType == null) {
                            return false;
                        }
                        
                        // 处理收入类型筛选
                        if ("DEPOSIT".equals(type)) {
                            // 押金相关包括押金和退还押金
                            return "DEPOSIT".equals(incomeType) || "DEPOSIT_REFUND".equals(incomeType);
                        } else if ("TERMINATED".equals(type)) {
                            // 退租相关记录
                            return Boolean.TRUE.equals(record.get("isTerminated")) || "RENT_LOSS".equals(incomeType);
                        } else {
                            return type.equals(incomeType);
                        }
                    })
                    .collect(Collectors.toList());
                
                // 更新记录和总数
                long total = filteredRecords.size();
                pageResult.setRecords(filteredRecords);
                pageResult.setTotal(total);
            }
            
            // 增加收入类型的描述信息
            pageResult.getRecords().forEach(record -> {
                String incomeType = (String) record.get("incomeType");
                if ("RENT".equals(incomeType)) {
                    record.put("incomeTypeDesc", "租金收入");
                } else if ("PENALTY".equals(incomeType)) {
                    record.put("incomeTypeDesc", "违约金收入");
                } else if ("DEPOSIT".equals(incomeType)) {
                    record.put("incomeTypeDesc", "押金收入");
                } else if ("DEPOSIT_REFUND".equals(incomeType)) {
                    record.put("incomeTypeDesc", "押金退还");
                } else if ("RENT_LOSS".equals(incomeType)) {
                    record.put("incomeTypeDesc", "退租扣除");
                    // 确保金额为负数
                    if (record.get("amount") != null) {
                        BigDecimal amount = new BigDecimal(record.get("amount").toString());
                        if (amount.compareTo(BigDecimal.ZERO) > 0) {
                            record.put("amount", amount.negate());
                        }
                    }
                } else {
                    record.put("incomeTypeDesc", "其他收入");
                }
                
                // 添加状态描述
                String status = (String) record.get("status");
                if ("PAID".equals(status)) {
                    record.put("statusDesc", "已支付");
                } else if ("ACTIVE".equals(status)) {
                    record.put("statusDesc", "租赁中");
                } else if ("TERMINATED".equals(status)) {
                    record.put("statusDesc", "已退租");
                } else if ("COMPLETED".equals(status)) {
                    record.put("statusDesc", "已完成");
                } else {
                    record.put("statusDesc", status);
                }
                
                // 额外添加退租信息
                if (Boolean.TRUE.equals(record.get("isTerminated")) || "RENT_LOSS".equals(incomeType)) {
                    // 设置退租标记
                    record.put("isTerminated", true);
                    
                    // 计算提前终止的天数
                    if (record.get("totalDays") != null && record.get("daysRented") != null) {
                        Long totalDays = Long.valueOf(record.get("totalDays").toString());
                        Long daysRented = Long.valueOf(record.get("daysRented").toString());
                        // 计算剩余未租住的天数
                        Long daysNotRented = totalDays - daysRented;
                        
                        record.put("daysNotRentedDesc", "提前终止 " + daysNotRented + " 天");
                        
                        // 如果有原始金额和实际收取金额，计算差额
                        if (record.get("originalAmount") != null && record.get("amount") != null) {
                            BigDecimal originalAmount = new BigDecimal(record.get("originalAmount").toString());
                            BigDecimal actualAmount = new BigDecimal(record.get("amount").toString());
                            
                            // 对于扣除类型，金额已经是负数，不需要再计算差额
                            if (!"RENT_LOSS".equals(incomeType)) {
                                // 计算退还的租金：原始金额 - 实际收取金额
                                BigDecimal difference = originalAmount.subtract(actualAmount);
                                record.put("rentDifference", difference);
                                record.put("rentDifferenceDesc", "退租退还租金: " + difference + " 元");
                            } else {
                                // 对于退租扣除类型，直接使用金额的绝对值
                                record.put("rentDifference", actualAmount.abs());
                                record.put("rentDifferenceDesc", "退租未使用租期退款: " + actualAmount.abs() + " 元");
                            }
                        }
                    }
                }
            });
            
            // 2. 封装返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("records", pageResult.getRecords());
            result.put("total", pageResult.getTotal());
            result.put("size", pageResult.getSize());
            result.put("current", pageResult.getCurrent());
            
            // 添加收入类型列表，供前端筛选
            Map<String, String> incomeTypes = new HashMap<>();
            incomeTypes.put("RENT", "租金收入");
            incomeTypes.put("PENALTY", "违约金收入");
            incomeTypes.put("DEPOSIT", "押金相关");
            incomeTypes.put("TERMINATED", "退租记录");
            incomeTypes.put("RENT_LOSS", "退租扣除");
            result.put("incomeTypes", incomeTypes);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取房东收入明细失败: {}", e.getMessage(), e);
            return Result.fail("获取收入明细失败: " + e.getMessage());
        }
    }

    /**
     * 房东编辑房源
     */
    @PostMapping("/landlord/house/edit")
    public Result<Boolean> editHouse(@RequestPart(value = "data") @Valid HouseEditDTO editDTO,
                                    @RequestPart(value = "coverImage", required = false) MultipartFile coverImageFile,
                                    @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
                                    HttpServletRequest request) {
        // 从请求中获取当前登录的房东ID
        Long landlordId = (Long) request.getAttribute("userId");
        log.info("房东编辑房源: landlordId={}, houseId={}", landlordId, editDTO.getId());
        
        try {
            // 设置文件
            if (coverImageFile != null && !coverImageFile.isEmpty()) {
                editDTO.setCoverImageFile(coverImageFile);
            }
            
            if (imageFiles != null && !imageFiles.isEmpty()) {
                editDTO.setImageFiles(imageFiles);
            }
            
            // 编辑房源
            boolean success = houseService.editHouse(editDTO, landlordId);
            
            if (success) {
                log.info("房东编辑房源成功: houseId={}", editDTO.getId());
                return Result.success(true);
            } else {
                log.warn("房东编辑房源失败: houseId={}", editDTO.getId());
                return Result.fail("编辑房源失败，请检查权限或房源状态");
            }
        } catch (Exception e) {
            log.error("房东编辑房源异常: {}", e.getMessage(), e);
            return Result.fail("编辑房源失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取房源的图片列表
     */
    @GetMapping("/landlord/house/{houseId}/images")
    public Result<List<String>> getHouseImages(@PathVariable Long houseId, HttpServletRequest request) {
        // 从请求中获取当前登录的房东ID
        Long landlordId = (Long) request.getAttribute("userId");
        log.info("房东获取房源图片: landlordId={}, houseId={}", landlordId, houseId);
        
        try {
            // 获取房源图片
            List<String> images = houseService.getHouseImages(houseId);
            
            log.info("房东获取房源图片成功: houseId={}, 图片数量={}", houseId, images.size());
            return Result.success(images);
        } catch (Exception e) {
            log.error("房东获取房源图片异常: {}", e.getMessage(), e);
            return Result.fail("获取房源图片失败: " + e.getMessage());
        }
    }
} 