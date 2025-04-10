package com.zufang.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zufang.dto.ContractTemplateDTO;
import com.zufang.entity.ContractTemplate;

/**
 * 合同模板服务接口
 */
public interface ContractTemplateService extends IService<ContractTemplate> {
    
    /**
     * 创建合同模板
     * @param dto 合同模板信息
     * @return 创建后的合同模板ID
     */
    Long createContractTemplate(ContractTemplateDTO dto);
    
    /**
     * 更新合同模板
     * @param id 合同模板ID
     * @param dto 合同模板信息
     * @return 是否更新成功
     */
    boolean updateContractTemplate(Long id, ContractTemplateDTO dto);
    
    /**
     * 删除合同模板
     * @param id 合同模板ID
     * @return 是否删除成功
     */
    boolean deleteContractTemplate(Long id);
    
    /**
     * 获取合同模板详情
     * @param id 合同模板ID
     * @return 合同模板详情
     */
    ContractTemplateDTO getContractTemplate(Long id);
    
    /**
     * 分页查询合同模板
     * @param page 分页参数
     * @return 合同模板分页数据
     */
    Page<ContractTemplateDTO> pageContractTemplates(Page<ContractTemplate> page);
} 