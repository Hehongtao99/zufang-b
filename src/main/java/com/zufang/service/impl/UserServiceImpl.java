package com.zufang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zufang.common.exception.BusinessException;
import com.zufang.common.util.JwtUtil;
import com.zufang.dto.UserInfoDTO;
import com.zufang.dto.UserLoginDTO;
import com.zufang.dto.UserRegisterDTO;
import com.zufang.entity.User;
import com.zufang.mapper.UserMapper;
import com.zufang.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Map<String, Object> login(UserLoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        
        log.info("执行登录逻辑，用户名: {}", username);
        try {
            // 1. 查询用户
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, username)
                    .eq(User::getIsDeleted, 0); // 确保用户未被删除
            User user = super.getOne(wrapper);
            
            // 2. 校验用户是否存在
            if (user == null) {
                log.warn("用户不存在: {}", username);
                throw new BusinessException("用户名或密码错误");
            }
            
            // 2.5 校验用户状态是否正常
            if (!"ACTIVE".equals(user.getStatus())) {
                 log.warn("用户账号已被禁用或状态异常: {}, status={}", username, user.getStatus());
                 throw new BusinessException("用户账号已被禁用或状态异常");
            }
            
            // 3. 校验密码是否正确
            if (!user.getPassword().equals(password)) { // 实际项目中应该使用加密方式验证
                log.warn("密码错误，用户名: {}", username);
                throw new BusinessException("用户名或密码错误");
            }
            
            // 4. 生成Token
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            claims.put("username", user.getUsername());
            claims.put("role", user.getRole());
            
            String token = JwtUtil.generateToken(claims);
            log.info("登录成功，生成Token, 用户ID: {}", user.getId());
            
            // 返回登录结果
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userId", user.getId());
            result.put("role", user.getRole());
            
            return result;
        } catch (BusinessException e) {
            throw e; // 业务异常直接抛出
        } catch (Exception e) {
            log.error("登录过程中发生异常: {}", e.getMessage(), e);
            throw new BusinessException("登录失败：" + e.getMessage());
        }
    }

    @Override
    public Long register(UserRegisterDTO registerDTO) {
        log.info("执行注册逻辑，用户名: {}", registerDTO.getUsername());
        try {
            // 1. 校验两次密码是否一致
            if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
                log.warn("两次密码不一致: {}", registerDTO.getUsername());
                throw new BusinessException("两次密码不一致");
            }
            
            // 2. 校验用户名是否已存在
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, registerDTO.getUsername());
            User existUser = this.getOne(wrapper);
            if (existUser != null) {
                log.warn("用户名已存在: {}", registerDTO.getUsername());
                throw new BusinessException("用户名已存在");
            }
            
            // 3. 创建用户
            User user = new User();
            BeanUtils.copyProperties(registerDTO, user);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            user.setIsDeleted(false);
            
            // 设置昵称（如果为空，则使用用户名）
            if (user.getNickname() == null || user.getNickname().isEmpty()) {
                user.setNickname(user.getUsername());
            }
            
            // 不再设置默认头像，允许用户后续上传自己的头像
            
            // 4. 保存用户
            boolean success = this.save(user);
            if (!success) {
                log.error("用户保存失败: {}", registerDTO.getUsername());
                throw new BusinessException("注册失败，请稍后再试");
            }
            
            log.info("注册成功，用户ID: {}", user.getId());
            return user.getId();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("注册过程发生异常: {}", e.getMessage(), e);
            throw new BusinessException("注册失败：" + e.getMessage());
        }
    }

    @Override
    public UserInfoDTO getUserInfo(Long userId) {
        log.info("获取用户信息，用户ID: {}", userId);
        try {
            // 直接从数据库中获取
            User user = this.getById(userId);
                
            // 判断用户是否存在
            if (user == null) {
                log.warn("用户不存在: {}", userId);
                throw new BusinessException("用户不存在");
            }
            
            // 转换为DTO
            UserInfoDTO userInfoDTO = new UserInfoDTO();
            BeanUtils.copyProperties(user, userInfoDTO);
            
            log.info("获取用户信息成功: {}", userId);
            return userInfoDTO;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取用户信息过程发生异常: {}", e.getMessage(), e);
            throw new BusinessException("获取用户信息失败：" + e.getMessage());
        }
    }

    @Override
    public long count() {
        log.info("获取用户总数");
        try {
            // 创建查询条件，只统计未删除的用户
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getIsDeleted, false);
            
            // 调用MyBatis-Plus的count方法
            long count = super.count(wrapper);
            log.info("获取用户总数成功，总数: {}", count);
            return count;
        } catch (Exception e) {
            log.error("获取用户总数失败: {}", e.getMessage(), e);
            return 0; // 出错时返回0，避免影响页面显示
        }
    }

    @Override
    public UserInfoDTO getUserById(Long id) {
        log.info("获取用户信息，用户ID：{}", id);
        
        // 直接从数据库中获取
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        BeanUtils.copyProperties(user, userInfoDTO);
        
        return userInfoDTO;
    }
    
    @Override
    public Page<UserInfoDTO> getUserList(int pageNum, int pageSize, String query, String role, String status) {
        log.info("获取用户列表, 页码: {}, 每页数量: {}, 搜索词: {}, 角色: {}, 状态: {}", 
                pageNum, pageSize, query, role, status);
        
        try {
            // 构建查询条件
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            
            // 只查询未删除的用户
            wrapper.eq(User::getIsDeleted, false);
            
            // 按关键词搜索（用户名、姓名、手机号）
            if (StringUtils.hasText(query)) {
                wrapper.and(w -> w
                    .like(User::getUsername, query)
                    .or()
                    .like(User::getNickname, query)
                    .or()
                    .like(User::getRealName, query)
                    .or()
                    .like(User::getPhone, query)
                );
            }
            
            // 按角色筛选
            if (StringUtils.hasText(role)) {
                wrapper.eq(User::getRole, role);
            }
            
            // 按状态筛选
            if (StringUtils.hasText(status)) {
                wrapper.eq(User::getStatus, status);
            }
            
            // 按创建时间降序排序
            wrapper.orderByDesc(User::getCreateTime);
            
            // 执行分页查询
            Page<User> userPage = new Page<>(pageNum, pageSize);
            Page<User> result = userMapper.selectPage(userPage, wrapper);
            
            // 转换为DTO
            Page<UserInfoDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
            
            List<UserInfoDTO> records = result.getRecords().stream().map(user -> {
                UserInfoDTO dto = new UserInfoDTO();
                BeanUtils.copyProperties(user, dto);
                return dto;
            }).collect(Collectors.toList());
            
            dtoPage.setRecords(records);
            
            log.info("获取用户列表成功, 总数: {}", dtoPage.getTotal());
            return dtoPage;
        } catch (Exception e) {
            log.error("获取用户列表失败: {}", e.getMessage(), e);
            throw new BusinessException("获取用户列表失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean updateUserStatus(Long userId, String status) {
        log.info("更新用户状态, 用户ID: {}, 新状态: {}", userId, status);
        
        try {
            // 检查用户是否存在
            User user = userMapper.selectById(userId);
            if (user == null || user.getIsDeleted()) {
                log.warn("用户不存在, ID: {}", userId);
                throw new BusinessException("用户不存在");
            }
            
            // 使用UpdateWrapper进行更新
            com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<User> updateWrapper = 
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
            updateWrapper.eq("id", userId)
                        .set("status", status)
                        .set("update_time", LocalDateTime.now());
            
            int result = userMapper.update(null, updateWrapper);
            
            if (result > 0) {
                log.info("更新用户状态成功, 用户ID: {}", userId);
                return true;
            } else {
                log.warn("更新用户状态失败, 用户ID: {}", userId);
                return false;
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新用户状态过程中发生异常: {}", e.getMessage(), e);
            throw new BusinessException("更新用户状态失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean deleteUser(Long userId) {
        log.info("删除用户, 用户ID: {}", userId);
        
        try {
            // 检查用户是否存在
            User user = userMapper.selectById(userId);
            if (user == null || user.getIsDeleted()) {
                log.warn("用户不存在, ID: {}", userId);
                throw new BusinessException("用户不存在");
            }
            
            // 方式1：使用UpdateWrapper
            com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<User> updateWrapper = 
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
            updateWrapper.eq("id", userId)
                        .set("is_deleted", 1)
                        .set("update_time", LocalDateTime.now());
            
            int result = userMapper.update(null, updateWrapper);
            
            if (result > 0) {
                log.info("删除用户成功, 用户ID: {}", userId);
                return true;
            } else {
                log.warn("删除用户失败, 用户ID: {}", userId);
                return false;
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除用户过程中发生异常: {}", e.getMessage(), e);
            throw new BusinessException("删除用户失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean resetPassword(Long userId, String newPassword) {
        log.info("重置用户密码, 用户ID: {}", userId);
        
        try {
            // 检查用户是否存在
            User user = userMapper.selectById(userId);
            if (user == null || user.getIsDeleted()) {
                log.warn("用户不存在, ID: {}", userId);
                throw new BusinessException("用户不存在");
            }
            
            // 重置密码
            user.setPassword(newPassword); // 实际项目中应该加密存储
            user.setUpdateTime(LocalDateTime.now());
            
            int result = userMapper.updateById(user);
            
            if (result > 0) {
                log.info("重置用户密码成功, 用户ID: {}", userId);
                return true;
            } else {
                log.warn("重置用户密码失败, 用户ID: {}", userId);
                return false;
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("重置用户密码过程中发生异常: {}", e.getMessage(), e);
            throw new BusinessException("重置用户密码失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean updateUserInfo(UserInfoDTO userInfoDTO) {
        log.info("更新用户信息, 用户ID: {}", userInfoDTO.getId());
        
        try {
            // 检查用户是否存在
            User user = userMapper.selectById(userInfoDTO.getId());
            if (user == null || user.getIsDeleted()) {
                log.warn("用户不存在, ID: {}", userInfoDTO.getId());
                throw new BusinessException("用户不存在");
            }
            
            // 使用UpdateWrapper进行更新
            com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<User> updateWrapper = 
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
            updateWrapper.eq("id", userInfoDTO.getId());
            
            // 设置要更新的字段
            updateWrapper.set("nickname", userInfoDTO.getNickname());
            
            if (userInfoDTO.getAvatar() != null && !userInfoDTO.getAvatar().isEmpty()) {
                log.info("更新用户头像, 用户ID: {}, 旧头像: {}, 新头像: {}", 
                    userInfoDTO.getId(), user.getAvatar(), userInfoDTO.getAvatar());
                updateWrapper.set("avatar", userInfoDTO.getAvatar());
            }
            
            if (userInfoDTO.getRealName() != null) {
                log.info("更新用户真实姓名, 用户ID: {}, 旧姓名: {}, 新姓名: {}", 
                    userInfoDTO.getId(), user.getRealName(), userInfoDTO.getRealName());
                updateWrapper.set("real_name", userInfoDTO.getRealName());
            }
            
            if (userInfoDTO.getIdCard() != null) {
                log.info("更新用户身份证号, 用户ID: {}", userInfoDTO.getId());
                updateWrapper.set("id_card", userInfoDTO.getIdCard());
            }
            
            updateWrapper.set("phone", userInfoDTO.getPhone());
            updateWrapper.set("email", userInfoDTO.getEmail());
            updateWrapper.set("description", userInfoDTO.getDescription());
            updateWrapper.set("update_time", LocalDateTime.now());
            
            int result = userMapper.update(null, updateWrapper);
            
            if (result > 0) {
                log.info("更新用户信息成功, 用户ID: {}", userInfoDTO.getId());
                return true;
            } else {
                log.warn("更新用户信息失败, 用户ID: {}", userInfoDTO.getId());
                return false;
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新用户信息过程中发生异常: {}", e.getMessage(), e);
            throw new BusinessException("更新用户信息失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("修改用户密码, 用户ID: {}", userId);
        
        try {
            // 检查用户是否存在
            User user = userMapper.selectById(userId);
            if (user == null || user.getIsDeleted()) {
                log.warn("用户不存在, ID: {}", userId);
                throw new BusinessException("用户不存在");
            }
            
            // 验证原密码是否正确
            if (!user.getPassword().equals(oldPassword)) { // 实际项目中应该使用加密方式验证
                log.warn("原密码错误，用户ID: {}", userId);
                throw new BusinessException("原密码错误");
            }
            
            // 修改密码
            user.setPassword(newPassword); // 实际项目中应该加密存储
            user.setUpdateTime(LocalDateTime.now());
            
            int result = userMapper.updateById(user);
            
            if (result > 0) {
                log.info("修改用户密码成功, 用户ID: {}", userId);
                return true;
            } else {
                log.warn("修改用户密码失败, 用户ID: {}", userId);
                return false;
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("修改用户密码过程中发生异常: {}", e.getMessage(), e);
            throw new BusinessException("修改用户密码失败：" + e.getMessage());
        }
    }
} 