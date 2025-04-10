package com.zufang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zufang.common.Result;
import com.zufang.dto.ContractTemplateDTO;
import com.zufang.entity.ContractTemplate;
import com.zufang.service.ContractTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公共合同API控制器 - 提供给房东和租客使用
 */
@RestController
@RequestMapping("/contract-templates")
@Slf4j
public class PublicContractController {

    @Autowired
    private ContractTemplateService contractTemplateService;
    
    /**
     * 获取所有可用的合同模板
     * 供房东在发布房源时选择使用
     */
    @GetMapping("/all")
    public Result<List<ContractTemplateDTO>> getAllTemplates() {
        log.info("获取所有可用合同模板");
        try {
            // 使用一个较大的页面大小来获取所有模板
            Page<ContractTemplate> pageParam = new Page<>(1, 100);
            Page<ContractTemplateDTO> result = contractTemplateService.pageContractTemplates(pageParam);
            return Result.success(result.getRecords());
        } catch (Exception e) {
            log.error("获取合同模板失败", e);
            return Result.error("获取合同模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取单个合同模板详情
     */
    @GetMapping("/{id}")
    public Result<ContractTemplateDTO> getTemplateDetail(@PathVariable Long id) {
        log.info("获取合同模板详情: {}", id);
        try {
            ContractTemplateDTO dto = contractTemplateService.getContractTemplate(id);
            if (dto == null) {
                return Result.error("未找到指定合同模板");
            }
            return Result.success(dto);
        } catch (Exception e) {
            log.error("获取合同模板详情失败", e);
            return Result.error("获取合同模板详情失败: " + e.getMessage());
        }
    }
} 