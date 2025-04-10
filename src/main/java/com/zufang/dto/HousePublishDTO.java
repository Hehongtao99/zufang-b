package com.zufang.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

/**
 * 房源发布DTO
 */
@Data
public class HousePublishDTO {
    
    @NotBlank(message = "标题不能为空")
    private String title; // 标题
    
    private String description; // 描述
    
    @NotNull(message = "面积不能为空")
    @Min(value = 1, message = "面积必须大于0")
    private Integer area; // 面积（平方米）
    
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price; // 月租价格
    
    @NotBlank(message = "地址不能为空")
    private String address; // 地址
    
    // @NotBlank(message = "城市不能为空") // 移除NotBlank，后端根据cityId填充
    private String city; // 城市
    
    @NotBlank(message = "区域不能为空")
    private String district; // 区域
    
    // 添加province字段，用于接收前端传递的省份名称
    private String province; // 省份名称
    
    @NotNull(message = "省份不能为空")
    private Long provinceId; // 省份ID
    
    @NotNull(message = "城市不能为空")
    private Long cityId; // 城市ID
    
    @NotNull(message = "区域不能为空")
    private Long districtId; // 区域ID
    
    @NotNull(message = "卧室数量不能为空")
    @Min(value = 0, message = "卧室数量必须大于等于0")
    private Integer bedroomCount; // 卧室数量
    
    @NotNull(message = "客厅数量不能为空")
    @Min(value = 0, message = "客厅数量必须大于等于0")
    private Integer livingRoomCount; // 客厅数量
    
    @NotNull(message = "卫生间数量不能为空")
    @Min(value = 0, message = "卫生间数量必须大于等于0")
    private Integer bathroomCount; // 卫生间数量
    
    private String orientation; // 朝向
    
    private Integer floor; // 楼层
    
    private Integer totalFloor; // 总楼层
    
    private String decoration; // 装修情况
    
    private Boolean hasElevator; // 是否有电梯
    
    private Boolean hasParking; // 是否有停车位
    
    @NotBlank(message = "房源类型不能为空")
    private String houseType; // 房源类型：APARTMENT-公寓，HOUSE-别墅
    
    @NotBlank(message = "出租类型不能为空")
    private String rentType; // 出租类型：WHOLE-整租，SHARED-合租
    
    private String coverImage; // 封面图片URL，如果使用MultipartFile上传，则不需要此字段
    
    private MultipartFile coverImageFile; // 封面图片文件，用于文件上传
    
    private List<String> imageUrls; // 房源图片URL列表，如果使用MultipartFile上传，则不需要此字段
    
    private List<MultipartFile> imageFiles; // 房源图片文件列表，用于文件上传
    
    @NotNull(message = "最短租期不能为空")
    @Min(value = 1, message = "最短租期必须大于等于1个月")
    private Integer minLeaseTerm; // 最短租期(月)
    
    @NotNull(message = "押金月数不能为空")
    @Min(value = 1, message = "押金月数必须大于等于1个月")
    private Integer depositMonths; // 押金月数
    
    @NotNull(message = "违约金比例不能为空")
    @DecimalMin(value = "0", message = "违约金比例不能小于0")
    @DecimalMax(value = "100", message = "违约金比例不能大于100")
    private BigDecimal penaltyRate; // 违约金比例（百分比）
    
    @NotNull(message = "最低违约金不能为空")
    @DecimalMin(value = "0", message = "最低违约金不能小于0")
    private BigDecimal minPenalty; // 最低违约金金额
    
    @NotNull(message = "合同模板ID不能为空")
    private Long contractTemplateId; // 合同模板ID
} 