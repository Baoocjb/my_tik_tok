package com.bao.controller;

import com.bao.bo.LoginRegisterBO;
import com.bao.pojo.Users;
import com.bao.result.GraceJSONResult;
import com.bao.result.ResponseStatusEnum;
import com.bao.service.UserService;
import com.bao.service.base.BaseInfoProperties;
import com.bao.utils.IPUtil;
import com.bao.utils.SMSUtils;
import com.bao.vo.UsersVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.UUID;

@Slf4j
@Api(tags = "PassportController 通行证接口模块")
@RequestMapping("passport")
@RestController
public class PassportController extends BaseInfoProperties {
    @Autowired
    private SMSUtils smsUtils;
    @Autowired
    private UserService userService;

    @PostMapping("getSMSCode")
    public GraceJSONResult getSMSCode(@RequestParam String mobile, HttpServletRequest request) throws Exception {
        if(StringUtils.isBlank(mobile)){
            return GraceJSONResult.ok();
        }
        // 获取对应用户ip, 防止大量验证码请求
        String userIp = IPUtil.getRequestIp(request);
        redis.setnx60s(MOBILE_SMSCODE + ":" + userIp, userIp);
        // 生成6位验证码
        String code = String.valueOf((int)((Math.random() * 9 + 1) * Math.pow(10, 5)));
        // TODO 发送验证码
//        smsUtils.sendSMS(mobile, code);
        // 将验证码存入 Redis
        redis.set(MOBILE_SMSCODE + ":" + mobile, code, 30 * 60);
        log.info(code);
        return GraceJSONResult.ok();
    }

    @PostMapping("login")
    public GraceJSONResult login(@Valid @RequestBody LoginRegisterBO loginRegisterBo, HttpServletRequest request) throws Exception {
        String mobile = loginRegisterBo.getMobile();
        String smsCode = loginRegisterBo.getSmsCode();

        // 校验验证码
        String code = redis.get(MOBILE_SMSCODE + ":" + mobile);
        // 校验验证码是否过期或者不匹配
        if(StringUtils.isBlank(code) || !code.equalsIgnoreCase(smsCode)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }
        // 查询用户是否已经注册
        Users users = userService.queryUserIsExist(mobile);

        if(users == null){
            // 创建用户
            users = userService.createUser(mobile);
        }
        String uToken = UUID.randomUUID().toString();
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(users, usersVO);
        usersVO.setUserToken(uToken);
        redis.set(REDIS_USER_TOKEN + ":" + users.getId(), uToken);

        // 删除redis验证码
        redis.del(MOBILE_SMSCODE + ":" + mobile);
        return GraceJSONResult.ok(usersVO);
    }

    @PostMapping("logout")
    public GraceJSONResult logout(@RequestParam String userId, HttpServletRequest request) throws Exception {
        // 清除 redis 中的 token
        redis.del(REDIS_USER_TOKEN + ":" + userId);
        return GraceJSONResult.ok();
    }

}
