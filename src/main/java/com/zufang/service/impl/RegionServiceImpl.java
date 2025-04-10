package com.zufang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zufang.dto.RegionDTO;
import com.zufang.entity.RegionCity;
import com.zufang.entity.RegionDistrict;
import com.zufang.entity.RegionProvince;
import com.zufang.mapper.RegionCityMapper;
import com.zufang.mapper.RegionDistrictMapper;
import com.zufang.mapper.RegionProvinceMapper;
import com.zufang.service.RegionService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 地区服务实现类
 */
@Slf4j
@Service
public class RegionServiceImpl implements RegionService {
    
    private static final Logger logger = LoggerFactory.getLogger(RegionServiceImpl.class);
    
    @Autowired
    private RegionProvinceMapper regionProvinceMapper;
    
    @Autowired
    private RegionCityMapper regionCityMapper;
    
    @Autowired
    private RegionDistrictMapper regionDistrictMapper;
    
    @Override
    public List<RegionProvince> getAllProvinces() {
        LambdaQueryWrapper<RegionProvince> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(RegionProvince::getId);
        return regionProvinceMapper.selectList(wrapper);
    }
    
    @Override
    public List<RegionCity> getCitiesByProvinceId(Long provinceId) {
        LambdaQueryWrapper<RegionCity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegionCity::getProvinceId, provinceId);
        wrapper.orderByAsc(RegionCity::getId);
        return regionCityMapper.selectList(wrapper);
    }
    
    @Override
    public RegionProvince getProvinceById(Long id) {
        return regionProvinceMapper.selectById(id);
    }
    
    @Override
    public RegionCity getCityById(Long id) {
        return regionCityMapper.selectById(id);
    }
    
    @Override
    @Transactional
    public Long addProvince(RegionProvince province) {
        // 检查是否已存在同名省份
        LambdaQueryWrapper<RegionProvince> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegionProvince::getName, province.getName());
        if (regionProvinceMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("已存在同名省份");
        }
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        province.setCreateTime(now);
        province.setUpdateTime(now);
        province.setIsDeleted(0);
        
        // 为code字段设置默认值，如果前端没有提供
        if (province.getCode() == null || province.getCode().trim().isEmpty()) {
            // 生成一个基于省份名称拼音首字母的代码，例如"北京市"生成"BJS"
            // 这里简化处理，使用省份名称的前三个字符作为代码
            String name = province.getName();
            String code = name.length() > 3 ? name.substring(0, 3) : name;
            // 添加随机数，确保唯一性
            code += System.currentTimeMillis() % 10000;
            province.setCode(code);
        }
        
        // 使用MyBatis-Plus的insert方法
        regionProvinceMapper.insert(province);
        return province.getId();
    }
    
    @Override
    @Transactional
    public Long addCity(RegionCity city) {
        // 检查省份是否存在
        if (regionProvinceMapper.selectById(city.getProvinceId()) == null) {
            throw new RuntimeException("所属省份不存在");
        }
        
        // 检查是否已存在同名城市（同一省份下）
        LambdaQueryWrapper<RegionCity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegionCity::getProvinceId, city.getProvinceId())
               .eq(RegionCity::getName, city.getName());
        if (regionCityMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("该省份下已存在同名城市");
        }
        
        // 为code字段设置默认值，如果前端没有提供
        if (city.getCode() == null || city.getCode().trim().isEmpty()) {
            // 获取省份信息，用于生成编码前缀
            RegionProvince province = regionProvinceMapper.selectById(city.getProvinceId());
            String provincePrefix = province != null && province.getCode() != null ? 
                province.getCode().substring(0, Math.min(3, province.getCode().length())) : 
                "CTY";
            
            // 生成一个基于城市名称和省份代码的唯一代码
            String name = city.getName();
            String cityCode = name.length() > 2 ? name.substring(0, 2) : name;
            // 添加随机数和时间戳，确保唯一性
            String code = provincePrefix + cityCode + System.currentTimeMillis() % 10000;
            city.setCode(code);
            
            logger.info("为城市[{}]生成代码: {}", city.getName(), code);
        }
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        city.setCreateTime(now);
        city.setUpdateTime(now);
        city.setIsDeleted(0);
        
        regionCityMapper.insert(city);
        return city.getId();
    }
    
    @Override
    @Transactional
    public boolean updateProvince(RegionProvince province) {
        // 检查是否已存在同名省份（排除自身）
        LambdaQueryWrapper<RegionProvince> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegionProvince::getName, province.getName())
               .ne(RegionProvince::getId, province.getId());
        if (regionProvinceMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("已存在同名省份");
        }
        
        // 设置更新时间
        province.setUpdateTime(LocalDateTime.now());
        
        // 使用MyBatis-Plus的updateById方法
        return regionProvinceMapper.updateById(province) > 0;
    }
    
    @Override
    @Transactional
    public boolean updateCity(RegionCity city) {
        // 检查省份是否存在
        if (regionProvinceMapper.selectById(city.getProvinceId()) == null) {
            throw new RuntimeException("所属省份不存在");
        }
        
        // 检查是否已存在同名城市（同一省份下，排除自身）
        LambdaQueryWrapper<RegionCity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegionCity::getProvinceId, city.getProvinceId())
               .eq(RegionCity::getName, city.getName())
               .ne(RegionCity::getId, city.getId());
        if (regionCityMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("该省份下已存在同名城市");
        }
        
        // 获取原城市信息，保留原code值
        RegionCity existingCity = regionCityMapper.selectById(city.getId());
        if (existingCity == null) {
            throw new RuntimeException("要更新的城市不存在");
        }
        
        // 如果前端没有提供code或提供的是空值，保留原code
        if (city.getCode() == null || city.getCode().trim().isEmpty()) {
            city.setCode(existingCity.getCode());
        }
        
        // 设置更新时间
        city.setUpdateTime(LocalDateTime.now());
        
        return regionCityMapper.updateById(city) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteProvince(Long id) {
        // 先删除该省份下的所有城市和区域
        List<RegionCity> cities = getCitiesByProvinceId(id);
        for (RegionCity city : cities) {
            deleteCity(city.getId());
        }
        
        // 使用MyBatis-Plus的逻辑删除
        return regionProvinceMapper.deleteById(id) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteCity(Long id) {
        // 先删除该城市下的所有区域
        regionDistrictMapper.logicalDeleteByCityId(id);
        
        // 删除城市
        return regionCityMapper.logicalDelete(id) > 0;
    }
    
    @Override
    public List<RegionDTO> getAllRegionsTree() {
        // 获取所有省份
        List<RegionProvince> provinces = getAllProvinces();
        
        // 转换为树形结构
        return provinces.stream().map(province -> {
            RegionDTO provinceDTO = new RegionDTO();
            provinceDTO.setId(province.getId());
            provinceDTO.setName(province.getName());
            provinceDTO.setCode(province.getCode());
            provinceDTO.setParentId(null); // 省份没有父级
            
            // 获取该省份下的所有城市
            List<RegionCity> cities = getCitiesByProvinceId(province.getId());
            
            // 转换城市列表
            List<RegionDTO> cityDTOs = cities.stream().map(city -> {
                RegionDTO cityDTO = new RegionDTO();
                cityDTO.setId(city.getId());
                cityDTO.setName(city.getName());
                cityDTO.setCode(city.getCode());
                cityDTO.setParentId(province.getId());
                
                // 获取该城市下的所有区域
                List<RegionDistrict> districts = getDistrictsByCityId(city.getId());
                
                // 转换区域列表
                List<RegionDTO> districtDTOs = districts.stream().map(district -> {
                    RegionDTO districtDTO = new RegionDTO();
                    districtDTO.setId(district.getId());
                    districtDTO.setName(district.getName());
                    districtDTO.setCode(district.getCode());
                    districtDTO.setParentId(city.getId());
                    districtDTO.setChildren(new ArrayList<>()); // 区域没有子级
                    return districtDTO;
                }).collect(Collectors.toList());
                
                cityDTO.setChildren(districtDTOs);
                return cityDTO;
            }).collect(Collectors.toList());
            
            provinceDTO.setChildren(cityDTOs);
            return provinceDTO;
        }).collect(Collectors.toList());
    }
    
    @Override
    public List<RegionDistrict> getDistrictsByCityId(Long cityId) {
        LambdaQueryWrapper<RegionDistrict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegionDistrict::getCityId, cityId);
        wrapper.orderByAsc(RegionDistrict::getId);
        return regionDistrictMapper.selectList(wrapper);
    }
    
    @Override
    public RegionDistrict getDistrictById(Long id) {
        return regionDistrictMapper.selectById(id);
    }
    
    @Override
    @Transactional
    public Long addDistrict(RegionDistrict district) {
        // 检查城市是否存在
        if (regionCityMapper.selectById(district.getCityId()) == null) {
            throw new RuntimeException("所属城市不存在");
        }
        
        // 检查省份是否存在
        if (regionProvinceMapper.selectById(district.getProvinceId()) == null) {
            throw new RuntimeException("所属省份不存在");
        }
        
        // 检查是否已存在同名区域（同一城市下）
        LambdaQueryWrapper<RegionDistrict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegionDistrict::getCityId, district.getCityId())
               .eq(RegionDistrict::getName, district.getName());
        if (regionDistrictMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("该城市下已存在同名区域");
        }
        
        // 为code字段设置默认值，如果前端没有提供
        if (district.getCode() == null || district.getCode().trim().isEmpty()) {
            // 获取城市信息，用于生成编码前缀
            RegionCity city = regionCityMapper.selectById(district.getCityId());
            String cityPrefix = city != null && city.getCode() != null ? 
                city.getCode().substring(0, Math.min(3, city.getCode().length())) : 
                "DST";
            
            // 生成一个基于区域名称和城市代码的唯一代码
            String name = district.getName();
            String districtCode = name.length() > 2 ? name.substring(0, 2) : name;
            // 添加随机数和时间戳，确保唯一性
            String code = cityPrefix + districtCode + System.currentTimeMillis() % 10000;
            district.setCode(code);
            
            logger.info("为区域[{}]生成代码: {}", district.getName(), code);
        }
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        district.setCreateTime(now);
        district.setUpdateTime(now);
        district.setIsDeleted(0);
        
        regionDistrictMapper.insert(district);
        return district.getId();
    }
    
    @Override
    @Transactional
    public boolean updateDistrict(RegionDistrict district) {
        // 检查城市是否存在
        if (regionCityMapper.selectById(district.getCityId()) == null) {
            throw new RuntimeException("所属城市不存在");
        }
        
        // 检查省份是否存在
        if (regionProvinceMapper.selectById(district.getProvinceId()) == null) {
            throw new RuntimeException("所属省份不存在");
        }
        
        // 检查是否已存在同名区域（同一城市下，排除自身）
        LambdaQueryWrapper<RegionDistrict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RegionDistrict::getCityId, district.getCityId())
               .eq(RegionDistrict::getName, district.getName())
               .ne(RegionDistrict::getId, district.getId());
        if (regionDistrictMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("该城市下已存在同名区域");
        }
        
        // 获取原区域信息，保留原code值
        RegionDistrict existingDistrict = regionDistrictMapper.selectById(district.getId());
        if (existingDistrict == null) {
            throw new RuntimeException("要更新的区域不存在");
        }
        
        // 如果前端没有提供code或提供的是空值，保留原code
        if (district.getCode() == null || district.getCode().trim().isEmpty()) {
            district.setCode(existingDistrict.getCode());
        }
        
        // 设置更新时间
        district.setUpdateTime(LocalDateTime.now());
        
        return regionDistrictMapper.updateById(district) > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteDistrict(Long id) {
        // 删除区域
        return regionDistrictMapper.logicalDelete(id) > 0;
    }
} 