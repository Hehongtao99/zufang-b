package com.zufang.common.response;

import lombok.Data;

/**
 * 统一返回结果
 */
@Data
public class Result<T> {
    
    private Integer code; // 状态码
    
    private String message; // 提示信息
    
    private T data; // 数据
    
    // 成功返回结果（无数据）
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        return result;
    }
    
    // 成功返回结果
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }
    
    // 成功返回结果（自定义提示信息）
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
    
    // 失败返回结果
    public static <T> Result<T> fail(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }
    
    // 失败返回结果（自定义状态码）
    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
    
    // 错误返回结果，与fail方法相同，提供更语义化的方法名
    public static <T> Result<T> error(String message) {
        return fail(message);
    }
    
    // 错误返回结果（自定义状态码），与fail方法相同，提供更语义化的方法名
    public static <T> Result<T> error(Integer code, String message) {
        return fail(code, message);
    }
    
    // 成功返回结果，与success方法相同，提供更语义化的方法名
    public static <T> Result<T> ok(T data) {
        return success(data);
    }
    
    // 设置响应数据，支持链式调用
    public Result<T> setData(T data) {
        this.data = data;
        return this;
    }
} 