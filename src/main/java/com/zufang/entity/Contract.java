package com.zufang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 合同实体类
 */
@Data
@TableName("contract")
public class Contract {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 房源ID
     */
    private Long houseId;

    /**
     * 租客ID
     */
    private Long userId;

    /**
     * 房东ID
     */
    private Long landlordId;

    /**
     * 合同编号
     */
    private String contractNo;

    /**
     * 合同文件URL
     */
    private String contractUrl;

    /**
     * 租期开始日期
     */
    private LocalDate startDate;

    /**
     * 租期结束日期
     */
    private LocalDate endDate;

    /**
     * 状态：PENDING-待签署，SIGNED-已签署，TERMINATED-已终止
     */
    private String status;

    /**
     * 违约金
     */
    private BigDecimal penaltyAmount;

    /**
     * 合同模板ID
     */
    private Long contractTemplateId;

    /**
     * 填充后的合同内容
     */
    private String filledContent;

    /**
     * 甲方签名
     */
    private String partyASignature;

    /**
     * 乙方签名
     */
    private String partyBSignature;

    /**
     * 签署日期
     */
    private LocalDateTime signDate;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    private Boolean isDeleted;
} 