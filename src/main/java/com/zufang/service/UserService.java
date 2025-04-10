package com.zufang.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zufang.dto.UserInfoDTO;
import com.zufang.dto.UserLoginDTO;
import com.zufang.dto.UserRegisterDTO;
import com.zufang.entity.User;

import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    
    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return 登录结果，包含token、userId和role
     */
    Map<String, Object> login(UserLoginDTO loginDTO);
    
    /**
     * 用户注册
     * @param registerDTO 注册信息
     * @return 用户ID
     */
    Long register(UserRegisterDTO registerDTO);
    
    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    UserInfoDTO getUserInfo(Long userId);
    
    /**
     * 获取用户总数
     * @return 用户总数
     */
    long count();
    
    /**
     * 根据ID获取用户信息
     * @param id 用户ID
     * @return 用户信息
     */
    UserInfoDTO getUserById(Long id);
    
    /**
     * 获取用户列表（分页）
     * @param page 页码
     * @param size 每页大小
     * @param query 搜索关键词（用户名、姓名或手机号）
     * @param role 角色筛选
     * @param status 状态筛选
     * @return 用户列表分页对象
     */
    Page<UserInfoDTO> getUserList(int page, int size, String query, String role, String status);
    
    /**
     * 更新用户状态
     * @param userId 用户ID
     * @param status 新状态
     * @return 是否成功
     */
    boolean updateUserStatus(Long userId, String status);
    
    /**
     * 删除用户
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteUser(Long userId);
    
    /**
     * 重置用户密码
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean resetPassword(Long userId, String newPassword);
    
    /**
     * 更新用户信息
     * @param userInfoDTO 用户信息DTO
     * @return 是否成功
     */
    boolean updateUserInfo(UserInfoDTO userInfoDTO);
    
    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);
} 