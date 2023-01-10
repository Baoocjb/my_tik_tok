package com.bao.service.customer;

import com.bao.exception.GraceException;
import com.bao.mo.MessageMO;
import com.bao.result.ResponseStatusEnum;
import com.bao.service.MsgService;
import com.bao.service.base.BaseInfoProperties;
import com.bao.service.base.RabbitMQConfig;
import com.bao.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RabbitCustomer extends BaseInfoProperties {
    @Autowired
    private MsgService msgService;

    @RabbitListener(queues = {RabbitMQConfig.QUEUE_SYS_MSG})
    public void watchQueue(String payload, Message message){
        // 获取消息内容
        MessageMO messageMO = JsonUtils.jsonToPojo(payload, MessageMO.class);
        // 获取消息路由
        String receivedRoutingKey = message.getMessageProperties().getReceivedRoutingKey();
        if(StringUtils.isNotBlank(receivedRoutingKey) && receivedRoutingKey.startsWith(SYS_MSG_PREFIX)){
            msgService.createMsg(
                    messageMO.getFromUserId(),
                    messageMO.getToUserId(),
                    messageMO.getMsgType(),
                    messageMO.getMsgContent()
            );
        }else{
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }
    }
}
