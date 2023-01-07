package com.bao.service.impl;

import com.bao.enums.YesOrNo;
import com.bao.mapper.FansMapper;
import com.bao.mapper.FansMapperCustom;
import com.bao.pojo.Fans;
import com.bao.service.FansService;
import com.bao.service.base.BaseInfoProperties;
import com.bao.utils.PagedGridResult;
import com.bao.vo.FansVO;
import com.bao.vo.VlogerVO;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
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
