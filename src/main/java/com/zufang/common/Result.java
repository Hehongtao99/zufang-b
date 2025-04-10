package com.zufang.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果类
 */
@Data
public class Result<T> implements Serializable {
    private Integer code; // 状态码
    private String message; // 提示信息
    private T data; // 数据

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
    
    /**
     * 失败返回结果（自定义提示信息）
     * @param message 提示信息
     * @return Result对象
     */
    public static <T> Result<T> fail(String message) {
        return error(400, message);
    }
    
    /**
     * 设置数据，支持链式调用
     * @param data 数据
     * @return Result对象
     */
    public Result<T> setData(T data) {
        this.data = data;
        return this;
    }
} 