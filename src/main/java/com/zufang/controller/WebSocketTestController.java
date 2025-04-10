package com.zufang.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket测试控制器
 */
@RestController
@RequestMapping("/api/ws-test")
@Slf4j
public class WebSocketTestController {

    /**
     * 测试WebSocket是否可访问
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        log.info("收到WebSocket状态测试请求");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "up");
        response.put("message", "WebSocket服务正在运行");
        response.put("endpoint", "/websocket");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 输出WebSocket配置信息
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        log.info("收到WebSocket配置信息请求");
        
        Map<String, Object> config = new HashMap<>();
        config.put("websocketEndpoint", "/websocket");
        config.put("serverPort", 8080);
        config.put("enableDebug", true);
        config.put("connectTimeout", 30000);
        
        return ResponseEntity.ok(config);
    }
} 