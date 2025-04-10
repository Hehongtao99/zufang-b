package com.zufang.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zufang.common.response.Result;
import com.zufang.entity.User;
import com.zufang.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试控制器
 */
@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private UserMapper userMapper;
    
    @Value("${spring.datasource.url}")
    private String datasourceUrl;
    
    /**
     * 测试接口，返回用户信息
     */
    @GetMapping("/user-info")
    public Result<Map<String, Object>> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        String role = (String) request.getAttribute("role");
        
        log.info("测试接口 - 用户信息, userId: {}, username: {}, role: {}", userId, username, role);
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", userId);
        userInfo.put("username", username);
        userInfo.put("role", role);
        
        return Result.success(userInfo);
    }
    
    /**
     * 数据库测试接口
     */
    @GetMapping("/db")
    public Result<String> testDb() {
        return Result.success("数据库连接测试成功");
    }
    
    /**
     * 系统信息测试接口
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> testInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("osName", System.getProperty("os.name"));
        info.put("osVersion", System.getProperty("os.version"));
        
        return Result.success(info);
    }
} 