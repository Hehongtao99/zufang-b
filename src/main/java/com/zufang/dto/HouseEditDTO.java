package com.zufang.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

/**
 * 房源编辑DTO
 */
@Data
public class HouseEditDTO {
    
    @NotNull(message = "房源ID不能为空")
    private Long id; // 房源ID
    
    private String title; // 标题
    
    private String description; // 描述
    
    @Min(value = 1, message = "面积必须大于0")
    private Integer area; // 面积（平方米）
    
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price; // 月租价格
    
    private String address; // 地址
    
    private String province; // 省份名称
    
    private String city; // 城市名称
    
    private String district; // 区域
    
    private Long provinceId; // 省份ID
    
    @NotNull(message = "区域不能为空")
    private Long districtId; // 区域ID
    
    private Long cityId; // 城市ID
    
    @Min(value = 0, message = "卧室数量必须大于等于0")
    private Integer bedroomCount; // 卧室数量
    
    @Min(value = 0, message = "客厅数量必须大于等于0")
    private Integer livingRoomCount; // 客厅数量
    
    @Min(value = 0, message = "卫生间数量必须大于等于0")
    private Integer bathroomCount; // 卫生间数量
    
    private String orientation; // 朝向
    
    private Integer floor; // 楼层
    
    private Integer totalFloor; // 总楼层
    
    private String decoration; // 装修情况
    
    private Boolean hasElevator; // 是否有电梯
    
    private Boolean hasParking; // 是否有停车位
    
    private String houseType; // 房源类型：APARTMENT-公寓，HOUSE-别墅
    
    private String rentType; // 出租类型：WHOLE-整租，SHARED-合租
    
    private String status; // 状态（仅管理员可设置）：PENDING-待审核，APPROVED-已上架，REJECTED-已拒绝，RENTED-已出租，OFFLINE-已下架
    
    private String coverImage; // 封面图片URL，如果使用MultipartFile上传，则不需要此字段
    
    private MultipartFile coverImageFile; // 封面图片文件，用于文件上传
    
    private List<String> imagesToDelete; // 需要删除的图片URL列表
    
    private List<String> imageUrls; // 房源图片URL列表，如果使用MultipartFile上传，则不需要此字段
    
    private List<MultipartFile> imageFiles; // 房源图片文件列表，用于文件上传
    
    @Min(value = 1, message = "最短租期必须大于等于1个月")
    private Integer minLeaseTerm; // 最短租期(月)
    
    @Min(value = 1, message = "押金月数必须大于等于1个月")
    private Integer depositMonths; // 押金月数
    
    private Long contractTemplateId; // 合同模板ID
    
    private String rejectReason; // 拒绝原因（仅管理员可设置）
} 