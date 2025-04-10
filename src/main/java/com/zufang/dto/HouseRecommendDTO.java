package com.zufang.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 推荐房源DTO，适配前端需求
 */
@Data
public class HouseRecommendDTO {
    
    private Long id; // 房源ID
    
    private String title; // 标题
    
    private String description; // 描述
    
    private Integer area; // 面积（平方米）
    
    private BigDecimal price; // 月租价格
    
    private String address; // 地址
    
    private String province; // 省份
    
    private String city; // 城市
    
    private String district; // 区域
    
    private Integer bedroomCount; // 卧室数量
    
    private Integer livingRoomCount; // 客厅数量
    
    private Integer bathroomCount; // 卫生间数量
    
    private String orientation; // 朝向
    
    private Integer floor; // 楼层
    
    private Integer totalFloor; // 总楼层
    
    private String decoration; // 装修情况
    
    private Boolean hasElevator; // 是否有电梯
    
    private Boolean hasParking; // 是否有停车位
    
    private String houseType; // 房源类型：APARTMENT-公寓，HOUSE-别墅
    
    private String rentType; // 出租类型：WHOLE-整租，SHARED-合租
    
    private String status; // 状态：PENDING-待审核，APPROVED-已上架，REJECTED-已拒绝，RENTED-已出租，OFFLINE-已下架
    
    private String coverImage; // 封面图片
    
    private List<Map<String, String>> images; // 房源图片列表，格式为[{url: 'xxx'}]
    
    private Long ownerId; // 房东ID
    
    private String ownerName; // 房东名称
    
    private String ownerAvatar; // 房东头像
    
    private String ownerPhone; // 房东联系电话
    
    private LocalDateTime createTime; // 创建时间
    
    private LocalDateTime updateTime; // 更新时间
    
    /**
     * 从HouseInfoDTO转换为HouseRecommendDTO
     */
    public static HouseRecommendDTO fromHouseInfoDTO(HouseInfoDTO infoDTO) {
        if (infoDTO == null) {
            return null;
        }
        
        HouseRecommendDTO recommendDTO = new HouseRecommendDTO();
        recommendDTO.setId(infoDTO.getId());
        recommendDTO.setTitle(infoDTO.getTitle());
        recommendDTO.setDescription(infoDTO.getDescription());
        recommendDTO.setArea(infoDTO.getArea());
        recommendDTO.setPrice(infoDTO.getPrice());
        recommendDTO.setAddress(infoDTO.getAddress());
        recommendDTO.setProvince(infoDTO.getProvince());
        recommendDTO.setCity(infoDTO.getCity());
        recommendDTO.setDistrict(infoDTO.getDistrict());
        recommendDTO.setBedroomCount(infoDTO.getBedroomCount());
        recommendDTO.setLivingRoomCount(infoDTO.getLivingRoomCount());
        recommendDTO.setBathroomCount(infoDTO.getBathroomCount());
        recommendDTO.setOrientation(infoDTO.getOrientation());
        recommendDTO.setFloor(infoDTO.getFloor());
        recommendDTO.setTotalFloor(infoDTO.getTotalFloor());
        recommendDTO.setDecoration(infoDTO.getDecoration());
        recommendDTO.setHasElevator(infoDTO.getHasElevator());
        recommendDTO.setHasParking(infoDTO.getHasParking());
        recommendDTO.setHouseType(infoDTO.getHouseType());
        recommendDTO.setRentType(infoDTO.getRentType());
        recommendDTO.setStatus(infoDTO.getStatus());
        recommendDTO.setCoverImage(infoDTO.getCoverImage());
        recommendDTO.setOwnerId(infoDTO.getOwnerId());
        recommendDTO.setOwnerName(infoDTO.getOwnerName());
        recommendDTO.setOwnerAvatar(infoDTO.getOwnerAvatar());
        recommendDTO.setOwnerPhone(infoDTO.getOwnerPhone());
        recommendDTO.setCreateTime(infoDTO.getCreateTime());
        recommendDTO.setUpdateTime(infoDTO.getUpdateTime());
        
        return recommendDTO;
    }
} 