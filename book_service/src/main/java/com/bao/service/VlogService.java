package com.bao.service;

import com.bao.bo.VlogBO;
import com.bao.pojo.Vlog;
import com.bao.utils.PagedGridResult;
import com.bao.vo.IndexVlogVO;

public interface VlogService {
    /**
     * 保存vlog对象
     */
    void createVlog(VlogBO vlogBO);

    /**
     * 查询首页/我的主页视频信息
     * @param search
     * @return
     */
    PagedGridResult getIndexVlogVOList(String userId, String search, Integer page, Integer pageSize);

    /**
     * 查询根据视频主键详细信息
     * @return
     */
    IndexVlogVO getIndexVlogVODetailById(String userId, String vlogId);

    /**
     * 查询我的主页的vlog(私密或公开)
     * @param userId
     * @param isPrivate
     * @param page
     * @param pageSize
     * @return
     */
    PagedGridResult queryMyIndexVlog(String userId, Integer isPrivate, Integer page, Integer pageSize);

    /**
     * 把我的视频设置为私密
     */
    void changeMyVlogToPrivate(String userId, String vlogId);

    /**
     * 把我的视频设置为公开
     */
    void changeMyVlogToPublic(String userId, String vlogId);

    /**
     * 视频点赞
     * @param userId
     * @param vlogId
     */
    void userLikeVlog(String userId, String vlogerId, String vlogId);

    /**
     * 用户对视频取消点赞
     * @param userId
     * @param vlogId
     */
    void userUnlikeVlog(String userId, String vlogerId, String vlogId);

    /**
     * 通过主键获取vlog
     * @param vlogId
     */
    Vlog getVlog(String vlogId);

    /**
     * 查询我点赞过的视频列表
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PagedGridResult queryMyLikedList(String userId, Integer page, Integer pageSize);

    /**
     * 查询我 关注/是否互粉 的视频列表
     * @param myId
     * @param page
     * @param pageSize
     * @return
     */
    PagedGridResult queryMyFollowList(String myId, Integer page, Integer pageSize, Integer isFriend);

}
