package com.zufang.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.zufang.service.OrderService;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
    
    @Autowired
    private OrderService orderService;

    /**
     * 定时检查并修复订单和房源状态不一致的问题
     * 每天凌晨3点执行一次
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void fixInconsistentStatus() {
        log.info("开始执行定时任务：检查并修复状态不一致的订单和房源...");
        try {
            int fixedCount = orderService.checkAndFixInconsistentStatus();
            log.info("定时修复状态不一致任务完成，共修复{}条记录", fixedCount);
        } catch (Exception e) {
            log.error("定时修复状态不一致任务执行失败: {}", e.getMessage(), e);
        }
    }
} 