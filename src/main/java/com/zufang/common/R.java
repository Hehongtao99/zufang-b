package com.zufang.common;

import lombok.Data;
import java.io.Serializable;

/**
 * 通用返回结果类，服务端响应的数据最终都会封装成此对象
 * @param <T> 数据类型
 */
@Data
public class R<T> implements Serializable {

    private Integer code; // 编码：1成功，0和其它数字为失败
    private String message; // 错误信息
    private T data; // 数据

    public static <T> R<T> success(T object) {
        R<T> r = new R<T>();
        r.data = object;
        r.code = 200;
        r.message = "操作成功";
        return r;
    }

    public static <T> R<T> error(String msg) {
        R<T> r = new R<T>();
        r.message = msg;
        r.code = 500;
        return r;
    }

    public static <T> R<T> error(Integer code, String msg) {
        R<T> r = new R<T>();
        r.message = msg;
        r.code = code;
        return r;
    }
} 