package com.bao.config;

import com.bao.interceptor.PassportInterceptor;
import com.bao.interceptor.ServiceAdviceInterceptor;
import com.bao.interceptor.UserTokenInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public PassportInterceptor passportInterceptor(){
        return new PassportInterceptor();
    }
    @Bean
    public UserTokenInterceptor userTokenInterceptor(){
        return new UserTokenInterceptor();
    }
    @Bean
    public ServiceAdviceInterceptor serviceAdviceInterceptor(){
        return new ServiceAdviceInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(passportInterceptor()).addPathPatterns("/passport/getSMSCode");
        registry.addInterceptor(userTokenInterceptor()).addPathPatterns("/userInfo/modifyImage", "/userInfo/modifyUserInfo");
//        registry.addInterceptor(serviceAdviceInterceptor()).addPathPatterns("/**");
    }
}
