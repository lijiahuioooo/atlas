package com.mfw.atlas.provider.job;

import com.mfw.atlas.provider.manager.ConsumerServiceManager;
import com.mfw.atlas.provider.manager.InstanceManager;
import com.mfw.atlas.provider.manager.InstancePortManager;
import com.mfw.atlas.provider.manager.ProviderServiceManager;
import com.mfw.atlas.provider.model.po.InstancePO;
import com.mfw.atlas.provider.util.GsonUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@JobHandler(value = "cleaningDirtyDataJobHandler")
@Component
@Slf4j
public class CleaningDirtyDataJobHandler extends IJobHandler {
    @Autowired
    private InstanceManager instanceManager;

    @Autowired
    private InstancePortManager instancePortManager;

    @Autowired
    private ProviderServiceManager providerServiceManager;

    @Autowired
    private ConsumerServiceManager consumerServiceManager;

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        try {
            int days = Integer.valueOf(param);
            List<InstancePO> instancePOList = instanceManager.getDirtyInstance(days);
            if (!CollectionUtils.isEmpty(instancePOList)) {
                List<String> removeInstancesIds = instancePOList.stream().map(InstancePO::getInstanceId)
                        .collect(Collectors.toList());
                List<Long> ids = instancePOList.stream().map(InstancePO::getId)
                        .collect(Collectors.toList());
                XxlJobLogger.log("remove instance ids:" + GsonUtils.toJsonString(removeInstancesIds));
                providerServiceManager.removeProviderByOfflineInstance(removeInstancesIds);
                consumerServiceManager.removeConsumerByOfflineInstance(removeInstancesIds);
                instancePortManager.removeOfflineInstancePort(removeInstancesIds);
                instanceManager.removeOfflineInstance(ids);
            }
        } catch (Exception e) {
            XxlJobLogger.log("clean instance execute exception:" + e.getMessage());
        }

        return SUCCESS;
    }

}
