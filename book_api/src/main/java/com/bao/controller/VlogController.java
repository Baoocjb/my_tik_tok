package com.bao.controller;

import com.bao.bo.VlogBO;
import com.bao.enums.YesOrNo;
import com.bao.result.GraceJSONResult;
import com.bao.result.ResponseStatusEnum;
import com.bao.service.UserService;
import com.bao.service.VlogService;
import com.bao.service.base.BaseInfoProperties;
import com.bao.utils.PagedGridResult;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@Api(tags = "VlogController 短视频服务接口模块")
@RequestMapping("vlog")
@RestController
public class VlogController extends BaseInfoProperties {
    @Autowired
    private VlogService vlogService;
    @Autowired
    private UserService userService;

    @PostMapping("publish")
    public GraceJSONResult publish(@Valid @RequestBody VlogBO vlogBO) {
        // 校验VlogBO
        vlogService.createVlog(vlogBO);
        return GraceJSONResult.ok();
    }

    @GetMapping("indexList")
    public GraceJSONResult indexList(@RequestParam(defaultValue = "") String userId,
                                     @RequestParam(defaultValue = "") String search,
                                     @RequestParam Integer page,
                                     @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult gridResult = vlogService.getIndexVlogVOList(userId, search, page, pageSize);
        return GraceJSONResult.ok(gridResult);
    }

    @GetMapping("detail")
    public GraceJSONResult detail(@RequestParam(defaultValue = "") String userId,
                                  @RequestParam String vlogId) {
        if (!checkVlogIsExist(vlogId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ARGS_ERROR);
        }
        return GraceJSONResult.ok(vlogService.getIndexVlogVODetailById(userId, vlogId));
    }

    @GetMapping("myPublicList")
    public GraceJSONResult myPublicList(@RequestParam String userId,
                                        @RequestParam Integer page,
                                        @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        if(!checkUserIsExist(userId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ARGS_ERROR);
        }
        PagedGridResult pagedGridResult = vlogService.queryMyIndexVlog(userId, YesOrNo.NO.type, page, pageSize);
        return GraceJSONResult.ok(pagedGridResult);
    }

    @GetMapping("myPrivateList")
    public GraceJSONResult myPrivateList(@RequestParam String userId,
                                        @RequestParam Integer page,
                                        @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        if(!checkUserIsExist(userId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ARGS_ERROR);
        }
        PagedGridResult pagedGridResult = vlogService.queryMyIndexVlog(userId, YesOrNo.YES.type, page, pageSize);
        return GraceJSONResult.ok(pagedGridResult);
    }

    @GetMapping("myLikedList")
    public GraceJSONResult myLikedList(@RequestParam String userId,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        if(!checkUserIsExist(userId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ARGS_ERROR);
        }
        PagedGridResult pagedGridResult = vlogService.queryMyLikedList(userId, page, pageSize);
        return GraceJSONResult.ok(pagedGridResult);
    }

    @PostMapping("changeToPrivate")
    public GraceJSONResult changeToPrivate(@RequestParam String userId, @RequestParam String vlogId) {
        if(!checkUserIsExist(userId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ARGS_ERROR);
        }
        if (!checkVlogIsExist(vlogId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ARGS_ERROR);
        }
        vlogService.changeMyVlogToPrivate(userId, vlogId);
        return GraceJSONResult.ok();
    }

    @PostMapping("changeToPublic")
    public GraceJSONResult changeToPublic(@RequestParam String userId, @RequestParam String vlogId) {
        if(!checkUserIsExist(userId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ARGS_ERROR);
        }
        if (!checkVlogIsExist(vlogId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ARGS_ERROR);
        }
        vlogService.changeMyVlogToPublic(userId, vlogId);
        return GraceJSONResult.ok();
    }

    @PostMapping("like")
    public GraceJSONResult like(@RequestParam String userId, @RequestParam String vlogerId, @RequestParam String vlogId) {
        if(!checkUserIsExist(userId) || !checkUserIsExist(vlogerId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ARGS_ERROR);
        }
        if (!checkVlogIsExist(vlogId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ARGS_ERROR);
        }
        vlogService.userLikeVlog(userId, vlogerId, vlogId);
        return GraceJSONResult.ok();
    }

    @PostMapping("unlike")
    public GraceJSONResult unlike(@RequestParam String userId, @RequestParam String vlogerId, @RequestParam String vlogId) {
        if(!checkUserIsExist(userId) || !checkUserIsExist(vlogerId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ARGS_ERROR);
        }
        if (!checkVlogIsExist(vlogId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ARGS_ERROR);
        }
        vlogService.userUnlikeVlog(userId, vlogerId, vlogId);
        return GraceJSONResult.ok();
    }



    @GetMapping("followList")
    public GraceJSONResult getMyFollowList(@RequestParam String myId,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        if(!checkUserIsExist(myId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ARGS_ERROR);
        }
        PagedGridResult pagedGridResult = vlogService.queryMyFollowList(myId, page, pageSize, YesOrNo.NO.type);
        return GraceJSONResult.ok(pagedGridResult);
    }

    @GetMapping("friendList")
    public GraceJSONResult getMyFriendList(@RequestParam String myId,
                                           @RequestParam Integer page,
                                           @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        if(!checkUserIsExist(myId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ARGS_ERROR);
        }
        PagedGridResult pagedGridResult = vlogService.queryMyFollowList(myId, page, pageSize, YesOrNo.YES.type);
        return GraceJSONResult.ok(pagedGridResult);
    }
}
