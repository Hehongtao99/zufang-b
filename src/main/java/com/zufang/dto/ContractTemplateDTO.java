package com.zufang.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 合同模板DTO
 */
@Data
public class ContractTemplateDTO {
    
    private Long id;
    
    /**
     * 模板名称
     */
    private String name;
    
    /**
     * 合同正文内容
     */
    private String content;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 