package com.zufang.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zufang.common.Result;
import com.zufang.service.SystemService;
import com.zufang.entity.SystemSetting;
import com.zufang.dto.FeatureToggleDTO;

import java.util.Map;

/**
 * 系统设置控制器
 */
@RestController
@RequestMapping("/admin/system")
public class SystemController {
    private static final Logger logger = LoggerFactory.getLogger(SystemController.class);
    
    @Autowired
    private SystemService systemService;
    
    /**
     * 获取系统设置
     */
    @GetMapping("/settings")
    public Result getSystemSettings() {
        try {
            logger.info("获取系统设置");
            SystemSetting settings = systemService.getSystemSettings();
            return Result.success(settings);
        } catch (Exception e) {
            logger.error("获取系统设置失败", e);
            return Result.error("获取系统设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新系统设置
     */
    @PostMapping("/settings")
    public Result updateSystemSettings(@RequestBody SystemSetting settings) {
        try {
            logger.info("更新系统设置: {}", settings);
            systemService.updateSystemSettings(settings);
            return Result.success("更新系统设置成功");
        } catch (Exception e) {
            logger.error("更新系统设置失败", e);
            return Result.error("更新系统设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取功能开关设置
     */
    @GetMapping("/features")
    public Result getFeatureToggles() {
        try {
            logger.info("获取功能开关设置");
            FeatureToggleDTO features = systemService.getFeatureToggles();
            return Result.success(features);
        } catch (Exception e) {
            logger.error("获取功能开关设置失败", e);
            return Result.error("获取功能开关设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新功能开关设置
     */
    @PostMapping("/features")
    public Result updateFeatureToggles(@RequestBody FeatureToggleDTO features) {
        try {
            logger.info("更新功能开关设置: {}", features);
            systemService.updateFeatureToggles(features);
            return Result.success("更新功能开关设置成功");
        } catch (Exception e) {
            logger.error("更新功能开关设置失败", e);
            return Result.error("更新功能开关设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 清除缓存
     */
    @PostMapping("/cache/clear")
    public Result clearCache(@RequestBody Map<String, String> params) {
        try {
            String cacheType = params.get("type");
            logger.info("清除缓存: {}", cacheType);
            systemService.clearCache(cacheType);
            return Result.success("清除缓存成功");
        } catch (Exception e) {
            logger.error("清除缓存失败", e);
            return Result.error("清除缓存失败: " + e.getMessage());
        }
    }
    
    /**
     * 数据库备份
     */
    @PostMapping("/backup")
    public Result backupDatabase() {
        try {
            logger.info("开始数据库备份");
            String backupPath = systemService.backupDatabase();
            return Result.success("数据库备份成功，备份路径: " + backupPath);
        } catch (Exception e) {
            logger.error("数据库备份失败", e);
            return Result.error("数据库备份失败: " + e.getMessage());
        }
    }
    
    /**
     * 清除过期数据
     */
    @PostMapping("/clean-expired")
    public Result cleanExpiredData() {
        try {
            logger.info("开始清除过期数据");
            int count = systemService.cleanExpiredData();
            return Result.success("清除过期数据成功，共删除 " + count + " 条记录");
        } catch (Exception e) {
            logger.error("清除过期数据失败", e);
            return Result.error("清除过期数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取系统日志
     */
    @GetMapping("/logs")
    public Result getSystemLogs(@RequestParam(value = "page", defaultValue = "1") Integer page,
                              @RequestParam(value = "size", defaultValue = "20") Integer size,
                              @RequestParam(value = "level", required = false) String level,
                              @RequestParam(value = "startDate", required = false) String startDate,
                              @RequestParam(value = "endDate", required = false) String endDate) {
        try {
            logger.info("获取系统日志: page={}, size={}, level={}, startDate={}, endDate={}", 
                    page, size, level, startDate, endDate);
            return Result.success(systemService.getSystemLogs(page, size, level, startDate, endDate));
        } catch (Exception e) {
            logger.error("获取系统日志失败", e);
            return Result.error("获取系统日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 系统监控信息
     */
    @GetMapping("/monitor")
    public Result getSystemMonitor() {
        try {
            logger.info("获取系统监控信息");
            return Result.success(systemService.getSystemMonitor());
        } catch (Exception e) {
            logger.error("获取系统监控信息失败", e);
            return Result.error("获取系统监控信息失败: " + e.getMessage());
        }
    }
} 