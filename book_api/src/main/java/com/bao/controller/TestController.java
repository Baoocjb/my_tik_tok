package com.bao.controller;

import com.bao.mapper.CommentMapper;
import com.bao.mapper.CommentMapperCustom;
import com.bao.pojo.Comment;
import com.bao.service.base.BaseInfoProperties;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Api(tags = "TestController 评论服务接口模块")
@RequestMapping("test")
@RestController
public class TestController extends BaseInfoProperties {

    @Autowired
    private CommentMapperCustom commentMapperCustom;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private Sid sid;

    @GetMapping("test1")
    public void test1(){
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        log.info("test1开始! 当前时间:" + now);
        int count = 90000;
        for(int i = 10000; i < count; i++){
            Comment comment = new Comment();
            comment.setId(i + "");
            comment.setVlogerId("1");
            comment.setLikeCounts(0);
            comment.setFatherCommentId("0");
            comment.setIsValid(1);
            comment.setContent("1");
            comment.setVlogId("1");
            comment.setCreateTime(new Date());
            comment.setCommentUserId("1");
            redis.setHashValue(REDIS_VLOG_COMMENT_LIKED_COUNTS, comment.getId(), "1");
            commentMapper.insert(comment);
        }

        String end = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        log.info("test1结束! 当前时间:" + end);
    }

    @GetMapping("test2")
    public void test2(){
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        log.info("Comment点赞数据落库开始执行! 当前时间:" + now);
        Map<Object, Object> allBeLikedComments = redis.hgetall(REDIS_VLOG_COMMENT_LIKED_COUNTS);
        Set<Map.Entry<Object, Object>> entries = allBeLikedComments.entrySet();

        List<Comment> list = new ArrayList<>(entries.size());
        for (Map.Entry<Object, Object> entry : entries) {
            String commentId = (String) entry.getKey();
            if (entry.getValue() != null){
                Integer likeCounts = Integer.parseInt((String) entry.getValue());
                Comment comment = new Comment();
                comment.setId(commentId);
                comment.setLikeCounts(likeCounts);
                list.add(comment);
            }
        }

        String end = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        log.info("Comment点赞数据落库结束! 当前时间:" + end);
    }
}
