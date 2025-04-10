package com.zufang.controller;

import com.zufang.common.Result;
import com.zufang.dto.RegionDTO;
import com.zufang.entity.RegionCity;
import com.zufang.entity.RegionDistrict;
import com.zufang.entity.RegionProvince;
import com.zufang.service.RegionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 地区控制器
 */
@Slf4j
@RestController
@RequestMapping("/region")
public class RegionController {
    
    @Autowired
    private RegionService regionService;
    
    /**
     * 获取所有省份
     */
    @GetMapping("/provinces")
    public Result<List<RegionProvince>> getAllProvinces() {
        try {
            List<RegionProvince> provinces = regionService.getAllProvinces();
            return Result.success(provinces);
        } catch (Exception e) {
            log.error("获取省份列表失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 获取指定省份的所有城市
     */
    @GetMapping("/cities/{provinceId}")
    public Result<List<RegionCity>> getCitiesByProvinceId(@PathVariable Long provinceId) {
        try {
            List<RegionCity> cities = regionService.getCitiesByProvinceId(provinceId);
            return Result.success(cities);
        } catch (Exception e) {
            log.error("获取城市列表失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 获取所有地区（树形结构）
     */
    @GetMapping("/tree")
    public Result<List<RegionDTO>> getAllRegionsTree() {
        try {
            List<RegionDTO> regions = regionService.getAllRegionsTree();
            return Result.success(regions);
        } catch (Exception e) {
            log.error("获取地区树失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 获取指定城市的所有区域
     */
    @GetMapping("/districts/{cityId}")
    public Result<List<RegionDistrict>> getDistrictsByCityId(@PathVariable Long cityId) {
        try {
            List<RegionDistrict> districts = regionService.getDistrictsByCityId(cityId);
            return Result.success(districts);
        } catch (Exception e) {
            log.error("获取区域列表失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 添加省份（管理员）
     */
    @PostMapping("/province")
    public Result<Long> addProvince(@RequestBody RegionProvince province, HttpServletRequest request) {
        try {
            // 从request中获取用户角色
            String role = (String) request.getAttribute("role");
            
            // 校验用户是否为管理员
            if (!"ADMIN".equals(role)) {
                return Result.fail("只有管理员才能添加省份");
            }
            
            Long id = regionService.addProvince(province);
            return Result.success(id);
        } catch (Exception e) {
            log.error("添加省份失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 添加城市（管理员）
     */
    @PostMapping("/city")
    public Result<Long> addCity(@RequestBody RegionCity city, HttpServletRequest request) {
        try {
            // 从request中获取用户角色
            String role = (String) request.getAttribute("role");
            
            // 校验用户是否为管理员
            if (!"ADMIN".equals(role)) {
                return Result.fail("只有管理员才能添加城市");
            }
            
            Long id = regionService.addCity(city);
            return Result.success(id);
        } catch (Exception e) {
            log.error("添加城市失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 添加区域（管理员）
     */
    @PostMapping("/district")
    public Result<Long> addDistrict(@RequestBody RegionDistrict district, HttpServletRequest request) {
        try {
            // 从request中获取用户角色
            String role = (String) request.getAttribute("role");
            
            // 校验用户是否为管理员
            if (!"ADMIN".equals(role)) {
                return Result.fail("只有管理员才能添加区域");
            }
            
            Long id = regionService.addDistrict(district);
            return Result.success(id);
        } catch (Exception e) {
            log.error("添加区域失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 更新省份（管理员）
     */
    @PutMapping("/province/{id}")
    public Result<Boolean> updateProvince(@PathVariable Long id, @RequestBody RegionProvince province, HttpServletRequest request) {
        try {
            // 从request中获取用户角色
            String role = (String) request.getAttribute("role");
            
            // 校验用户是否为管理员
            if (!"ADMIN".equals(role)) {
                return Result.fail("只有管理员才能更新省份");
            }
            
            province.setId(id);
            boolean success = regionService.updateProvince(province);
            return Result.success(success);
        } catch (Exception e) {
            log.error("更新省份失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 更新城市（管理员）
     */
    @PutMapping("/city/{id}")
    public Result<Boolean> updateCity(@PathVariable Long id, @RequestBody RegionCity city, HttpServletRequest request) {
        try {
            // 从request中获取用户角色
            String role = (String) request.getAttribute("role");
            
            // 校验用户是否为管理员
            if (!"ADMIN".equals(role)) {
                return Result.fail("只有管理员才能更新城市");
            }
            
            city.setId(id);
            boolean success = regionService.updateCity(city);
            return Result.success(success);
        } catch (Exception e) {
            log.error("更新城市失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 更新区域（管理员）
     */
    @PutMapping("/district/{id}")
    public Result<Boolean> updateDistrict(@PathVariable Long id, @RequestBody RegionDistrict district, HttpServletRequest request) {
        try {
            // 从request中获取用户角色
            String role = (String) request.getAttribute("role");
            
            // 校验用户是否为管理员
            if (!"ADMIN".equals(role)) {
                return Result.fail("只有管理员才能更新区域");
            }
            
            district.setId(id);
            boolean success = regionService.updateDistrict(district);
            return Result.success(success);
        } catch (Exception e) {
            log.error("更新区域失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 删除省份（管理员）
     */
    @DeleteMapping("/province/{id}")
    public Result<Boolean> deleteProvince(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 从request中获取用户角色
            String role = (String) request.getAttribute("role");
            
            // 校验用户是否为管理员
            if (!"ADMIN".equals(role)) {
                return Result.fail("只有管理员才能删除省份");
            }
            
            boolean success = regionService.deleteProvince(id);
            return Result.success(success);
        } catch (Exception e) {
            log.error("删除省份失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 删除城市（管理员）
     */
    @DeleteMapping("/city/{id}")
    public Result<Boolean> deleteCity(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 从request中获取用户角色
            String role = (String) request.getAttribute("role");
            
            // 校验用户是否为管理员
            if (!"ADMIN".equals(role)) {
                return Result.fail("只有管理员才能删除城市");
            }
            
            boolean success = regionService.deleteCity(id);
            return Result.success(success);
        } catch (Exception e) {
            log.error("删除城市失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 删除区域（管理员）
     */
    @DeleteMapping("/district/{id}")
    public Result<Boolean> deleteDistrict(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 从request中获取用户角色
            String role = (String) request.getAttribute("role");
            
            // 校验用户是否为管理员
            if (!"ADMIN".equals(role)) {
                return Result.fail("只有管理员才能删除区域");
            }
            
            boolean success = regionService.deleteDistrict(id);
            return Result.success(success);
        } catch (Exception e) {
            log.error("删除区域失败: {}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }
} 