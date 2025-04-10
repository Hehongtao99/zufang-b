package com.zufang.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zufang.dto.ContractDTO;
import com.zufang.dto.ContractSignDTO;
import com.zufang.entity.Contract;
import org.springframework.web.multipart.MultipartFile;

/**
 * 合同服务接口
 */
public interface ContractService extends IService<Contract> {
    
    /**
     * 创建合同
     * @param orderId 订单ID
     * @param landlordId 房东ID
     * @return 合同ID
     */
    Long createContract(Long orderId, Long landlordId);
    
    /**
     * 签署合同
     * @param dto 合同签署DTO
     * @param userId 用户ID
     * @return 是否签署成功
     */
    boolean signContract(ContractSignDTO dto, Long userId);
    
    /**
     * 获取合同详情
     * @param id 合同ID
     * @return 合同详情
     */
    ContractDTO getContractDetail(Long id);
    
    /**
     * 获取租客的合同列表
     * @param userId 租客ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 合同分页数据
     */
    Page<ContractDTO> getUserContracts(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取房东的合同列表
     * @param landlordId 房东ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 合同分页数据
     */
    Page<ContractDTO> getLandlordContracts(Long landlordId, Integer pageNum, Integer pageSize);
    
    /**
     * 上传签名
     * @param file 签名图片
     * @param userId 用户ID
     * @return 签名URL
     */
    String uploadSignature(MultipartFile file, Long userId);
    
    /**
     * 终止合同
     * @param id 合同ID
     * @return 是否终止成功
     */
    boolean terminateContract(Long id);
} 