package com.zufang.service;

import com.zufang.dto.RegionDTO;
import com.zufang.entity.RegionCity;
import com.zufang.entity.RegionDistrict;
import com.zufang.entity.RegionProvince;

import java.util.List;

/**
 * 地区服务接口
 */
public interface RegionService {
    
    /**
     * 获取所有省份列表
     */
    List<RegionProvince> getAllProvinces();
    
    /**
     * 获取指定省份的所有城市
     */
    List<RegionCity> getCitiesByProvinceId(Long provinceId);
    
    /**
     * 获取指定城市的所有区域
     */
    List<RegionDistrict> getDistrictsByCityId(Long cityId);
    
    /**
     * 获取省份详情
     */
    RegionProvince getProvinceById(Long id);
    
    /**
     * 获取城市详情
     */
    RegionCity getCityById(Long id);
    
    /**
     * 获取区域详情
     */
    RegionDistrict getDistrictById(Long id);
    
    /**
     * 添加省份
     */
    Long addProvince(RegionProvince province);
    
    /**
     * 添加城市
     */
    Long addCity(RegionCity city);
    
    /**
     * 添加区域
     */
    Long addDistrict(RegionDistrict district);
    
    /**
     * 更新省份
     */
    boolean updateProvince(RegionProvince province);
    
    /**
     * 更新城市
     */
    boolean updateCity(RegionCity city);
    
    /**
     * 更新区域
     */
    boolean updateDistrict(RegionDistrict district);
    
    /**
     * 删除省份
     */
    boolean deleteProvince(Long id);
    
    /**
     * 删除城市
     */
    boolean deleteCity(Long id);
    
    /**
     * 删除区域
     */
    boolean deleteDistrict(Long id);
    
    /**
     * 获取所有地区（树形结构）
     */
    List<RegionDTO> getAllRegionsTree();
} 