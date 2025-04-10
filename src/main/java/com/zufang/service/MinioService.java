package com.zufang.service;

import org.springframework.web.multipart.MultipartFile;

public interface MinioService {

    /**
     * 上传文件到MinIO
     * 
     * @param file 文件
     * @param objectName 对象名（可选，为空则自动生成）
     * @param contentType 内容类型（可选）
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String objectName, String contentType);
    
    /**
     * 上传文件到MinIO的指定目录
     * 
     * @param file 文件
     * @param directory 目录，例如"house"
     * @return 文件访问URL
     */
    String uploadFileToDirectory(MultipartFile file, String directory);
    
    /**
     * 从MinIO删除文件
     * 
     * @param objectName 对象名
     * @return 是否删除成功
     */
    boolean deleteFile(String objectName);
    
    /**
     * 检查对象是否存在
     * 
     * @param objectName 对象名
     * @return 是否存在
     */
    boolean isObjectExist(String objectName);
    
    /**
     * 从URL获取对象名
     * 
     * @param url URL
     * @return 对象名
     */
    String getObjectNameFromUrl(String url);

    /**
     * 检查文件是否存在
     * @param objectName 对象名称
     * @return 是否存在
     */
    boolean fileExists(String objectName);

    /**
     * 检查文件是否存在
     * @param fileName 文件名
     * @return 是否存在
     */
    boolean checkFileExists(String fileName);
} 