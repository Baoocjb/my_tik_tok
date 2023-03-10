package com.bao.service.impl;

import com.bao.bo.VlogBO;
import com.bao.enums.MessageEnum;
import com.bao.enums.YesOrNo;
import com.bao.exception.GraceException;
import com.bao.mapper.MyLikedVlogMapper;
import com.bao.mapper.VlogMapper;
import com.bao.mapper.VlogMapperCustom;
import com.bao.pojo.MyLikedVlog;
import com.bao.pojo.Vlog;
import com.bao.result.ResponseStatusEnum;
import com.bao.service.FansService;
import com.bao.service.MsgService;
import com.bao.service.VlogService;
import com.bao.base.BaseInfoProperties;
import com.bao.base.RabbitMQConfig;
import com.bao.utils.JsonUtils;
import com.bao.utils.PagedGridResult;
import com.bao.vo.IndexVlogVO;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VlogServiceImpl extends BaseInfoProperties implements VlogService {
    @Autowired
    private VlogMapper vlogMapper;
    @Autowired
    private VlogMapperCustom vlogMapperCustom;
    @Autowired
    private FansService fansService;
    @Autowired
    private MyLikedVlogMapper myLikedVlogMapper;
    @Autowired
    private MsgService msgService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private Sid sid;

    @Transactional
    @Override
    public void createVlog(VlogBO vlogBO) {
        Vlog vlog = new Vlog();
        String vid = sid.nextShort();
        BeanUtils.copyProperties(vlogBO, vlog);
        vlog.setId(vid);
        vlog.setCreatedTime(new Date());
        vlog.setUpdatedTime(new Date());
        vlog.setIsPrivate(YesOrNo.NO.type);
        vlog.setCommentsCounts(0);
        vlog.setLikeCounts(0);
        vlogMapper.insert(vlog);
    }

    @Override
    public PagedGridResult getIndexVlogVOList(String userId, String search, Integer page, Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(search)) {
            map.put("search", search);
        }
        // ????????????
        PageHelper.startPage(page, pageSize);
        List<IndexVlogVO> indexVlogVOList = vlogMapperCustom.getIndexVlogVOList(map);
        if (indexVlogVOList != null) {
            // ??? ??????/?????? ??????????????????
            indexVlogVOList.forEach(indexVlogVO -> {
                setterIndexVlogVO(indexVlogVO, userId);
            });
        }
        return setterPagedGrid(indexVlogVOList, page);
    }

    private IndexVlogVO setterIndexVlogVO(IndexVlogVO indexVlogVO, String userId){
        String vlogerId = indexVlogVO.getVlogerId();
        if (StringUtils.isNotBlank(userId)) {
            // ???????????????????????????
            if (fansService.queryDoIFollowVloger(userId, vlogerId)) {
                indexVlogVO.setDoIFollowVloger(true);
            }
            // ????????????????????????????????????
            if (queryDoILikeVlog(userId, indexVlogVO.getId())){
                indexVlogVO.setDoILikeThisVlog(true);
            }
        }
        // ??????????????????
        indexVlogVO.setLikeCounts(queryVlogBeLikedCounts(indexVlogVO.getId()));
        indexVlogVO.setLikeCounts(queryVlogBeLikedCounts(indexVlogVO.getId()));
        // ??????????????????
        String commentCountStr = redis.get(REDIS_VLOG_COMMENT_COUNTS + ":" + indexVlogVO.getId());
        if(StringUtils.isBlank(commentCountStr)){
            commentCountStr = "0";
        }
        indexVlogVO.setCommentsCounts(Integer.parseInt(commentCountStr));
        return indexVlogVO;
    }

    private int queryVlogBeLikedCounts(String vlogId){
        String vlogBeLikedCounts = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId);
        if(StringUtils.isNotBlank(vlogBeLikedCounts)){
            return Integer.parseInt(vlogBeLikedCounts);
        }
        return 0;
    }

    /**
     * ?????????????????????????????????????????????
     * @param userId
     * @param vlogId
     * @return
     */
    public boolean queryDoILikeVlog(String userId, String vlogId){
        String isLike = redis.get(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId);
        if(StringUtils.isNotBlank(isLike) && "1".equalsIgnoreCase(isLike)){
            return true;
        }
        return false;
    }

    @Override
    public IndexVlogVO getIndexVlogVODetailById(String userId, String vlogId) {
        Map<String, Object> map = new HashMap<>();
        map.put("vlogId", vlogId);
        List<IndexVlogVO> indexVlogVODetail = vlogMapperCustom.getIndexVlogVODetail(map);
        if (indexVlogVODetail != null && indexVlogVODetail.size() == 1) {
            return setterIndexVlogVO(indexVlogVODetail.get(0), userId);
        }
        return null;
    }

    @Override
    public PagedGridResult queryMyIndexVlog(String userId, Integer isPrivate, Integer page, Integer pageSize) {
        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId", userId);
        criteria.andEqualTo("isPrivate", isPrivate);

        PageHelper.startPage(page, pageSize);
        // ???????????????????????????
        List<Vlog> list = vlogMapper.selectByExample(example);
        return setterPagedGrid(list, page);
    }

    @Override
    public PagedGridResult queryMyLikedList(String userId, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        Map<String, Object> map = new HashMap<>();
        map.put("myId", userId);
        List<IndexVlogVO> list = vlogMapperCustom.queryMyLikedList(map);
        return setterPagedGrid(list, page);
    }

    @Override
    public PagedGridResult queryMyFollowList(String myId, Integer page, Integer pageSize, Integer isFriend) {
        PageHelper.startPage(page, pageSize);
        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);
        map.put("isFriend", isFriend);
        List<IndexVlogVO> list = vlogMapperCustom.queryMyFollowList(map);
        if (list != null) {
            // ??? ??????/?????? ??????????????????
            list.forEach(indexVlogVO -> {
                setterIndexVlogVO(indexVlogVO, myId);
            });
        }
        return setterPagedGrid(list, page);
    }

    @Override
    public Integer getTotalLikedCounts(String vlogId) {
        String totalLikedCounts = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId);
        if(StringUtils.isBlank(totalLikedCounts)){
            totalLikedCounts = "0";
        }
        return Integer.parseInt(totalLikedCounts);
    }

    @Override
    @Transactional
    public void changeMyVlogToPrivate(String userId, String vlogId) {
        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId", userId);
        criteria.andEqualTo("id", vlogId);
        Vlog vlog = new Vlog();
        vlog.setIsPrivate(YesOrNo.YES.type);
        int result = vlogMapper.updateByExampleSelective(vlog, example);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.VLOG_UPDATED_ERROR);
        }
    }

    @Override
    @Transactional
    public void changeMyVlogToPublic(String userId, String vlogId) {
        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId", userId);
        criteria.andEqualTo("id", vlogId);
        Vlog vlog = new Vlog();
        vlog.setIsPrivate(YesOrNo.NO.type);
        int result = vlogMapper.updateByExampleSelective(vlog, example);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.VLOG_UPDATED_ERROR);
        }
    }

    @Override
    public void userLikeVlog(String userId, String vlogerId, String vlogId) {
        // ??????????????????
        String lid = sid.nextShort();
        MyLikedVlog myLikedVlog = new MyLikedVlog();
        myLikedVlog.setId(lid);
        myLikedVlog.setVlogId(vlogId);
        myLikedVlog.setUserId(userId);
        myLikedVlogMapper.insert(myLikedVlog);

        // ????????? redis
        redis.increment(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId, 1);
        redis.increment(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + vlogerId, 1);
        redis.set(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId, "1");

        // ????????????: ???????????????
        Vlog vlog = this.getVlog(vlogId);
        Map<String, Object> msgContent = new HashMap<>();
        msgContent.put("vlogCover", vlog.getCover());
        msgContent.put("vlogId", vlog.getId());
//        msgService.createMsg(userId, vlogerId, MessageEnum.LIKE_VLOG.type, msgContent);

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG,
                SYS_MSG_PREFIX + MessageEnum.LIKE_VLOG.enValue,
                JsonUtils.objectToJson(messageMOBuilder(userId, vlogerId, MessageEnum.LIKE_VLOG.type, msgContent)));
    }

    @Override
    public void userUnlikeVlog(String userId, String vlogerId, String vlogId) {
        // ?????????????????????
        Example example = new Example(MyLikedVlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogId", vlogId);
        criteria.andEqualTo("userId", userId);
        myLikedVlogMapper.deleteByExample(example);

        // ?????? redis
        redis.decrement(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId, 1);
        redis.decrement(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + vlogerId, 1);
        redis.set(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId, "0");
    }

    @Override
    public Vlog getVlog(String vlogId) {
        return vlogMapper.selectByPrimaryKey(vlogId);
    }


}
