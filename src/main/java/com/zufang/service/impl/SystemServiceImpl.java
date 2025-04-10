package com.zufang.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zufang.entity.SystemSetting;
import com.zufang.entity.FeatureToggle;
import com.zufang.dto.FeatureToggleDTO;
import com.zufang.mapper.SystemSettingMapper;
import com.zufang.mapper.FeatureToggleMapper;
import com.zufang.service.SystemService;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;

/**
 * 系统设置服务实现
 */
@Service
public class SystemServiceImpl implements SystemService {
    private static final Logger logger = LoggerFactory.getLogger(SystemServiceImpl.class);
    
    @Autowired
    private SystemSettingMapper systemSettingMapper;
    
    @Autowired
    private FeatureToggleMapper featureToggleMapper;
    
    @Autowired(required = false)
    private CacheManager cacheManager;
    
    @Value("${spring.datasource.url}")
    private String dbUrl;
    
    @Value("${spring.datasource.username}")
    private String dbUsername;
    
    @Value("${spring.datasource.password}")
    private String dbPassword;
    
    @Value("${zufang.backup.path:./backup}")
    private String backupPath;
    
    @Value("${zufang.log.path:./logs}")
    private String logPath;
    
    @Override
    public SystemSetting getSystemSettings() {
        logger.info("获取系统设置");
        SystemSetting settings = systemSettingMapper.selectOne(
                new LambdaQueryWrapper<SystemSetting>().orderByDesc(SystemSetting::getId).last("LIMIT 1"));
        
        if (settings == null) {
            // 如果没有设置，创建默认设置
            settings = createDefaultSettings();
        }
        
        return settings;
    }
    
    @Override
    @Transactional
    public void updateSystemSettings(SystemSetting settings) {
        logger.info("更新系统设置: {}", settings);
        
        // 检查是否存在设置
        SystemSetting existingSetting = systemSettingMapper.selectOne(
                new LambdaQueryWrapper<SystemSetting>().orderByDesc(SystemSetting::getId).last("LIMIT 1"));
        
        if (existingSetting != null) {
            // 更新现有设置
            settings.setId(existingSetting.getId());
            settings.setUpdateTime(LocalDateTime.now());
            systemSettingMapper.updateById(settings);
        } else {
            // 创建新设置
            settings.setCreateTime(LocalDateTime.now());
            settings.setUpdateTime(LocalDateTime.now());
            systemSettingMapper.insert(settings);
        }
    }
    
    @Override
    public FeatureToggleDTO getFeatureToggles() {
        logger.info("获取功能开关设置");
        List<FeatureToggle> toggles = featureToggleMapper.selectList(new LambdaQueryWrapper<>());
        
        if (toggles.isEmpty()) {
            // 如果没有设置，创建默认设置
            toggles = createDefaultFeatureToggles();
        }
        
        // 转换为DTO
        FeatureToggleDTO dto = new FeatureToggleDTO();
        for (FeatureToggle toggle : toggles) {
            switch (toggle.getFeatureKey()) {
                case "register":
                    dto.setEnableRegister(toggle.getEnabled());
                    break;
                case "house_publish":
                    dto.setEnableHousePublish(toggle.getEnabled());
                    break;
                case "appointment":
                    dto.setEnableAppointment(toggle.getEnabled());
                    break;
                case "order":
                    dto.setEnableOrder(toggle.getEnabled());
                    break;
                case "contract":
                    dto.setEnableContract(toggle.getEnabled());
                    break;
                case "notification":
                    dto.setEnableNotification(toggle.getEnabled());
                    break;
                default:
                    break;
            }
        }
        
        return dto;
    }
    
    @Override
    @Transactional
    public void updateFeatureToggles(FeatureToggleDTO features) {
        logger.info("更新功能开关设置: {}", features);
        
        // 获取所有现有功能开关
        List<FeatureToggle> existingToggles = featureToggleMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, FeatureToggle> toggleMap = existingToggles.stream()
                .collect(Collectors.toMap(FeatureToggle::getFeatureKey, toggle -> toggle));
        
        // 更新功能开关
        updateToggle(toggleMap, "register", "用户注册", features.getEnableRegister());
        updateToggle(toggleMap, "house_publish", "房源发布", features.getEnableHousePublish());
        updateToggle(toggleMap, "appointment", "房源预约", features.getEnableAppointment());
        updateToggle(toggleMap, "order", "订单功能", features.getEnableOrder());
        updateToggle(toggleMap, "contract", "合同功能", features.getEnableContract());
        updateToggle(toggleMap, "notification", "消息通知", features.getEnableNotification());
    }
    
    private void updateToggle(Map<String, FeatureToggle> toggleMap, String key, String name, Boolean enabled) {
        if (toggleMap.containsKey(key)) {
            // 更新现有功能开关
            FeatureToggle toggle = toggleMap.get(key);
            toggle.setEnabled(enabled);
            toggle.setUpdateTime(LocalDateTime.now());
            featureToggleMapper.updateById(toggle);
        } else {
            // 创建新功能开关
            FeatureToggle toggle = new FeatureToggle();
            toggle.setFeatureKey(key);
            toggle.setFeatureName(name);
            toggle.setEnabled(enabled);
            toggle.setDescription("功能开关: " + name);
            toggle.setCreateTime(LocalDateTime.now());
            toggle.setUpdateTime(LocalDateTime.now());
            featureToggleMapper.insert(toggle);
        }
    }
    
    @Override
    public void clearCache(String cacheType) {
        logger.info("清除缓存: {}", cacheType);
        
        if (cacheManager == null) {
            logger.warn("缓存管理器未配置");
            return;
        }
        
        switch (cacheType) {
            case "house":
                cacheManager.getCache("houseCache").clear();
                break;
            case "order":
                cacheManager.getCache("orderCache").clear();
                break;
            case "user":
                cacheManager.getCache("userCache").clear();
                break;
            case "all":
                for (String cacheName : cacheManager.getCacheNames()) {
                    cacheManager.getCache(cacheName).clear();
                }
                break;
            default:
                logger.warn("未知的缓存类型: {}", cacheType);
        }
    }
    
    @Override
    public String backupDatabase() {
        logger.info("开始数据库备份");
        
        try {
            // 创建备份目录
            File backupDir = new File(backupPath);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }
            
            // 提取数据库名称
            String dbName = extractDatabaseName(dbUrl);
            
            // 生成备份文件名
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = dbName + "_" + timestamp + ".sql";
            String backupFilePath = backupPath + File.separator + backupFileName;
            
            // 构建备份命令
            String[] command = buildBackupCommand(dbName, backupFilePath);
            
            // 执行备份命令
            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                logger.info("数据库备份成功: {}", backupFilePath);
                return backupFilePath;
            } else {
                String errorOutput = new BufferedReader(new InputStreamReader(process.getErrorStream()))
                        .lines().collect(Collectors.joining("\n"));
                logger.error("数据库备份失败，错误码: {}, 错误信息: {}", exitCode, errorOutput);
                throw new RuntimeException("数据库备份失败: " + errorOutput);
            }
        } catch (Exception e) {
            logger.error("数据库备份出错", e);
            throw new RuntimeException("数据库备份出错: " + e.getMessage());
        }
    }
    
    private String extractDatabaseName(String jdbcUrl) {
        // 从JDBC URL中提取数据库名称
        int lastSlashIndex = jdbcUrl.lastIndexOf('/');
        if (lastSlashIndex > 0) {
            String dbNameWithParams = jdbcUrl.substring(lastSlashIndex + 1);
            int paramIndex = dbNameWithParams.indexOf('?');
            if (paramIndex > 0) {
                return dbNameWithParams.substring(0, paramIndex);
            }
            return dbNameWithParams;
        }
        throw new RuntimeException("无法从JDBC URL中提取数据库名称: " + jdbcUrl);
    }
    
    private String[] buildBackupCommand(String dbName, String backupFilePath) {
        return new String[] {
            "mysqldump",
            "--host=" + parseHostFromUrl(dbUrl),
            "--port=" + parsePortFromUrl(dbUrl),
            "--user=" + dbUsername,
            "--password=" + dbPassword,
            "--add-drop-database",
            "--databases",
            dbName,
            "--result-file=" + backupFilePath
        };
    }
    
    private String parseHostFromUrl(String jdbcUrl) {
        // 从JDBC URL中提取主机名
        int hostStartIndex = jdbcUrl.indexOf("://") + 3;
        int hostEndIndex = jdbcUrl.indexOf(":", hostStartIndex);
        if (hostEndIndex > hostStartIndex) {
            return jdbcUrl.substring(hostStartIndex, hostEndIndex);
        }
        hostEndIndex = jdbcUrl.indexOf("/", hostStartIndex);
        if (hostEndIndex > hostStartIndex) {
            return jdbcUrl.substring(hostStartIndex, hostEndIndex);
        }
        return "localhost";
    }
    
    private int parsePortFromUrl(String jdbcUrl) {
        // 从JDBC URL中提取端口号
        int portStartIndex = jdbcUrl.indexOf(":", jdbcUrl.indexOf("://") + 3) + 1;
        if (portStartIndex > 0) {
            int portEndIndex = jdbcUrl.indexOf("/", portStartIndex);
            if (portEndIndex > portStartIndex) {
                try {
                    return Integer.parseInt(jdbcUrl.substring(portStartIndex, portEndIndex));
                } catch (NumberFormatException e) {
                    logger.warn("解析端口号失败，使用默认端口3306", e);
                }
            }
        }
        return 3306;
    }
    
    @Override
    @Transactional
    public int cleanExpiredData() {
        logger.info("开始清除过期数据");
        
        int totalCount = 0;
        
        try {
            // 假设清除一年前的数据
            LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
            
            // 在这里添加清除逻辑，例如：
            // 清除过期的日志数据
            // int logCount = logMapper.deleteExpiredLogs(oneYearAgo);
            // totalCount += logCount;
            
            // 清除过期的临时数据
            // int tempCount = tempDataMapper.deleteExpiredData(oneYearAgo);
            // totalCount += tempCount;
            
            logger.info("清除过期数据完成，共删除 {} 条记录", totalCount);
            return totalCount;
            
        } catch (Exception e) {
            logger.error("清除过期数据失败", e);
            throw new RuntimeException("清除过期数据失败: " + e.getMessage());
        }
    }
    
    @Override
    public Page<Map<String, Object>> getSystemLogs(Integer page, Integer size, String level, String startDate, String endDate) {
        logger.info("获取系统日志: page={}, size={}, level={}, startDate={}, endDate={}", 
                page, size, level, startDate, endDate);
        
        try {
            Page<Map<String, Object>> result = new Page<>(page, size);
            List<Map<String, Object>> logsList = new ArrayList<>();
            
            // 检查日志目录是否存在
            File logDir = new File(logPath);
            if (!logDir.exists() || !logDir.isDirectory()) {
                logger.warn("日志目录不存在: {}", logPath);
                return result;
            }
            
            // 获取所有日志文件
            List<File> logFiles = getLogFiles(logDir, startDate, endDate);
            
            // 处理分页
            int start = (page - 1) * size;
            int end = Math.min(start + size, getTotalLogLines(logFiles, level));
            
            // 读取日志
            int currentLine = 0;
            for (File logFile : logFiles) {
                List<String> lines = Files.readAllLines(logFile.toPath());
                for (String line : lines) {
                    if (matchesLogLevel(line, level)) {
                        if (currentLine >= start && currentLine < end) {
                            Map<String, Object> logEntry = parseLogLine(line);
                            logEntry.put("file", logFile.getName());
                            logsList.add(logEntry);
                        }
                        currentLine++;
                        if (currentLine >= end) {
                            break;
                        }
                    }
                }
                if (currentLine >= end) {
                    break;
                }
            }
            
            result.setRecords(logsList);
            result.setTotal(getTotalLogLines(logFiles, level));
            
            return result;
            
        } catch (Exception e) {
            logger.error("获取系统日志失败", e);
            throw new RuntimeException("获取系统日志失败: " + e.getMessage());
        }
    }
    
    private List<File> getLogFiles(File logDir, String startDate, String endDate) {
        List<File> result = new ArrayList<>();
        File[] files = logDir.listFiles((dir, name) -> name.endsWith(".log"));
        
        if (files == null) {
            return result;
        }
        
        for (File file : files) {
            // 如果指定了日期范围，则过滤日志文件
            if (startDate != null && endDate != null) {
                LocalDate fileDate = extractDateFromLogFileName(file.getName());
                if (fileDate != null) {
                    LocalDate start = LocalDate.parse(startDate);
                    LocalDate end = LocalDate.parse(endDate);
                    
                    if (fileDate.isBefore(start) || fileDate.isAfter(end)) {
                        continue;
                    }
                }
            }
            
            result.add(file);
        }
        
        return result;
    }
    
    private LocalDate extractDateFromLogFileName(String fileName) {
        try {
            // 假设日志文件名格式为 application-yyyy-MM-dd.log
            int dateStartIndex = fileName.indexOf('-') + 1;
            int dateEndIndex = fileName.lastIndexOf('.');
            
            if (dateStartIndex > 0 && dateEndIndex > dateStartIndex) {
                String dateStr = fileName.substring(dateStartIndex, dateEndIndex);
                return LocalDate.parse(dateStr);
            }
        } catch (Exception e) {
            logger.warn("无法从日志文件名中提取日期: {}", fileName);
        }
        
        return null;
    }
    
    private int getTotalLogLines(List<File> logFiles, String level) {
        int total = 0;
        
        try {
            for (File logFile : logFiles) {
                List<String> lines = Files.readAllLines(logFile.toPath());
                for (String line : lines) {
                    if (matchesLogLevel(line, level)) {
                        total++;
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("计算日志行数时出错", e);
        }
        
        return total;
    }
    
    private boolean matchesLogLevel(String logLine, String level) {
        if (level == null || level.isEmpty()) {
            return true;
        }
        
        String upperLevel = level.toUpperCase();
        return logLine.contains("[" + upperLevel + "]") || logLine.contains(" " + upperLevel + " ");
    }
    
    private Map<String, Object> parseLogLine(String logLine) {
        Map<String, Object> logEntry = new HashMap<>();
        
        try {
            // 假设日志格式：[时间] [级别] [线程] [类] - 消息
            // 例如：2023-06-01 12:34:56.789 [INFO] [main] [com.example.Class] - Log message
            
            // 提取时间
            int timeEnd = logLine.indexOf('[', logLine.indexOf(' '));
            if (timeEnd > 0) {
                logEntry.put("timestamp", logLine.substring(0, timeEnd).trim());
                
                // 提取级别
                int levelStart = logLine.indexOf('[', timeEnd) + 1;
                int levelEnd = logLine.indexOf(']', levelStart);
                if (levelStart > 0 && levelEnd > levelStart) {
                    logEntry.put("level", logLine.substring(levelStart, levelEnd));
                    
                    // 提取线程
                    int threadStart = logLine.indexOf('[', levelEnd) + 1;
                    int threadEnd = logLine.indexOf(']', threadStart);
                    if (threadStart > 0 && threadEnd > threadStart) {
                        logEntry.put("thread", logLine.substring(threadStart, threadEnd));
                        
                        // 提取类
                        int classStart = logLine.indexOf('[', threadEnd) + 1;
                        int classEnd = logLine.indexOf(']', classStart);
                        if (classStart > 0 && classEnd > classStart) {
                            logEntry.put("class", logLine.substring(classStart, classEnd));
                            
                            // 提取消息
                            int messageStart = logLine.indexOf('-', classEnd) + 1;
                            if (messageStart > 0) {
                                logEntry.put("message", logLine.substring(messageStart).trim());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("解析日志行时出错: {}", logLine, e);
        }
        
        // 如果解析失败，保存原始日志行
        if (logEntry.isEmpty()) {
            logEntry.put("rawLog", logLine);
        }
        
        return logEntry;
    }
    
    @Override
    public Map<String, Object> getSystemMonitor() {
        logger.info("获取系统监控信息");
        
        Map<String, Object> monitorInfo = new HashMap<>();
        
        try {
            // 操作系统信息
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            Map<String, Object> osInfo = new HashMap<>();
            osInfo.put("name", osBean.getName());
            osInfo.put("version", osBean.getVersion());
            osInfo.put("arch", osBean.getArch());
            osInfo.put("availableProcessors", osBean.getAvailableProcessors());
            osInfo.put("systemLoadAverage", osBean.getSystemLoadAverage());
            monitorInfo.put("os", osInfo);
            
            // 内存信息
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            Map<String, Object> memoryInfo = new HashMap<>();
            
            Map<String, Object> heapInfo = new HashMap<>();
            heapInfo.put("init", memoryBean.getHeapMemoryUsage().getInit());
            heapInfo.put("used", memoryBean.getHeapMemoryUsage().getUsed());
            heapInfo.put("committed", memoryBean.getHeapMemoryUsage().getCommitted());
            heapInfo.put("max", memoryBean.getHeapMemoryUsage().getMax());
            memoryInfo.put("heap", heapInfo);
            
            Map<String, Object> nonHeapInfo = new HashMap<>();
            nonHeapInfo.put("init", memoryBean.getNonHeapMemoryUsage().getInit());
            nonHeapInfo.put("used", memoryBean.getNonHeapMemoryUsage().getUsed());
            nonHeapInfo.put("committed", memoryBean.getNonHeapMemoryUsage().getCommitted());
            nonHeapInfo.put("max", memoryBean.getNonHeapMemoryUsage().getMax());
            memoryInfo.put("nonHeap", nonHeapInfo);
            
            monitorInfo.put("memory", memoryInfo);
            
            // JVM信息
            Map<String, Object> jvmInfo = new HashMap<>();
            jvmInfo.put("version", System.getProperty("java.version"));
            jvmInfo.put("vendor", System.getProperty("java.vendor"));
            jvmInfo.put("home", System.getProperty("java.home"));
            jvmInfo.put("startTime", ManagementFactory.getRuntimeMXBean().getStartTime());
            jvmInfo.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());
            monitorInfo.put("jvm", jvmInfo);
            
            // 系统属性
            Map<String, Object> systemProps = new HashMap<>();
            systemProps.put("user.dir", System.getProperty("user.dir"));
            systemProps.put("user.name", System.getProperty("user.name"));
            systemProps.put("user.timezone", System.getProperty("user.timezone"));
            monitorInfo.put("systemProperties", systemProps);
            
            return monitorInfo;
            
        } catch (Exception e) {
            logger.error("获取系统监控信息失败", e);
            throw new RuntimeException("获取系统监控信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建默认系统设置
     */
    private SystemSetting createDefaultSettings() {
        logger.info("创建默认系统设置");
        
        SystemSetting settings = new SystemSetting();
        settings.setSystemName("租房网");
        settings.setSystemDescription("专业的线上租房平台，为用户提供便捷、安全的租房服务。");
        settings.setContactPhone("400-123-4567");
        settings.setContactEmail("contact@zufang.com");
        settings.setIcp("京ICP备12345678号");
        settings.setLogoUrl("/logo.png");
        settings.setVersion("1.0.0");
        settings.setCreateTime(LocalDateTime.now());
        settings.setUpdateTime(LocalDateTime.now());
        
        systemSettingMapper.insert(settings);
        return settings;
    }
    
    /**
     * 创建默认功能开关
     */
    private List<FeatureToggle> createDefaultFeatureToggles() {
        logger.info("创建默认功能开关");
        
        List<FeatureToggle> toggles = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        // 用户注册
        FeatureToggle registerToggle = new FeatureToggle();
        registerToggle.setFeatureKey("register");
        registerToggle.setFeatureName("用户注册");
        registerToggle.setEnabled(true);
        registerToggle.setDescription("控制用户是否可以注册新账号");
        registerToggle.setCreateTime(now);
        registerToggle.setUpdateTime(now);
        featureToggleMapper.insert(registerToggle);
        toggles.add(registerToggle);
        
        // 房源发布
        FeatureToggle housePublishToggle = new FeatureToggle();
        housePublishToggle.setFeatureKey("house_publish");
        housePublishToggle.setFeatureName("房源发布");
        housePublishToggle.setEnabled(true);
        housePublishToggle.setDescription("控制房东是否可以发布新房源");
        housePublishToggle.setCreateTime(now);
        housePublishToggle.setUpdateTime(now);
        featureToggleMapper.insert(housePublishToggle);
        toggles.add(housePublishToggle);
        
        // 房源预约
        FeatureToggle appointmentToggle = new FeatureToggle();
        appointmentToggle.setFeatureKey("appointment");
        appointmentToggle.setFeatureName("房源预约");
        appointmentToggle.setEnabled(true);
        appointmentToggle.setDescription("控制用户是否可以预约看房");
        appointmentToggle.setCreateTime(now);
        appointmentToggle.setUpdateTime(now);
        featureToggleMapper.insert(appointmentToggle);
        toggles.add(appointmentToggle);
        
        // 订单功能
        FeatureToggle orderToggle = new FeatureToggle();
        orderToggle.setFeatureKey("order");
        orderToggle.setFeatureName("订单功能");
        orderToggle.setEnabled(true);
        orderToggle.setDescription("控制用户是否可以创建租房订单");
        orderToggle.setCreateTime(now);
        orderToggle.setUpdateTime(now);
        featureToggleMapper.insert(orderToggle);
        toggles.add(orderToggle);
        
        // 合同功能
        FeatureToggle contractToggle = new FeatureToggle();
        contractToggle.setFeatureKey("contract");
        contractToggle.setFeatureName("合同功能");
        contractToggle.setEnabled(true);
        contractToggle.setDescription("控制是否启用合同签署功能");
        contractToggle.setCreateTime(now);
        contractToggle.setUpdateTime(now);
        featureToggleMapper.insert(contractToggle);
        toggles.add(contractToggle);
        
        // 消息通知
        FeatureToggle notificationToggle = new FeatureToggle();
        notificationToggle.setFeatureKey("notification");
        notificationToggle.setFeatureName("消息通知");
        notificationToggle.setEnabled(true);
        notificationToggle.setDescription("控制是否启用消息通知功能");
        notificationToggle.setCreateTime(now);
        notificationToggle.setUpdateTime(now);
        featureToggleMapper.insert(notificationToggle);
        toggles.add(notificationToggle);
        
        return toggles;
    }
} 