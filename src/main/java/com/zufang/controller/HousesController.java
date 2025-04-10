package com.zufang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zufang.common.response.Result;
import com.zufang.dto.HouseInfoDTO;
import com.zufang.dto.HousePublishDTO;
import com.zufang.dto.HouseRecommendDTO;
import com.zufang.dto.HouseSearchDTO;
import com.zufang.service.HouseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 房源控制器（复数形式，兼容前端API）
 * 为了匹配前端的API路径，将/houses请求转发到/house端点
 */
@Slf4j
@RestController
@RequestMapping("/houses")
public class HousesController {
    
    @Autowired
    private HouseService houseService;
    
    /**
     * 发布房源
     */
    @PostMapping("")
    public Result<Long> publishHouse(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("area") Integer area,
            @RequestParam("price") BigDecimal price,
            @RequestParam("address") String address,
            @RequestParam("province") String province,
            @RequestParam("provinceId") Long provinceId,
            @RequestParam("city") String city,
            @RequestParam("cityId") Long cityId,
            @RequestParam("district") String district,
            @RequestParam("districtId") Long districtId,
            @RequestParam("bedroomCount") Integer bedroomCount,
            @RequestParam("livingRoomCount") Integer livingRoomCount,
            @RequestParam("bathroomCount") Integer bathroomCount,
            @RequestParam("orientation") String orientation,
            @RequestParam("floor") Integer floor,
            @RequestParam("totalFloor") Integer totalFloor,
            @RequestParam("decoration") String decoration,
            @RequestParam("hasElevator") Boolean hasElevator,
            @RequestParam("hasParking") Boolean hasParking,
            @RequestParam("houseType") String houseType,
            @RequestParam("rentType") String rentType,
            @RequestParam("coverImageFile") MultipartFile coverImageFile,
            @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
            HttpServletRequest request) {
        try {
            // 从request中获取用户ID
            Long userId = (Long) request.getAttribute("userId");
            log.info("发布房源(/houses), 用户ID: {}", userId);
            
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
            
            log.info("发布房源(/houses)成功, 房源ID: {}", houseId);
            return Result.success(houseId);
        } catch (Exception e) {
            log.error("发布房源(/houses)失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 获取房源详情
     */
    @GetMapping("/{id}")
    public Result getHouseInfo(@PathVariable Long id) {
        try {
            HouseInfoDTO houseInfo = houseService.getHouseInfo(id);
            
            if (houseInfo == null) {
                // 房源不存在时，返回错误信息但不抛出异常
                log.warn("获取房源详情失败: 房源[{}]不存在", id);
                return Result.fail("房源不存在");
            }
            
            // 如果房源已删除，会返回带有isDeleted=true标记的有限信息
            return Result.success(houseInfo);
        } catch (Exception e) {
            log.error("获取房源详情(/houses)失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 获取房源详情 - POST方法，解决前端可能使用POST方法的问题
     */
    @PostMapping("/{houseId}")
    public Result<HouseInfoDTO> getHouseInfoPost(@PathVariable Long houseId) {
        return getHouseInfo(houseId);
    }
    
    /**
     * 搜索房源
     */
    @GetMapping("/search")
    public Result<Page<HouseInfoDTO>> searchHouses(
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
            @RequestParam(defaultValue = "10") Integer pageSize) {
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
            
            log.info("搜索房源(/houses/search), 条件: {}", searchDTO);
            
            // 搜索房源
            Page<HouseInfoDTO> housePage = houseService.searchHouses(searchDTO);
            
            log.info("搜索房源(/houses/search)成功, 结果数: {}", housePage.getTotal());
            return Result.success(housePage);
        } catch (Exception e) {
            log.error("搜索房源(/houses/search)失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 获取推荐房源
     */
    @GetMapping("/recommend")
    public Result<List<HouseRecommendDTO>> getRecommendHouses(@RequestParam(defaultValue = "4") Integer limit) {
        try {
            log.info("获取推荐房源(/houses/recommend), 限制数量: {}", limit);
            
            // 获取推荐房源
            List<HouseInfoDTO> recommendHouses = houseService.getRecommendHouses(limit);
            
            // 转换为推荐房源DTO列表
            List<HouseRecommendDTO> result = recommendHouses.stream()
                    .map(house -> {
                        HouseRecommendDTO dto = HouseRecommendDTO.fromHouseInfoDTO(house);
                        
                        // 处理图片列表
                        if (house.getImages() != null && !house.getImages().isEmpty()) {
                            List<Map<String, String>> imagesList = new ArrayList<>();
                            for (String imageUrl : house.getImages()) {
                                Map<String, String> imageMap = new HashMap<>();
                                imageMap.put("url", imageUrl);
                                imagesList.add(imageMap);
                            }
                            dto.setImages(imagesList);
                        }
                        
                        return dto;
                    })
                    .collect(Collectors.toList());
            
            log.info("获取推荐房源(/houses/recommend)成功, 结果数: {}", result.size());
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取推荐房源(/houses/recommend)失败: {}", e.getMessage(), e);
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
            log.info("下架房源(/houses), 房源ID: {}, 用户ID: {}", houseId, userId);
            
            // 下架房源
            houseService.offlineHouse(houseId, userId);
            
            log.info("下架房源(/houses)成功, 房源ID: {}", houseId);
            return Result.success();
        } catch (Exception e) {
            log.error("下架房源(/houses)失败: {}", e.getMessage(), e);
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
            log.info("上架房源(/houses), 房源ID: {}, 用户ID: {}", houseId, userId);
            
            // 上架房源
            houseService.onlineHouse(houseId, userId);
            
            log.info("上架房源(/houses)成功, 房源ID: {}", houseId);
            return Result.success();
        } catch (Exception e) {
            log.error("上架房源(/houses)失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
} 