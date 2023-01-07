package com.bao.service;

import com.bao.utils.PagedGridResult;

public interface FansService {

    /**
     * 关注用户功能
     * @param myId
     * @param vlogerId
     */
    public void doFollow(String myId, String vlogerId);

    /**
     * 取消关注用户
     * @param myId
     * @param vlogerId
     */
    public void doCancel(String myId, String vlogerId);

    /**
     * 查询我是否关注了该用户
     * @param myId
     * @param vlogerId
     * @return
     */
    boolean queryDoIFollowVloger(String myId, String vlogerId);

    /**
     * 查询我关注的博主列表
     * @param myId
     * @param page
     * @param pageSize
     * @return
     */
    PagedGridResult queryMyFollows(String myId, Integer page, Integer pageSize);

    /**
     * 查询我的粉丝列表
     * @param myId
     * @param page
     * @param pageSize
     * @return
     */
    PagedGridResult queryMyFans(String myId, Integer page, Integer pageSize);
}
