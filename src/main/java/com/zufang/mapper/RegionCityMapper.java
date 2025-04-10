package com.zufang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zufang.entity.RegionCity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

/**
 * 城市Mapper接口
 * 完全使用MyBatis-Plus注解方式，无需XML配置
 */
@Mapper
public interface RegionCityMapper extends BaseMapper<RegionCity> {
    
    /**
     * 根据省份ID和城市名称查询城市
     * 
     * @param provinceId 省份ID
     * @param name 城市名称
     * @return 城市对象
     */
    @Select("SELECT * FROM region_city WHERE province_id = #{provinceId} AND name = #{name} AND is_deleted = 0 LIMIT 1")
    RegionCity selectByProvinceIdAndName(@Param("provinceId") Long provinceId, @Param("name") String name);
    
    /**
     * 逻辑删除城市
     * 
     * @param id 城市ID
     * @return 影响的行数
     */
    @Update("UPDATE region_city SET is_deleted = 1, update_time = NOW() WHERE id = #{id} AND is_deleted = 0")
    int logicalDelete(@Param("id") Long id);
    
    /**
     * 根据省份ID逻辑删除所有城市
     * 
     * @param provinceId 省份ID
     * @return 影响的行数
     */
    @Update("UPDATE region_city SET is_deleted = 1, update_time = NOW() WHERE province_id = #{provinceId} AND is_deleted = 0")
    int logicalDeleteByProvinceId(@Param("provinceId") Long provinceId);
} 