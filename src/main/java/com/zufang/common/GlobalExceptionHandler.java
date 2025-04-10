package com.zufang.common;

import com.zufang.common.exception.BusinessException;
import com.zufang.common.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.security.auth.message.AuthException;
import java.net.ConnectException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理数据库异常
     */
    @ExceptionHandler({SQLException.class, DataAccessException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleDatabaseException(Exception e) {
        log.error("数据库异常: {}", e.getMessage(), e);
        String message = "数据库操作失败";
        if (e.getMessage() != null && e.getMessage().contains("Connection")) {
            message = "数据库连接失败，请检查数据库配置和网络";
        }
        Map<String, String> debugInfo = new HashMap<>();
        debugInfo.put("exception", e.getClass().getName());
        debugInfo.put("message", e.getMessage());
        return Result.fail(message).setData(debugInfo);
    }

    /**
     * 处理连接异常
     */
    @ExceptionHandler(ConnectException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleConnectException(ConnectException e) {
        log.error("连接异常: {}", e.getMessage(), e);
        return Result.fail("连接远程服务失败，请检查网络和服务状态");
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Object> handleAuthException(AuthException e) {
        log.error("认证异常: {}", e.getMessage(), e);
        return Result.fail("认证失败: " + e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleValidationException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String errorMsg = bindingResult.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数校验异常: {}", errorMsg);
        return Result.fail(errorMsg);
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常: {}", e.getMessage(), e);
        return Result.fail("系统错误: 空指针异常");
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.fail(e.getMessage());
    }

    /**
     * 处理其他所有异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleException(Exception e) {
        log.error("未知异常: {}", e.getMessage(), e);
        Map<String, String> debugInfo = new HashMap<>();
        debugInfo.put("exception", e.getClass().getName());
        debugInfo.put("message", e.getMessage());
        return Result.fail("系统内部错误，请联系管理员").setData(debugInfo);
    }
} 