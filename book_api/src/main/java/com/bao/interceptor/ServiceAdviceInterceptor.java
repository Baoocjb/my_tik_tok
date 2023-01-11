package com.bao.interceptor;

import com.bao.base.BaseInfoProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Slf4j
public class ServiceAdviceInterceptor extends BaseInfoProperties implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("用户在:" + new Date(System.currentTimeMillis()) + "访问了: " + request.getRequestURL());
        return true;
    }
}
