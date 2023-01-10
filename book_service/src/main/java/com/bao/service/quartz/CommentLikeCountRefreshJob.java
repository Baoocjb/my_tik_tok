package com.bao.service.quartz;

import com.bao.mapper.CommentMapperCustom;
import com.bao.pojo.Comment;
import com.bao.service.base.BaseInfoProperties;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class CommentLikeCountRefreshJob extends BaseInfoProperties implements Job {
    @Autowired
    private CommentMapperCustom commentMapperCustom;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
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

        // 创建临时表
        commentMapperCustom.createTempTable();
        commentMapperCustom.insertList(list);
        // 联表批量更新
        commentMapperCustom.updateList();
        // 删除临时表
        commentMapperCustom.delTempTable();
        String end = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        log.info("Comment点赞数据落库结束! 当前时间:" + end);
    }

}
