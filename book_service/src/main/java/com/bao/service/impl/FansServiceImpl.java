package com.bao.service.impl;

import com.bao.enums.MessageEnum;
import com.bao.enums.YesOrNo;
import com.bao.mapper.FansMapper;
import com.bao.mapper.FansMapperCustom;
import com.bao.pojo.Fans;
import com.bao.service.FansService;
import com.bao.service.MsgService;
import com.bao.service.base.BaseInfoProperties;
import com.bao.service.base.RabbitMQConfig;
import com.bao.utils.JsonUtils;
import com.bao.utils.PagedGridResult;
import com.bao.vo.FansVO;
import com.bao.vo.VlogerVO;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FansServiceImpl extends BaseInfoProperties implements FansService {
    @Autowired
    private FansMapper fansMapper;
    @Autowired
    private FansMapperCustom fansMapperCustom;
    @Autowired
    private MsgService msgService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private Sid sid;

    @Override
    @Transactional
    public void doFollow(String myId, String vlogerId) {
        String fid = sid.nextShort();
        Fans fans = new Fans();
        fans.setId(fid);
        fans.setFanId(myId);
        fans.setVlogerId(vlogerId);
        Fans vlogerIsMyFans = queryFansRelationship(myId, vlogerId);
        // 如果存在关系的话
        if (vlogerIsMyFans != null){
            vlogerIsMyFans.setIsFanFriendOfMine(YesOrNo.YES.type);
            fans.setIsFanFriendOfMine(YesOrNo.YES.type);
            fansMapper.updateByPrimaryKeySelective(vlogerIsMyFans);
        }else{
            fans.setIsFanFriendOfMine(YesOrNo.NO.type);
        }
        fansMapper.insert(fans);

        // 此时 redis 要放在 controller 中/ 或者是 mysql 操作之后, 避免 service 事务回滚但是 redis 操作不能取消
        redis.increment(REDIS_MY_FOLLOWS_COUNTS + ":" + myId, 1);
        redis.increment(REDIS_MY_FANS_COUNTS + ":" + vlogerId, 1);
        redis.set(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + vlogerId, "1");

        // 系统通知: 关注
        Map<String, Object> msgContent = new HashMap<>();
        msgContent.put("isFriend", doIBeFollowed(myId, vlogerId));
//        msgService.createMsg(myId, vlogerId, MessageEnum.FOLLOW_YOU.type, msgContent);

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG,
                SYS_MSG_PREFIX + MessageEnum.FOLLOW_YOU.enValue,
                JsonUtils.objectToJson(messageMOBuilder(myId, vlogerId, MessageEnum.FOLLOW_YOU.type, msgContent)));
    }

    @Override
    @Transactional
    public void doCancel(String myId, String vlogerId) {
        // 查询我关注对方的对象
        Fans myFollow = queryFansRelationship(vlogerId, myId);
        // 如果存在朋友关系的话
        if (myFollow != null && myFollow.getIsFanFriendOfMine() == YesOrNo.YES.type){
            Fans vlogerIsMyFans = queryFansRelationship(myId, vlogerId);
            vlogerIsMyFans.setIsFanFriendOfMine(YesOrNo.NO.type);
            fansMapper.updateByPrimaryKeySelective(vlogerIsMyFans);
        }
        fansMapper.delete(myFollow);
    }

    @Override
    public boolean queryDoIFollowVloger(String myId, String vlogerId) {
        String relationship = redis.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + vlogerId);
        if(StringUtils.isNotBlank(relationship) && "1".equalsIgnoreCase(relationship)){
            return true;
        }
        return false;
    }

    @Override
    public PagedGridResult queryMyFollows(String myId, Integer page, Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);
        PageHelper.startPage(page, pageSize);
        List<VlogerVO> list = fansMapperCustom.queryMyFollows(map);
        return setterPagedGrid(list, page);
    }

    @Override
    public PagedGridResult queryMyFans(String myId, Integer page, Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);
        PageHelper.startPage(page, pageSize);
        List<FansVO> list = fansMapperCustom.queryMyFans(map);
        // 查询是否我关注了我的粉丝
        if(list != null){
            list.forEach(fansVO -> {
                if(queryDoIFollowVloger(myId, fansVO.getFanId())){
                    fansVO.setFriend(true);
                }else{
                    fansVO.setFriend(false);
                }
            });
        }
        return setterPagedGrid(list, page);
    }

    /**
     * 查询我是否被关注
     * @param myId
     * @param vlogerId
     * @return
     */
    private boolean doIBeFollowed(String myId, String vlogerId){
        String haveRelationshipStr = redis.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + vlogerId + ":" + myId);
        if(StringUtils.isBlank(haveRelationshipStr) || "0".equalsIgnoreCase(haveRelationshipStr)){
            return false;
        }
        return "1".equalsIgnoreCase(haveRelationshipStr);
    }

    /**
     * 查询我和被关注博主是否存在粉丝关系
     * @param myId
     * @param vlogerId
     * @return
     */
    private Fans queryFansRelationship(String myId, String vlogerId){
        Example example = new Example(Fans.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId", myId);
        criteria.andEqualTo("fanId", vlogerId);
        List<Fans> list = fansMapper.selectByExample(example);
        if(list != null && list.size() == 1){
            return list.get(0);
        }
        return null;
    }
}
