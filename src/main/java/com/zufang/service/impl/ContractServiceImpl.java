package com.zufang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zufang.dto.ContractDTO;
import com.zufang.dto.ContractSignDTO;
import com.zufang.dto.ContractTemplateDTO;
import com.zufang.dto.HouseInfoDTO;
import com.zufang.dto.UserInfoDTO;
import com.zufang.entity.Contract;
import com.zufang.entity.ContractTemplate;
import com.zufang.entity.Order;
import com.zufang.event.OrderPaidEvent;
import com.zufang.mapper.ContractMapper;
import com.zufang.mapper.OrderMapper;
import com.zufang.service.ContractService;
import com.zufang.service.ContractTemplateService;
import com.zufang.service.HouseService;
import com.zufang.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

/**
 * 合同服务实现类
 */
@Service
@Slf4j
public class ContractServiceImpl extends ServiceImpl<ContractMapper, Contract> implements ContractService {

    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private HouseService houseService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ContractTemplateService contractTemplateService;
    
    /**
     * 监听订单支付成功事件，创建合同
     * @param event 订单支付成功事件
     */
    @EventListener
    @Transactional
    public void handleOrderPaidEvent(OrderPaidEvent event) {
        log.info("接收到订单支付成功事件，订单ID：{}", event.getOrderId());
        createContract(event.getOrderId(), null); // 使用null作为房东ID，因为这是系统自动创建
    }
    
    /**
     * 创建合同
     * @param orderId 订单ID
     * @param landlordId 房东ID
     * @return 合同ID
     */
    @Override
    @Transactional
    public Long createContract(Long orderId, Long landlordId) {
        try {
            log.info("开始创建合同: orderId={}, landlordId={}", orderId, landlordId);
            
            // 查询订单
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                log.error("创建合同失败，订单不存在：{}", orderId);
                return null;
            }
            
            log.info("查询到订单信息: orderId={}, 状态={}, 用户ID={}, 房东ID={}", 
                    orderId, order.getStatus(), order.getUserId(), order.getLandlordId());
            
            // 验证房东身份（如果提供了房东ID）
            if (landlordId != null && !order.getLandlordId().equals(landlordId)) {
                log.error("创建合同失败，房东身份验证不通过：订单房东={}, 请求房东={}", order.getLandlordId(), landlordId);
                return null;
            }
            
            // 查询房源
            HouseInfoDTO houseInfo = houseService.getHouseInfo(order.getHouseId());
            if (houseInfo == null) {
                log.error("创建合同失败，房源不存在：{}", order.getHouseId());
                return null;
            }
            
            log.info("查询到房源信息: houseId={}, 房源状态={}", houseInfo.getId(), houseInfo.getStatus());
            
            // 查询合同模板，如果房源未设置模板，则使用默认模板（ID为1）
            Long templateId = houseInfo.getContractTemplateId();
            if (templateId == null) {
                log.info("房源未设置合同模板，使用默认模板ID=1：房源ID={}", houseInfo.getId());
                templateId = 1L; // 使用默认模板
            }
            
            // 检查是否已存在合同
            LambdaQueryWrapper<Contract> existCheckWrapper = new LambdaQueryWrapper<>();
            existCheckWrapper.eq(Contract::getOrderId, orderId);
            Contract existingContract = getOne(existCheckWrapper, false);
            if (existingContract != null) {
                log.info("该订单已存在合同，直接返回: orderId={}, contractId={}", orderId, existingContract.getId());
                return existingContract.getId();
            }
            
            // 创建合同
            Contract contract = new Contract();
            contract.setOrderId(orderId);
            contract.setHouseId(order.getHouseId());
            contract.setUserId(order.getUserId());
            contract.setLandlordId(order.getLandlordId());
            contract.setContractNo(generateContractNo());
            contract.setStartDate(order.getStartDate());
            contract.setEndDate(order.getEndDate());
            contract.setStatus("PENDING"); // 初始状态为待签署
            contract.setContractTemplateId(templateId);
            
            // 生成甲方(房东)签名
            try {
                // 这里可以使用默认的签名图片，或者生成包含房东名字的简单签名
                UserInfoDTO landlord = userService.getUserInfo(contract.getLandlordId());
                String landlordName = landlord != null ? 
                    (landlord.getRealName() != null ? landlord.getRealName() : landlord.getNickname()) : "房东";
                
                log.info("获取房东信息成功: landlordId={}, name={}", contract.getLandlordId(), landlordName);
                
                String partyASignature = generateLandlordSignature(landlordName);
                contract.setPartyASignature(partyASignature);
            } catch (Exception e) {
                log.error("生成房东签名失败，但继续创建合同: {}", e.getMessage(), e);
                // 使用默认签名或空签名继续
                contract.setPartyASignature("data:image/svg+xml;utf8,<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"200\" height=\"50\"><text x=\"10\" y=\"30\" font-family=\"Arial\" font-size=\"20\" fill=\"blue\">房东（已预签）</text></svg>");
            }
            
            // 填充合同内容
            try {
                String filledContent = fillContractContent(contract);
                contract.setFilledContent(filledContent);
            } catch (Exception e) {
                log.error("填充合同内容失败: {}", e.getMessage(), e);
                // 设置一个基本的内容，确保能创建合同
                contract.setFilledContent("合同内容生成失败，请联系管理员");
            }
            
            // 设置创建时间和更新时间
            LocalDateTime now = LocalDateTime.now();
            contract.setCreateTime(now);
            contract.setUpdateTime(now);
            
            log.info("准备保存合同: contractNo={}, 订单ID={}", contract.getContractNo(), contract.getOrderId());
            save(contract);
            log.info("合同创建成功: contractId={}", contract.getId());
            
            return contract.getId();
        } catch (Exception e) {
            log.error("创建合同过程中发生异常: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 生成房东签名
     * @param landlordName 房东名称
     * @return 签名Base64字符串
     */
    private String generateLandlordSignature(String landlordName) {
        // 使用简短的签名字符串（一个简单的SVG签名，避免长Base64字符串）
        // 创建一个简单的SVG格式签名，包含房东名字
        String svgSignature = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"200\" height=\"50\">"
                + "<text x=\"10\" y=\"30\" font-family=\"Arial\" font-size=\"20\" fill=\"blue\">"
                + landlordName + "（已预签）</text></svg>";
        
        // 将SVG转换为data URI格式
        return "data:image/svg+xml;utf8," + svgSignature;
    }
    
    /**
     * 签署合同
     * @param dto 合同签署DTO
     * @param userId 用户ID
     * @return 是否签署成功
     */
    @Override
    @Transactional
    public boolean signContract(ContractSignDTO dto, Long userId) {
        Contract contract = getById(dto.getContractId());
        if (contract == null) {
            log.error("签署合同失败，合同不存在：{}", dto.getContractId());
            return false;
        }
        
        // 验证用户身份
        if (!contract.getUserId().equals(userId)) {
            log.error("签署合同失败，非法操作：合同用户={}, 请求用户={}", contract.getUserId(), userId);
            return false;
        }
        
        if (!"PENDING".equals(contract.getStatus())) {
            log.error("签署合同失败，合同状态不是待签署：{}", contract.getStatus());
            return false;
        }
        
        // 处理乙方签名，确保不超过数据库字段长度限制
        String signature = processSignature(dto.getSignature());
        
        // 设置乙方签名
        contract.setPartyBSignature(signature);
        // 更新合同状态为已签署
        contract.setStatus("SIGNED");
        // 设置当前日期为签署日期
        contract.setSignDate(LocalDateTime.now());
        
        return updateById(contract);
    }
    
    /**
     * 处理签名数据，确保不超过数据库字段长度
     * @param originalSignature 原始签名数据
     * @return 处理后的签名数据
     */
    private String processSignature(String originalSignature) {
        if (originalSignature == null) {
            return null;
        }
        
        // 检查签名数据长度
        if (originalSignature.length() <= 255) {
            return originalSignature;
        }
        
        log.info("签名数据过长({}字符)，进行处理以适应数据库字段长度限制", originalSignature.length());
        
        // 创建一个简单的SVG格式签名，表示用户已签名
        String svgSignature = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"200\" height=\"50\">"
                + "<text x=\"10\" y=\"30\" font-family=\"Arial\" font-size=\"20\" fill=\"green\">租客已签名</text></svg>";
        
        // 将SVG转换为data URI格式
        return "data:image/svg+xml;utf8," + svgSignature;
    }
    
    /**
     * 获取合同详情
     * @param id 合同ID
     * @return 合同详情
     */
    @Override
    public ContractDTO getContractDetail(Long id) {
        Contract contract = getById(id);
        if (contract == null) {
            return null;
        }
        
        return convertToDTO(contract);
    }
    
    /**
     * 获取租客的合同列表
     * @param userId 租客ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 合同分页数据
     */
    @Override
    public Page<ContractDTO> getUserContracts(Long userId, Integer pageNum, Integer pageSize) {
        Page<Contract> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Contract> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Contract::getUserId, userId)
               .eq(Contract::getIsDeleted, 0)
               .orderByDesc(Contract::getCreateTime);
        
        Page<Contract> contractPage = page(page, queryWrapper);
        return convertToContractDTOPage(contractPage);
    }
    
    /**
     * 获取房东的合同列表
     * @param landlordId 房东ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 合同分页数据
     */
    @Override
    public Page<ContractDTO> getLandlordContracts(Long landlordId, Integer pageNum, Integer pageSize) {
        Page<Contract> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Contract> queryWrapper = new LambdaQueryWrapper<>();
        
        // 仅当landlordId不为null时（即房东查询而非管理员查询），才添加landlordId条件
        if (landlordId != null) {
            queryWrapper.eq(Contract::getLandlordId, landlordId);
        }
        
        // 只查询未删除的合同
        queryWrapper.eq(Contract::getIsDeleted, 0)
               .orderByDesc(Contract::getCreateTime);
        
        Page<Contract> contractPage = page(page, queryWrapper);
        return convertToContractDTOPage(contractPage);
    }
    
    /**
     * 上传签名
     * @param file 签名图片
     * @param userId 用户ID
     * @return 签名URL
     */
    @Override
    public String uploadSignature(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            log.error("上传签名失败，文件为空");
            return null;
        }
        
        try {
            // 这里可以实现实际的文件上传逻辑，例如保存到本地或云存储
            // 为简化示例，这里生成一个简单的SVG签名
            
            UserInfoDTO user = userService.getUserInfo(userId);
            String userName = user != null ? 
                (user.getRealName() != null ? user.getRealName() : user.getNickname()) : "用户";
            
            // 创建一个简单的SVG格式签名
            String svgSignature = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"200\" height=\"50\">"
                    + "<text x=\"10\" y=\"30\" font-family=\"Arial\" font-size=\"20\" fill=\"green\">"
                    + userName + "（已签名）</text></svg>";
            
            // 将SVG转换为data URI格式
            return "data:image/svg+xml;utf8," + svgSignature;
        } catch (Exception e) {
            log.error("上传签名失败：{}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 终止合同
     * @param id 合同ID
     * @return 是否终止成功
     */
    @Override
    @Transactional
    public boolean terminateContract(Long id) {
        Contract contract = getById(id);
        if (contract == null) {
            log.error("终止合同失败，合同不存在：{}", id);
            return false;
        }
        
        if (!"SIGNED".equals(contract.getStatus())) {
            log.error("终止合同失败，合同状态不是已签署：{}", contract.getStatus());
            return false;
        }
        
        contract.setStatus("TERMINATED");
        contract.setUpdateTime(LocalDateTime.now());
        return updateById(contract);
    }
    
    /**
     * 填充合同内容
     * @param contract 合同对象
     * @return 填充后的合同内容
     */
    private String fillContractContent(Contract contract) {
        try {
            // 获取合同模板
            ContractTemplate template = contractTemplateService.getById(contract.getContractTemplateId());
            if (template == null) {
                log.error("填充合同内容失败，合同模板不存在：templateId={}", contract.getContractTemplateId());
                return "无法获取合同模板";
            }
            
            // 获取房源信息
            HouseInfoDTO house = houseService.getHouseInfo(contract.getHouseId());
            if (house == null) {
                log.error("填充合同内容失败，房源不存在：houseId={}", contract.getHouseId());
                return "无法获取房源信息";
            }
            
            // 获取房东信息
            UserInfoDTO landlord = userService.getUserInfo(contract.getLandlordId());
            if (landlord == null) {
                log.error("填充合同内容失败，房东不存在：landlordId={}", contract.getLandlordId());
                return "无法获取房东信息";
            }
            
            // 获取租客信息
            UserInfoDTO tenant = userService.getUserInfo(contract.getUserId());
            if (tenant == null) {
                log.error("填充合同内容失败，租客不存在：userId={}", contract.getUserId());
                return "无法获取租客信息";
            }
            
            log.info("填充合同内容，房源信息：{}, 房东信息：{}, 租客信息：{}", house, landlord, tenant);
            
            // 获取姓名
            String landlordName = landlord.getRealName() != null ? landlord.getRealName() : landlord.getNickname();
            String tenantName = tenant.getRealName() != null ? tenant.getRealName() : tenant.getNickname();
            
            // 填充变量
            String content = template.getContent();
            
            // 检查并处理HTML内容
            // 如果内容包含HTML标签但不是完整的HTML文档，则进行适当处理
            boolean hasHtmlTags = content.contains("<") && content.contains(">");
            
            if (hasHtmlTags) {
                // 计算违约金
                String penaltyAmount = "1000.00";
                if (house.getPenaltyAmount() != null) {
                    penaltyAmount = house.getPenaltyAmount().toString();
                } 
                
                // 获取当前日期，用于签订日期
                LocalDate today = LocalDate.now();
                int year = today.getYear();
                int month = today.getMonthValue();
                int day = today.getDayOfMonth();
                
                // 先创建替换值的映射
                Map<String, String> replaceValues = new HashMap<>();
                replaceValues.put("contractNo", contract.getContractNo());
                replaceValues.put("signDate", contract.getCreateTime() != null ? contract.getCreateTime().toLocalDate().toString() : LocalDate.now().toString());
                replaceValues.put("year", String.valueOf(year));
                replaceValues.put("month", String.valueOf(month));
                replaceValues.put("day", String.valueOf(day));
                replaceValues.put("houseTitle", house.getTitle());
                replaceValues.put("houseAddress", house.getAddress());
                replaceValues.put("houseArea", String.valueOf(house.getArea()));
                replaceValues.put("startDate", contract.getStartDate().toString());
                replaceValues.put("endDate", contract.getEndDate().toString());
                replaceValues.put("rentAmount", house.getPrice().toString());
                replaceValues.put("monthlyRent", house.getPrice().toString());
                replaceValues.put("depositAmount", house.getPrice().multiply(new BigDecimal(house.getDepositMonths())).toString());
                replaceValues.put("landlordName", landlordName);
                replaceValues.put("landlordIdCard", landlord.getIdCard() != null ? landlord.getIdCard() : "");
                replaceValues.put("landlordPhone", landlord.getPhone() != null ? landlord.getPhone() : "");
                replaceValues.put("userName", tenantName);
                replaceValues.put("tenantName", tenantName);
                replaceValues.put("userIdCard", tenant.getIdCard() != null ? tenant.getIdCard() : "");
                replaceValues.put("tenantIdCard", tenant.getIdCard() != null ? tenant.getIdCard() : "");
                replaceValues.put("userPhone", tenant.getPhone() != null ? tenant.getPhone() : "");
                replaceValues.put("tenantPhone", tenant.getPhone() != null ? tenant.getPhone() : "");
                replaceValues.put("penaltyAmount", penaltyAmount);
                
                // 替换所有${变量}格式的占位符
                for (Map.Entry<String, String> entry : replaceValues.entrySet()) {
                    content = content.replace("${" + entry.getKey() + "}", entry.getValue());
                }
                
                // 替换所有{{变量}}格式的占位符
                for (Map.Entry<String, String> entry : replaceValues.entrySet()) {
                    content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
                }
                
                // 处理签订日期格式：_______年_______月_______日
                content = content.replaceAll("_+年", year + "年");
                content = content.replaceAll("_+月", month + "月");
                content = content.replaceAll("_+日", day + "日");
                
                // 处理甲方乙方签字
                content = content.replaceAll("甲方\\s*（签字）\\s*：\\s*_+", "甲方（签字）：" + landlordName);
                content = content.replaceAll("乙方\\s*（签字）\\s*：\\s*_+", "乙方（签字）：" + tenantName);
                
                // 使用正则表达式查找并替换任何剩余的${...}和{{...}}格式的变量
                content = content.replaceAll("\\$\\{[^}]+\\}", "");
                content = content.replaceAll("\\{\\{[^}]+\\}\\}", "");
            } else {
                // 如果是纯文本，转换为HTML格式，保持原来的逻辑
                LocalDate today = LocalDate.now();
                int year = today.getYear();
                int month = today.getMonthValue();
                int day = today.getDayOfMonth();
                
                content = "<div class=\"contract-content\">" +
                          "<h1 style=\"text-align: center;\">房屋租赁合同</h1>" +
                          "<p style=\"text-align: right;\">合同编号：" + contract.getContractNo() + "</p>" +
                          "<p style=\"text-align: right;\">签订日期：" + year + "年" + month + "月" + day + "日</p>" +
                          "<p>甲方（出租方）：" + landlordName + "</p>" +
                          "<p>身份证号：" + (landlord.getIdCard() != null ? landlord.getIdCard() : "") + "</p>" +
                          "<p>联系电话：" + (landlord.getPhone() != null ? landlord.getPhone() : "") + "</p>" +
                          "<p>乙方（承租方）：" + tenantName + "</p>" +
                          "<p>身份证号：" + (tenant.getIdCard() != null ? tenant.getIdCard() : "") + "</p>" +
                          "<p>联系电话：" + (tenant.getPhone() != null ? tenant.getPhone() : "") + "</p>" +
                          "<p>甲乙双方就房屋租赁事宜，达成如下协议：</p>" +
                          "<p>一、甲方将位于" + house.getAddress() + "，建筑面积" + house.getArea() + "平方米的房屋出租给乙方使用。</p>" +
                          "<p>二、租赁期限自" + contract.getStartDate().toString() + "至" + contract.getEndDate().toString() + "，共计" + ChronoUnit.MONTHS.between(contract.getStartDate(), contract.getEndDate()) + "个月。</p>" +
                          "<p>三、租金为每月" + house.getPrice() + "元。</p>" +
                          "<p>四、违约金：" + (house.getPenaltyAmount() != null ? house.getPenaltyAmount().toString() : "1000.00") + "元。</p>" +
                          "<p>九、甲方（签字）：" + landlordName + " 乙方（签字）：" + tenantName + "</p>" +
                          "<p>签订日期：" + year + "年" + month + "月" + day + "日</p>" +
                          "</div>";
            }
            
            return content;
        } catch (Exception e) {
            log.error("填充合同内容异常：", e);
            return "<p>生成合同内容时发生错误：" + e.getMessage() + "</p>";
        }
    }
    
    /**
     * 生成合同编号
     * @return 合同编号
     */
    private String generateContractNo() {
        return "CTR" + DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now()) 
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * 转换合同实体为DTO
     * @param contract 合同实体
     * @return 合同DTO
     */
    private ContractDTO convertToDTO(Contract contract) {
        ContractDTO dto = new ContractDTO();
        BeanUtils.copyProperties(contract, dto);
        
        // 设置关联信息
        HouseInfoDTO houseInfo = houseService.getHouseInfo(contract.getHouseId());
        if (houseInfo != null) {
            dto.setHouseTitle(houseInfo.getTitle());
            // 设置房源删除状态
            dto.setHouseDeleted(houseInfo.getIsDeleted() != null && houseInfo.getIsDeleted());
        } else {
            // 如果房源不存在，也标记为已删除
            dto.setHouseDeleted(true);
        }
        
        UserInfoDTO landlord = userService.getUserInfo(contract.getLandlordId());
        if (landlord != null) {
            dto.setLandlordName(landlord.getRealName() != null ? landlord.getRealName() : landlord.getNickname());
        }
        
        UserInfoDTO user = userService.getUserInfo(contract.getUserId());
        if (user != null) {
            dto.setUserName(user.getRealName() != null ? user.getRealName() : user.getNickname());
        }
        
        return dto;
    }
    
    /**
     * 转换合同分页数据为DTO分页数据
     * @param contractPage 合同分页数据
     * @return DTO分页数据
     */
    private Page<ContractDTO> convertToContractDTOPage(Page<Contract> contractPage) {
        List<ContractDTO> records = contractPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        Page<ContractDTO> dtoPage = new Page<>();
        dtoPage.setRecords(records);
        dtoPage.setCurrent(contractPage.getCurrent());
        dtoPage.setSize(contractPage.getSize());
        dtoPage.setTotal(contractPage.getTotal());
        dtoPage.setPages(contractPage.getPages());
        
        return dtoPage;
    }
} 