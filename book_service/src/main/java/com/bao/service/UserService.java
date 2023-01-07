package com.bao.service;

import com.bao.bo.UpdatedUserBO;
import com.bao.pojo.Users;
import com.bao.vo.UsersVO;


public interface UserService {
    /**
     * 查询对应手机号用户是否注册
     * @param mobile
     * @return
     */
    Users queryUserIsExist(String mobile);

    /**
     * 创建用户, 并返回
     * @param mobile
     * @return
     */
    Users createUser(String mobile);

    /**
     * 根据用户Id查询用户
     */
    Users getUser(String userId);

    /**
     * 更新用户信息
     * @param updatedUserBO
     * @return
     */
    Users updateUserInfo(UpdatedUserBO updatedUserBO);

    /**
     * 根据传入的type判断是否能够修改
     * @param updatedUserBO
     * @return
     */
    Users updateUserInfo(UpdatedUserBO updatedUserBO, Integer type);

    /**
     * 查询用户基本信息
     * @param userId
     * @return
     */
    UsersVO queryUserInfo(String userId);
}
