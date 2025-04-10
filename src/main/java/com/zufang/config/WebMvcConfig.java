package com.zufang.config;

import com.zufang.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Spring MVC配置类，处理文件上传等问题
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    /**
     * 配置文件上传解析器
     */
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    /**
     * 配置消息转换器
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2HttpMessageConverter());
    }
    
    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册登录拦截器，拦截所有请求
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                // 排除一些不需要拦截的路径，如登录、注册等
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/user/reset-password",
                        "/house/list",
                        "/house/detail/**",
                        "/file/download/**",
                        "/websocket/**",
                        "/ws/**",
                        "/error",
                        "/swagger-resources/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/webjars/**"
                );
    }
    
    /**
     * 配置视图控制器
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 对于前端路由，转发到index.html
        // 注意：这里只转发前端路由，不包括API路径
        registry.addViewController("/landlord").setViewName("forward:/index.html");
        registry.addViewController("/landlord/home").setViewName("forward:/index.html");
        registry.addViewController("/landlord/house/**").setViewName("forward:/index.html");
        
        registry.addViewController("/admin").setViewName("forward:/index.html");
        registry.addViewController("/admin/home").setViewName("forward:/index.html");
        registry.addViewController("/admin/house").setViewName("forward:/index.html");
        registry.addViewController("/admin/house/approve/**").setViewName("forward:/index.html");
        
        registry.addViewController("/user").setViewName("forward:/index.html");
        registry.addViewController("/user/home").setViewName("forward:/index.html");
        registry.addViewController("/user/house/**").setViewName("forward:/index.html");
        registry.addViewController("/user/myhouse").setViewName("forward:/index.html");
        
        // 登录和注册页面
        registry.addViewController("/login").setViewName("forward:/index.html");
        registry.addViewController("/register").setViewName("forward:/index.html");
    }
    
    /**
     * 配置资源处理器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
} 