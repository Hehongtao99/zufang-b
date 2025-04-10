package com.zufang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 区域实体类
 */
@Data
@TableName("region_district")
public class RegionDistrict {
    
    @TableId(type = IdType.AUTO)
    private Long id; // 区域ID
    
    private String name; // 区域名称
    
    private String code; // 区域代码
    
    private Long cityId; // 所属城市ID
    
    private Long provinceId; // 所属省份ID
    
    private LocalDateTime createTime; // 创建时间
    
    private LocalDateTime updateTime; // 更新时间
    
    @TableLogic
    private Integer isDeleted; // 是否删除：0-未删除，1-已删除
} 