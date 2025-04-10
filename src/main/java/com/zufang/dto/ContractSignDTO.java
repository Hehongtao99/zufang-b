package com.zufang.dto;

import lombok.Data;

/**
 * 合同签署DTO
 */
@Data
public class ContractSignDTO {
    
    /**
     * 合同ID
     */
    private Long contractId;
    
    /**
     * 乙方签名（Base64编码的签名图片）
     */
    private String signature;
} 