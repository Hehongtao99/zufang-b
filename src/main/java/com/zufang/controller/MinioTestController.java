package com.zufang.controller;

import com.zufang.common.response.Result;
import com.zufang.service.MinioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * MinIO测试控制器
 */
@Slf4j
@RestController
@RequestMapping("/minio")
public class MinioTestController {
    
    @Autowired
    private MinioService minioService;
    
    /**
     * 测试MinIO上传
     */
    @PostMapping("/upload")
    public Result<String> uploadTest(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = minioService.uploadFileToDirectory(file, "test");
            log.info("测试MinIO上传成功: {}", fileUrl);
            return Result.success(fileUrl);
        } catch (Exception e) {
            log.error("测试MinIO上传失败: {}", e.getMessage(), e);
            return Result.fail("上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试MinIO删除
     */
    @DeleteMapping("/{objectName}")
    public Result<Boolean> deleteTest(@PathVariable String objectName) {
        try {
            boolean result = minioService.deleteFile(objectName);
            log.info("测试MinIO删除结果: {}", result);
            return Result.success(result);
        } catch (Exception e) {
            log.error("测试MinIO删除失败: {}", e.getMessage(), e);
            return Result.fail("删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试MinIO是否存在
     */
    @GetMapping("/exists/{objectName}")
    public Result<Boolean> existsTest(@PathVariable String objectName) {
        try {
            boolean result = minioService.isObjectExist(objectName);
            log.info("测试MinIO对象是否存在: {}", result);
            return Result.success(result);
        } catch (Exception e) {
            log.error("测试MinIO对象是否存在失败: {}", e.getMessage(), e);
            return Result.fail("检查失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试MinIO信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> infoTest() {
        Map<String, Object> info = new HashMap<>();
        info.put("enabled", true);
        info.put("endpoint", "http://113.45.161.48:9000");
        info.put("bucket", "zufang");
        info.put("externalUrl", "http://113.45.161.48:9000/zufang/");
        return Result.success(info);
    }
} 