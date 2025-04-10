package com.zufang.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zufang.dto.HouseApproveDTO;
import com.zufang.dto.HouseContractSettingDTO;
import com.zufang.dto.HouseEditDTO;
import com.zufang.dto.HouseInfoDTO;
import com.zufang.dto.HousePublishDTO;
import com.zufang.dto.HouseSearchDTO;
import com.zufang.entity.House;

import java.util.List;
import java.util.Map;

/**
 * 房源服务接口
 */
public interface HouseService extends IService<House> {
    
    /**
     * 发布房源
     * @param publishDTO 房源发布DTO
     * @param userId 用户ID（房东ID）
     * @return 房源ID
     */
    Long publishHouse(HousePublishDTO publishDTO, Long userId);
    
    /**
     * 审核房源
     * @param approveDTO 审核DTO
     */
    void approveHouse(HouseApproveDTO approveDTO);
    
    /**
     * 审核房源（带布尔标志）
     * @param houseId 房源ID
     * @param approved 是否通过
     * @param reason 拒绝原因（如果未通过）
     * @return 操作是否成功
     */
    boolean approveHouse(Long houseId, boolean approved, String reason);
    
    /**
     * 获取房源详情
     * @param houseId 房源ID
     * @return 房源详情DTO
     */
    HouseInfoDTO getHouseInfo(Long houseId);
    
    /**
     * 搜索房源
     * @param searchDTO 搜索DTO
     * @return 房源分页结果
     */
    Page<HouseInfoDTO> searchHouses(HouseSearchDTO searchDTO);
    
    /**
     * 获取房东发布的房源列表
     * @param userId 用户ID（房东ID）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 房源分页结果
     */
    Page<HouseInfoDTO> getLandlordHouses(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取待审核房源列表（管理员）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 房源分页结果
     */
    Page<HouseInfoDTO> getPendingHouses(Integer pageNum, Integer pageSize);
    
    /**
     * 下架房源
     * @param houseId 房源ID
     * @param userId 用户ID（房东ID）
     */
    void offlineHouse(Long houseId, Long userId);
    
    /**
     * 上架房源
     * @param houseId 房源ID
     * @param userId 用户ID（房东ID）
     */
    void onlineHouse(Long houseId, Long userId);
    
    /**
     * 删除房源
     * @param houseId 房源ID
     * @param userId 用户ID（房东ID）
     */
    void deleteHouse(Long houseId, Long userId);
    
    /**
     * 获取所有房源列表（管理员）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 房源分页结果
     */
    Page<HouseInfoDTO> getAllHouses(Integer pageNum, Integer pageSize);
    
    /**
     * 获取指定状态的房源列表（管理员）
     * @param status 状态：PENDING-待审核，APPROVED-已上架，REJECTED-已拒绝，RENTED-已出租，OFFLINE-已下架
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 房源分页结果
     */
    Page<HouseInfoDTO> getHousesByStatus(String status, Integer pageNum, Integer pageSize);
    
    /**
     * 获取推荐房源列表
     * @param limit 限制数量
     * @return 推荐房源列表
     */
    List<HouseInfoDTO> getRecommendHouses(Integer limit);
    
    /**
     * 获取房源总数
     * @return 房源总数
     */
    long count();
    
    /**
     * 更新房源合同设置
     * @param dto 房源合同设置DTO
     * @return 是否更新成功
     */
    boolean updateHouseContractSettings(HouseContractSettingDTO dto);
    
    /**
     * 更新房源状态
     * @param houseId 房源ID
     * @param status 新状态
     * @return 是否更新成功
     */
    boolean updateHouseStatus(Long houseId, String status);
    
    /**
     * 统计房东的房源数量
     * @param landlordId 房东ID
     * @return 房东的房源数量
     */
    int countLandlordHouses(Long landlordId);
    
    /**
     * 获取已删除的房源列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 已删除的房源分页列表
     */
    Page<HouseInfoDTO> getDeletedHouses(Integer pageNum, Integer pageSize);
    
    /**
     * 编辑房源信息
     * @param editDTO 房源编辑DTO
     * @param userId 用户ID（操作人ID）
     * @return 是否编辑成功
     */
    boolean editHouse(HouseEditDTO editDTO, Long userId);
    
    /**
     * 管理员编辑房源信息
     * @param editDTO 房源编辑DTO
     * @return 是否编辑成功
     */
    boolean adminEditHouse(HouseEditDTO editDTO);
    
    /**
     * 获取房源的所有图片
     * @param houseId 房源ID
     * @return 图片URL列表
     */
    List<String> getHouseImages(Long houseId);
    
    /**
     * 根据条件查询房源列表
     * @param query 房源查询条件
     * @param queryParams 额外查询参数
     * @param pageRequest 分页请求
     * @return 查询结果
     */
    Map<String, Object> getHouseList(House query, Map<String, Object> queryParams, org.springframework.data.domain.PageRequest pageRequest);
} 