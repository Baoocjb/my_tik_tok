package com.bao.service.impl;

import com.bao.repository.MsgRepository;
import com.bao.mo.MessageMO;
import com.bao.pojo.Users;
import com.bao.service.MsgService;
import com.bao.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class MsgServiceImpl implements MsgService {
    @Autowired
    private MsgRepository msgRepository;
    @Autowired
    private UserService userService;

    @Override
    public void createMsg(String fromUserId, String toUserId, Integer msgType, Map msgContent) {
        MessageMO messageMO = new MessageMO();
        Users fromUser = userService.getUser(fromUserId);

        messageMO.setFromUserId(fromUser.getId());
        messageMO.setFromFace(fromUser.getFace());
        messageMO.setFromNickname(fromUser.getNickname());

        Users toUser = userService.getUser(toUserId);
        messageMO.setToUserId(toUser.getId());

        messageMO.setMsgType(msgType);
        if(msgContent != null){
            messageMO.setMsgContent(msgContent);
        }
        messageMO.setCreateTime(new Date());
        // save和insert区别, save如果主键存在则会更新
        msgRepository.save(messageMO);
    }

    @Override
    public List<MessageMO> getMsgList(String userId, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.Direction.DESC, "createTime");
        List<MessageMO> list = msgRepository.getAllByToUserIdEqualsAndFromUserIdNotOrderByCreateTimeDesc(userId, userId, pageable);
        return list;
    }
}
