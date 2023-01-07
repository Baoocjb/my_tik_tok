package com.bao.exception;

import com.bao.result.GraceJSONResult;
import com.bao.result.ResponseStatusEnum;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一异常拦截处理
 * 针对异常类进行捕获, 并传回json给前端
 */
@ControllerAdvice
public class GraceExceptionHandler {

    /**
     * 捕获 MyCustomException 异常类
     * @param e
     * @return
     */
    @ExceptionHandler(MyCustomException.class)
    @ResponseBody
    public GraceJSONResult returnMyCustomException(MyCustomException e){
        return GraceJSONResult.exception(e.getResponseStatusEnum());
    }

    /**
     * 捕获 MethodArgumentNotValidException 异常类
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public GraceJSONResult returnMethodArgumentNotValidException(MethodArgumentNotValidException e){
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> map = getErrors(bindingResult);
        return GraceJSONResult.errorMap(map);
    }

    /**
     * 捕获 MaxUploadSizeExceededException 异常类
     * @param e
     * @return
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    public GraceJSONResult returnMaxUploadSizeExceededException(MaxUploadSizeExceededException e){
        return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_MAX_SIZE_2MB_ERROR);
    }

    /**
     * 通用捕获异常
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public GraceJSONResult returnException(Exception e){
        e.printStackTrace();
        return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
    }

    private Map<String, String> getErrors(BindingResult bindingResult){
        Map<String, String> map = new HashMap<>();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        for (FieldError fieldError : fieldErrors) {
            String field = fieldError.getField();
            String message = fieldError.getDefaultMessage();
            map.put(field, message);
        }
        return map;
    }

}
