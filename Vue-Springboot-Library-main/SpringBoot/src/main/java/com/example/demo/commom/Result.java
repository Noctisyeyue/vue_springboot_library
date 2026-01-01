package com.example.demo.commom;

/**
 * 统一响应结果封装类
 * 核心用途：统一API响应格式，包含状态码、提示信息和数据
 * @param <T> 响应数据的类型
 */
public class Result<T> {
    private String code;  // 状态码，"0"表示成功，其他表示失败
    private String msg;   // 提示信息
    private T data;       // 响应数据

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Result() {
    }

    public Result(T data) {
        this.data = data;
    }

    /**
     * 创建成功响应（无数据）
     * @return Result 状态码为"0"，提示信息为"成功"
     */
    public static Result success() {
        Result result = new Result<>();
        result.setCode("0");
        result.setMsg("成功");
        return result;
    }

    /**
     * 创建成功响应（含数据）
     * @param data 响应数据
     * @param <T> 数据类型
     * @return Result<T> 状态码为"0"，提示信息为"成功"，包含数据
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>(data);
        result.setCode("0");
        result.setMsg("成功");
        return result;
    }

    /**
     * 创建错误响应（指定错误码和错误信息）
     * @param code 错误码
     * @param msg 错误信息
     * @return Result 包含错误码和错误信息的响应对象
     */
    public static Result error(String code, String msg) {
        Result result = new Result();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    /**
     * 创建错误响应（默认错误码为"-1"）
     * @param msg 错误信息
     * @return Result 错误码为"-1"，包含错误信息
     */
    public static Result error(String msg) {
        Result result = new Result();
        result.setCode("-1");
        result.setMsg(msg);
        return result;
    }
}
