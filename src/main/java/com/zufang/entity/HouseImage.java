package com.zufang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 房源图片实体类
 */
@Data
@TableName("house_image")
public class HouseImage implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id; // 图片ID
    
    private Long houseId; // 房源ID
    
    private String url; // 图片URL
    
    private Integer sort; // 排序
    
    @TableField(value = "is_cover") // 明确指定数据库列名
    private Integer isCover = 0; // 默认值为0
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 创建时间
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime; // 更新时间
    
    @TableLogic
    @TableField(value = "is_deleted") // 明确指定数据库列名
    private Integer isDeleted; // 是否删除：0-未删除，1-已删除
} 