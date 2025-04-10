package com.zufang.common;

import com.zufang.service.MinioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件上传工具类
 */
@Slf4j
@Component
public class FileUtil {

    // 获取项目根目录
    private final String projectRootPath = System.getProperty("user.dir");
    
    // 上传文件存储的目录
    private final String UPLOAD_DIR = projectRootPath + "/uploads";
    
    // 图片存储的相对路径
    private static final String IMAGES_PATH = "/images";
    
    // 房源图片存储的相对路径
    private static final String HOUSE_IMAGES_PATH = IMAGES_PATH + "/house";

    @Autowired
    private MinioService minioService;
    
    // 是否使用MinIO存储
    @Value("${minio.enabled:true}")
    private boolean minioEnabled;
    
    /**
     * 构造函数，确保上传目录存在
     */
    public FileUtil() {
        // 确保主上传目录存在
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (created) {
                log.info("Created upload directory: {}", UPLOAD_DIR);
            } else {
                log.error("Failed to create upload directory: {}", UPLOAD_DIR);
            }
        }
        
        // 确保图片上传目录存在
        File imagesDir = new File(UPLOAD_DIR + IMAGES_PATH);
        if (!imagesDir.exists()) {
            boolean created = imagesDir.mkdirs();
            if (created) {
                log.info("Created images directory: {}", UPLOAD_DIR + IMAGES_PATH);
            }
        }
        
        // 确保房源图片目录存在
        File houseImagesDir = new File(UPLOAD_DIR + HOUSE_IMAGES_PATH);
        if (!houseImagesDir.exists()) {
            boolean created = houseImagesDir.mkdirs();
            if (created) {
                log.info("Created house images directory: {}", UPLOAD_DIR + HOUSE_IMAGES_PATH);
            }
        }
    }
    
    /**
     * 上传文件到本地或MinIO
     * @param file 文件
     * @param subDir 子目录，例如"house"
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        // 如果启用了MinIO，则使用MinIO存储
        if (minioEnabled && minioService != null) {
            try {
                String fileUrl = minioService.uploadFileToDirectory(file, subDir);
                log.info("File uploaded to MinIO successfully: {}", fileUrl);
                return fileUrl;
            } catch (Exception e) {
                log.error("Failed to upload file to MinIO: {}", e.getMessage(), e);
                // 如果MinIO上传失败，回退到本地存储
                return uploadFileToLocal(file, subDir);
            }
        } else {
            // 使用本地存储
            return uploadFileToLocal(file, subDir);
        }
    }
    
    /**
     * 上传文件到本地存储
     * @param file 文件
     * @param subDir 子目录，例如"house"
     * @return 文件访问URL
     */
    private String uploadFileToLocal(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        try {
            // 获取文件名
            String originalFilename = file.getOriginalFilename();
            if (!StringUtils.isNotBlank(originalFilename)) {
                return null;
            }
            
            // 获取文件后缀
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            
            // 生成新的文件名
            String newFilename = UUID.randomUUID().toString().replace("-", "") + suffix;
            
            // 确定存储路径
            String targetDirStr = UPLOAD_DIR + (StringUtils.isNotBlank(subDir) ? IMAGES_PATH + "/" + subDir : IMAGES_PATH);
            File targetDir = new File(targetDirStr);
            
            // 确保目录存在
            if (!targetDir.exists()) {
                boolean created = targetDir.mkdirs();
                if (!created) {
                    log.error("Failed to create directory: {}", targetDirStr);
                    return null;
                }
                log.info("Directory created: {}", targetDirStr);
            }
            
            // 保存文件
            File targetFile = new File(targetDir, newFilename);
            log.info("Saving file to: {}", targetFile.getAbsolutePath());
            file.transferTo(targetFile);
            
            // 返回文件URL（相对路径）
            String relativePath = (StringUtils.isNotBlank(subDir) ? "/images/" + subDir : "/images") + "/" + newFilename;
            
            // 确保路径格式统一 - 不以/api开头，以便前端处理
            if (relativePath.startsWith("/api/")) {
                relativePath = relativePath.substring(4);
            } else if (!relativePath.startsWith("/")) {
                relativePath = "/" + relativePath;
            }
            
            log.info("File uploaded successfully: {}", relativePath);
            return relativePath;
        } catch (IOException e) {
            log.error("Failed to upload file: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 上传房源图片
     * @param file 图片文件
     * @return 图片URL
     */
    public String uploadHouseImage(MultipartFile file) {
        return uploadFile(file, "house");
    }
    
    /**
     * 删除文件
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    public boolean deleteFile(String fileUrl) {
        if (!StringUtils.isNotBlank(fileUrl)) {
            return false;
        }
        
        // 如果启用了MinIO，先尝试从MinIO删除
        if (minioEnabled && minioService != null) {
            try {
                boolean deleted = minioService.deleteFile(fileUrl);
                if (deleted) {
                    log.info("File deleted from MinIO successfully: {}", fileUrl);
                    return true;
                }
            } catch (Exception e) {
                log.error("Failed to delete file from MinIO: {}", e.getMessage(), e);
            }
        }
        
        // 如果从MinIO删除失败或者没有启用MinIO，从本地删除
        try {
            // 移除URL前面的"/"
            if (fileUrl.startsWith("/")) {
                fileUrl = fileUrl.substring(1);
            }
            
            // 从URL路径中提取文件路径
            String filePath = fileUrl.replace("/images/", IMAGES_PATH + "/");
            
            // 构建文件路径
            File file = new File(UPLOAD_DIR, filePath);
            
            // 删除文件
            if (file.exists()) {
                return file.delete();
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to delete file: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 检查文件是否存在
     * @param path 文件路径
     * @return 是否存在
     */
    public boolean checkFileExists(String path) {
        if (!StringUtils.isNotBlank(path)) {
            return false;
        }
        
        // 如果启用了MinIO，先尝试从MinIO检查
        if (minioEnabled && minioService != null) {
            try {
                return minioService.fileExists(path);
            } catch (Exception e) {
                log.error("MinIO检查文件存在失败: {}", e.getMessage(), e);
            }
        }
        
        // 如果MinIO检查失败或者没有启用MinIO，检查本地文件
        try {
            // 移除URL前面的"/"
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            
            // 构建文件路径
            File file = new File(UPLOAD_DIR, path);
            
            // 检查文件是否存在
            boolean exists = file.exists();
            log.info("本地文件检查: {}, 路径: {}, 存在: {}", path, file.getAbsolutePath(), exists);
            return exists;
        } catch (Exception e) {
            log.error("本地检查文件存在失败: {}", e.getMessage(), e);
            return false;
        }
    }
} 