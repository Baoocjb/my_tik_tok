package com.bao.controller;

import com.bao.bo.CommentBO;
import com.bao.result.GraceJSONResult;
import com.bao.result.ResponseStatusEnum;
import com.bao.service.CommentService;
import com.bao.service.base.BaseInfoProperties;
import com.bao.utils.PagedGridResult;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@Api(tags = "CommentController 评论服务接口模块")
@RequestMapping("comment")
@RestController
public class CommentController extends BaseInfoProperties {
    @Autowired
    private CommentService commentService;

    @PostMapping("create")
    public GraceJSONResult create(@Valid @RequestBody CommentBO commentBO){
        return GraceJSONResult.ok(commentService.createComment(commentBO));
    }

    @PostMapping("like")
    public GraceJSONResult like(@RequestParam String userId, @RequestParam String commentId){
        if(!checkUserIsExist(userId) || !checkCommentIsExist(commentId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }
        commentService.userLikeComment(userId, commentId);
        return GraceJSONResult.ok();
    }

    @PostMapping("unlike")
    public GraceJSONResult unlike(@RequestParam String userId, @RequestParam String commentId){
        if(!checkUserIsExist(userId) || !checkCommentIsExist(commentId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }
        commentService.userunLikeComment(userId, commentId);
        return GraceJSONResult.ok();
    }

    @GetMapping("counts")
    public GraceJSONResult counts(@RequestParam String vlogId){
        if(!checkVlogIsExist(vlogId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }
        int counts = commentService.getCommentCounts(vlogId);
        return GraceJSONResult.ok(counts);
    }

    @GetMapping("list")
    public GraceJSONResult list(@RequestParam String vlogId,
                                @RequestParam(defaultValue = "") String userId,
                                @RequestParam Integer page,
                                @RequestParam Integer pageSize){
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        if(!checkUserIsExist(userId) || !checkVlogIsExist(vlogId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }
        PagedGridResult pagedGridResult = commentService.getCommentList(vlogId, userId, page, pageSize);
        return GraceJSONResult.ok(pagedGridResult);
    }
}
