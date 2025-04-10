package com.zufang.controller;

import com.zufang.common.FileUtil;
import com.zufang.common.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/upload")
public class FileUploadController {

    @Autowired
    private FileUtil fileUtil;

    /**
     * 测试接口
     */
    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("文件上传服务正常");
    }

    /**
     * 上传单个文件
     */
    @PostMapping("/file")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.fail("文件为空");
            }
            
            String url = fileUtil.uploadFile(file, "test");
            log.info("文件上传成功: {}", url);
            
            return Result.success(url);
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            return Result.fail("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传多个文件
     */
    @PostMapping("/files")
    public Result<List<String>> uploadFiles(@RequestParam("files") List<MultipartFile> files) {
        try {
            if (files == null || files.isEmpty()) {
                return Result.fail("文件列表为空");
            }
            
            List<String> urls = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String url = fileUtil.uploadFile(file, "test");
                    urls.add(url);
                }
            }
            
            log.info("多文件上传成功，数量: {}", urls.size());
            return Result.success(urls);
        } catch (Exception e) {
            log.error("多文件上传失败: {}", e.getMessage(), e);
            return Result.fail("多文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 混合表单提交测试（包含文件和普通字段）
     */
    @PostMapping("/form")
    public Result<Map<String, Object>> uploadForm(
            @RequestParam("name") String name,
            @RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("name", name);
            
            if (!file.isEmpty()) {
                String url = fileUtil.uploadFile(file, "form");
                result.put("fileUrl", url);
            }
            
            log.info("表单上传成功: {}", result);
            return Result.success(result);
        } catch (Exception e) {
            log.error("表单上传失败: {}", e.getMessage(), e);
            return Result.fail("表单上传失败: " + e.getMessage());
        }
    }
} 