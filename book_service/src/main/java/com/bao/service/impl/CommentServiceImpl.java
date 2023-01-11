package com.bao.service.impl;

import com.bao.bo.CommentBO;
import com.bao.enums.MessageEnum;
import com.bao.enums.YesOrNo;
import com.bao.exception.GraceException;
import com.bao.mapper.CommentMapper;
import com.bao.mapper.CommentMapperCustom;
import com.bao.pojo.Comment;
import com.bao.pojo.Vlog;
import com.bao.result.ResponseStatusEnum;
import com.bao.service.CommentService;
import com.bao.service.MsgService;
import com.bao.service.VlogService;
import com.bao.base.BaseInfoProperties;
import com.bao.base.RabbitMQConfig;
import com.bao.utils.JsonUtils;
import com.bao.utils.PagedGridResult;
import com.bao.utils.SensitiveFilterUtil;
import com.bao.vo.CommentVO;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl extends BaseInfoProperties implements CommentService {
    @Autowired
    private CommentMapperCustom commentMapperCustom;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private MsgService msgService;
    @Autowired
    private VlogService vlogService;
    @Autowired
    private SensitiveFilterUtil sensitiveFilterUtil;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private Sid sid;

    @Override
    @Transactional
    public CommentVO createComment(CommentBO commentBO) {
        Comment comment = new Comment();
        comment.setId(sid.nextShort());
        comment.setFatherCommentId(commentBO.getFatherCommentId());

        // 设置是否包含敏感词
        if(!sensitiveFilterUtil.isLawful(commentBO.getContent())){
            GraceException.display(ResponseStatusEnum.COMMENT_UNLAWFUL_ERROR);
        }
        comment.setContent(commentBO.getContent());

        comment.setCommentUserId(commentBO.getCommentUserId());
        comment.setCreateTime(new Date());

        comment.setVlogId(commentBO.getVlogId());
        comment.setVlogerId(commentBO.getVlogerId());
        comment.setLikeCounts(0);
        comment.setIsValid(YesOrNo.YES.type);
        commentMapper.insert(comment);

        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment, commentVO);

        // 操作 redis
        redis.increment(REDIS_VLOG_COMMENT_COUNTS + ":" + commentBO.getVlogId(), 1);

        // 系统通知: 评论与回复
        Vlog vlog = vlogService.getVlog(comment.getVlogId());
        Map<String, Object> msgContent = new HashMap<>();
        msgContent.put("vlogId", vlog.getId());
        msgContent.put("vlogCover", vlog.getCover());
        msgContent.put("commentContent", comment.getContent());

        String toUserId;
        Integer type;
        String routingKeySuffix;
        if(StringUtils.isNotBlank(comment.getFatherCommentId()) && !"0".equalsIgnoreCase(comment.getFatherCommentId())){
            // 说明是对 评论视频的人 的回复
            CommentVO fatherCommentVO = commentMapperCustom.queryTheFatherComment(comment.getFatherCommentId());
            toUserId = fatherCommentVO.getCommentUserId();
            type = MessageEnum.REPLY_YOU.type;
            // 设置路由
            routingKeySuffix = MessageEnum.REPLY_YOU.enValue;
        }else{
            // 说明是对视频博主的评论
            toUserId = comment.getVlogerId();
            type = MessageEnum.COMMENT_VLOG.type;
            // 设置路由
            routingKeySuffix = MessageEnum.COMMENT_VLOG.enValue;
        }
//        msgService.createMsg(comment.getCommentUserId(), toUserId, type, msgContent);

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG,
                SYS_MSG_PREFIX + routingKeySuffix,
                JsonUtils.objectToJson(messageMOBuilder(comment.getCommentUserId(), toUserId, type, msgContent)));

        return commentVO;
    }

    @Override
    public void userLikeComment(String userId, String commentId) {
        // 利用Hash存储, 方便定期入库
        redis.incrementHash(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId, 1);
        redis.set(REDIS_USER_LIKE_COMMENT + ":" + userId + ":" + commentId, "1");

        // 系统通知: 点赞评论
        Comment comment = getComment(commentId);
        Map<String, Object> msgContent = new HashMap<>();
        Vlog vlog = vlogService.getVlog(comment.getVlogId());
        msgContent.put("vlogId", vlog.getId());
        msgContent.put("vlogCover", vlog.getCover());
//        msgService.createMsg(userId, comment.getCommentUserId(), MessageEnum.LIKE_COMMENT.type, msgContent);

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG,
                SYS_MSG_PREFIX + MessageEnum.LIKE_COMMENT.enValue,
                JsonUtils.objectToJson(messageMOBuilder(userId, comment.getCommentUserId(), MessageEnum.LIKE_COMMENT.type, msgContent)));
    }

    @Override
    public Comment getComment(String commentId) {
        return commentMapper.selectByPrimaryKey(commentId);
    }

    @Override
    public int getCommentCounts(String vlogId) {
        String countStr = redis.get(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId);
        if(StringUtils.isBlank(countStr)){
            countStr = "0";
        }
        return Integer.parseInt(countStr);
    }

    @Override
    public void userUnlikeComment(String userId, String commentId) {
        redis.del(REDIS_USER_LIKE_COMMENT + ":" + userId + ":" + commentId);
        redis.decrementHash(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId, 1);
    }

    @Override
    public PagedGridResult getCommentList(String vlogId, String userId, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        Map<String, Object> map = new HashMap<>();
        map.put("vlogId", vlogId);
        // 查询第一级评论
        List<CommentVO> list = commentMapperCustom.queryFirstLevelComments(map);
        if (list != null) {
            for(CommentVO cv : list){
                // 有回复的目标
                if(!"0".equalsIgnoreCase(cv.getFatherCommentId())){
                    CommentVO commentVO = commentMapperCustom.queryTheFatherComment(cv.getFatherCommentId());
                    // 设置被回复者的nickname
                    cv.setReplyedUserNickname(commentVO.getReplyedUserNickname());
                }
                String commentLikeCountStr = redis.getHashValue(REDIS_VLOG_COMMENT_LIKED_COUNTS, cv.getCommentId());
                if(StringUtils.isBlank(commentLikeCountStr)){
                    commentLikeCountStr = "0";
                }
                cv.setLikeCounts(Integer.parseInt(commentLikeCountStr));
                // 设置当前用户是否点赞
                cv.setIsLike(doIIsLikeComment(userId, cv.getCommentId()));
            }
        }
        return setterPagedGrid(list, page);
    }

    @Override
    @Transactional
    public void userDelComment(String commentUserId, String commentId, String vlogId) {
        Comment comment = new Comment();
        comment.setIsValid(0);
        comment.setId(commentId);
        commentMapper.updateByPrimaryKeySelective(comment);
        redis.decrement(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId, 1);
    }

    /**
     * 我是否点赞了该评论
     * @param userId
     * @param commentId
     * @return
     */
    private Integer doIIsLikeComment(String userId, String commentId){
        if(StringUtils.isNotBlank(userId)){
            String isLike = redis.get(REDIS_USER_LIKE_COMMENT + ":" + userId + ":" + commentId);
            if("1".equalsIgnoreCase(isLike)){
                return YesOrNo.YES.type;
            }
        }
        return YesOrNo.NO.type;
    }


}
