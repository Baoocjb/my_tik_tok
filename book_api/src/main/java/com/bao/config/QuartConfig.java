package com.bao.config;

import com.bao.service.quartz.CommentLikeCountRefreshJob;
import com.bao.service.quartz.VlogLikeCountRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

// 配置 -> 数据库 -> 调用
@Configuration
public class QuartConfig {

    // FactoryBean可简化Bean的实例化过程
    // 1.通过FactoryBean可以简化实例化Bean的过程
    // 2.将FactoryBean装配到Spring容器中管理
    // 3.将FactoryBean注入给其他的Bean
    // 4.其他的Bean得到的是FactoryBean所管理的对象实例

    // 配置JobDetail
    @Bean
    public JobDetailFactoryBean commentLikeCountRefreshJobDetail(){
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setJobClass(CommentLikeCountRefreshJob.class);
        jobDetailFactoryBean.setName("commentLikeCountRefreshJob");
        jobDetailFactoryBean.setGroup("redBookJobGroup");
        jobDetailFactoryBean.setDurability(true);// 持久保存
        jobDetailFactoryBean.setRequestsRecovery(true);// 是否可恢复
        return jobDetailFactoryBean;
    }

    // 配置Trigger(SimpleTriggerFacterBean, CronFactoryBean)
    @Bean
    public SimpleTriggerFactoryBean commentLikeCountRefreshTrigger(JobDetail commentLikeCountRefreshJobDetail){
        SimpleTriggerFactoryBean triggerFactoryBean = new SimpleTriggerFactoryBean();
        triggerFactoryBean.setJobDetail(commentLikeCountRefreshJobDetail);
        triggerFactoryBean.setName("commentLikeCountRefreshTrigger");
        triggerFactoryBean.setGroup("redBookTriggerGroup");
        // 单位是毫秒
        triggerFactoryBean.setRepeatInterval(1000 * 1800);
        triggerFactoryBean.setJobDataMap(new JobDataMap());
        return triggerFactoryBean;
    }

    @Bean
    public JobDetailFactoryBean vlogLikeCountRefreshJobDetail(){
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setJobClass(VlogLikeCountRefreshJob.class);
        jobDetailFactoryBean.setName("vlogLikeCountRefreshJob");
        jobDetailFactoryBean.setGroup("redBookJobGroup");
        jobDetailFactoryBean.setDurability(true);// 持久保存
        jobDetailFactoryBean.setRequestsRecovery(true);// 是否可恢复
        return jobDetailFactoryBean;
    }

    // 配置Trigger(SimpleTriggerFacterBean, CronFactoryBean)
    @Bean
    public SimpleTriggerFactoryBean vlogLikeCountRefreshTrigger(JobDetail vlogLikeCountRefreshJobDetail){
        SimpleTriggerFactoryBean triggerFactoryBean = new SimpleTriggerFactoryBean();
        triggerFactoryBean.setJobDetail(vlogLikeCountRefreshJobDetail);
        triggerFactoryBean.setName("vlogLikeCountRefreshTrigger");
        triggerFactoryBean.setGroup("redBookTriggerGroup");
        // 单位是毫秒
        triggerFactoryBean.setRepeatInterval(1000 * 1800);
        triggerFactoryBean.setJobDataMap(new JobDataMap());
        return triggerFactoryBean;
    }

}
