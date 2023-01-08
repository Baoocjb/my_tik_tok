package com.bao.service.impl;

import com.bao.bo.CommentBO;
import com.bao.enums.YesOrNo;
import com.bao.mapper.CommentMapper;
import com.bao.mapper.CommentMapperCustom;
import com.bao.pojo.Comment;
import com.bao.service.CommentService;
import com.bao.service.base.BaseInfoProperties;
import com.bao.utils.PagedGridResult;
import com.bao.vo.CommentVO;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
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
    private Sid sid;

    @Override
    @Transactional
    public CommentVO createComment(CommentBO commentBO) {
        Comment comment = new Comment();
        comment.setId(sid.nextShort());
        comment.setFatherCommentId(commentBO.getFatherCommentId());

        comment.setContent(commentBO.getContent());
        comment.setCommentUserId(commentBO.getCommentUserId());
        comment.setCreateTime(new Date());

        comment.setVlogId(commentBO.getVlogId());
        comment.setVlogerId(commentBO.getVlogerId());
        comment.setLikeCounts(0);
        commentMapper.insert(comment);

        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment, commentVO);

        // 操作 redis
        redis.increment(REDIS_VLOG_COMMENT_COUNTS + ":" + commentBO.getVlogId(), 1);
        return commentVO;
    }

    @Override
    public void userLikeComment(String userId, String commentId) {
        // 利用Hash存储, 方便定期入库
        redis.incrementHash(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId, 1);
        redis.set(REDIS_USER_LIKE_COMMENT + ":" + userId + ":" + commentId, "1");

        // TODO 添加定时任务, 刷入数据库
        // https://cloud.tencent.com/developer/article/1536852
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
    public void userunLikeComment(String userId, String commentId) {
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
                // 有回复别人
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
