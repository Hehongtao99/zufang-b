package com.zufang.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 处理无效请求的配置类
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class InvalidRequestHandlerConfig implements WebMvcConfigurer {

    /**
     * 添加资源处理器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 静态资源映射
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }

    /**
     * 处理404异常
     */
    @ControllerAdvice
    public static class GlobalExceptionHandler {
        
        @ExceptionHandler(NoHandlerFoundException.class)
        public String handleNotFound(NoHandlerFoundException e, HttpServletRequest request) {
            String path = request.getRequestURI();
            
            // 如果是API请求，返回404
            if (path.startsWith("/api/")) {
                return "forward:/error";
            }
            
            // 处理前端路由路径，让前端路由接管
            // 但排除以 /admin/houses 开头的API路径
            if ((path.startsWith("/landlord/") && !path.startsWith("/landlord/houses")) || 
                (path.startsWith("/admin/") && !path.startsWith("/admin/houses")) || 
                (path.startsWith("/user/") && !path.startsWith("/user/houses"))) {
                return "forward:/index.html";
            }
            
            // 前端路由处理，让前端路由接管
            return "forward:/index.html";
        }
    }
} 