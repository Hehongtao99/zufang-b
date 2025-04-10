package com.zufang.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web全局配置类，包括跨域等配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 添加资源处理器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目根目录
        String projectRootPath = System.getProperty("user.dir");
        String uploadPath = "file:" + projectRootPath + "/uploads/";
        
        // 添加自定义的资源映射
        registry.addResourceHandler("/images/**")
                .addResourceLocations(uploadPath);
        
        // 添加默认的资源映射
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
                
        // 添加额外的图片映射，支持不同的路径格式
        registry.addResourceHandler("/api/images/**")
                .addResourceLocations(uploadPath);
    }
    
    /**
     * 跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 跨域过滤器
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.setAllowCredentials(true);
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
} 