package com.bao.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UsersVO {
    private String id;
    private String mobile;
    private String nickname;
    private String imoocNum;
    private String face;
    private Integer sex;
    private Date birthday;
    private String country;
    private String province;
    private String city;
    private String district;
    private String description;
    private String bgImg;
    private Integer canImoocNumBeUpdated;
    private Date createdTime;
    private Date updatedTime;

    // 用户token，传递给前端
    private String userToken;
    // 用户关注数量
    private Integer myFollowsCounts;
    // 用户粉丝数量
    private Integer myFansCounts;
    // 用户获赞数量
    private Integer totalLikeMeCounts;
}
