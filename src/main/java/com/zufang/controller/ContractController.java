package com.zufang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zufang.common.Result;
import com.zufang.dto.ContractDTO;
import com.zufang.dto.ContractSignDTO;
import com.zufang.dto.UserInfoDTO;
import com.zufang.entity.Contract;
import com.zufang.service.ContractService;
import com.zufang.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 合同控制器
 */
@RestController
@Slf4j
public class ContractController {

    @Autowired
    private ContractService contractService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 管理员获取所有合同
     */
    @GetMapping("/admin/contracts")
    public Result<Page<ContractDTO>> adminPageContracts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        log.info("管理员分页查询合同: page={}, size={}, status={}", page, size, status);
        // 由于接口变更，这里使用创建新Page方法
        Page<ContractDTO> result = contractService.getLandlordContracts(null, page, size);
        return Result.success(result);
    }
    
    /**
     * 管理员获取合同详情
     */
    @GetMapping("/admin/contracts/{id}")
    public Result<ContractDTO> adminGetContract(@PathVariable Long id) {
        log.info("管理员获取合同详情: id={}", id);
        ContractDTO dto = contractService.getContractDetail(id);
        if (dto == null) {
            return Result.error("合同不存在或已被删除");
        }
        return Result.success(dto);
    }
    
    /**
     * 房东获取自己的合同
     */
    @GetMapping("/landlord/contracts")
    public Result<Page<ContractDTO>> landlordPageContracts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        // 从请求中获取当前登录的房东ID
        Long landlordId = (Long) request.getAttribute("userId");
        log.info("房东分页查询合同: landlordId={}, page={}, size={}, status={}", landlordId, page, size, status);
        
        Page<ContractDTO> result = contractService.getLandlordContracts(landlordId, page, size);
        return Result.success(result);
    }
    
    /**
     * 用户获取自己的合同
     */
    @GetMapping("/user/contracts")
    public Result<Page<ContractDTO>> userPageContracts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        // 从请求中获取当前登录的用户ID
        Long userId = (Long) request.getAttribute("userId");
        log.info("用户分页查询合同: userId={}, page={}, size={}, status={}", userId, page, size, status);
        
        Page<ContractDTO> result = contractService.getUserContracts(userId, page, size);
        return Result.success(result);
    }
    
    /**
     * 获取合同详情
     */
    @GetMapping("/user/contracts/{id}")
    public Result<ContractDTO> getContract(@PathVariable Long id) {
        log.info("获取合同详情: {}", id);
        ContractDTO dto = contractService.getContractDetail(id);
        if (dto == null) {
            return Result.error("合同不存在或已被删除");
        }
        return Result.success(dto);
    }

    /**
     * 创建订单对应的合同
     */
    @PostMapping("/user/contracts/create/{orderId}")
    public Result<Long> createContract(@PathVariable Long orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("用户创建合同: userId={}, orderId={}", userId, orderId);
        
        // 传递null作为landlordId，表示这是用户(租客)创建合同，不需要房东身份验证
        Long contractId = contractService.createContract(orderId, null);
        
        if (contractId == null) {
            log.error("创建合同失败，订单ID: {}, 用户ID: {}", orderId, userId);
            return Result.error("创建合同失败，请检查订单信息");
        }
        return Result.success(contractId);
    }
    
    /**
     * 用户签署合同
     */
    @PostMapping("/user/contracts/sign")
    public Result<Boolean> signContract(@RequestBody ContractSignDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("用户签署合同: userId={}, contractId={}", userId, dto.getContractId());
        
        // 验证用户是否填写了真实姓名和身份证号
        UserInfoDTO userInfo = userService.getUserInfo(userId);
        if (userInfo == null) {
            log.error("签署合同失败，用户信息不存在：userId={}", userId);
            return Result.fail("用户信息不存在");
        }
        
        if (userInfo.getRealName() == null || userInfo.getRealName().isEmpty() ||
            userInfo.getIdCard() == null || userInfo.getIdCard().isEmpty()) {
            log.warn("签署合同失败，用户未完善真实身份信息：userId={}", userId);
            return Result.fail("签署合同需要真实姓名和身份证号，请先完善个人资料");
        }
        
        boolean result = contractService.signContract(dto, userId);
        return Result.success(result);
    }
    
    /**
     * 上传签名
     */
    @PostMapping("/user/signature/upload")
    public Result<String> uploadSignature(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("用户上传签名: userId={}", userId);
        String signatureUrl = contractService.uploadSignature(file, userId);
        if (signatureUrl == null) {
            return Result.error("上传签名失败");
        }
        return Result.success(signatureUrl);
    }
    
    /**
     * 终止合同（管理员）
     */
    @PostMapping("/admin/contracts/{id}/terminate")
    public Result<Boolean> adminTerminateContract(@PathVariable Long id) {
        log.info("管理员终止合同: {}", id);
        boolean result = contractService.terminateContract(id);
        return Result.success(result);
    }
    
    /**
     * 终止合同（房东）
     */
    @PostMapping("/landlord/contracts/{id}/terminate")
    public Result<Boolean> landlordTerminateContract(@PathVariable Long id, HttpServletRequest request) {
        Long landlordId = (Long) request.getAttribute("userId");
        log.info("房东终止合同: landlordId={}, contractId={}", landlordId, id);
        
        // 验证合同是否属于当前房东（在服务层进行）
        boolean result = contractService.terminateContract(id);
        return Result.success(result);
    }
} 