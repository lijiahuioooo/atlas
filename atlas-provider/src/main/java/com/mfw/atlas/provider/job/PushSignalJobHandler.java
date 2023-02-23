package com.mfw.atlas.provider.job;

import com.mfw.atlas.provider.service.InstanceChangeService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@JobHandler(value = "pushSignalJobHandler")
@Component
@Slf4j
public class PushSignalJobHandler extends IJobHandler {

    @Autowired
    private InstanceChangeService instanceChangeService;
    /**
     * 推送信号源到网关，提醒拉取服务。
     * @param param
     * @return
     * @throws Exception
     */
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        instanceChangeService.pubSynAllGatewayEvent();
        return SUCCESS;
    }

}
