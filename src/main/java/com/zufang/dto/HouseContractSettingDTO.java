package com.zufang.dto;

import lombok.Data;

/**
 * 房源合同设置DTO
 */
@Data
public class HouseContractSettingDTO {
    
    /**
     * 房源ID
     */
    private Long houseId;
    
    /**
     * 合同模板ID
     */
    private Long contractTemplateId;
    
    /**
     * 最短租期(月)
     */
    private Integer minLeaseTerm;
    
    /**
     * 押金月数
     */
    private Integer depositMonths;
} 