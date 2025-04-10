package com.zufang.interceptor;

import com.zufang.common.exception.BusinessException;
import com.zufang.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器，验证登录状态
 */
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求路径
        String uri = request.getRequestURI();
        log.info("拦截到请求: {}", uri);
        
        // 获取token
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            log.warn("请求未包含token, URI: {}", uri);
            throw new BusinessException("未登录，请先登录");
        }
        
        try {
            // 验证token
            if (JwtUtil.isTokenExpired(token)) {
                log.warn("token已过期, URI: {}", uri);
                throw new BusinessException("登录已过期，请重新登录");
            }
            
            // 解析用户ID
            String userId = JwtUtil.getUserId(token);
            if (userId == null) {
                log.warn("token无效，无法解析用户ID, URI: {}", uri);
                throw new BusinessException("无效的登录信息，请重新登录");
            }
            
            // 解析用户角色
            String role = JwtUtil.getRole(token);
            
            // 将用户信息存入request
            request.setAttribute("userId", Long.valueOf(userId));
            request.setAttribute("role", role);
            
            log.info("用户已登录, userId: {}, role: {}", userId, role);
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("验证token异常: {}", e.getMessage(), e);
            throw new BusinessException("登录信息验证失败，请重新登录");
        }
    }
} 