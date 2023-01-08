package com.bao.controller;

import com.bao.bo.UpdatedUserBO;
import com.bao.config.MinIOConfig;
import com.bao.enums.FileTypeEnum;
import com.bao.enums.UserInfoModifyType;
import com.bao.pojo.Users;
import com.bao.result.GraceJSONResult;
import com.bao.result.ResponseStatusEnum;
import com.bao.service.UserService;
import com.bao.service.base.BaseInfoProperties;
import com.bao.utils.MinIOUtils;
import com.bao.vo.UsersVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Api(tags = "UserInfoController 用户信息接口模块")
@RequestMapping("userInfo")
@RestController
public class UserInfoController extends BaseInfoProperties {

    @Autowired
    private MinIOConfig minIOConfig;

    @Autowired
    private UserService userService;

    @GetMapping("query")
    public GraceJSONResult query(@RequestParam String userId) {
        UsersVO usersVO = userService.queryUserInfo(userId);
        return GraceJSONResult.ok(usersVO);
    }

    @PostMapping("modifyUserInfo")
    public GraceJSONResult modifyUserInfo(@RequestBody UpdatedUserBO updatedUserBO, @RequestParam Integer type) {
        // 判断type是否合法
        UserInfoModifyType.checkUserInfoTypeIsRight(type);
        Users newUserInfo = userService.updateUserInfo(updatedUserBO, type);
        return GraceJSONResult.ok(newUserInfo);
    }


    @PostMapping("modifyImage")
    public GraceJSONResult modifyImage(@RequestParam String userId, @RequestParam Integer type, MultipartFile file) throws Exception {
        Users user = userService.getUser(userId);
        // 校验 type
        if(type != FileTypeEnum.FACE.type && type != FileTypeEnum.BGIMG.type && user == null){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_INFO_UPDATED_ERROR);
        }

        String filename = file.getOriginalFilename();
        MinIOUtils.uploadFile(minIOConfig.getBucketName(), filename, file.getInputStream());
        String imgUrl = minIOConfig.getFileHost() + "/" + minIOConfig.getBucketName() + "/" + filename;

        UpdatedUserBO updatedUserBO = new UpdatedUserBO();
        if(type == FileTypeEnum.FACE.type){
            updatedUserBO.setFace(imgUrl);
        }else {
            updatedUserBO.setBgImg(imgUrl);
        }
        updatedUserBO.setId(userId);
        userService.updateUserInfo(updatedUserBO);
        return GraceJSONResult.ok(updatedUserBO);
    }


}
