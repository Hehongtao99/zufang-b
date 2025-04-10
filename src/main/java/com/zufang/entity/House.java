package com.zufang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 房源实体类
 */
@Data
@TableName("house")
public class House implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id; // 房源ID
    
    private String title; // 标题
    
    private String description; // 描述
    
    private Integer area; // 面积（平方米）
    
    private BigDecimal price; // 月租价格
    
    private String address; // 地址
    
    private String province; // 省份
    
    private String city; // 城市
    
    private String district; // 区域
    
    private Long provinceId; // 省份ID
    
    private Long cityId; // 城市ID
    
    private Long districtId; // 区域ID
    
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
    
    private String rejectReason; // 拒绝原因
    
    private String coverImage; // 封面图片
    
    private Long ownerId; // 房东ID
    
    private Long contractTemplateId; // 使用的合同模板ID
    
    private Integer minLeaseTerm; // 最短租期(月)
    
    private Integer depositMonths; // 押金月数
    
    private BigDecimal penaltyRate; // 违约金比例（百分比）
    
    private BigDecimal minPenalty; // 最低违约金金额
    
    private BigDecimal penaltyAmount; // 违约金
    
    private LocalDateTime createTime; // 创建时间
    
    private LocalDateTime updateTime; // 更新时间
    
    @TableLogic
    private Integer isDeleted; // 是否删除：0-未删除，1-已删除
    
    // 获取房东ID
    public Long getLandlordId() {
        return this.ownerId;
    }
} 