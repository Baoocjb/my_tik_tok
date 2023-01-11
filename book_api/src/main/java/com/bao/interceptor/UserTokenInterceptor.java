package com.bao.interceptor;

import com.bao.base.BaseInfoProperties;
import com.bao.exception.GraceException;
import com.bao.result.ResponseStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class UserTokenInterceptor extends BaseInfoProperties implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = request.getHeader("headerUserId");
        String userToken = request.getHeader("headerUserToken");
        if(StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(userToken)){
            String redisToken = redis.get(REDIS_USER_TOKEN + ":" + userId);
            if(StringUtils.isBlank(redisToken)){
                GraceException.display(ResponseStatusEnum.TICKET_INVALID);
                return false;
            }else{
                if(!redisToken.equalsIgnoreCase(userToken)){
                    // 如果token不一致说明在其他地方登陆
                    GraceException.display(ResponseStatusEnum.TICKET_INVALID);
                    return false;
                }
            }
        }else{
            GraceException.display(ResponseStatusEnum.UN_LOGIN);
            return false;
        }
        return true;
    }
}
