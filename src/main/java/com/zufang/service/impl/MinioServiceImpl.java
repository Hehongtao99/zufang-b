package com.zufang.service.impl;

import com.zufang.config.MinioConfig;
import com.zufang.service.MinioService;
import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Slf4j
@Service
public class MinioServiceImpl implements MinioService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfig minioConfig;

    @Override
    public String uploadFile(MultipartFile file, String objectName, String contentType) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // 检查桶是否存在
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(minioConfig.getBucket())
                        .build());
                log.info("Bucket {} created successfully", minioConfig.getBucket());
            }

            // 生成唯一的对象名
            if (!StringUtils.isNotBlank(objectName)) {
                String originalFilename = file.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                objectName = UUID.randomUUID().toString().replace("-", "") + extension;
            }

            // 上传文件
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(StringUtils.isNotBlank(contentType) ? contentType : file.getContentType())
                    .build());
            inputStream.close();

            // 生成访问URL
            String externalEndpoint = minioConfig.getExternalEndpoint();
            String fileUrl = externalEndpoint + "/" + minioConfig.getBucket() + "/" + objectName;
            log.info("File uploaded successfully: {}", fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public String uploadFileToDirectory(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // 生成对象名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String objectName = (StringUtils.isNotBlank(directory) ? directory + "/" : "") +
                    UUID.randomUUID().toString().replace("-", "") + extension;

            // 上传文件
            return uploadFile(file, objectName, file.getContentType());
        } catch (Exception e) {
            log.error("Failed to upload file to directory {}: {}", directory, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean deleteFile(String objectName) {
        if (!StringUtils.isNotBlank(objectName)) {
            return false;
        }

        try {
            // 如果是完整URL，提取对象名
            objectName = getObjectNameFromUrl(objectName);
            
            // 删除对象
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(objectName)
                    .build());
            log.info("File deleted successfully: {}", objectName);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete file from MinIO: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean isObjectExist(String objectName) {
        if (!StringUtils.isNotBlank(objectName)) {
            return false;
        }

        try {
            // 如果是完整URL，提取对象名
            objectName = getObjectNameFromUrl(objectName);
            
            // 检查对象是否存在
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            // 对象不存在
            log.info("Object does not exist: {}", objectName);
            return false;
        }
    }

    @Override
    public String getObjectNameFromUrl(String url) {
        if (!StringUtils.isNotBlank(url)) {
            return "";
        }

        log.info("从URL提取对象名: {}", url);
        
        // 如果是完整URL，提取对象名
        if (url.startsWith("http")) {
            try {
                URL urlObj = new URL(url);
                String path = urlObj.getPath();
                log.info("URL路径部分: {}", path);
                
                // 移除桶名前缀
                String bucketPrefix = "/" + minioConfig.getBucket() + "/";
                if (path.startsWith(bucketPrefix)) {
                    String objectName = path.substring(bucketPrefix.length());
                    log.info("提取的对象名: {}", objectName);
                    return objectName;
                }
                
                // 如果URL不遵循标准格式，尝试使用简单方法提取
                String[] parts = url.split("/");
                // 跳过协议、域名、端口和bucket名
                if (parts.length > 4) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 4; i < parts.length; i++) {
                        sb.append(parts[i]);
                        if (i < parts.length - 1) {
                            sb.append("/");
                        }
                    }
                    String objectName = sb.toString();
                    log.info("使用简单方法提取的对象名: {}", objectName);
                    return objectName;
                }
                
                log.info("从URL中无法提取对象名，返回路径: {}", path.startsWith("/") ? path.substring(1) : path);
                return path.startsWith("/") ? path.substring(1) : path;
            } catch (Exception e) {
                log.error("解析URL失败: {}", e.getMessage(), e);
                // 尝试使用简单方法提取
                String[] parts = url.split("/");
                if (parts.length > 0) {
                    String fileName = parts[parts.length - 1];
                    log.info("解析失败，使用文件名作为对象名: {}", fileName);
                    return fileName;
                }
                return url;
            }
        }

        // 如果不是完整URL，直接返回
        return url;
    }

    @Override
    public boolean fileExists(String objectName) {
        try {
            log.info("检查MinIO中文件是否存在: {}", objectName);
            
            // 处理objectName，确保格式正确
            if (objectName.startsWith("/")) {
                objectName = objectName.substring(1);
            }
            
            // 尝试获取文件的元数据，如果能获取到则表示文件存在
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(objectName)
                    .build()
            );
            
            log.info("MinIO中文件存在: {}", objectName);
            return true;
        } catch (ErrorResponseException e) {
            // 如果是文件不存在的错误，则正常返回false
            if (e.errorResponse().code().equals("NoSuchKey") || 
                e.errorResponse().code().equals("NoSuchObject") ||
                e.errorResponse().code().equals("NotFound")) {
                log.info("MinIO中文件不存在: {}", objectName);
                return false;
            }
            
            // 其他错误则记录日志
            log.error("检查MinIO文件存在时发生错误: {}, 错误码: {}", e.getMessage(), e.errorResponse().code(), e);
            return false;
        } catch (Exception e) {
            log.error("检查MinIO文件存在时发生异常: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean checkFileExists(String fileName) {
        log.info("检查MinIO中文件是否存在: {}", fileName);
        
        // 如果文件名为空，返回false
        if (StringUtils.isBlank(fileName)) {
            return false;
        }
        
        try {
            // 尝试直接用文件名检查
            boolean exists = fileExists(fileName);
            if (exists) {
                return true;
            }
            
            // 尝试在标准路径下检查
            String objectName = "images/house/" + fileName;
            exists = fileExists(objectName);
            if (exists) {
                return true;
            }
            
            // 尝试直接用子目录路径检查
            if (fileName.contains("/")) {
                exists = fileExists(fileName);
                if (exists) {
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            log.error("检查MinIO中文件是否存在时出错: {}", e.getMessage(), e);
            return false;
        }
    }
} 