package com.zufang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zufang.common.exception.BusinessException;
import com.zufang.common.FileUtil;
import com.zufang.dto.HouseApproveDTO;
import com.zufang.dto.HouseContractSettingDTO;
import com.zufang.dto.HouseInfoDTO;
import com.zufang.dto.HousePublishDTO;
import com.zufang.dto.HouseSearchDTO;
import com.zufang.dto.HouseEditDTO;
import com.zufang.entity.House;
import com.zufang.entity.HouseImage;
import com.zufang.entity.User;
import com.zufang.entity.RegionCity;
import com.zufang.entity.RegionDistrict;
import com.zufang.entity.RegionProvince;
import com.zufang.mapper.HouseImageMapper;
import com.zufang.mapper.HouseMapper;
import com.zufang.mapper.UserMapper;
import com.zufang.mapper.RegionCityMapper;
import com.zufang.mapper.RegionDistrictMapper;
import com.zufang.mapper.RegionProvinceMapper;
import com.zufang.service.HouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Map;

/**
 * 房源服务实现类
 */
@Slf4j
@Service
public class HouseServiceImpl extends ServiceImpl<HouseMapper, House> implements HouseService {

    @Autowired
    private HouseImageMapper houseImageMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private FileUtil fileUtil;
    
    @Autowired
    private RegionCityMapper regionCityMapper;
    
    @Autowired
    private RegionDistrictMapper regionDistrictMapper;
    
    @Autowired
    private RegionProvinceMapper regionProvinceMapper;
    
    /**
     * 上传图片到本地静态资源文件夹
     * @param file 图片文件
     * @return 图片URL
     */
    private String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return fileUtil.uploadHouseImage(file);
    }
    
    @Override
    @Transactional
    public Long publishHouse(HousePublishDTO publishDTO, Long userId) {
        log.info("发布房源, 用户ID: {}", userId);
        
        // 1. 校验用户是否为房东
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!"LANDLORD".equals(user.getRole()) && !"ADMIN".equals(user.getRole())) {
            throw new BusinessException("只有房东才能发布房源");
        }
        
        // 2. 上传封面图片
        String coverImageUrl = publishDTO.getCoverImage();
        if (publishDTO.getCoverImageFile() != null && !publishDTO.getCoverImageFile().isEmpty()) {
            coverImageUrl = uploadImage(publishDTO.getCoverImageFile());
        }
        if (!StringUtils.hasText(coverImageUrl)) {
            throw new BusinessException("封面图片不能为空");
        }
        
        // 3. 创建房源
        House house = new House();
        BeanUtils.copyProperties(publishDTO, house);
        
        // 检查从 DTO 复制过来的 city 值
        log.info("从 DTO 复制的 City: {}", house.getCity());
        log.info("从 DTO 复制的 City ID: {}", house.getCityId());
        
        // 确保 city 不为空，如果为空则抛出异常或记录错误
        if (!StringUtils.hasText(house.getCity())) {
             log.error("City field is empty after copying from DTO. DTO city: {}, DTO cityId: {}", publishDTO.getCity(), publishDTO.getCityId());
             throw new BusinessException("城市信息不能为空，请检查前端提交的数据");
        }
        
        // 根据provinceId设置省份名称
        if (house.getProvinceId() != null) {
            RegionProvince provinceEntity = regionProvinceMapper.selectById(house.getProvinceId());
            if (provinceEntity != null) {
                house.setProvince(provinceEntity.getName());
            }
        }
        
        // 修正区域名称（使用districtId获取区域名称）
        if (house.getDistrictId() != null) {
            RegionDistrict districtEntity = regionDistrictMapper.selectById(house.getDistrictId());
            if (districtEntity != null) {
                house.setDistrict(districtEntity.getName());
            }
        }
        
        house.setCoverImage(coverImageUrl);
        house.setOwnerId(userId);
        house.setStatus("PENDING"); // 待审核
        house.setCreateTime(LocalDateTime.now());
        house.setUpdateTime(LocalDateTime.now());
        house.setIsDeleted(0);
        
        log.info("准备保存的House实体 City: {}", house.getCity());

        // 4. 保存房源
        boolean saveResult = this.save(house);
        if (!saveResult) {
            throw new BusinessException("发布房源失败");
        }
        
        // 5. 上传并保存房源图片
        List<HouseImage> houseImages = new ArrayList<>();
        
        // 5.1 处理图片URL列表
        if (publishDTO.getImageUrls() != null && !publishDTO.getImageUrls().isEmpty()) {
            int sort = 0;
            for (String imageUrl : publishDTO.getImageUrls()) {
                if (StringUtils.hasText(imageUrl)) {
                    HouseImage houseImage = new HouseImage();
                    houseImage.setHouseId(house.getId());
                    houseImage.setUrl(imageUrl);
                    houseImage.setSort(sort++);
                    houseImage.setCreateTime(LocalDateTime.now());
                    houseImage.setUpdateTime(LocalDateTime.now());
                    houseImage.setIsDeleted(0);
                    houseImage.setIsCover(0);
                    houseImages.add(houseImage);
                }
            }
        }
        
        // 5.2 处理图片文件列表
        if (publishDTO.getImageFiles() != null && !publishDTO.getImageFiles().isEmpty()) {
            int sort = houseImages.size();
            for (MultipartFile imageFile : publishDTO.getImageFiles()) {
                if (imageFile != null && !imageFile.isEmpty()) {
                    String imageUrl = uploadImage(imageFile);
                    if (StringUtils.hasText(imageUrl)) {
                        HouseImage houseImage = new HouseImage();
                        houseImage.setHouseId(house.getId());
                        houseImage.setUrl(imageUrl);
                        houseImage.setSort(sort++);
                        houseImage.setCreateTime(LocalDateTime.now());
                        houseImage.setUpdateTime(LocalDateTime.now());
                        houseImage.setIsDeleted(0);
                        houseImage.setIsCover(0);
                        houseImages.add(houseImage);
                    }
                }
            }
        }
        
        // 5.3 保存房源图片
        if (!houseImages.isEmpty()) {
            for (HouseImage houseImage : houseImages) {
                houseImageMapper.insert(houseImage);
            }
        }
        
        log.info("发布房源成功, 房源ID: {}", house.getId());
        return house.getId();
    }
    
    @Override
    @Transactional
    public void approveHouse(HouseApproveDTO approveDTO) {
        log.info("审核房源, 房源ID: {}, 状态: {}", approveDTO.getHouseId(), approveDTO.getStatus());
        
        // 1. 查询房源
        House house = this.getById(approveDTO.getHouseId());
        if (house == null || house.getIsDeleted() == 1) {
            throw new BusinessException("房源不存在");
        }
        
        // 2. 校验房源状态
        if (!"PENDING".equals(house.getStatus())) {
            throw new BusinessException("只能审核待审核状态的房源");
        }
        
        // 3. 校验审核状态
        if (!"APPROVED".equals(approveDTO.getStatus()) && !"REJECTED".equals(approveDTO.getStatus())) {
            throw new BusinessException("审核状态不正确");
        }
        
        // 4. 如果拒绝，需要提供拒绝原因
        if ("REJECTED".equals(approveDTO.getStatus()) && !StringUtils.hasText(approveDTO.getRejectReason())) {
            throw new BusinessException("拒绝时需要提供拒绝原因");
        }
        
        // 5. 更新房源状态
        house.setStatus(approveDTO.getStatus());
        house.setUpdateTime(LocalDateTime.now());
        
        boolean updateResult = this.updateById(house);
        if (!updateResult) {
            throw new BusinessException("审核房源失败");
        }
        
        log.info("审核房源成功, 房源ID: {}, 状态: {}", approveDTO.getHouseId(), approveDTO.getStatus());
    }
    
    @Override
    @Transactional
    public boolean approveHouse(Long houseId, boolean approved, String reason) {
        log.info("审核房源(新接口), 房源ID: {}, 是否通过: {}", houseId, approved);
        
        try {
            // 1. 查询房源
            House house = this.getById(houseId);
            if (house == null || house.getIsDeleted() == 1) {
                log.warn("房源不存在或已删除, 房源ID: {}", houseId);
                return false;
            }
            
            // 2. 校验房源状态 - 同时支持字符串状态和数字状态
            String status = house.getStatus();
            if (!isPendingStatus(status)) {
                log.warn("只能审核待审核状态的房源, 当前状态: {}", status);
                return false;
            }
            
            // 3. 如果拒绝，需要提供拒绝原因
            if (!approved && !StringUtils.hasText(reason)) {
                log.warn("拒绝时需要提供拒绝原因");
                return false;
            }
            
            // 4. 更新房源状态
            // 根据前端传入状态类型(数字/字符串)判断使用哪种状态格式
            String newStatus;
            if (approved) {
                // 如果原状态是数字格式，则使用数字状态
                newStatus = isNumericStatus(status) ? "1" : "APPROVED";
            } else {
                // 如果原状态是数字格式，则使用数字状态
                newStatus = isNumericStatus(status) ? "2" : "REJECTED";
                house.setRejectReason(reason);
            }
            
            house.setStatus(newStatus);
            house.setUpdateTime(LocalDateTime.now());
            
            boolean updateResult = this.updateById(house);
            if (!updateResult) {
                log.error("审核房源失败, 更新数据库失败");
                return false;
            }
            
            log.info("审核房源成功, 房源ID: {}, 审核结果: {}", houseId, house.getStatus());
            return true;
        } catch (Exception e) {
            log.error("审核房源异常, 房源ID: {}", houseId, e);
            return false;
        }
    }
    
    /**
     * 判断状态是否为数字格式
     * @param status 状态字符串
     * @return 是否为数字格式
     */
    private boolean isNumericStatus(String status) {
        if (status == null) {
            return false;
        }
        try {
            Integer.parseInt(status);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 判断状态是否为已审核(APPROVED)状态
     */
    private boolean isApprovedStatus(String status) {
        return "APPROVED".equals(status) || "1".equals(status) || "3".equals(status);
    }
    
    /**
     * 判断状态是否为已下架(OFFLINE)状态
     */
    private boolean isOfflineStatus(String status) {
        return "OFFLINE".equals(status) || "4".equals(status);
    }
    
    /**
     * 判断状态是否为已拒绝(REJECTED)状态
     */
    private boolean isRejectedStatus(String status) {
        return "REJECTED".equals(status) || "2".equals(status);
    }
    
    /**
     * 判断状态是否为待审核(PENDING)状态
     */
    private boolean isPendingStatus(String status) {
        return "PENDING".equals(status) || "0".equals(status);
    }
    
    @Override
    public HouseInfoDTO getHouseInfo(Long houseId) {
        // 1. 查询房源
        House house = this.getById(houseId);
        if (house == null) {
            log.warn("房源不存在: houseId={}", houseId);
            return null;
        }
        
        // 处理逻辑删除的房源，返回有限的基本信息而不是null
        HouseInfoDTO houseInfoDTO = new HouseInfoDTO();
        if (house.getIsDeleted() == 1) {
            log.warn("房源已被删除但仍返回基本信息: houseId={}", houseId);
            // 只复制基本信息
            houseInfoDTO.setId(house.getId());
            houseInfoDTO.setTitle("(已删除) " + house.getTitle());
            houseInfoDTO.setArea(house.getArea());
            houseInfoDTO.setPrice(house.getPrice());
            houseInfoDTO.setAddress(house.getAddress());
            houseInfoDTO.setProvince(house.getProvince());
            houseInfoDTO.setCity(house.getCity());
            houseInfoDTO.setDistrict(house.getDistrict());
            houseInfoDTO.setBedroomCount(house.getBedroomCount());
            houseInfoDTO.setLivingRoomCount(house.getLivingRoomCount());
            houseInfoDTO.setBathroomCount(house.getBathroomCount());
            houseInfoDTO.setHouseType(house.getHouseType());
            houseInfoDTO.setRentType(house.getRentType());
            houseInfoDTO.setCoverImage(house.getCoverImage());
            houseInfoDTO.setImages(new ArrayList<>());
            houseInfoDTO.setIsDeleted(true); // 标记为已删除
            houseInfoDTO.setStatus("DELETED"); // 设置状态为已删除
            return houseInfoDTO;
        }
        
        // 2. 只有已上架的房源才能被非管理员用户查看
        if (!"APPROVED".equals(house.getStatus()) && !"RENTED".equals(house.getStatus())) {
            // 这里实际业务中可能需要检查当前用户是否为房东或管理员
        }
        
        // 3. 查询房源图片
        LambdaQueryWrapper<HouseImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseImage::getHouseId, houseId);
        wrapper.eq(HouseImage::getIsDeleted, 0);
        wrapper.orderByAsc(HouseImage::getSort);
        List<HouseImage> houseImages = houseImageMapper.selectList(wrapper);
        
        // 4. 查询房东信息
        User owner = userMapper.selectById(house.getOwnerId());
        
        // 5. 组装房源详情DTO
        BeanUtils.copyProperties(house, houseInfoDTO);
        houseInfoDTO.setIsDeleted(false); // 标记为未删除
        
        // 设置房源图片
        if (houseImages != null && !houseImages.isEmpty()) {
            List<String> imageUrls = houseImages.stream()
                    .map(HouseImage::getUrl)
                    .collect(Collectors.toList());
            houseInfoDTO.setImages(imageUrls);
        } else {
            houseInfoDTO.setImages(new ArrayList<>());
        }
        
        // 设置房东信息
        if (owner != null) {
            houseInfoDTO.setOwnerName(owner.getNickname());
            houseInfoDTO.setOwnerAvatar(owner.getAvatar());
            houseInfoDTO.setOwnerPhone(owner.getPhone());
        }
        
        return houseInfoDTO;
    }
    
    @Override
    public Page<HouseInfoDTO> searchHouses(HouseSearchDTO searchDTO) {
        log.info("搜索房源, 条件: {}", searchDTO);
        
        // 1. 构建查询条件
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(House::getIsDeleted, 0);
        wrapper.eq(House::getStatus, "APPROVED"); // 只查询已上架的房源
        
        // 关键词搜索
        if (StringUtils.hasText(searchDTO.getKeyword())) {
            // 检查是否是直辖市搜索关键词（北京、上海、天津、重庆）
            boolean isDirectCity = isDirect(searchDTO.getKeyword());
            
            // 处理直辖市特殊情况
            if (isDirectCity) {
                String cityName = searchDTO.getKeyword().replace("市", "").trim(); // 去掉"市"后的关键词
                wrapper.and(w -> w
                        .eq(House::getCity, cityName)
                        .or()
                        .eq(House::getCity, cityName + "市")
                );
            } 
            // 其他普通地区搜索
            else if (searchDTO.getKeyword().contains("省") || 
                searchDTO.getKeyword().contains("区") ||
                searchDTO.getKeyword().contains("县")) {
                
                // 如果关键词看起来像地区名称，优先按地区匹配
                wrapper.and(w -> w
                        .like(House::getProvince, searchDTO.getKeyword())
                        .or()
                        .like(House::getCity, searchDTO.getKeyword())
                        .or()
                        .like(House::getDistrict, searchDTO.getKeyword())
                        .or()
                        .like(House::getTitle, searchDTO.getKeyword())
                        .or()
                        .like(House::getDescription, searchDTO.getKeyword())
                        .or()
                        .like(House::getAddress, searchDTO.getKeyword())
                );
            } else {
                // 标准关键词搜索
                wrapper.and(w -> w
                        .like(House::getTitle, searchDTO.getKeyword())
                        .or()
                        .like(House::getDescription, searchDTO.getKeyword())
                        .or()
                        .like(House::getAddress, searchDTO.getKeyword())
                );
            }
        }
        
        // 省份筛选
        if (StringUtils.hasText(searchDTO.getProvince())) {
            wrapper.eq(House::getProvince, searchDTO.getProvince());
            log.info("按省份筛选: {}", searchDTO.getProvince());
        }
        
        // 城市筛选 - 如果明确指定了城市，这个优先级更高
        if (StringUtils.hasText(searchDTO.getCity())) {
            // 处理直辖市特殊情况(北京、上海、天津、重庆)
            if (isDirect(searchDTO.getCity())) {
                String cityName = searchDTO.getCity().replace("市", "").trim(); // 去掉"市"后的城市名
                // 使用严格的等值匹配，确保不会匹配到其他城市
                wrapper.and(w -> w
                        .eq(House::getCity, cityName)
                        .or()
                        .eq(House::getCity, cityName + "市")
                );
                
                log.info("搜索直辖市: {}, 规范化后: {}", searchDTO.getCity(), cityName);
            } else {
                // 使用eq而不是like，确保精确匹配城市名称
                wrapper.eq(House::getCity, searchDTO.getCity());
                log.info("搜索普通城市: {}", searchDTO.getCity());
            }
        }
        
        // 区域筛选
        if (StringUtils.hasText(searchDTO.getDistrict())) {
            // 区域必须和城市配套，避免错误匹配
            if (StringUtils.hasText(searchDTO.getCity())) {
                // 确保区域是在已选择的城市下
                wrapper.eq(House::getDistrict, searchDTO.getDistrict());
                log.info("城市[{}]下搜索区域: {}", searchDTO.getCity(), searchDTO.getDistrict());
            } else {
                // 没有指定城市时，单独搜索区域（但这可能导致不同城市下相同区名的混淆）
                wrapper.eq(House::getDistrict, searchDTO.getDistrict());
                log.info("单独搜索区域: {}", searchDTO.getDistrict());
            }
        }
        
        // 价格区间
        if (searchDTO.getMinPrice() != null) {
            wrapper.ge(House::getPrice, searchDTO.getMinPrice());
        }
        if (searchDTO.getMaxPrice() != null) {
            wrapper.le(House::getPrice, searchDTO.getMaxPrice());
        }
        
        // 面积区间
        if (searchDTO.getMinArea() != null) {
            wrapper.ge(House::getArea, searchDTO.getMinArea());
        }
        if (searchDTO.getMaxArea() != null) {
            wrapper.le(House::getArea, searchDTO.getMaxArea());
        }
        
        // 卧室数量
        if (searchDTO.getBedroomCount() != null) {
            wrapper.eq(House::getBedroomCount, searchDTO.getBedroomCount());
        }
        
        // 房源类型
        if (StringUtils.hasText(searchDTO.getHouseType())) {
            wrapper.eq(House::getHouseType, searchDTO.getHouseType());
        }
        
        // 出租类型
        if (searchDTO.getRentType() != null) {
            wrapper.eq(House::getRentType, searchDTO.getRentType());
        }
        
        // 是否有电梯
        if (searchDTO.getHasElevator() != null) {
            wrapper.eq(House::getHasElevator, searchDTO.getHasElevator());
        }
        
        // 是否有停车位
        if (searchDTO.getHasParking() != null) {
            wrapper.eq(House::getHasParking, searchDTO.getHasParking());
        }
        
        // 排序方式
        if (StringUtils.hasText(searchDTO.getSortField())) {
            if ("price".equals(searchDTO.getSortField())) {
                if ("asc".equals(searchDTO.getSortOrder())) {
                    wrapper.orderByAsc(House::getPrice);
                } else {
                    wrapper.orderByDesc(House::getPrice);
                }
            } else if ("area".equals(searchDTO.getSortField())) {
                if ("asc".equals(searchDTO.getSortOrder())) {
                    wrapper.orderByAsc(House::getArea);
                } else {
                    wrapper.orderByDesc(House::getArea);
                }
            } else if ("createTime".equals(searchDTO.getSortField())) {
                if ("asc".equals(searchDTO.getSortOrder())) {
                    wrapper.orderByAsc(House::getCreateTime);
                } else {
                    wrapper.orderByDesc(House::getCreateTime);
                }
            }
        } else {
            // 默认按创建时间倒序
            wrapper.orderByDesc(House::getCreateTime);
        }
        
        // 处理优先加载标志 - 如果是第一页且设置了优先加载标志，则优先加载
        boolean isPriorityLoad = searchDTO.getPriorityLoad() != null && searchDTO.getPriorityLoad() && searchDTO.getPageNum() == 1;
        
        // 2. 分页查询
        Page<House> page = new Page<>(searchDTO.getPageNum(), searchDTO.getPageSize());
        
        // 如果是优先加载，可以优先执行这个查询
        if (isPriorityLoad) {
            log.info("优先加载第一页数据");
            // 可以在这里添加一些性能优化措施，例如限制返回字段
            // 或者减少一些不必要的关联查询
        }
        
        Page<House> housePage = this.page(page, wrapper);
        
        // 3. 转换为DTO
        List<HouseInfoDTO> houseInfoDTOList = new ArrayList<>();
        if (housePage.getRecords() != null && !housePage.getRecords().isEmpty()) {
            for (House house : housePage.getRecords()) {
                // 如果是优先加载，可以简化DTO转换逻辑，减少查询次数
                HouseInfoDTO houseInfoDTO = isPriorityLoad ? 
                    convertToDTOSimplified(house) : convertToDTO(house);
                houseInfoDTOList.add(houseInfoDTO);
            }
        }
        
        // 4. 组装分页结果
        Page<HouseInfoDTO> resultPage = new Page<>();
        resultPage.setCurrent(housePage.getCurrent());
        resultPage.setSize(housePage.getSize());
        resultPage.setTotal(housePage.getTotal());
        resultPage.setPages(housePage.getPages());
        resultPage.setRecords(houseInfoDTOList);
        
        return resultPage;
    }
    
    /**
     * 简化版的DTO转换，用于优先加载场景
     * 减少不必要的关联查询，提高响应速度
     */
    private HouseInfoDTO convertToDTOSimplified(House house) {
        if (house == null) {
            return null;
        }
        
        HouseInfoDTO houseInfoDTO = new HouseInfoDTO();
        BeanUtils.copyProperties(house, houseInfoDTO);
        
        // 处理封面图片URL
        if (StringUtils.hasText(house.getCoverImage())) {
            String coverImage = house.getCoverImage();
            // 确保URL格式正确
            if (!coverImage.startsWith("/") && !coverImage.startsWith("http")) {
                coverImage = "/" + coverImage;
            }
            houseInfoDTO.setCoverImage(coverImage);
        }
        
        // 不查询其他图片和用户详情，减少数据库查询
        houseInfoDTO.setImages(new ArrayList<>());
        
        return houseInfoDTO;
    }
    
    @Override
    public Page<HouseInfoDTO> getLandlordHouses(Long userId, Integer pageNum, Integer pageSize) {
        log.info("获取房东发布的房源列表, 房东ID: {}, 页码: {}, 每页数量: {}", userId, pageNum, pageSize);

        // 1. 构建查询条件
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(House::getOwnerId, userId);
        // 不再过滤已删除的房源
        // 修改为按创建时间升序排列
        wrapper.orderByAsc(House::getCreateTime);

        // 2. 分页查询
        Page<House> page = new Page<>(pageNum, pageSize);
        Page<House> housePage = this.page(page, wrapper);

        if (housePage == null || housePage.getRecords() == null) {
            log.warn("分页查询结果为空, 房东ID: {}", userId);
            return new Page<>(pageNum, pageSize, 0);
        }

        log.info("分页查询成功, 房东ID: {}, 查询到 {} 条记录, 总记录数: {}", 
                 userId, housePage.getRecords().size(), housePage.getTotal());

        // 3. 转换DTO
        List<HouseInfoDTO> dtoList = housePage.getRecords().stream()
                .map(house -> {
                    HouseInfoDTO dto = convertToDTO(house);
                    if (house.getIsDeleted() == 1) {
                        // 如果是已删除的房源，添加标记
                        dto.setTitle("(已删除) " + dto.getTitle());
                        dto.setStatus("DELETED");
                        dto.setIsDeleted(true);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
        
        if (log.isDebugEnabled()) {
            dtoList.forEach(dto -> log.debug("房源DTO: {}", dto));
        }

        // 4. 返回分页结果
        Page<HouseInfoDTO> resultPage = new Page<>(housePage.getCurrent(), housePage.getSize(), housePage.getTotal());
        resultPage.setRecords(dtoList);
        
        log.info("返回房东房源列表分页结果, 房东ID: {}, 当前页: {}, 每页数量: {}, 总数: {}", 
                 userId, resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
                 
        return resultPage;
    }
    
    @Override
    public Page<HouseInfoDTO> getPendingHouses(Integer pageNum, Integer pageSize) {
        log.info("获取待审核房源列表");
        
        // 1. 构建查询条件
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(House::getStatus, "PENDING");
        wrapper.eq(House::getIsDeleted, 0);
        // 修改为按创建时间升序排列
        wrapper.orderByAsc(House::getCreateTime);
        
        // 2. 分页查询
        Page<House> page = new Page<>(pageNum, pageSize);
        Page<House> housePage = this.page(page, wrapper);
        
        // 3. 转换为DTO
        List<HouseInfoDTO> houseInfoDTOList = new ArrayList<>();
        if (housePage.getRecords() != null && !housePage.getRecords().isEmpty()) {
            for (House house : housePage.getRecords()) {
                HouseInfoDTO houseInfoDTO = convertToDTO(house);
                houseInfoDTOList.add(houseInfoDTO);
            }
        }
        
        // 4. 组装分页结果
        Page<HouseInfoDTO> resultPage = new Page<>();
        resultPage.setCurrent(housePage.getCurrent());
        resultPage.setSize(housePage.getSize());
        resultPage.setTotal(housePage.getTotal());
        resultPage.setPages(housePage.getPages());
        resultPage.setRecords(houseInfoDTOList);
        
        return resultPage;
    }
    
    @Override
    @Transactional
    public void offlineHouse(Long houseId, Long userId) {
        log.info("下架房源, 房源ID: {}, 用户ID: {}", houseId, userId);
        
        // 1. 查询房源
        House house = this.getById(houseId);
        if (house == null || house.getIsDeleted() == 1) {
            throw new BusinessException("房源不存在");
        }
        
        // 2. 校验房源所有者，如果userId为null则表示管理员操作，不需要校验
        if (userId != null && !Objects.equals(house.getOwnerId(), userId)) {
            // 检查当前用户是否为管理员 - 实际业务中应通过权限系统判断
            throw new BusinessException("只有房源所有者才能下架房源");
        }
        
        // 3. 校验房源状态
        String status = house.getStatus();
        if (!isApprovedStatus(status)) {
            throw new BusinessException("只有已上架的房源才能下架");
        }
        
        // 4. 更新房源状态
        // 如果原状态是数字格式，则使用数字状态
        String newStatus = isNumericStatus(status) ? "4" : "OFFLINE";
        house.setStatus(newStatus);
        house.setUpdateTime(LocalDateTime.now());
        
        boolean updateResult = this.updateById(house);
        if (!updateResult) {
            throw new BusinessException("下架房源失败");
        }
        
        log.info("下架房源成功, 房源ID: {}", houseId);
    }
    
    @Override
    @Transactional
    public void onlineHouse(Long houseId, Long userId) {
        log.info("上架房源, 房源ID: {}, 用户ID: {}", houseId, userId);
        
        // 1. 查询房源
        House house = this.getById(houseId);
        if (house == null || house.getIsDeleted() == 1) {
            throw new BusinessException("房源不存在");
        }
        
        // 2. 校验房源所有者，如果userId为null则表示管理员操作，不需要校验
        if (userId != null && !Objects.equals(house.getOwnerId(), userId)) {
            // 检查当前用户是否为管理员 - 实际业务中应通过权限系统判断
            throw new BusinessException("只有房源所有者才能上架房源");
        }
        
        // 3. 校验房源状态
        String status = house.getStatus();
        // 允许已审核、已下架或已拒绝的房源上架
        if (!isApprovedStatus(status) && !isOfflineStatus(status) && !isRejectedStatus(status)) {
            throw new BusinessException("只有已审核、已下架或已拒绝的房源才能上架");
        }
        
        // 4. 更新房源状态为已上架(已审核)
        house.setStatus("APPROVED");
        house.setUpdateTime(LocalDateTime.now());
        
        boolean updateResult = this.updateById(house);
        if (!updateResult) {
            throw new BusinessException("上架房源失败");
        }
        
        log.info("上架房源成功, 房源ID: {}", houseId);
    }
    
    @Override
    @Transactional
    public void deleteHouse(Long houseId, Long userId) {
        log.info("删除房源, 房源ID: {}, 用户ID: {}", houseId, userId);
        
        // 1. 查询房源
        House house = this.getById(houseId);
        if (house == null || house.getIsDeleted() == 1) {
            log.warn("尝试删除不存在或已删除的房源, 房源ID: {}", houseId);
            throw new BusinessException("房源不存在");
        }
        
        log.info("查询到的房源信息: ID={}, OwnerId={}", house.getId(), house.getOwnerId());
        
        // 2. 校验房源所有者 (如果userId为null，表示是管理员操作，跳过此校验)
        if (userId != null) {
            log.info("执行房东删除校验: 房源OwnerId={}, 请求UserID={}", house.getOwnerId(), userId);
            if (!Objects.equals(house.getOwnerId(), userId)) {
                log.warn("无权删除房源: 房源OwnerId={}, 请求UserID={}", house.getOwnerId(), userId);
                throw new BusinessException("只有房源所有者才能删除房源");
            }
        } else {
            log.info("管理员操作，跳过所有者校验");
        }
        
        // 3. 校验房源状态：不能删除已出租的房源
        if ("RENTED".equals(house.getStatus())) {
            log.warn("无法删除已出租的房源, 房源ID: {}, 状态: {}", houseId, house.getStatus());
            throw new BusinessException("已出租的房源不能删除");
        }
        
        // 4. 逻辑删除房源 - 使用MyBatis-Plus的removeById方法进行逻辑删除
        log.info("准备逻辑删除房源, House ID: {}", houseId);
        boolean removed = this.removeById(houseId);
        
        if (!removed) {
            log.error("逻辑删除房源失败, House ID: {}", houseId);
            throw new BusinessException("删除房源失败");
        }
        log.info("逻辑删除房源成功, House ID: {}", houseId);
        
        // 5. 逻辑删除房源图片
        LambdaQueryWrapper<HouseImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseImage::getHouseId, houseId);
        List<HouseImage> houseImages = houseImageMapper.selectList(wrapper);
        if (houseImages != null && !houseImages.isEmpty()) {
            log.info("开始逻辑删除房源图片, 数量: {}, 房源ID: {}", houseImages.size(), houseId);
            int deletedImageCount = 0;
            for (HouseImage houseImage : houseImages) {
                boolean imageRemoved = houseImageMapper.deleteById(houseImage.getId()) > 0;
                if (imageRemoved) {
                    deletedImageCount++;
                } else {
                    log.warn("逻辑删除房源图片失败, 图片ID: {}, 房源ID: {}", houseImage.getId(), houseId);
                }
            }
            log.info("逻辑删除房源图片完成, 成功数量: {}, 房源ID: {}", deletedImageCount, houseId);
        } else {
            log.info("房源没有关联图片需要删除, 房源ID: {}", houseId);
        }
        
        log.info("完成删除房源操作, 即将提交事务, House ID: {}", houseId);
    }
    
    @Override
    public Page<HouseInfoDTO> getAllHouses(Integer pageNum, Integer pageSize) {
        log.info("获取所有房源列表: pageNum={}, pageSize={}", pageNum, pageSize);
        
        // 1. 构建查询条件
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
        // 不再过滤已删除的房源
        wrapper.orderByDesc(House::getCreateTime);
        
        // 2. 分页查询
        Page<House> page = new Page<>(pageNum, pageSize);
        Page<House> housePage = this.page(page, wrapper);
        
        // 3. 转换为DTO
        List<HouseInfoDTO> houseInfoDTOList = new ArrayList<>();
        if (housePage.getRecords() != null && !housePage.getRecords().isEmpty()) {
            for (House house : housePage.getRecords()) {
                HouseInfoDTO houseInfoDTO = convertToDTO(house);
                if (house.getIsDeleted() == 1) {
                    // 如果是已删除的房源，添加标记
                    houseInfoDTO.setTitle("(已删除) " + houseInfoDTO.getTitle());
                    houseInfoDTO.setStatus("DELETED");
                    houseInfoDTO.setIsDeleted(true);
                }
                houseInfoDTOList.add(houseInfoDTO);
            }
        }
        
        // 4. 组装分页结果
        Page<HouseInfoDTO> resultPage = new Page<>();
        resultPage.setCurrent(housePage.getCurrent());
        resultPage.setSize(housePage.getSize());
        resultPage.setTotal(housePage.getTotal());
        resultPage.setPages(housePage.getPages());
        resultPage.setRecords(houseInfoDTOList);
        
        log.info("获取所有房源列表成功，总记录数：{}", resultPage.getTotal());
        return resultPage;
    }
    
    @Override
    public Page<HouseInfoDTO> getHousesByStatus(String status, Integer pageNum, Integer pageSize) {
        log.info("获取指定状态的房源列表: status={}, pageNum={}, pageSize={}", status, pageNum, pageSize);
        
        // 1. 构建查询条件
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
        // 对于DELETED状态特殊处理
        if ("DELETED".equals(status)) {
            wrapper.eq(House::getIsDeleted, 1);
        } else {
            wrapper.eq(House::getStatus, status);
            // 其他状态时，只查询未删除的
            wrapper.eq(House::getIsDeleted, 0);
        }
        wrapper.orderByDesc(House::getCreateTime);
        
        // 2. 分页查询
        Page<House> page = new Page<>(pageNum, pageSize);
        Page<House> housePage = this.page(page, wrapper);
        
        // 3. 转换为DTO
        List<HouseInfoDTO> houseInfoDTOList = new ArrayList<>();
        if (housePage.getRecords() != null && !housePage.getRecords().isEmpty()) {
            for (House house : housePage.getRecords()) {
                HouseInfoDTO houseInfoDTO = convertToDTO(house);
                if (house.getIsDeleted() == 1) {
                    houseInfoDTO.setTitle("(已删除) " + houseInfoDTO.getTitle());
                    houseInfoDTO.setStatus("DELETED");
                    houseInfoDTO.setIsDeleted(true);
                }
                houseInfoDTOList.add(houseInfoDTO);
            }
        }
        
        // 4. 组装分页结果
        Page<HouseInfoDTO> resultPage = new Page<>();
        resultPage.setCurrent(housePage.getCurrent());
        resultPage.setSize(housePage.getSize());
        resultPage.setTotal(housePage.getTotal());
        resultPage.setPages(housePage.getPages());
        resultPage.setRecords(houseInfoDTOList);
        
        log.info("获取指定状态的房源列表成功，状态: {}, 总记录数：{}", status, resultPage.getTotal());
        return resultPage;
    }
    
    /**
     * 将House实体转换为HouseInfoDTO
     * @param house 房源实体
     * @return 房源详情DTO
     */
    private HouseInfoDTO convertToDTO(House house) {
        if (house == null) {
            return null;
        }

        HouseInfoDTO houseInfoDTO = new HouseInfoDTO();
        BeanUtils.copyProperties(house, houseInfoDTO);

        // 处理封面图片URL
        if (StringUtils.hasText(house.getCoverImage())) {
            String coverImage = house.getCoverImage();
            // 确保URL格式正确
            if (!coverImage.startsWith("/") && !coverImage.startsWith("http")) {
                coverImage = "/" + coverImage;
            }
            houseInfoDTO.setCoverImage(coverImage);
        }

        // 查询房源图片
        LambdaQueryWrapper<HouseImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseImage::getHouseId, house.getId());
        wrapper.eq(HouseImage::getIsDeleted, 0);
        wrapper.orderByAsc(HouseImage::getSort);
        List<HouseImage> houseImages = houseImageMapper.selectList(wrapper);
        
        // 查询房东信息
        User owner = userMapper.selectById(house.getOwnerId());
        
        // 设置房源图片
        if (houseImages != null && !houseImages.isEmpty()) {
            List<String> imageUrls = houseImages.stream()
                    .map(houseImage -> {
                        String url = houseImage.getUrl();
                        // 确保URL格式正确
                        if (!url.startsWith("/") && !url.startsWith("http")) {
                            url = "/" + url;
                        }
                        return url;
                    })
                 .collect(Collectors.toList());
            houseInfoDTO.setImages(imageUrls);
        } else {
            houseInfoDTO.setImages(new ArrayList<>());
        }

        // 设置房东信息
        if (owner != null) {
            houseInfoDTO.setOwnerName(owner.getNickname());
            houseInfoDTO.setOwnerAvatar(owner.getAvatar());
            houseInfoDTO.setOwnerPhone(owner.getPhone());
        }

        return houseInfoDTO;
    }
    
    /**
     * 获取推荐房源列表
     */
    @Override
    public List<HouseInfoDTO> getRecommendHouses(Integer limit) {
        log.info("获取推荐房源列表, 限制数量: {}", limit);
        
        // 1. 构建查询条件：查询已上架的房源
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(House::getStatus, "APPROVED"); // 只查询已上架的房源
        wrapper.eq(House::getIsDeleted, 0);
        wrapper.orderByDesc(House::getCreateTime); // 按创建时间倒序，展示最新的房源
        
        // 2. 限制查询数量
        Page<House> page = new Page<>(1, limit);
        Page<House> housePage = this.page(page, wrapper);
        
        // 3. 转换为DTO列表
        List<HouseInfoDTO> result = new ArrayList<>();
        if (housePage.getRecords() != null && !housePage.getRecords().isEmpty()) {
            for (House house : housePage.getRecords()) {
                HouseInfoDTO dto = convertToDTO(house);
                result.add(dto);
            }
        }
        
        log.info("获取推荐房源列表成功, 结果数: {}", result.size());
        return result;
    }
    
    /**
     * 获取房源总数
     */
    @Override
    public long count() {
        log.info("获取房源总数");
        try {
            // 创建查询条件，只统计未删除的房源
            LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(House::getIsDeleted, 0);
            
            // 调用MyBatis-Plus的count方法
            long count = super.count(wrapper);
            log.info("获取房源总数成功，总数: {}", count);
            return count;
        } catch (Exception e) {
            log.error("获取房源总数失败: {}", e.getMessage(), e);
            return 0; // 出错时返回0，避免影响页面显示
        }
    }

    /**
     * 更新房源合同设置
     * @param dto 房源合同设置DTO
     * @return 是否更新成功
     */
    @Override
    @Transactional
    public boolean updateHouseContractSettings(HouseContractSettingDTO dto) {
        House house = getById(dto.getHouseId());
        if (house == null) {
            log.error("更新房源合同设置失败，房源不存在：{}", dto.getHouseId());
            return false;
        }
        
        // 更新房源合同设置
        house.setContractTemplateId(dto.getContractTemplateId());
        house.setMinLeaseTerm(dto.getMinLeaseTerm());
        house.setDepositMonths(dto.getDepositMonths());
        
        return updateById(house);
    }
    
    /**
     * 更新房源状态
     * @param houseId 房源ID
     * @param status 新状态
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateHouseStatus(Long houseId, String status) {
        log.info("开始更新房源状态: houseId={}, 目标状态={}", houseId, status);
        
        House house = getById(houseId);
        if (house == null) {
            log.error("更新房源状态失败，房源不存在：{}", houseId);
            return false;
        }
        
        // 先记录当前状态，用于日志
        String oldStatus = house.getStatus();
        log.info("当前房源信息: ID={}, 当前状态={}, 房源标题={}", house.getId(), oldStatus, house.getTitle());
        
        // 验证状态值是否合法
        if (!StringUtils.hasText(status)) {
            log.error("更新房源状态失败，状态值不能为空");
            return false;
        }
        
        // 允许的状态值：PENDING-待审核，APPROVED-已上架，REJECTED-已拒绝，RENTED-已出租，OFFLINE-已下架
        if (!status.equals("PENDING") && !status.equals("APPROVED") && 
            !status.equals("REJECTED") && !status.equals("RENTED") && 
            !status.equals("OFFLINE")) {
            log.error("更新房源状态失败，状态值不合法：{}", status);
            return false;
        }
        
        // 更新房源状态
        house.setStatus(status);
        house.setUpdateTime(LocalDateTime.now());
        
        try {
            boolean result = updateById(house);
            if (result) {
                log.info("房源状态更新成功: houseId={}, 旧状态={}, 新状态={}", 
                        houseId, oldStatus, status);
                
                // 验证更新是否实际生效
                House updatedHouse = getById(houseId);
                if (updatedHouse != null && status.equals(updatedHouse.getStatus())) {
                    log.info("房源状态验证成功: houseId={}, status={}", houseId, updatedHouse.getStatus());
                } else {
                    String actualStatus = updatedHouse != null ? updatedHouse.getStatus() : "未知";
                    log.warn("房源状态验证失败! 预期={}, 实际={}", status, actualStatus);
                    
                    if (updatedHouse != null && !status.equals(updatedHouse.getStatus())) {
                        // 再次尝试更新
                        log.info("尝试再次更新房源状态...");
                        updatedHouse.setStatus(status);
                        updatedHouse.setUpdateTime(LocalDateTime.now());
                        boolean retryResult = updateById(updatedHouse);
                        log.info("再次更新结果: {}", retryResult ? "成功" : "失败");
                        return retryResult; // 返回再次尝试的结果
                    }
                }
            } else {
                log.error("房源状态更新失败，数据库操作未成功: houseId={}", houseId);
            }
            return result;
        } catch (Exception e) {
            log.error("更新房源状态时发生异常: houseId={}, 目标状态={}, 异常={}", 
                    houseId, status, e.getMessage(), e);
            throw e; // 抛出异常以触发事务回滚
        }
    }

    /**
     * 统计房东的房源数量
     * @param landlordId 房东ID
     * @return 房东的房源数量
     */
    @Override
    public int countLandlordHouses(Long landlordId) {
        QueryWrapper<House> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("owner_id", landlordId)
                   .eq("is_deleted", 0);
        
        return Math.toIntExact(count(queryWrapper));
    }

    /**
     * 获取已删除的房源列表
     */
    @Override
    public Page<HouseInfoDTO> getDeletedHouses(Integer pageNum, Integer pageSize) {
        log.info("获取已删除的房源列表");
        
        // 1. 构建查询条件
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(House::getIsDeleted, 1); // 查询已删除的房源
        wrapper.orderByDesc(House::getUpdateTime); // 按更新时间倒序
        
        // 2. 分页查询
        Page<House> page = new Page<>(pageNum, pageSize);
        Page<House> housePage = this.page(page, wrapper);
        
        // 3. 转换为DTO
        List<HouseInfoDTO> houseInfoDTOList = new ArrayList<>();
        if (housePage.getRecords() != null && !housePage.getRecords().isEmpty()) {
            for (House house : housePage.getRecords()) {
                HouseInfoDTO houseInfoDTO = convertToDTO(house);
                // 设置状态为"已删除"
                houseInfoDTO.setStatus("DELETED");
                houseInfoDTOList.add(houseInfoDTO);
            }
        }
        
        // 4. 组装分页结果
        Page<HouseInfoDTO> resultPage = new Page<>();
        resultPage.setCurrent(housePage.getCurrent());
        resultPage.setSize(housePage.getSize());
        resultPage.setTotal(housePage.getTotal());
        resultPage.setPages(housePage.getPages());
        resultPage.setRecords(houseInfoDTOList);
        
        log.info("获取已删除的房源列表成功，总记录数：{}", resultPage.getTotal());
        return resultPage;
    }

    @Override
    @Transactional
    public boolean editHouse(HouseEditDTO editDTO, Long userId) {
        log.info("编辑房源, 用户ID: {}, 房源ID: {}", userId, editDTO.getId());
        House house = this.getById(editDTO.getId());
        if (house == null || !house.getOwnerId().equals(userId)) {
            log.warn("编辑房源失败: 房源不存在或无权限, 房源ID: {}, 用户ID: {}", editDTO.getId(), userId);
            return false;
        }
        
        // 检查是否可以编辑（例如，不能编辑已出租的房源）
        if ("RENTED".equals(house.getStatus())) {
            log.warn("编辑房源失败: 房源已出租, 房源ID: {}", editDTO.getId());
            // 可以选择抛出异常或返回false
            // throw new CustomException("无法编辑已出租的房源");
            return false;
        }
        
        // 更新房源基础信息
        BeanUtils.copyProperties(editDTO, house, "id", "ownerId", "createTime", "coverImage", "status");
        // 更新省市区信息
        if (editDTO.getProvinceId() != null) {
            house.setProvinceId(editDTO.getProvinceId());
            // 根据 provinceId 更新省份名称
            RegionProvince provinceEntity = regionProvinceMapper.selectById(editDTO.getProvinceId());
            if (provinceEntity != null) {
                house.setProvince(provinceEntity.getName());
            }
        }
        
        if (editDTO.getDistrictId() != null) {
            house.setDistrictId(editDTO.getDistrictId());
            // 根据 districtId 更新区域名称
            RegionDistrict districtEntity = regionDistrictMapper.selectById(editDTO.getDistrictId());
            if (districtEntity != null) {
                house.setDistrict(districtEntity.getName());
            }
        }
        
        // 处理封面图片
        String newCoverImageUrl = null;
        boolean coverImageChanged = false;
        
        // 情况1: 上传了新的封面文件
        if (editDTO.getCoverImageFile() != null && !editDTO.getCoverImageFile().isEmpty()) {
            newCoverImageUrl = uploadImage(editDTO.getCoverImageFile());
            coverImageChanged = true;
            log.info("上传了新的封面图片, URL: {}", newCoverImageUrl);
        } 
        // 情况2: 提供了新的封面URL (例如从已上传图片中选择)
        else if (StringUtils.hasText(editDTO.getCoverImage()) && !editDTO.getCoverImage().equals(house.getCoverImage())) {
            newCoverImageUrl = editDTO.getCoverImage();
            coverImageChanged = true;
            log.info("提供了新的封面图片URL: {}", newCoverImageUrl);
        } 
        // 情况3: 明确要删除封面 (如果前端允许删除封面且不设置新的，editDTO.getCoverImage()可能为空或特定标记)
        // else if (editDTO.getCoverImage() == null && house.getCoverImage() != null) {
        //     // 删除封面逻辑
        //     coverImageChanged = true;
        //     newCoverImageUrl = null;
        // }
        
        // 如果封面图片发生变化
        if (coverImageChanged) {
            // 更新House表的coverImage字段
            house.setCoverImage(newCoverImageUrl);
            
            // 更新HouseImage表中的is_cover状态
            updateCoverImageStatus(house.getId(), newCoverImageUrl);
        }
        
        // 更新房源基础信息到数据库
        this.updateById(house);
        
        // 处理房源图片（删除、新增）
        handleHouseImages(editDTO, house.getId());
        
        log.info("编辑房源成功, 房源ID: {}", editDTO.getId());
        return true;
    }
    
    @Override
    @Transactional
    public boolean adminEditHouse(HouseEditDTO editDTO) {
        log.info("管理员编辑房源: 房源ID={}", editDTO.getId());
        
        // 1. 验证房源是否存在
        House house = this.getById(editDTO.getId());
        if (house == null || house.getIsDeleted() == 1) {
            log.warn("房源不存在: houseId={}", editDTO.getId());
            return false;
        }
        
        // 2. 更新房源基本信息
        return updateHouseBasicInfo(house, editDTO);
    }
    
    /**
     * 更新房源基本信息
     */
    private boolean updateHouseBasicInfo(House house, HouseEditDTO editDTO) {
        // 1. 更新基本信息
        if (StringUtils.hasText(editDTO.getTitle())) {
            house.setTitle(editDTO.getTitle());
        }
        
        if (StringUtils.hasText(editDTO.getDescription())) {
            house.setDescription(editDTO.getDescription());
        }
        
        if (editDTO.getArea() != null) {
            house.setArea(editDTO.getArea());
        }
        
        if (editDTO.getPrice() != null) {
            house.setPrice(editDTO.getPrice());
        }
        
        if (StringUtils.hasText(editDTO.getAddress())) {
            house.setAddress(editDTO.getAddress());
        }
        
        if (StringUtils.hasText(editDTO.getProvince())) {
            house.setProvince(editDTO.getProvince());
        }
        
        if (StringUtils.hasText(editDTO.getCity())) {
            house.setCity(editDTO.getCity());
        }
        
        if (StringUtils.hasText(editDTO.getDistrict())) {
            house.setDistrict(editDTO.getDistrict());
        }
        
        if (editDTO.getProvinceId() != null) {
            house.setProvinceId(editDTO.getProvinceId());
            // 根据 provinceId 更新省份名称
            RegionProvince provinceEntity = regionProvinceMapper.selectById(editDTO.getProvinceId());
            if (provinceEntity != null) {
                house.setProvince(provinceEntity.getName());
            }
        }
        
        if (editDTO.getCityId() != null) {
            house.setCityId(editDTO.getCityId());
            // 根据 cityId 更新城市名称
            RegionCity cityEntity = regionCityMapper.selectById(editDTO.getCityId());
            if (cityEntity != null) {
                house.setCity(cityEntity.getName());
            }
        }
        
        if (editDTO.getBedroomCount() != null) {
            house.setBedroomCount(editDTO.getBedroomCount());
        }
        
        if (editDTO.getLivingRoomCount() != null) {
            house.setLivingRoomCount(editDTO.getLivingRoomCount());
        }
        
        if (editDTO.getBathroomCount() != null) {
            house.setBathroomCount(editDTO.getBathroomCount());
        }
        
        if (StringUtils.hasText(editDTO.getOrientation())) {
            house.setOrientation(editDTO.getOrientation());
        }
        
        if (editDTO.getFloor() != null) {
            house.setFloor(editDTO.getFloor());
        }
        
        if (editDTO.getTotalFloor() != null) {
            house.setTotalFloor(editDTO.getTotalFloor());
        }
        
        if (StringUtils.hasText(editDTO.getDecoration())) {
            house.setDecoration(editDTO.getDecoration());
        }
        
        if (editDTO.getHasElevator() != null) {
            house.setHasElevator(editDTO.getHasElevator());
        }
        
        if (editDTO.getHasParking() != null) {
            house.setHasParking(editDTO.getHasParking());
        }
        
        if (StringUtils.hasText(editDTO.getHouseType())) {
            house.setHouseType(editDTO.getHouseType());
        }
        
        if (StringUtils.hasText(editDTO.getRentType())) {
            house.setRentType(editDTO.getRentType());
        }
        
        if (editDTO.getMinLeaseTerm() != null) {
            house.setMinLeaseTerm(editDTO.getMinLeaseTerm());
        }
        
        if (editDTO.getDepositMonths() != null) {
            house.setDepositMonths(editDTO.getDepositMonths());
        }
        
        if (editDTO.getContractTemplateId() != null) {
            house.setContractTemplateId(editDTO.getContractTemplateId());
        }
        
        // 仅管理员可设置状态和拒绝原因
        if (StringUtils.hasText(editDTO.getStatus())) {
            house.setStatus(editDTO.getStatus());
            
            // 如果状态是拒绝，更新拒绝原因
            if ("REJECTED".equals(editDTO.getStatus()) && StringUtils.hasText(editDTO.getRejectReason())) {
                house.setRejectReason(editDTO.getRejectReason());
            }
        }
        
        // 2. 处理封面图片
        if (editDTO.getCoverImageFile() != null && !editDTO.getCoverImageFile().isEmpty()) {
            String coverImageUrl = uploadImage(editDTO.getCoverImageFile());
            if (StringUtils.hasText(coverImageUrl)) {
                house.setCoverImage(coverImageUrl);
            }
        } else if (StringUtils.hasText(editDTO.getCoverImage())) {
            house.setCoverImage(editDTO.getCoverImage());
        }
        
        // 3. 更新时间
        house.setUpdateTime(LocalDateTime.now());
        
        // 4. 保存房源
        boolean updated = this.updateById(house);
        if (!updated) {
            log.error("更新房源信息失败: houseId={}", house.getId());
            return false;
        }
        
        // 5. 处理房源图片
        handleHouseImages(editDTO, house.getId());
        
        log.info("房源编辑成功: houseId={}", house.getId());
        return true;
    }
    
    /**
     * 处理房源图片（添加新图片、删除指定图片）
     */
    private void handleHouseImages(HouseEditDTO editDTO, Long houseId) {
        // 1. 删除指定的图片
        if (editDTO.getImagesToDelete() != null && !editDTO.getImagesToDelete().isEmpty()) {
            log.info("准备删除图片: {}", editDTO.getImagesToDelete());
            LambdaQueryWrapper<HouseImage> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(HouseImage::getHouseId, houseId)
                         .in(HouseImage::getUrl, editDTO.getImagesToDelete());
            // 注意：这里使用逻辑删除，MyBatis-Plus会自动处理is_deleted
            int deletedCount = houseImageMapper.delete(deleteWrapper);
            log.info("成功逻辑删除了 {} 张图片", deletedCount);
            
            // 删除MinIO或本地文件 (可选，取决于你的策略)
            // for (String urlToDelete : editDTO.getImagesToDelete()) {
            //     deleteImageFile(urlToDelete);
            // }
        }
        
        // 获取当前最大排序值
        int sort = getMaxSort(houseId) + 1;
        
        // 2. 处理新增的图片文件
        if (editDTO.getImageFiles() != null && !editDTO.getImageFiles().isEmpty()) {
            log.info("准备上传 {} 张新图片", editDTO.getImageFiles().size());
            for (MultipartFile imageFile : editDTO.getImageFiles()) {
                if (imageFile != null && !imageFile.isEmpty()) {
                    String imageUrl = uploadImage(imageFile);
                    if (StringUtils.hasText(imageUrl)) {
                         // 检查图片是否已作为封面存在，避免重复插入
                        boolean isCover = imageUrl.equals(editDTO.getCoverImage());
                         if (!isImageExists(houseId, imageUrl)) {
                            HouseImage houseImage = new HouseImage();
                            houseImage.setHouseId(houseId);
                            houseImage.setUrl(imageUrl);
                            houseImage.setSort(sort++);
                            houseImage.setIsCover(isCover ? 1 : 0);
                            houseImage.setCreateTime(LocalDateTime.now());
                            houseImage.setUpdateTime(LocalDateTime.now());
                            houseImage.setIsDeleted(0);
                            houseImageMapper.insert(houseImage);
                            log.info("成功添加新图片: {}, isCover: {}", imageUrl, isCover);
                        } else if (isCover) {
                             // 如果图片已存在但现在被设为封面
                             updateCoverImageStatus(houseId, imageUrl);
                        }
                    }
                }
            }
        }
        // (如果还支持通过URL添加图片，这里添加相应逻辑)
        // 3. 处理新增的图片URL (如果需要)
        // if (editDTO.getImageUrls() != null && !editDTO.getImageUrls().isEmpty()) { ... }
        
        // 确保封面图片记录存在且is_cover=1 (最后检查一遍)
        if (StringUtils.hasText(editDTO.getCoverImage())) {
            updateCoverImageStatus(houseId, editDTO.getCoverImage());
        }
    }
    
    // 辅助方法: 获取房源当前最大排序值
    private int getMaxSort(Long houseId) {
        LambdaQueryWrapper<HouseImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HouseImage::getHouseId, houseId)
                   .orderByDesc(HouseImage::getSort)
                   .last("LIMIT 1");
        HouseImage lastImage = houseImageMapper.selectOne(queryWrapper);
        return lastImage != null ? lastImage.getSort() : -1; // 返回-1表示没有图片
    }
    
    // 辅助方法: 检查图片URL是否已存在于房源
    private boolean isImageExists(Long houseId, String imageUrl) {
        LambdaQueryWrapper<HouseImage> existCheck = new LambdaQueryWrapper<>();
        existCheck.eq(HouseImage::getHouseId, houseId)
                 .eq(HouseImage::getUrl, imageUrl);
        return houseImageMapper.selectCount(existCheck) > 0;
    }

    // 辅助方法: 更新封面图片状态
    private void updateCoverImageStatus(Long houseId, String newCoverUrl) {
        // 1. 将该房源所有图片的 is_cover 设置为 0
        HouseImage updateImage = new HouseImage();
        updateImage.setIsCover(0);
        updateImage.setUpdateTime(LocalDateTime.now()); // 设置更新时间
        LambdaQueryWrapper<HouseImage> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(HouseImage::getHouseId, houseId);
        houseImageMapper.update(updateImage, updateWrapper);
        log.info("已将房源 {} 的所有图片is_cover重置为0", houseId);
        
        // 2. 如果有新的封面图片URL，将其对应的 is_cover 设置为 1
        if (StringUtils.hasText(newCoverUrl)) {
            HouseImage newCover = new HouseImage();
            newCover.setIsCover(1);
            newCover.setUpdateTime(LocalDateTime.now()); // 设置更新时间
            LambdaQueryWrapper<HouseImage> coverWrapper = new LambdaQueryWrapper<>();
            coverWrapper.eq(HouseImage::getHouseId, houseId)
                       .eq(HouseImage::getUrl, newCoverUrl);
            int updatedRows = houseImageMapper.update(newCover, coverWrapper);
            if (updatedRows > 0) {
                 log.info("已将图片 {} 设置为房源 {} 的封面", newCoverUrl, houseId);
            } else {
                // 如果URL对应的记录不存在（可能是刚上传的），则插入一条新的封面记录
                HouseImage coverHouseImage = new HouseImage();
                coverHouseImage.setHouseId(houseId);
                coverHouseImage.setUrl(newCoverUrl);
                coverHouseImage.setSort(0); // 封面通常排最前
                coverHouseImage.setIsCover(1);
                coverHouseImage.setCreateTime(LocalDateTime.now()); // 设置创建时间
                coverHouseImage.setUpdateTime(LocalDateTime.now()); // 设置更新时间
                coverHouseImage.setIsDeleted(0); // 设置删除标记为0
                houseImageMapper.insert(coverHouseImage);
                log.info("为房源 {} 插入了新的封面图片记录: {}", houseId, newCoverUrl);
            }
        }
    }
    
    @Override
    public List<String> getHouseImages(Long houseId) {
        log.info("获取房源图片: houseId={}", houseId);
        
        // 验证房源是否存在
        House house = this.getById(houseId);
        if (house == null || house.getIsDeleted() == 1) {
            log.warn("房源不存在: houseId={}", houseId);
            return new ArrayList<>();
        }
        
        // 查询房源图片
        LambdaQueryWrapper<HouseImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseImage::getHouseId, houseId)
               .eq(HouseImage::getIsDeleted, 0)
               .orderByAsc(HouseImage::getSort);
        List<HouseImage> images = houseImageMapper.selectList(wrapper);
        
        // 转换为URL列表
        return images.stream()
                .map(HouseImage::getUrl)
                .collect(Collectors.toList());
    }

    /**
     * 根据条件查询房源列表
     * @param query 房源查询条件
     * @param queryParams 额外查询参数
     * @param pageRequest 分页请求
     * @return 查询结果
     */
    @Override
    public Map<String, Object> getHouseList(House query, Map<String, Object> queryParams, org.springframework.data.domain.PageRequest pageRequest) {
        log.info("查询房源列表: query={}, queryParams={}, pageRequest={}", query, queryParams, pageRequest);
        
        // 构建查询条件
        QueryWrapper<House> queryWrapper = new QueryWrapper<>();
        
        // 设置基本条件：未删除
        queryWrapper.eq("is_deleted", 0);
        
        // 设置基本条件：状态为已上架
        queryWrapper.eq("status", "APPROVED");
        
        // 设置查询条件：省份
        if (query != null && StringUtils.hasText(query.getProvince())) {
            queryWrapper.eq("province", query.getProvince());
        }
        
        // 设置查询条件：城市
        if (query != null && StringUtils.hasText(query.getCity())) {
            queryWrapper.eq("city", query.getCity());
        }
        
        // 设置查询条件：区域
        if (query != null && StringUtils.hasText(query.getDistrict())) {
            queryWrapper.eq("district", query.getDistrict());
        }
        
        // 设置查询条件：租赁类型
        if (query != null && StringUtils.hasText(query.getRentType())) {
            queryWrapper.eq("rent_type", query.getRentType());
        }
        
        // 设置查询条件：房屋类型
        if (query != null && StringUtils.hasText(query.getHouseType())) {
            queryWrapper.eq("house_type", query.getHouseType());
        }
        
        // 设置查询条件：卧室数量
        if (query != null && query.getBedroomCount() != null) {
            queryWrapper.eq("bedroom_count", query.getBedroomCount());
        }
        
        // 设置查询条件：关键词（模糊查询）
        if (queryParams != null && queryParams.containsKey("keyword")) {
            String keyword = (String) queryParams.get("keyword");
            if (StringUtils.hasText(keyword)) {
                queryWrapper.and(wrapper -> wrapper
                    .like("title", keyword)
                    .or()
                    .like("description", keyword)
                    .or()
                    .like("address", keyword)
                    .or()
                    .like("facilities", keyword)
                );
            }
        }
        
        // 设置查询条件：价格范围
        if (queryParams != null && queryParams.containsKey("minPrice")) {
            queryWrapper.ge("rent", queryParams.get("minPrice"));
        }
        
        if (queryParams != null && queryParams.containsKey("maxPrice")) {
            queryWrapper.le("rent", queryParams.get("maxPrice"));
        }
        
        // 设置排序：默认按照创建时间倒序
        queryWrapper.orderByDesc("create_time");
        
        // 执行分页查询
        Page<House> page = new Page<>(pageRequest.getPageNumber() + 1, pageRequest.getPageSize());
        Page<House> resultPage = this.page(page, queryWrapper);
        
        // 转换为DTO
        List<HouseInfoDTO> houseList = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 构建返回结果
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("total", resultPage.getTotal());
        result.put("pages", resultPage.getPages());
        result.put("current", resultPage.getCurrent());
        result.put("size", resultPage.getSize());
        result.put("records", houseList);
        
        return result;
    }

    /**
     * 判断是否是直辖市（北京、上海、天津、重庆）
     * @param cityName 城市名称
     * @return 是否是直辖市
     */
    private boolean isDirect(String cityName) {
        if (!StringUtils.hasText(cityName)) {
            return false;
        }
        
        String normalizedName = cityName.replace("市", "").trim();
        return "北京".equals(normalizedName) || 
               "上海".equals(normalizedName) || 
               "天津".equals(normalizedName) || 
               "重庆".equals(normalizedName);
    }
} 