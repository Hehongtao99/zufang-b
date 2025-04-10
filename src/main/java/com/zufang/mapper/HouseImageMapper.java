package com.zufang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zufang.entity.HouseImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 房源图片Mapper接口
 */
@Mapper
public interface HouseImageMapper extends BaseMapper<HouseImage> {
    
    /**
     * 批量查询多个房源的图片
     * @param houseIds 房源ID列表
     * @return 图片URL列表映射，key为房源ID，value为该房源的图片URL列表
     */
    @Select("<script>" +
            "SELECT hi.house_id, hi.url " +
            "FROM house_image hi " +
            "WHERE hi.is_deleted = 0 " +
            "AND hi.house_id IN " +
            "<foreach collection='houseIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " ORDER BY hi.house_id, hi.sort ASC" +
            "</script>")
    List<Map<String, Object>> selectImagesByHouseIds(@Param("houseIds") List<Long> houseIds);
} 