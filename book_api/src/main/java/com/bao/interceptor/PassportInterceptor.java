package com.bao.interceptor;

import com.bao.service.base.BaseInfoProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class PassportInterceptor extends BaseInfoProperties implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // FIXME: 上线需要开启拦截器
//        String reqIp = IPUtil.getRequestIp(request);
//        if(redis.keyIsExist(MOBILE_SMSCODE + ":" + reqIp)){
//            GraceException.display(ResponseStatusEnum.SMS_NEED_WAIT_ERROR);
//            log.warn("短信发送频率太快!");
//            return false;
//        }
        return true;
    }
}
