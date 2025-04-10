package com.zufang.dto;

import lombok.Data;

/**
 * 统一返回结果
 * @param <T> 返回数据类型
 */
@Data
public class Result<T> {
    /**
     * 状态码
     */
    private Integer code;

    /**
     * 状态信息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 成功结果（带数据）
     * @param data 数据
     * @param <T> 数据类型
     * @return Result
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 成功结果（不带数据）
     * @return Result
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        return result;
    }

    /**
     * 失败结果
     * @param message 错误信息
     * @return Result
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败结果（自定义状态码）
     * @param code 状态码
     * @param message 错误信息
     * @return Result
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
} 