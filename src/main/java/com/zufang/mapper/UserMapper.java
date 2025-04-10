package com.zufang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zufang.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 查询用户类型
     * @param userId 用户ID
     * @return 用户类型
     */
    @Select("SELECT role FROM user WHERE id = #{userId} AND is_deleted = 0")
    String getUserType(Long userId);
    
    /**
     * 检查用户是否为管理员
     * @param userId 用户ID
     * @return 是否为管理员
     */
    @Select("SELECT COUNT(*) > 0 FROM user WHERE id = #{userId} AND role = 'ADMIN' AND is_deleted = 0")
    boolean isAdmin(Long userId);
    
    /**
     * 检查用户是否为房东
     * @param userId 用户ID
     * @return 是否为房东
     */
    @Select("SELECT COUNT(*) > 0 FROM user WHERE id = #{userId} AND role = 'LANDLORD' AND is_deleted = 0")
    boolean isLandlord(Long userId);
} 