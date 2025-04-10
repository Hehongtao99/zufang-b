package com.zufang.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zufang.common.response.Result;
import com.zufang.dto.HouseApproveDTO;
import com.zufang.dto.HouseContractSettingDTO;
import com.zufang.dto.HouseInfoDTO;
import com.zufang.dto.HousePublishDTO;
import com.zufang.dto.HouseSearchDTO;
import com.zufang.dto.HouseEditDTO;
import com.zufang.entity.House;
import com.zufang.entity.Order;
import com.zufang.service.HouseService;
import com.zufang.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 房源控制器
 */
@Slf4j
@RestController
@RequestMapping("/house")
public class HouseController {
    
    @Autowired
    private HouseService houseService;
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 发布房源
     */
    @PostMapping("/publish")
    public Result<Long> publishHouse(@RequestPart(value = "data") @Valid HousePublishDTO publishDTO,
                                    @RequestPart(value = "coverImage", required = false) MultipartFile coverImageFile,
                                    @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
                                    HttpServletRequest request) {
        try {
            // 从request中获取用户ID
            Long userId = (Long) request.getAttribute("userId");
            log.info("发布房源, 用户ID: {}", userId);
            
            // 设置文件
            if (coverImageFile != null && !coverImageFile.isEmpty()) {
                publishDTO.setCoverImageFile(coverImageFile);
            }
            
            if (imageFiles != null && !imageFiles.isEmpty()) {
                publishDTO.setImageFiles(imageFiles);
            }
            
            // 发布房源
            Long houseId = houseService.publishHouse(publishDTO, userId);
            
            log.info("发布房源成功, 房源ID: {}", houseId);
            return Result.success(houseId);
        } catch (Exception e) {
            log.error("发布房源失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 审核房源（管理员）
     */
    @PostMapping("/approve")
    public Result<Object> approveHouse(@RequestBody @Valid HouseApproveDTO approveDTO, HttpServletRequest request) {
        try {
            // 从request中获取用户角色
            String role = (String) request.getAttribute("role");
            log.info("审核房源, 房源ID: {}, 用户角色: {}", approveDTO.getHouseId(), role);
            
            // 校验用户是否为管理员
            if (!"ADMIN".equals(role)) {
                return Result.fail("只有管理员才能审核房源");
            }
            
            // 审核房源
            houseService.approveHouse(approveDTO);
            
            log.info("审核房源成功, 房源ID: {}", approveDTO.getHouseId());
            return Result.success();
        } catch (Exception e) {
            log.error("审核房源失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 获取房源详情
     */
    @GetMapping("/info/{houseId}")
    public Result<HouseInfoDTO> getHouseInfo(@PathVariable Long houseId) {
        try {
            log.info("获取房源详情, 房源ID: {}", houseId);
            
            // 获取房源详情
            HouseInfoDTO houseInfo = houseService.getHouseInfo(houseId);
            
            log.info("获取房源详情成功, 房源ID: {}", houseId);
            return Result.success(houseInfo);
        } catch (Exception e) {
            log.error("获取房源详情失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 搜索房源
     */
    @PostMapping("/search")
    public Result<Page<HouseInfoDTO>> searchHousesPost(@RequestBody HouseSearchDTO searchDTO) {
        try {
            log.info("搜索房源(POST), 条件: {}", searchDTO);
            
            // 搜索房源
            Page<HouseInfoDTO> housePage = houseService.searchHouses(searchDTO);
            
            log.info("搜索房源成功, 结果数: {}", housePage.getTotal());
            return Result.success(housePage);
        } catch (Exception e) {
            log.error("搜索房源失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 搜索房源 (GET方式)
     */
    @GetMapping("/search")
    public Result<Page<HouseInfoDTO>> searchHousesGet(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minArea,
            @RequestParam(required = false) Integer maxArea,
            @RequestParam(required = false) Integer bedroomCount,
            @RequestParam(required = false) String houseType,
            @RequestParam(required = false) String rentType,
            @RequestParam(required = false) Boolean hasElevator,
            @RequestParam(required = false) Boolean hasParking,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Boolean priorityLoad) {
        try {
            // 构建搜索DTO
            HouseSearchDTO searchDTO = new HouseSearchDTO();
            searchDTO.setKeyword(keyword);
            searchDTO.setProvince(province);
            searchDTO.setCity(city);
            searchDTO.setDistrict(district);
            searchDTO.setMinPrice(minPrice);
            searchDTO.setMaxPrice(maxPrice);
            searchDTO.setMinArea(minArea);
            searchDTO.setMaxArea(maxArea);
            searchDTO.setBedroomCount(bedroomCount);
            searchDTO.setHouseType(houseType);
            searchDTO.setRentType(rentType);
            searchDTO.setHasElevator(hasElevator);
            searchDTO.setHasParking(hasParking);
            searchDTO.setSortField(sortField);
            searchDTO.setSortOrder(sortOrder);
            searchDTO.setPageNum(pageNum);
            searchDTO.setPageSize(pageSize);
            searchDTO.setPriorityLoad(priorityLoad);
            
            log.info("搜索房源(GET), 条件: {}", searchDTO);
            
            // 搜索房源
            Page<HouseInfoDTO> housePage = houseService.searchHouses(searchDTO);
            
            log.info("搜索房源成功, 结果数: {}", housePage.getTotal());
            return Result.success(housePage);
        } catch (Exception e) {
            log.error("搜索房源失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 获取房东发布的房源列表
     */
    @GetMapping("/landlord")
    public Result<Page<HouseInfoDTO>> getLandlordHouses(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        try {
            // 从request中获取用户ID
            Long userId = (Long) request.getAttribute("userId");
            log.info("获取房东发布的房源列表, 房东ID: {}", userId);
            
            // 获取房东发布的房源列表
            Page<HouseInfoDTO> housePage = houseService.getLandlordHouses(userId, pageNum, pageSize);
            
            log.info("获取房东发布的房源列表成功, 房东ID: {}, 结果数: {}", userId, housePage.getTotal());
            return Result.success(housePage);
        } catch (Exception e) {
            log.error("获取房东发布的房源列表失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 获取待审核房源列表（管理员）
     */
    @GetMapping("/pending")
    public Result<Page<HouseInfoDTO>> getPendingHouses(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        try {
            // 从request中获取用户角色
            String role = (String) request.getAttribute("role");
            log.info("获取待审核房源列表, 用户角色: {}", role);
            
            // 校验用户是否为管理员
            if (!"ADMIN".equals(role)) {
                return Result.fail("只有管理员才能查看待审核房源");
            }
            
            // 获取待审核房源列表
            Page<HouseInfoDTO> housePage = houseService.getPendingHouses(pageNum, pageSize);
            
            log.info("获取待审核房源列表成功, 结果数: {}", housePage.getTotal());
            return Result.success(housePage);
        } catch (Exception e) {
            log.error("获取待审核房源列表失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 下架房源
     */
    @PostMapping("/offline/{houseId}")
    public Result<Object> offlineHouse(@PathVariable Long houseId, HttpServletRequest request) {
        try {
            // 从request中获取用户ID
            Long userId = (Long) request.getAttribute("userId");
            log.info("下架房源, 房源ID: {}, 用户ID: {}", houseId, userId);
            
            // 下架房源
            houseService.offlineHouse(houseId, userId);
            
            log.info("下架房源成功, 房源ID: {}", houseId);
            return Result.success();
        } catch (Exception e) {
            log.error("下架房源失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 上架房源
     */
    @PostMapping("/online/{houseId}")
    public Result<Object> onlineHouse(@PathVariable Long houseId, HttpServletRequest request) {
        try {
            // 从request中获取用户ID
            Long userId = (Long) request.getAttribute("userId");
            log.info("上架房源, 房源ID: {}, 用户ID: {}", houseId, userId);
            
            // 上架房源
            houseService.onlineHouse(houseId, userId);
            
            log.info("上架房源成功, 房源ID: {}", houseId);
            return Result.success();
        } catch (Exception e) {
            log.error("上架房源失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 删除房源
     */
    @DeleteMapping("/{houseId}")
    public Result<Object> deleteHouse(@PathVariable Long houseId, HttpServletRequest request) {
        try {
            // 从request中获取用户ID
            Long userId = (Long) request.getAttribute("userId");
            log.info("删除房源, 房源ID: {}, 用户ID: {}", houseId, userId);
            
            // 删除房源
            houseService.deleteHouse(houseId, userId);
            
            log.info("删除房源成功, 房源ID: {}", houseId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除房源失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 检查接口可用性
     */
    @GetMapping("/check")
    public Result<Map<String, String>> checkEndpoints() {
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("publish", "/house/publish - POST");
        endpoints.put("search", "/house/search - GET/POST");
        endpoints.put("info", "/house/info/{houseId} - GET");
        endpoints.put("landlord", "/house/landlord - GET");
        endpoints.put("pending", "/house/pending - GET");
        endpoints.put("approve", "/house/approve - POST");
        endpoints.put("online", "/house/online/{houseId} - POST");
        endpoints.put("offline", "/house/offline/{houseId} - POST");
        endpoints.put("delete", "/house/{houseId} - DELETE");
        
        return Result.success(endpoints);
    }
    
    /**
     * 简化版房源发布接口（表单提交）
     */
    @PostMapping("/publish/simple")
    public Result<Long> publishHouseSimple(@RequestParam String title,
                                         @RequestParam String description,
                                         @RequestParam Integer area,
                                         @RequestParam BigDecimal price, 
                                         @RequestParam String address,
                                         @RequestParam String province,
                                         @RequestParam Long provinceId,
                                         @RequestParam String city,
                                         @RequestParam Long cityId,
                                         @RequestParam String district,
                                         @RequestParam Long districtId,
                                         @RequestParam Integer bedroomCount,
                                         @RequestParam Integer livingRoomCount,
                                         @RequestParam Integer bathroomCount,
                                         @RequestParam(required = false) String orientation,
                                         @RequestParam(required = false) Integer floor,
                                         @RequestParam(required = false) Integer totalFloor,
                                         @RequestParam(required = false) String decoration,
                                         @RequestParam(required = false) Boolean hasElevator,
                                         @RequestParam(required = false) Boolean hasParking,
                                         @RequestParam String houseType,
                                         @RequestParam String rentType,
                                         @RequestParam(required = false) MultipartFile coverImageFile,
                                         @RequestParam(required = false) List<MultipartFile> imageFiles,
                                         HttpServletRequest request) {
        try {
            // 从request中获取用户ID
            Long userId = (Long) request.getAttribute("userId");
            log.info("简化版发布房源, 用户ID: {}", userId);
            
            // 构建发布DTO
            HousePublishDTO publishDTO = new HousePublishDTO();
            publishDTO.setTitle(title);
            publishDTO.setDescription(description);
            publishDTO.setArea(area);
            publishDTO.setPrice(price);
            publishDTO.setAddress(address);
            publishDTO.setProvince(province);
            publishDTO.setProvinceId(provinceId);
            publishDTO.setCity(city);
            publishDTO.setCityId(cityId);
            publishDTO.setDistrict(district);
            publishDTO.setDistrictId(districtId);
            publishDTO.setBedroomCount(bedroomCount);
            publishDTO.setLivingRoomCount(livingRoomCount);
            publishDTO.setBathroomCount(bathroomCount);
            publishDTO.setOrientation(orientation);
            publishDTO.setFloor(floor);
            publishDTO.setTotalFloor(totalFloor);
            publishDTO.setDecoration(decoration);
            publishDTO.setHasElevator(hasElevator);
            publishDTO.setHasParking(hasParking);
            publishDTO.setHouseType(houseType);
            publishDTO.setRentType(rentType);
            publishDTO.setCoverImageFile(coverImageFile);
            publishDTO.setImageFiles(imageFiles);
            
            // 发布房源
            Long houseId = houseService.publishHouse(publishDTO, userId);
            
            log.info("简化版发布房源成功, 房源ID: {}", houseId);
            return Result.success(houseId);
        } catch (Exception e) {
            log.error("简化版发布房源失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 房源根路径测试接口
     */
    @GetMapping("")
    public Result<String> testRootEndpoint() {
        return Result.success("房源服务接口正常工作");
    }
    
    /**
     * 获取所有房源列表（管理员）
     */
    @GetMapping("/admin/list")
    public Result<Page<HouseInfoDTO>> getAllHouses(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        try {
            // 从request中获取用户角色
            String role = (String) request.getAttribute("role");
            log.info("获取所有房源列表, 用户角色: {}", role);
            
            // 校验用户是否为管理员
            if (!"ADMIN".equals(role)) {
                return Result.fail("只有管理员才能查看所有房源");
            }
            
            // 获取所有房源列表
            Page<HouseInfoDTO> housePage;
            if (status != null && !status.isEmpty()) {
                // 如果指定了状态，则查询指定状态的房源
                housePage = houseService.getHousesByStatus(status, pageNum, pageSize);
            } else {
                // 否则查询所有房源
                housePage = houseService.getAllHouses(pageNum, pageSize);
            }
            
            log.info("获取所有房源列表成功, 结果数: {}", housePage.getTotal());
            return Result.success(housePage);
        } catch (Exception e) {
            log.error("获取所有房源列表失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 房东发布房源页面接口
     */
    @PostMapping("/landlord/add")
    public Result<Long> landlordAddHouse(@RequestBody HousePublishDTO publishDTO, HttpServletRequest request) {
        try {
            // 从request中获取用户ID
            Long userId = (Long) request.getAttribute("userId");
            log.info("房东发布房源, 用户ID: {}", userId);
            
            // 发布房源
            Long houseId = houseService.publishHouse(publishDTO, userId);
            
            log.info("房东发布房源成功, 房源ID: {}", houseId);
            return Result.success(houseId);
        } catch (Exception e) {
            log.error("房东发布房源失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 获取房东发布的房源列表（专用API）
     */
    @GetMapping("/landlord/houses")
    public Result<Page<HouseInfoDTO>> getLandlordHousesList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        try {
            // 从request中获取用户ID
            Object userIdObj = request.getAttribute("userId");
            String username = (String) request.getAttribute("username");
            String role = (String) request.getAttribute("role");
            
            log.info("获取房东发布的房源列表API, 用户ID原始类型: {}, 值: {}", 
                    userIdObj != null ? userIdObj.getClass().getName() : "null", 
                    userIdObj);
            
            // 安全地将userId转换为Long类型
            Long userId = null;
            if (userIdObj != null) {
                if (userIdObj instanceof Long) {
                    userId = (Long) userIdObj;
                } else if (userIdObj instanceof String) {
                    try {
                        userId = Long.valueOf((String) userIdObj);
                    } catch (NumberFormatException e) {
                        log.error("用户ID转换为Long类型失败: {}", userIdObj);
                        return Result.fail("用户ID格式不正确");
                    }
                } else {
                    log.error("用户ID类型不支持转换: {}", userIdObj.getClass().getName());
                    return Result.fail("用户ID类型不支持");
                }
            }
            
            log.info("获取房东发布的房源列表API, 房东ID: {}, 用户名: {}, 角色: {}", userId, username, role);
            
            // 用户ID必须存在
            if (userId == null) {
                log.error("获取房东发布的房源列表API失败: 用户ID为空");
                return Result.fail("用户未登录或登录已过期");
            }
            
            // 获取房东发布的房源列表
            Page<HouseInfoDTO> housePage = houseService.getLandlordHouses(userId, pageNum, pageSize);
            
            log.info("获取房东发布的房源列表API成功, 房东ID: {}, 结果数: {}", userId, housePage.getTotal());
            return Result.success(housePage);
        } catch (Exception e) {
            log.error("获取房东发布的房源列表API失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 房东设置房源合同信息
     */
    @PutMapping("/landlord/contract-settings")
    public Result<Boolean> updateHouseContractSettings(@RequestBody HouseContractSettingDTO dto, HttpServletRequest request) {
        try {
            // 从request中获取用户ID
            Long userId = (Long) request.getAttribute("userId");
            log.info("房东设置房源合同信息, 房东ID: {}, 房源ID: {}", userId, dto.getHouseId());
            
            // 验证房源是否属于当前房东
            HouseInfoDTO houseInfo = houseService.getHouseInfo(dto.getHouseId());
            if (houseInfo == null) {
                return Result.fail("房源不存在");
            }
            
            if (!houseInfo.getOwnerId().equals(userId)) {
                return Result.fail("无权操作该房源");
            }
            
            // 更新房源合同设置
            boolean result = houseService.updateHouseContractSettings(dto);
            
            log.info("房东设置房源合同信息成功, 房源ID: {}", dto.getHouseId());
            return Result.success(result);
        } catch (Exception e) {
            log.error("房东设置房源合同信息失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 更新房源状态
     */
    @PutMapping("/status/{houseId}")
    public Result<Boolean> updateHouseStatus(
            @PathVariable Long houseId,
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request) {
        try {
            String status = requestBody.get("status");
            log.info("更新房源状态, 房源ID: {}, 新状态: {}", houseId, status);
            
            if (status == null || status.isEmpty()) {
                return Result.fail("状态不能为空");
            }
            
            // 从request中获取用户角色
            String role = (String) request.getAttribute("role");
            log.info("更新房源状态, 用户角色: {}", role);
            
            // 校验用户是否为管理员
            if (!"ADMIN".equals(role)) {
                return Result.fail("只有管理员才能更新房源状态");
            }
            
            // 更新房源状态
            boolean result = houseService.updateHouseStatus(houseId, status);
            
            log.info("更新房源状态成功, 房源ID: {}, 新状态: {}", houseId, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("更新房源状态失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 修复退租后房源状态
     * 专门用于处理退租后但状态未更新为APPROVED的房源
     */
    @PostMapping("/admin/fixHouseStatus")
    public Result<Map<String, Object>> fixTerminatedHouseStatus(HttpServletRequest request) {
        try {
            // 验证用户是管理员
            String role = (String) request.getAttribute("role");
            if (!"ADMIN".equals(role)) {
                return Result.fail("只有管理员才能执行此操作");
            }
            
            log.info("开始修复退租后房源状态...");
            
            // 查找所有状态为TERMINATED的订单 - 修复查询方式
            List<Order> terminatedOrders = orderService.list();
            // 过滤出已终止的订单
            terminatedOrders = terminatedOrders.stream()
                .filter(order -> "TERMINATED".equals(order.getStatus()) || "COMPLETED".equals(order.getStatus()))
                .collect(Collectors.toList());
            
            log.info("找到已终止/完成的订单: {} 个", terminatedOrders.size());
            
            // 收集房源ID
            List<Long> houseIds = terminatedOrders.stream()
                .map(Order::getHouseId)
                .distinct()
                .collect(Collectors.toList());
            
            log.info("待修复的房源列表: {}", houseIds);
            
            // 结果统计
            int successCount = 0;
            int failedCount = 0;
            List<Long> successList = new ArrayList<>();
            List<Long> failedList = new ArrayList<>();
            
            // 遍历更新房源状态
            for (Long houseId : houseIds) {
                // 改用houseService.getHouseInfo获取房源信息
                HouseInfoDTO houseInfo = houseService.getHouseInfo(houseId);
                if (houseInfo != null) {
                    log.info("检查房源: id={}, 当前状态={}", houseId, houseInfo.getStatus());
                    
                    // 如果当前状态是RENTED，则更新为APPROVED
                    if ("RENTED".equals(houseInfo.getStatus())) {
                        try {
                            boolean updated = houseService.updateHouseStatus(houseId, "APPROVED");
                            if (updated) {
                                log.info("成功修复房源状态: id={}, 新状态=APPROVED", houseId);
                                successCount++;
                                successList.add(houseId);
                            } else {
                                log.warn("修复房源状态失败: id={}", houseId);
                                failedCount++;
                                failedList.add(houseId);
                            }
                        } catch (Exception e) {
                            log.error("修复房源状态时发生异常: id={}, error={}", houseId, e.getMessage(), e);
                            failedCount++;
                            failedList.add(houseId);
                        }
                    } else {
                        log.info("房源状态无需修复: id={}, 状态={}", houseId, houseInfo.getStatus());
                    }
                } else {
                    log.warn("房源不存在: id={}", houseId);
                }
            }
            
            // 返回修复结果
            Map<String, Object> result = new HashMap<>();
            result.put("totalProcessed", houseIds.size());
            result.put("successCount", successCount);
            result.put("failedCount", failedCount);
            result.put("successList", successList);
            result.put("failedList", failedList);
            
            log.info("修复退租后房源状态完成: 总数={}, 成功={}, 失败={}", 
                    houseIds.size(), successCount, failedCount);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("修复退租后房源状态失败: {}", e.getMessage(), e);
            return Result.fail("修复过程中发生异常: " + e.getMessage());
        }
    }
    
    /**
     * 房东编辑房源
     */
    @PostMapping("/edit")
    public Result<Boolean> editHouse(@RequestPart(value = "data") @Valid HouseEditDTO editDTO,
                                    @RequestPart(value = "coverImage", required = false) MultipartFile coverImageFile,
                                    @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles,
                                    HttpServletRequest request) {
        try {
            // 从request中获取用户ID
            Long userId = (Long) request.getAttribute("userId");
            log.info("编辑房源, 用户ID: {}, 房源ID: {}", userId, editDTO.getId());
            
            // 设置文件
            if (coverImageFile != null && !coverImageFile.isEmpty()) {
                editDTO.setCoverImageFile(coverImageFile);
            }
            
            if (imageFiles != null && !imageFiles.isEmpty()) {
                editDTO.setImageFiles(imageFiles);
            }
            
            // 编辑房源
            boolean success = houseService.editHouse(editDTO, userId);
            
            if (success) {
                log.info("编辑房源成功, 房源ID: {}", editDTO.getId());
                return Result.success(true);
            } else {
                log.warn("编辑房源失败, 房源ID: {}", editDTO.getId());
                return Result.fail("编辑房源失败，请检查权限或房源状态");
            }
        } catch (Exception e) {
            log.error("编辑房源失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 获取房源图片列表
     */
    @GetMapping("/{houseId}/images")
    public Result<List<String>> getHouseImages(@PathVariable Long houseId) {
        try {
            log.info("获取房源图片列表, 房源ID: {}", houseId);
            
            // 获取房源图片列表
            List<String> images = houseService.getHouseImages(houseId);
            
            log.info("获取房源图片列表成功, 房源ID: {}, 图片数量: {}", houseId, images.size());
            return Result.success(images);
        } catch (Exception e) {
            log.error("获取房源图片列表失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 根据条件查询房源列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getHouseList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String rentType,
            @RequestParam(required = false) String houseType,
            @RequestParam(required = false) Integer bedroomCount,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("查询房源列表: keyword={}, province={}, city={}, district={}, rentType={}, houseType={}, bedroomCount={}, priceRange=[{}, {}], page={}, size={}",
                    keyword, province, city, district, rentType, houseType, bedroomCount, minPrice, maxPrice, page, size);
            
            // 构建查询条件
            House query = new House();
            
            // 设置查询条件
            if (StringUtils.hasText(province)) {
                query.setProvince(province);
            }
            
            if (StringUtils.hasText(city)) {
                query.setCity(city);
            }
            
            if (StringUtils.hasText(district)) {
                query.setDistrict(district);
            }
            
            if (StringUtils.hasText(rentType)) {
                query.setRentType(rentType);
            }
            
            if (StringUtils.hasText(houseType)) {
                query.setHouseType(houseType);
            }
            
            if (bedroomCount != null) {
                query.setBedroomCount(bedroomCount);
            }
            
            // 支持关键词模糊搜索
            // 优化关键词搜索，支持标题、描述、地址等字段的模糊匹配
            Map<String, Object> queryParams = new HashMap<>();
            if (StringUtils.hasText(keyword)) {
                // 将关键词转换为模糊匹配模式 (like '%keyword%')
                queryParams.put("keyword", "%" + keyword + "%");
            }
            
            // 价格范围
            if (minPrice != null) {
                queryParams.put("minPrice", minPrice);
            }
            
            if (maxPrice != null) {
                queryParams.put("maxPrice", maxPrice);
            }
            
            // 分页条件
            PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
            
            // 查询房源数据
            Map<String, Object> result = houseService.getHouseList(query, queryParams, pageRequest);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询房源列表失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
} 