package com.zufang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zufang.common.Result;
import com.zufang.dto.ContractTemplateDTO;
import com.zufang.entity.ContractTemplate;
import com.zufang.service.ContractTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 合同模板控制器
 */
@RestController
@RequestMapping("/admin/contract-templates")
@Slf4j
public class ContractTemplateController {

    @Autowired
    private ContractTemplateService contractTemplateService;
    
    /**
     * 创建合同模板
     */
    @PostMapping
    public Result<Long> createContractTemplate(@RequestBody ContractTemplateDTO dto) {
        log.info("创建合同模板: {}", dto.getName());
        Long id = contractTemplateService.createContractTemplate(dto);
        return Result.success(id);
    }
    
    /**
     * 更新合同模板
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateContractTemplate(@PathVariable Long id, @RequestBody ContractTemplateDTO dto) {
        log.info("更新合同模板: {}", id);
        boolean result = contractTemplateService.updateContractTemplate(id, dto);
        return Result.success(result);
    }
    
    /**
     * 删除合同模板
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteContractTemplate(@PathVariable Long id) {
        log.info("删除合同模板: {}", id);
        boolean result = contractTemplateService.deleteContractTemplate(id);
        return Result.success(result);
    }
    
    /**
     * 获取合同模板详情
     */
    @GetMapping("/{id}")
    public Result<ContractTemplateDTO> getContractTemplate(@PathVariable Long id) {
        log.info("获取合同模板详情: {}", id);
        ContractTemplateDTO dto = contractTemplateService.getContractTemplate(id);
        return Result.success(dto);
    }
    
    /**
     * 分页查询合同模板
     */
    @GetMapping
    public Result<Page<ContractTemplateDTO>> pageContractTemplates(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("分页查询合同模板: page={}, size={}", page, size);
        Page<ContractTemplate> pageParam = new Page<>(page, size);
        Page<ContractTemplateDTO> result = contractTemplateService.pageContractTemplates(pageParam);
        return Result.success(result);
    }
    
    /**
     * 获取所有合同模板（用于下拉选择）
     */
    @GetMapping("/list")
    public Result<Page<ContractTemplateDTO>> listAllTemplates() {
        log.info("获取所有合同模板列表");
        // 使用一个较大的页面大小来获取所有模板
        Page<ContractTemplate> pageParam = new Page<>(1, 100);
        Page<ContractTemplateDTO> result = contractTemplateService.pageContractTemplates(pageParam);
        return Result.success(result);
    }
} 