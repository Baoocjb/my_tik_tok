package com.bao.service.impl;

import com.bao.bo.UpdatedUserBO;
import com.bao.enums.Sex;
import com.bao.enums.UserInfoModifyType;
import com.bao.enums.YesOrNo;
import com.bao.exception.GraceException;
import com.bao.mapper.UsersMapper;
import com.bao.pojo.Users;
import com.bao.result.ResponseStatusEnum;
import com.bao.service.UserService;
import com.bao.base.BaseInfoProperties;
import com.bao.utils.DateUtil;
import com.bao.utils.DesensitizationUtil;
import com.bao.utils.SensitiveFilterUtil;
import com.bao.vo.UsersVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;

@Service
public class UserServiceImpl extends BaseInfoProperties implements UserService {
    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private Sid sid;
    @Autowired
    private SensitiveFilterUtil sensitiveFilterUtil;
    // 用户默认头像
    private static final String USER_FACE1 = "http://122.152.205.72:88/group1/M00/00/05/CpoxxF6ZUySASMbOAABBAXhjY0Y649.png";

    @Override
    public Users queryUserIsExist(String mobile) {
        Example example = new Example(Users.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("mobile", mobile);
        return usersMapper.selectOneByExample(example);
    }

    @Transactional
    @Override
    public Users createUser(String mobile) {
        // 获得全局唯一主键
        String userId = sid.nextShort();
        Users user = new Users();
        user.setId(userId);

        user.setMobile(mobile);
        user.setNickname("用户：" + DesensitizationUtil.commonDisplay(mobile));
        user.setImoocNum("用户：" + DesensitizationUtil.commonDisplay(mobile));
        user.setFace(USER_FACE1);

        user.setBirthday(DateUtil.stringToDate("1900-01-01"));
        user.setSex(Sex.secret.type);

        user.setCountry("中国");
        user.setProvince("");
        user.setCity("");
        user.setDistrict("");
        user.setDescription("这家伙很懒，什么都没留下~");
        user.setCanImoocNumBeUpdated(YesOrNo.YES.type);

        user.setCreatedTime(new Date());
        user.setUpdatedTime(new Date());

        usersMapper.insert(user);
        return user;
    }

    @Override
    public Users getUser(String userId) {
        return usersMapper.selectByPrimaryKey(userId);
    }

    @Override
    public Users updateUserInfo(UpdatedUserBO updatedUserBO) {
        Users users = new Users();
        BeanUtils.copyProperties(updatedUserBO, users);
        int result = usersMapper.updateByPrimaryKeySelective(users);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.USER_UPDATE_ERROR);
        }
        return getUser(updatedUserBO.getId());
    }

    @Override
    public Users updateUserInfo(UpdatedUserBO updatedUserBO, Integer type) {
        Example example = new Example(Users.class);
        Example.Criteria criteria = example.createCriteria();
        // 判断昵称是否能被修改
        if(UserInfoModifyType.NICKNAME.type.equals(type)){
            boolean lawful = sensitiveFilterUtil.isLawful(updatedUserBO.getNickname());
            if(!lawful){
                GraceException.display(ResponseStatusEnum.USER_INFO_NICKNAME_UNLAWFUL_ERROR);
            }
            criteria.andEqualTo("nickname",updatedUserBO.getNickname());
            Users users = usersMapper.selectOneByExample(example);
            if(users != null){
                GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_NICKNAME_EXIST_ERROR);
            }
        }
        // 判断慕课号是否能被修改
        if(UserInfoModifyType.IMOOCNUM.type.equals(type)){
            boolean lawful = sensitiveFilterUtil.isLawful(updatedUserBO.getImoocNum());
            if(!lawful){
                GraceException.display(ResponseStatusEnum.USER_INFO_IMOOCNUM_UNLAWFUL_ERROR);
            }
            criteria.andEqualTo("imoocNum",updatedUserBO.getImoocNum());
            Users users = usersMapper.selectOneByExample(example);
            if(users != null){
                GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_IMOOCNUM_EXIST_ERROR);
            }
            users = getUser(updatedUserBO.getId());
            if(!YesOrNo.YES.type.equals(users.getCanImoocNumBeUpdated())){
                GraceException.display(ResponseStatusEnum.USER_INFO_CANT_UPDATED_IMOOCNUM_ERROR);
            }
            updatedUserBO.setCanImoocNumBeUpdated(YesOrNo.NO.type);
        }
        if(UserInfoModifyType.DESC.type.equals(type)){
            boolean lawful = sensitiveFilterUtil.isLawful(updatedUserBO.getDescription());
            if(!lawful){
                GraceException.display(ResponseStatusEnum.USER_INFO_DESC_UNLAWFUL_ERROR);
            }
        }
        return updateUserInfo(updatedUserBO);
    }

    @Override
    public UsersVO queryUserInfo(String userId) {
        Users user = getUser(userId);
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user, usersVO);

        // 通过查询 redis 中的数据来设置 VO 属性
        // 我的关注总数
        String myFollowsCountStr = redis.get(REDIS_MY_FOLLOWS_COUNTS + ":" + userId);
        // 我的粉丝总数
        String myFansCountStr = redis.get(REDIS_MY_FANS_COUNTS + ":" + userId);
        // 我的视频/评论被点赞总数
        String myVlogerBelikedCountStr = redis.get(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + userId);

        Integer myFollowsCounts = 0;
        Integer myFansCounts = 0;
        Integer myVlogerBelikedCounts = 0;
        Integer totalLikeMeCounts = 0;
        if (StringUtils.isNotBlank(myFollowsCountStr)) {
            myFollowsCounts = Integer.parseInt(myFollowsCountStr);
        }
        if (StringUtils.isNotBlank(myFansCountStr)) {
            myFansCounts = Integer.parseInt(myFansCountStr);
        }
        if (StringUtils.isNotBlank(myVlogerBelikedCountStr)) {
            myVlogerBelikedCounts = Integer.parseInt(myVlogerBelikedCountStr);
        }
        totalLikeMeCounts = myVlogerBelikedCounts;
        usersVO.setMyFollowsCounts(myFollowsCounts);
        usersVO.setMyFansCounts(myFansCounts);
        usersVO.setTotalLikeMeCounts(totalLikeMeCounts);
        return usersVO;
    }


}
