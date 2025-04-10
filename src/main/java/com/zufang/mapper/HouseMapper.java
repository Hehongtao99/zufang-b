package com.zufang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zufang.dto.HouseInfoDTO;
import com.zufang.entity.House;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 房源Mapper接口
 */
@Mapper
public interface HouseMapper extends BaseMapper<House> {
    
    /**
     * 查询所有房源信息（包含房东信息和图片信息）
     * 解决N+1查询问题
     */
    @Select("SELECT h.*, u.nickname as owner_name, u.avatar as owner_avatar, u.phone as owner_phone " +
            "FROM house h " +
            "LEFT JOIN user u ON h.owner_id = u.id " +
            "ORDER BY h.create_time DESC")
    List<HouseInfoDTO> selectHouseInfoList();
    
    /**
     * 分页查询所有房源信息（包含房东信息）
     * 解决N+1查询问题
     */
    @Select("SELECT h.*, u.nickname as owner_name, u.avatar as owner_avatar, u.phone as owner_phone " +
            "FROM house h " +
            "LEFT JOIN user u ON h.owner_id = u.id " +
            "ORDER BY h.create_time DESC")
    Page<HouseInfoDTO> selectHouseInfoPage(Page<HouseInfoDTO> page);
    
    /**
     * 根据状态分页查询房源信息（包含房东信息）
     */
    @Select("SELECT h.*, u.nickname as owner_name, u.avatar as owner_avatar, u.phone as owner_phone " +
            "FROM house h " +
            "LEFT JOIN user u ON h.owner_id = u.id " +
            "WHERE h.status = #{status} " +
            "ORDER BY h.create_time DESC")
    Page<HouseInfoDTO> selectHouseInfoPageByStatus(Page<HouseInfoDTO> page, @Param("status") String status);
    
    /**
     * 分页查询房东的房源信息
     */
    @Select("SELECT h.*, u.nickname as owner_name, u.avatar as owner_avatar, u.phone as owner_phone " +
            "FROM house h " +
            "LEFT JOIN user u ON h.owner_id = u.id " +
            "WHERE h.owner_id = #{ownerId} " +
            "ORDER BY h.create_time DESC")
    Page<HouseInfoDTO> selectLandlordHousePage(Page<HouseInfoDTO> page, @Param("ownerId") Long ownerId);
    
    /**
     * 分页查询待审核的房源信息
     */
    @Select("SELECT h.*, u.nickname as owner_name, u.avatar as owner_avatar, u.phone as owner_phone " +
            "FROM house h " +
            "LEFT JOIN user u ON h.owner_id = u.id " +
            "WHERE h.status = 'PENDING' " +
            "ORDER BY h.create_time ASC")
    Page<HouseInfoDTO> selectPendingHousePage(Page<HouseInfoDTO> page);
    
    /**
     * 高级搜索房源
     */
    @Select("<script>" +
            "SELECT h.*, u.nickname as owner_name, u.avatar as owner_avatar, u.phone as owner_phone " +
            "FROM house h " +
            "LEFT JOIN user u ON h.owner_id = u.id " +
            "WHERE h.status = 'APPROVED' " +
            "<if test='keyword != null and keyword != \"\"'>" +
                "AND (h.title LIKE CONCAT('%', #{keyword}, '%') " +
                "  OR h.description LIKE CONCAT('%', #{keyword}, '%') " +
                "  OR h.address LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='city != null and city != \"\"'>" +
                "AND h.city = #{city} " +
            "</if>" +
            "<if test='district != null and district != \"\"'>" +
                "AND h.district = #{district} " +
            "</if>" +
            "<if test='minPrice != null'>" +
                "AND h.price &gt;= #{minPrice} " +
            "</if>" +
            "<if test='maxPrice != null'>" +
                "AND h.price &lt;= #{maxPrice} " +
            "</if>" +
            "<if test='minArea != null'>" +
                "AND h.area &gt;= #{minArea} " +
            "</if>" +
            "<if test='maxArea != null'>" +
                "AND h.area &lt;= #{maxArea} " +
            "</if>" +
            "<if test='bedroomCount != null'>" +
                "AND h.bedroom_count = #{bedroomCount} " +
            "</if>" +
            "<if test='houseType != null and houseType != \"\"'>" +
                "AND h.house_type = #{houseType} " +
            "</if>" +
            "<if test='rentType != null and rentType != \"\"'>" +
                "AND h.rent_type = #{rentType} " +
            "</if>" +
            "<if test='hasElevator != null'>" +
                "AND h.has_elevator = #{hasElevator} " +
            "</if>" +
            "<if test='hasParking != null'>" +
                "AND h.has_parking = #{hasParking} " +
            "</if>" +
            "<choose>" +
                "<when test='sortField != null and sortField == \"price\" and sortOrder != null and sortOrder == \"asc\"'>" +
                    "ORDER BY h.price ASC" +
                "</when>" +
                "<when test='sortField != null and sortField == \"price\" and sortOrder != null and sortOrder == \"desc\"'>" +
                    "ORDER BY h.price DESC" +
                "</when>" +
                "<when test='sortField != null and sortField == \"area\" and sortOrder != null and sortOrder == \"asc\"'>" +
                    "ORDER BY h.area ASC" +
                "</when>" +
                "<when test='sortField != null and sortField == \"area\" and sortOrder != null and sortOrder == \"desc\"'>" +
                    "ORDER BY h.area DESC" +
                "</when>" +
                "<when test='sortField != null and sortField == \"createTime\" and sortOrder != null and sortOrder == \"asc\"'>" +
                    "ORDER BY h.create_time ASC" +
                "</when>" +
                "<otherwise>" +
                    "ORDER BY h.create_time DESC" +
                "</otherwise>" +
            "</choose>" +
            "</script>")
    Page<HouseInfoDTO> searchHouses(Page<HouseInfoDTO> page, 
            @Param("keyword") String keyword,
            @Param("city") String city,
            @Param("district") String district,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minArea") Integer minArea,
            @Param("maxArea") Integer maxArea,
            @Param("bedroomCount") Integer bedroomCount,
            @Param("houseType") String houseType,
            @Param("rentType") String rentType,
            @Param("hasElevator") Boolean hasElevator,
            @Param("hasParking") Boolean hasParking,
            @Param("sortField") String sortField,
            @Param("sortOrder") String sortOrder);
} 