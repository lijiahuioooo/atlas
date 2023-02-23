package com.mfw.atlas.provider.service;

import com.mfw.atlas.provider.config.AtlasEnvProperties;
import com.mfw.atlas.provider.constant.InstanceChangeEnum;
import com.mfw.atlas.provider.constant.InstanceStatusEnum;
import com.mfw.atlas.provider.convert.InstanceConvert;
import com.mfw.atlas.provider.event.InstanceLogEvent;
import com.mfw.atlas.provider.event.ServiceChangeEvent;
import com.mfw.atlas.provider.grpc.InstanceOuterClass;
import com.mfw.atlas.provider.grpc.InstanceOuterClass.Instance;
import com.mfw.atlas.provider.grpc.InstanceOuterClass.SynAllInstancesRequest;
import com.mfw.atlas.provider.grpc.InstanceOuterClass.SynInstancesRequest;
import com.mfw.atlas.provider.grpc.InstanceOuterClass.GetAllInstancesRequest;
import com.mfw.atlas.provider.manager.InstanceManager;
import com.mfw.atlas.provider.model.bo.InstanceBO;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;

import com.mfw.atlas.provider.model.dto.request.GetAllInstancesRequestDTO;
import com.mfw.atlas.provider.model.dto.request.QueryInstanceRequestDTO;
import com.mfw.atlas.provider.model.po.InstancePO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author KL
 * @Time 2020/10/29 10:39 上午
 */
@Slf4j
@Service
public class InstanceChangeService implements ApplicationEventPublisherAware {

    @Autowired
    private InstanceManager instanceManager;
    private ApplicationEventPublisher eventPublisher;
    @Resource
    private AtlasEnvProperties atlasEnvProperties;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    public void pubSynAllGatewayEvent() {
        this.eventPublisher
                .publishEvent(new ServiceChangeEvent(this, InstanceChangeEnum.GATEWAY_SYN_ALL,  InstanceBO.builder().build()));
    }

    public boolean publishChangeEvent(List<InstanceBO> instances, InstanceChangeEnum instanceChangeEnum) {

        if (!CollectionUtils.isEmpty(instances)) {
            instances.forEach(t -> {
                if (t != null) {
                    this.eventPublisher.publishEvent(new ServiceChangeEvent(this, instanceChangeEnum, t));
                }
            });
        }

        return true;
    }

    public boolean checkInstanceEnv(String env) {
        return atlasEnvProperties.getInstanceEnvMap() == null ? true : atlasEnvProperties.getInstanceEnvMap().contains(env);
    }

    public void synAllInstance(SynAllInstancesRequest request) {
        List<InstanceBO> instanceBOS = InstanceConvert.toBOList(request.getInstanceList());

        instanceBOS = instanceBOS.stream()
                .filter(t -> t != null && t.getInstancePO() != null && checkInstanceEnv(t.getInstancePO().getEnvType()))
                .collect(Collectors.toList());
        //用于整理发布事件的实例
        List<InstanceBO> publishInstanceBOS = new ArrayList<>();

        //发送日志事件
        if (!CollectionUtils.isEmpty(instanceBOS)) {
            instanceBOS.forEach(t -> {
                if (t != null) {
                    this.eventPublisher.publishEvent(new InstanceLogEvent(this, t));
                    //优先判断是否需要发布事件
                    if(isNeedPublish(t)) {
                        publishInstanceBOS.add(t);
                    }
                }
            });
        }
        //修改数据库
        boolean result = instanceManager.insertOrUpdateBatch(instanceBOS);
        //发布更改事件
        if (result) {
            publishChangeEvent(publishInstanceBOS, InstanceChangeEnum.SDK_GATEWAY_SYN);
        }
    }

    public void synInstance(SynInstancesRequest request) {
        List<InstanceBO> instanceBOS = InstanceConvert.toBOList(request.getInstanceList());
        if (instanceBOS == null || instanceBOS.size() == 0) {
            return;
        }
        synInstance(instanceBOS.get(0));
    }

    public void synInstance(InstanceBO instanceBO) {
        if (instanceBO == null || instanceBO.getInstanceId() == null || instanceBO.getInstancePO() == null
                || !checkInstanceEnv(instanceBO.getInstancePO().getEnvType())) {
            return;
        }
        //发送日志事件
        this.eventPublisher.publishEvent(new InstanceLogEvent(this, instanceBO));
        //优先判断是否需要发布事件
        boolean isNeedPub = isNeedPublish(instanceBO);
        //修改数据库
        boolean result = instanceManager.insertOrUpdate(instanceBO);
        //发布更改事件
        if (result && isNeedPub) {
            this.eventPublisher.publishEvent(new ServiceChangeEvent(this, InstanceChangeEnum.SDK_GATEWAY_SYN, instanceBO));
        }
    }

    public boolean isNeedPublish(InstanceBO instanceBO) {
        if (instanceBO == null || instanceBO.getInstanceId() == null || instanceBO.getInstancePO() == null
                || !checkInstanceEnv(instanceBO.getInstancePO().getEnvType())) {
            return false;
        }
        //新实例的status=1时，需要publish
        if(InstanceStatusEnum.ENABLE.getCode().equals(instanceBO.getInstancePO().getStatus())) {
            return true;
        }
        InstanceBO oldInstanceBO = instanceManager.getByInstanceId(instanceBO.getInstanceId());
        if(oldInstanceBO == null || oldInstanceBO.getInstanceId() == null || oldInstanceBO.getInstancePO() == null
                || !checkInstanceEnv(instanceBO.getInstancePO().getEnvType())) {
            return false;
        }
        //旧实例的status=1时，需要publish
        return InstanceStatusEnum.ENABLE.getCode().equals(oldInstanceBO.getInstancePO().getStatus());
    }

    public void publishEventForSDK(String appCode, String envType, String version) {
        QueryInstanceRequestDTO queryInstanceRequestDTO = new QueryInstanceRequestDTO();
        queryInstanceRequestDTO.setAppcode(appCode);
        queryInstanceRequestDTO.setEnvType(envType);
        queryInstanceRequestDTO.setVersion(version);
        List<InstancePO> instanceList = instanceManager.queryInstance(queryInstanceRequestDTO);
        if(instanceList.size() > 0) {
            InstancePO instance = instanceList.get(0);
            InstanceBO instanceBO = instanceManager.getByInstanceId(instance.getInstanceId());
            this.eventPublisher.publishEvent(new ServiceChangeEvent(this, InstanceChangeEnum.SDK_SYN, instanceBO));
        }
    }


    public List<Instance> getAllInstance(GetAllInstancesRequest request) {
        GetAllInstancesRequestDTO requestDTO = InstanceConvert.toAllInstancesRequestDTO(request);
        List<InstanceBO> BOList = instanceManager.getAllInstance(requestDTO);
        List<InstanceOuterClass.Instance> result = new ArrayList<>();
        BOList.forEach(bo -> {
            if (bo != null && bo.getInstanceId() != null) {
                result.add(InstanceConvert.toGrpcInstance(bo));
            }
        });
        return result;
    }

}

