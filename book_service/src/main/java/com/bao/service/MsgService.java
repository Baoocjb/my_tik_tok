package com.bao.service;

import com.bao.mo.MessageMO;

import java.util.List;
import java.util.Map;

public interface MsgService {

    /**
     * 创建消息
     * @param fromUserId
     * @param toUserId
     * @param msgType
     * @param msgContent
     */
    void createMsg(String fromUserId,
                   String toUserId,
                   Integer msgType,
                   Map msgContent);

    /**
     * 分页查询消息列表
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    List<MessageMO> getMsgList(String userId, Integer page, Integer pageSize);
}
