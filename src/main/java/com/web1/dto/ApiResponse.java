package com.web1.dto;

public class ApiResponse<T> {
    private Integer code;
    private String msg;
    private T data;
    
    public ApiResponse() {}
    
    public ApiResponse(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data);
    }
    
    public static <T> ApiResponse<T> success(String msg, T data) {
        return new ApiResponse<>(200, msg, data);
    }
    
    public static <T> ApiResponse<T> error(String msg) {
        return new ApiResponse<>(500, msg, null);
    }
    
    public static <T> ApiResponse<T> error(Integer code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }
    
    // Getter和Setter方法
    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }
    
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}