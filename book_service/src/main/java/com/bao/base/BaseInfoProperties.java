package com.bao.base;

import com.bao.mo.MessageMO;
import com.bao.service.CommentService;
import com.bao.service.UserService;
import com.bao.service.VlogService;
import com.bao.utils.PagedGridResult;
import com.bao.utils.RedisOperator;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

public class BaseInfoProperties {

    @Autowired
    private UserService userService;
    @Autowired
    private VlogService vlogService;
    @Autowired
    private CommentService commentService;

    @Autowired
    public RedisOperator redis;

    public static final Integer COMMON_START_PAGE = 1;
    public static final Integer COMMON_START_PAGE_ZERO = 0;
    public static final Integer COMMON_PAGE_SIZE = 10;

    public static final String MOBILE_SMSCODE = "mobile:smscode";
    public static final String REDIS_USER_TOKEN = "redis_user_token";
    public static final String REDIS_USER_INFO = "redis_user_info";

    // 短视频的评论总数
    public static final String REDIS_VLOG_COMMENT_COUNTS = "redis_vlog_comment_counts";
    // 短视频的评论喜欢数量
    public static final String REDIS_VLOG_COMMENT_LIKED_COUNTS = "redis_vlog_comment_liked_counts";
    // 用户点赞评论
    public static final String REDIS_USER_LIKE_COMMENT = "redis_user_like_comment";

    // 我的关注总数
    public static final String REDIS_MY_FOLLOWS_COUNTS = "redis_my_follows_counts";
    // 我的粉丝总数
    public static final String REDIS_MY_FANS_COUNTS = "redis_my_fans_counts";
    // 博主和粉丝的关联关系，用于判断他们是否互粉
    public static final String REDIS_FANS_AND_VLOGGER_RELATIONSHIP = "redis_fans_and_vlogger_relationship";

    // 视频和发布者获赞数
    public static final String REDIS_VLOG_BE_LIKED_COUNTS = "redis_vlog_be_liked_counts";
    public static final String REDIS_VLOGER_BE_LIKED_COUNTS = "redis_vloger_be_liked_counts";

    // 用户是否喜欢/点赞视频，取代数据库的关联关系，1：喜欢，0：不喜欢（默认） redis_user_like_vlog:{userId}:{vlogId}
    public static final String REDIS_USER_LIKE_VLOG = "redis_user_like_vlog";

    // 消息路由前缀
    public static final String SYS_MSG_PREFIX = "sys.msg.";

    public PagedGridResult setterPagedGrid(List<?> list,
                                           Integer page) {
        PageInfo<?> pageList = new PageInfo<>(list);
        PagedGridResult gridResult = new PagedGridResult();
        gridResult.setRows(list);
        gridResult.setPage(page);
        // 设置总数据数
        gridResult.setRecords(pageList.getTotal());
        // 设置总页数
        gridResult.setTotal(pageList.getPages());
        return gridResult;
    }

    /**
     * 构建msg
     * @param fromUserId
     * @param toUserId
     * @param msgType
     * @param msgContent
     * @return
     */
    public MessageMO messageMOBuilder(String fromUserId,
                                     String toUserId,
                                     Integer msgType,
                                     Map msgContent){
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(fromUserId);
        messageMO.setToUserId(toUserId);
        messageMO.setMsgType(msgType);
        messageMO.setMsgContent(msgContent);
        return messageMO;
    }

    /**
     * 校验String参数是否为空
     * @param str
     * @return
     */
    public boolean checkStrsIsNotValid(String ... str){
        for (String s : str) {
            if(StringUtils.isBlank(s)){
                return true;
            }
        }
        return false;
    }

    /**
     * 校验user是否存在
     * @param userId
     * @return
     */
    public boolean checkUserIsExist(String userId){
        if(StringUtils.isBlank(userId)){
            return false;
        }
        if(userService.getUser(userId) == null){
            return false;
        }
        return true;
    }

    /**
     * 校验评论是否存在
     * @param commentId
     * @return
     */
    public boolean checkCommentIsExist(String commentId){
        if(StringUtils.isBlank(commentId)){
            return false;
        }
        if(commentService.getComment(commentId) == null){
            return false;
        }
        return true;
    }

    /**
     * 校验user是否存在
     * @param vlogId
     * @return
     */
    public boolean checkVlogIsExist(String vlogId){
        if(StringUtils.isBlank(vlogId)){
            return false;
        }
        if(vlogService.getVlog(vlogId) == null){
            return false;
        }
        return true;
    }
}
