package com.zufang.service;

import com.zufang.entity.SystemSetting;
import com.zufang.dto.FeatureToggleDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Map;

/**
 * 系统设置服务接口
 */
public interface SystemService {
    
    /**
     * 获取系统设置
     * @return 系统设置
     */
    SystemSetting getSystemSettings();
    
    /**
     * 更新系统设置
     * @param settings 系统设置
     */
    void updateSystemSettings(SystemSetting settings);
    
    /**
     * 获取功能开关设置
     * @return 功能开关设置
     */
    FeatureToggleDTO getFeatureToggles();
    
    /**
     * 更新功能开关设置
     * @param features 功能开关设置
     */
    void updateFeatureToggles(FeatureToggleDTO features);
    
    /**
     * 清除缓存
     * @param cacheType 缓存类型: house, order, user, all
     */
    void clearCache(String cacheType);
    
    /**
     * 数据库备份
     * @return 备份文件路径
     */
    String backupDatabase();
    
    /**
     * 清除过期数据
     * @return 清除的记录数
     */
    int cleanExpiredData();
    
    /**
     * 获取系统日志
     * @param page 页码
     * @param size 每页大小
     * @param level 日志级别
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日志分页数据
     */
    Page<Map<String, Object>> getSystemLogs(Integer page, Integer size, String level, String startDate, String endDate);
    
    /**
     * 获取系统监控信息
     * @return 系统监控信息
     */
    Map<String, Object> getSystemMonitor();
} 