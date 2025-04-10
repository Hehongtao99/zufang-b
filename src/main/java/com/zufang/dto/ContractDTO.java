package com.zufang.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 合同DTO
 */
@Data
public class ContractDTO {
    
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
     * 房源标题
     */
    private String houseTitle;
    
    /**
     * 租客ID
     */
    private Long userId;
    
    /**
     * 租客姓名
     */
    private String userName;
    
    /**
     * 房东ID
     */
    private Long landlordId;
    
    /**
     * 房东姓名
     */
    private String landlordName;
    
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
    private LocalDateTime createTime;
    
    /**
     * 房源是否已删除
     */
    private Boolean houseDeleted;
} 