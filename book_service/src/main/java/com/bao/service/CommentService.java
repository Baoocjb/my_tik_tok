package com.bao.service;

import com.bao.bo.CommentBO;
import com.bao.pojo.Comment;
import com.bao.utils.PagedGridResult;
import com.bao.vo.CommentVO;

public interface CommentService {
    /**
     * 新增评论
     * @param commentBO
     */
    CommentVO createComment(CommentBO commentBO);

    /**
     * 用户点赞评论
     * @param userId
     * @param commentId
     */
    void userLikeComment(String userId, String commentId);

    /**
     * 查询评论
     * @param commentId
     * @return
     */
    Comment getComment(String commentId);

    /**
     * 查询视频的评论数量
     * @param vlogId
     * @return
     */
    int getCommentCounts(String vlogId);

    /**
     * 用户取消对评论的点赞
     * @param userId
     * @param commentId
     */
    void userunLikeComment(String userId, String commentId);

    /**
     * 查询评论列表
     * @param vlogId
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PagedGridResult getCommentList(String vlogId, String userId, Integer page, Integer pageSize);
}
