package com.bao.exception;

import com.bao.result.ResponseStatusEnum;

/**
 * 统一封装类, 优雅处理异常
 */
public class GraceException {
    /**
     * 优雅的抛出异常, 避免代码中出现直接 throw 的情况
     * @param responseStatusEnum
     */
    public static void display(ResponseStatusEnum responseStatusEnum){
        // 传入相应状态码
        throw new MyCustomException(responseStatusEnum);
    }
}
