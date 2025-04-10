package com.zufang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 城市实体类
 */
@Data
@TableName("region_city")
public class RegionCity {
    
    @TableId(type = IdType.AUTO)
    private Long id; // 城市ID
    
    private String name; // 城市名称
    
    private String code; // 城市代码
    
    private Long provinceId; // 所属省份ID
    
    private LocalDateTime createTime; // 创建时间
    
    private LocalDateTime updateTime; // 更新时间
    
    @TableLogic
    private Integer isDeleted; // 是否删除：0-未删除，1-已删除
} 