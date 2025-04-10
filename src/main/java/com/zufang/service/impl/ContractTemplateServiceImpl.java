package com.zufang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zufang.dto.ContractTemplateDTO;
import com.zufang.entity.ContractTemplate;
import com.zufang.mapper.ContractTemplateMapper;
import com.zufang.service.ContractTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 合同模板服务实现类
 */
@Service
@Slf4j
public class ContractTemplateServiceImpl extends ServiceImpl<ContractTemplateMapper, ContractTemplate> implements ContractTemplateService {

    /**
     * 创建合同模板
     * @param dto 合同模板信息
     * @return 创建后的合同模板ID
     */
    @Override
    public Long createContractTemplate(ContractTemplateDTO dto) {
        ContractTemplate template = new ContractTemplate();
        BeanUtils.copyProperties(dto, template);
        save(template);
        return template.getId();
    }

    /**
     * 更新合同模板
     * @param id 合同模板ID
     * @param dto 合同模板信息
     * @return 是否更新成功
     */
    @Override
    public boolean updateContractTemplate(Long id, ContractTemplateDTO dto) {
        ContractTemplate template = getById(id);
        if (template == null) {
            log.warn("更新合同模板失败，模板ID {} 不存在", id);
            return false;
        }
        
        BeanUtils.copyProperties(dto, template);
        template.setId(id);
        return updateById(template);
    }

    /**
     * 删除合同模板
     * @param id 合同模板ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteContractTemplate(Long id) {
        return removeById(id);
    }

    /**
     * 获取合同模板详情
     * @param id 合同模板ID
     * @return 合同模板详情
     */
    @Override
    public ContractTemplateDTO getContractTemplate(Long id) {
        ContractTemplate template = getById(id);
        if (template == null) {
            return null;
        }
        
        ContractTemplateDTO dto = new ContractTemplateDTO();
        BeanUtils.copyProperties(template, dto);
        return dto;
    }

    /**
     * 分页查询合同模板
     * @param page 分页参数
     * @return 合同模板分页数据
     */
    @Override
    public Page<ContractTemplateDTO> pageContractTemplates(Page<ContractTemplate> page) {
        Page<ContractTemplate> templatePage = page(page, new QueryWrapper<ContractTemplate>().orderByDesc("create_time"));
        
        List<ContractTemplateDTO> records = templatePage.getRecords().stream().map(template -> {
            ContractTemplateDTO dto = new ContractTemplateDTO();
            BeanUtils.copyProperties(template, dto);
            return dto;
        }).collect(Collectors.toList());
        
        Page<ContractTemplateDTO> dtoPage = new Page<>();
        dtoPage.setRecords(records);
        dtoPage.setCurrent(templatePage.getCurrent());
        dtoPage.setSize(templatePage.getSize());
        dtoPage.setTotal(templatePage.getTotal());
        dtoPage.setPages(templatePage.getPages());
        
        return dtoPage;
    }
} 