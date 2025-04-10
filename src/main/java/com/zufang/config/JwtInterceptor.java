package com.zufang.config;

import com.zufang.common.constants.RedisConstants;
import com.zufang.common.exception.BusinessException;
import com.zufang.common.util.JwtUtil;
import com.zufang.common.util.RedisUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

/**
 * JWT拦截器
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取token
        String token = request.getHeader("token");
        log.info("拦截到请求: {}, token: {}", request.getRequestURI(), token != null ? token.substring(0, Math.min(10, token.length())) + "..." : "null");
        
        if (token == null || token.isEmpty()) {
            log.warn("请求未包含token, URI: {}", request.getRequestURI());
            throw new BusinessException("未登录，请先登录");
        }
        
        try {
            // 解析token
            String userId = JwtUtil.getUserId(token);
            log.info("从token解析出userId: {}", userId);
            
            if (userId == null) {
                log.warn("从token解析出的userId为null, token: {}", token.substring(0, Math.min(10, token.length())) + "...");
                throw new BusinessException("token无效");
            }
            
            // 直接验证JWT的有效性，不再使用Redis
            if (JwtUtil.isTokenExpired(token)) {
                log.warn("token已过期, userId: {}", userId);
                throw new BusinessException("token已过期，请重新登录");
            }
            
            // 解析token中的信息
            String username = JwtUtil.getUsername(token);
            String role = JwtUtil.getRole(token);
            log.info("成功解析token信息 - userId: {}, username: {}, role: {}", userId, username, role);
            
            // 将用户信息存入request，确保userId为Long类型
            try {
                // 将String类型的userId转为Long存入request
                if (userId != null) {
                    request.setAttribute("userId", Long.valueOf(userId));
                } else {
                    request.setAttribute("userId", null);
                }
                request.setAttribute("username", username);
                request.setAttribute("role", role);
            } catch (NumberFormatException e) {
                log.error("用户ID转为Long类型失败: {}", userId, e);
                throw new BusinessException("用户ID格式不正确");
            }
            
            return true;
        } catch (BusinessException e) {
            log.warn("业务异常: {}, URI: {}", e.getMessage(), request.getRequestURI());
            throw e;
        } catch (JwtException e) {
            log.error("JWT解析异常: {}, URI: {}", e.getMessage(), request.getRequestURI());
            throw new BusinessException("token解析失败，请重新登录");
        }
    }
} 