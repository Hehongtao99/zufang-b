package com.zufang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zufang.entity.RegionProvince;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

/**
 * 省份Mapper接口
 * 完全使用MyBatis-Plus注解方式，无需XML配置
 */
@Mapper
public interface RegionProvinceMapper extends BaseMapper<RegionProvince> {
    
    /**
     * 根据名称查询省份
     * 
     * @param name 省份名称
     * @return 省份对象
     */
    @Select("SELECT * FROM region_province WHERE name = #{name} AND is_deleted = 0 LIMIT 1")
    RegionProvince selectByName(@Param("name") String name);
    
    /**
     * 逻辑删除省份（如果使用了@TableLogic注解，可以移除此方法）
     * 
     * @param id 省份ID
     * @return 影响的行数
     */
    @Update("UPDATE region_province SET is_deleted = 1, update_time = NOW() WHERE id = #{id} AND is_deleted = 0")
    int logicalDelete(@Param("id") Long id);
} 