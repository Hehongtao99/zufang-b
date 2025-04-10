package com.zufang.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 房源搜索DTO
 */
@Data
public class HouseSearchDTO {
    
    private String keyword; // 关键词，可以搜索标题和描述
    
    private String province; // 省份
    
    private String city; // 城市
    
    private String district; // 区域
    
    private BigDecimal minPrice; // 最低价格
    
    private BigDecimal maxPrice; // 最高价格
    
    private Integer minArea; // 最小面积
    
    private Integer maxArea; // 最大面积
    
    private Integer bedroomCount; // 卧室数量
    
    private String houseType; // 房源类型：APARTMENT-公寓，HOUSE-别墅
    
    private String rentType; // 出租类型：WHOLE-整租，SHARED-合租
    
    private Boolean hasElevator; // 是否有电梯
    
    private Boolean hasParking; // 是否有停车位
    
    private String sortField; // 排序字段：price-价格，area-面积，createTime-发布时间
    
    private String sortOrder; // 排序方式：asc-升序，desc-降序
    
    private Integer pageNum = 1; // 页码，默认第1页
    
    private Integer pageSize = 10; // 每页数量，默认10条
    
    private Boolean priorityLoad; // 是否优先加载，用于首页数据加载优化
} 