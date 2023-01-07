package com.bao.exception;

import com.bao.result.ResponseStatusEnum;

/**
 * 自定义封装异常类
 */
public class MyCustomException extends RuntimeException{
    private ResponseStatusEnum responseStatusEnum;

    public ResponseStatusEnum getResponseStatusEnum() {
        return responseStatusEnum;
    }

    public void setResponseStatusEnum(ResponseStatusEnum responseStatusEnum) {
        this.responseStatusEnum = responseStatusEnum;
    }

    public MyCustomException(ResponseStatusEnum responseStatusEnum){
        super("异常状态码为: " + responseStatusEnum.status() + "; 异常信息为: " + responseStatusEnum.msg());
        this.responseStatusEnum = responseStatusEnum;
    }
}
