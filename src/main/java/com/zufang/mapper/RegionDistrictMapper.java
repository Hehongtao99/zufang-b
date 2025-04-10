package com.zufang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zufang.entity.RegionDistrict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 区域Mapper接口
 * 完全使用MyBatis-Plus注解方式，无需XML配置
 */
@Mapper
public interface RegionDistrictMapper extends BaseMapper<RegionDistrict> {
    
    /**
     * 根据城市ID查询区域列表
     * 
     * @param cityId 城市ID
     * @return 区域列表
     */
    @Select("SELECT * FROM region_district WHERE city_id = #{cityId} AND is_deleted = 0 ORDER BY id ASC")
    List<RegionDistrict> selectByCityId(@Param("cityId") Long cityId);
    
    /**
     * 根据省份ID查询区域列表
     * 
     * @param provinceId 省份ID
     * @return 区域列表
     */
    @Select("SELECT * FROM region_district WHERE province_id = #{provinceId} AND is_deleted = 0 ORDER BY id ASC")
    List<RegionDistrict> selectByProvinceId(@Param("provinceId") Long provinceId);
    
    /**
     * 根据城市ID和区域名称查询区域
     * 
     * @param cityId 城市ID
     * @param name 区域名称
     * @return 区域对象
     */
    @Select("SELECT * FROM region_district WHERE city_id = #{cityId} AND name = #{name} AND is_deleted = 0 LIMIT 1")
    RegionDistrict selectByCityIdAndName(@Param("cityId") Long cityId, @Param("name") String name);
    
    /**
     * 逻辑删除区域
     * 
     * @param id 区域ID
     * @return 影响的行数
     */
    @Update("UPDATE region_district SET is_deleted = 1, update_time = NOW() WHERE id = #{id} AND is_deleted = 0")
    int logicalDelete(@Param("id") Long id);
    
    /**
     * 根据城市ID逻辑删除该城市下的所有区域
     * 
     * @param cityId 城市ID
     * @return 影响的行数
     */
    @Update("UPDATE region_district SET is_deleted = 1, update_time = NOW() WHERE city_id = #{cityId} AND is_deleted = 0")
    int logicalDeleteByCityId(@Param("cityId") Long cityId);
    
    /**
     * 根据省份ID逻辑删除该省份下的所有区域
     * 
     * @param provinceId 省份ID
     * @return 影响的行数
     */
    @Update("UPDATE region_district SET is_deleted = 1, update_time = NOW() WHERE province_id = #{provinceId} AND is_deleted = 0")
    int logicalDeleteByProvinceId(@Param("provinceId") Long provinceId);
} 