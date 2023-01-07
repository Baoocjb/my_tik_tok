package com.bao.controller;

import com.bao.pojo.Users;
import com.bao.result.GraceJSONResult;
import com.bao.result.ResponseStatusEnum;
import com.bao.service.FansService;
import com.bao.service.UserService;
import com.bao.service.base.BaseInfoProperties;
import com.bao.utils.PagedGridResult;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "FansController 关注接口模块")
@RequestMapping("fans")
@RestController
public class FansController extends BaseInfoProperties {
    @Autowired
    private FansService fansService;
    @Autowired
    private UserService userService;

    @PostMapping("follow")
    public GraceJSONResult follow(@RequestParam String myId, @RequestParam String vlogerId) {
        if (StringUtils.isBlank(myId) || StringUtils.isBlank(vlogerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ARGS_ERROR);
        }
        // 判断当前用户，自己不能关注自己
        if (myId.equalsIgnoreCase(vlogerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ARGS_ERROR);
        }
        Users my = userService.getUser(myId);
        if (my == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ARGS_ERROR);
        }
        Users vloger = userService.getUser(vlogerId);
        if (vloger == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ARGS_ERROR);
        }
        fansService.doFollow(myId, vlogerId);
        // 此时 redis 要放在 controller 中, 避免 service 事务回滚但是 redis 操作不能取消
        redis.increment(REDIS_MY_FOLLOWS_COUNTS + ":" + myId, 1);
        redis.increment(REDIS_MY_FANS_COUNTS + ":" + vlogerId, 1);
        redis.set(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + vlogerId, "1");
        return GraceJSONResult.ok();
    }

    @PostMapping("cancel")
    public GraceJSONResult cancel(@RequestParam String myId, @RequestParam String vlogerId) {
        if (StringUtils.isBlank(myId) || StringUtils.isBlank(vlogerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ARGS_ERROR);
        }
        Users my = userService.getUser(myId);
        if (my == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ARGS_ERROR);
        }
        Users vloger = userService.getUser(vlogerId);
        if (vloger == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ARGS_ERROR);
        }
        fansService.doCancel(myId, vlogerId);
        // 此时 redis 要放在 controller 中, 避免 service 事务回滚但是 redis 操作不能取消
        redis.decrement(REDIS_MY_FOLLOWS_COUNTS + ":" + myId, 1);
        redis.decrement(REDIS_MY_FANS_COUNTS + ":" + vlogerId, 1);
        redis.del(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + vlogerId);
        return GraceJSONResult.ok();
    }

    @GetMapping("queryDoIFollowVloger")
    public GraceJSONResult queryDoIFollowVloger(@RequestParam String myId, @RequestParam String vlogerId) {
        if (StringUtils.isBlank(myId) || StringUtils.isBlank(vlogerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ARGS_ERROR);
        }
        Users my = userService.getUser(myId);
        if (my == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ARGS_ERROR);
        }
        Users vloger = userService.getUser(vlogerId);
        if (vloger == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ARGS_ERROR);
        }
        return GraceJSONResult.ok(fansService.queryDoIFollowVloger(myId, vlogerId));
    }

    @GetMapping("queryMyFollows")
    public GraceJSONResult queryMyFollows(@RequestParam String myId,
                                          @RequestParam Integer page,
                                          @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult pagedGridResult = fansService.queryMyFollows(myId, page, pageSize);
        return GraceJSONResult.ok(pagedGridResult);
    }

    @GetMapping("queryMyFans")
    public GraceJSONResult queryMyFans(@RequestParam String myId,
                                          @RequestParam Integer page,
                                          @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult pagedGridResult = fansService.queryMyFans(myId, page, pageSize);
        return GraceJSONResult.ok(pagedGridResult);
    }
}
