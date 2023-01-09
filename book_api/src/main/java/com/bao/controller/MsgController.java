package com.bao.controller;

import com.bao.mo.MessageMO;
import com.bao.result.GraceJSONResult;
import com.bao.result.ResponseStatusEnum;
import com.bao.service.MsgService;
import com.bao.service.base.BaseInfoProperties;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Api(tags = "MsgController 消息服务接口模块")
@RequestMapping("msg")
@RestController
public class MsgController extends BaseInfoProperties {

    @Autowired
    private MsgService msgService;

    @GetMapping("list")
    public GraceJSONResult list(@RequestParam String userId,
                                @RequestParam Integer page,
                                @RequestParam Integer pageSize){
        if (page == null) {
            page = COMMON_START_PAGE_ZERO;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        if(!checkUserIsExist(userId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ARGS_ERROR);
        }
        List<MessageMO> list = msgService.getMsgList(userId, page, pageSize);
        return GraceJSONResult.ok(list);
    }
}
