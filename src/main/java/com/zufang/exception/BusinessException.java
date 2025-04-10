package com.zufang.exception;

/**
 * 业务逻辑异常
 */
public class BusinessException extends RuntimeException {
    /**
     * 错误码
     */
    private Integer code;

    /**
     * 构造函数
     * @param message 错误信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    /**
     * 构造函数
     * @param code 错误码
     * @param message 错误信息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 获取错误码
     * @return 错误码
     */
    public Integer getCode() {
        return code;
    }
} 