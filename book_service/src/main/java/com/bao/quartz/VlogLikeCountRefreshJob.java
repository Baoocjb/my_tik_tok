package com.bao.quartz;

import com.bao.mapper.VlogMapper;
import com.bao.mapper.VlogMapperCustom;
import com.bao.pojo.Vlog;
import com.bao.base.BaseInfoProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class VlogLikeCountRefreshJob extends BaseInfoProperties implements Job {
    @Autowired
    private VlogMapperCustom vlogMapperCustom;
    @Autowired
    private VlogMapper vlogMapper;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        log.info("Vlog点赞数据落库开始执行! 当前时间:" + now);
        List<Vlog> vlogs = vlogMapper.selectAll();
        List<Vlog> list = new ArrayList<>(vlogs.size());
        for (Vlog v : vlogs) {
            String likeCountStr = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + v.getId());
            Vlog vlog = new Vlog();
            if(StringUtils.isNotBlank(likeCountStr)){
                vlog.setId(v.getId());
                vlog.setLikeCounts(Integer.parseInt(likeCountStr));
                list.add(vlog);
            }
        }
        if(!list.isEmpty()){
            // 创建临时表
            vlogMapperCustom.createTempTable();
            vlogMapperCustom.insertList(list);
            // 联表批量更新
            vlogMapperCustom.updateList();
            // 删除临时表
            vlogMapperCustom.delTempTable();
        }
        String end = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        log.info("Vlog点赞数据落库结束! 当前时间:" + end);
    }
}
