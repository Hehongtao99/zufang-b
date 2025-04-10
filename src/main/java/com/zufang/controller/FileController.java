package com.zufang.controller;

import com.zufang.common.FileUtil;
import com.zufang.common.response.Result;
import com.zufang.service.MinioService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传下载控制器
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {
    
    @Autowired
    private FileUtil fileUtil;
    
    @Autowired
    private MinioService minioService;
    
    // 获取项目根目录
    private final String projectRootPath = System.getProperty("user.dir");
    
    // 上传文件存储的目录
    private final String UPLOAD_DIR = projectRootPath + "/uploads";
    
    /**
     * 获取上传路径
     */
    private String getUploadPath() {
        return UPLOAD_DIR;
    }
    
    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file, 
                                    @RequestParam(value = "type", defaultValue = "common") String type) {
        try {
            String fileUrl = fileUtil.uploadFile(file, type);
            if (fileUrl != null) {
                return Result.success(fileUrl);
            } else {
                return Result.fail("上传文件失败");
            }
        } catch (Exception e) {
            log.error("上传文件失败: {}", e.getMessage(), e);
            return Result.fail("上传文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除文件
     */
    @DeleteMapping("/{url}")
    public Result<Boolean> deleteFile(@PathVariable String url) {
        try {
            boolean result = fileUtil.deleteFile(url);
            if (result) {
                return Result.success(true);
            } else {
                return Result.fail("删除文件失败");
            }
        } catch (Exception e) {
            log.error("删除文件失败: {}", e.getMessage(), e);
            return Result.fail("删除文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 文件下载接口
     * @param filename 文件名
     * @return 文件资源
     */
    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            // 构建文件路径
            String filePath = UPLOAD_DIR + "/images/" + filename;
            File file = new File(filePath);
            
            // 检查文件是否存在
            if (!file.exists()) {
                // 尝试在子目录中查找
                File houseFile = new File(UPLOAD_DIR + "/images/house/" + filename);
                if (houseFile.exists()) {
                    file = houseFile;
                } else {
                    log.warn("文件不存在: {}", filePath);
                    return ResponseEntity.notFound().build();
                }
            }
            
            // 获取文件MIME类型
            String contentType = Files.probeContentType(Paths.get(file.getAbsolutePath()));
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            // 创建资源
            Resource resource = new FileSystemResource(file);
            
            // 返回文件
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                    .body(resource);
        } catch (IOException e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 检查文件是否存在
     * 支持通过文件名检查
     */
    @GetMapping("/check")
    public Result<Boolean> checkFileExists(@RequestParam(required = false) String url,
                                          @RequestParam(required = false) String fileName) {
        log.info("检查文件是否存在: url={}, fileName={}", url, fileName);
        
        try {
            String fileNameToCheck = fileName;
            
            // 如果提供了URL而不是fileName，从URL中提取文件名
            if (StringUtils.isBlank(fileNameToCheck) && StringUtils.isNotBlank(url)) {
                // 处理URL，提取文件名
                log.info("处理后的URL: {}", url);
                String[] parts = url.split("/");
                fileNameToCheck = parts[parts.length - 1];
                log.info("从URL提取的文件名: {}", fileNameToCheck);
            }
            
            if (StringUtils.isBlank(fileNameToCheck)) {
                return Result.error("未提供有效的文件名或URL");
            }
            
            // 优先检查MinIO
            if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                // 先尝试直接用URL检查MinIO
                boolean existsInMinio = minioService.fileExists(minioService.getObjectNameFromUrl(url));
                log.info("文件是否存在于MinIO (完整URL路径): {}", existsInMinio);
                
                if (existsInMinio) {
                    return Result.success(true);
                }
            }
            
            // 检查MinIO中的文件
            boolean existsInMinio = minioService.checkFileExists(fileNameToCheck);
            log.info("文件是否存在于MinIO (仅文件名): {}", existsInMinio);
            
            if (existsInMinio) {
                return Result.success(true);
            }

            // 尝试检查文件在MinIO的house目录
            boolean existsInMinioHouse = minioService.fileExists("house/" + fileNameToCheck);
            log.info("文件是否存在于MinIO的house目录: {}", existsInMinioHouse);
            
            if (existsInMinioHouse) {
                return Result.success(true);
            }
            
            // 尝试检查文件在MinIO的images/house目录
            boolean existsInMinioImagesHouse = minioService.fileExists("images/house/" + fileNameToCheck);
            log.info("文件是否存在于MinIO的images/house目录: {}", existsInMinioImagesHouse);
            
            if (existsInMinioImagesHouse) {
                return Result.success(true);
            }
            
            // 检查本地文件系统（作为备用选项）
            // 检查主图片目录
            String mainImagePath = getUploadPath() + File.separator + "images" + File.separator + fileNameToCheck;
            boolean existsInMainDir = new File(mainImagePath).exists();
            log.info("文件是否存在于主图片目录: {}, 路径: {}", existsInMainDir, mainImagePath);
            
            if (existsInMainDir) {
                return Result.success(true);
            }
            
            // 检查房源图片目录
            String houseImagePath = getUploadPath() + File.separator + "images" + File.separator + "house" + File.separator + fileNameToCheck;
            boolean existsInHouseDir = new File(houseImagePath).exists();
            log.info("文件是否存在于房源图片目录: {}, 路径: {}", existsInHouseDir, houseImagePath);
            
            if (existsInHouseDir) {
                return Result.success(true);
            }
            
            log.info("文件 {} 检查结果: false", fileNameToCheck);
            return Result.success(false);
        } catch (Exception e) {
            log.error("检查文件是否存在时出错", e);
            return Result.error("检查文件状态失败");
        }
    }
} 