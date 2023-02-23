package com.mfw.atlas.admin.manager;


import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mfw.atlas.admin.constant.InstanceStatusEnum;
import com.mfw.atlas.admin.constant.RegisterTypeEnum;
import com.mfw.atlas.admin.constant.ServiceTypeEnum;
import com.mfw.atlas.admin.convert.ConsumerServiceConvert;
import com.mfw.atlas.admin.convert.InstanceConvert;
import com.mfw.atlas.admin.convert.ProviderServiceConvert;
import com.mfw.atlas.admin.dao.ConsumerServiceDao;
import com.mfw.atlas.admin.dao.InstanceDao;
import com.mfw.atlas.admin.dao.InstancePortDao;
import com.mfw.atlas.admin.dao.ProviderServiceDao;
import com.mfw.atlas.admin.model.bo.ConsumerServiceBO;
import com.mfw.atlas.admin.model.bo.InstanceBO;
import com.mfw.atlas.admin.model.dto.ConsumerServiceDTO;
import com.mfw.atlas.admin.model.dto.ProviderServiceDTO;
import com.mfw.atlas.admin.model.dto.ProviderServiceGroupDTO;
import com.mfw.atlas.admin.model.po.ConsumerServicePO;
import com.mfw.atlas.admin.model.po.InstancePO;
import com.mfw.atlas.admin.model.po.InstancePortPO;
import com.mfw.atlas.admin.model.po.ProviderServicePO;

import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


/**
 * 实例服务管理
 *
 * @author huangrui
 */
@Slf4j
@Component
public class InstanceManager {

    @Autowired
    private InstanceDao instanceDao;
    @Autowired
    private ProviderServiceDao providerServiceDao;
    @Autowired
    private ConsumerServiceDao consumerServiceDao;
    @Autowired
    private InstancePortDao instancePortDao;
    @Autowired
    private InstancePortManager instancePortManager;

    /**
     * 获取服务列表
     *
     * @return
     */
    public List<InstancePO> getInstances(String appcode) {

        LambdaQueryWrapper<InstancePO> lambda = new QueryWrapper<InstancePO>().lambda();

        if (!StringUtils.isEmpty(appcode)) {
            lambda.like(InstancePO::getAppCode, appcode);

            //        lambda.last("limit " + 15);
            lambda.eq(InstancePO::getStatus, InstanceStatusEnum.ENABLE.getCode());

            List<InstancePO> poList = instanceDao.selectList(lambda);

            return poList;
        }
        return new ArrayList<>();
    }


    /**
     * 获取上或下线的instanceIds
     *
     * @return
     */
    public List<InstancePO> getInstancesByStatus(List<String> instanceIds, Integer status) {

        if (CollectionUtils.isEmpty(instanceIds)) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<InstancePO> lambda = new QueryWrapper<InstancePO>().lambda();
        lambda.select(InstancePO::getInstanceId);
        lambda.in(InstancePO::getInstanceId, instanceIds);
        lambda.eq(InstancePO::getStatus, status);

        List<InstancePO> poList = instanceDao.selectList(lambda);

        return poList;
    }

    public List<ProviderServiceGroupDTO> getProviderGroups(List<ProviderServiceDTO> providerServiceDTOS) {
        Map<String, ProviderServiceGroupDTO> providerServiceGroupDTOMap = new HashMap<>();
        for(ProviderServiceDTO providerServiceDTO : providerServiceDTOS) {
            if(!providerServiceGroupDTOMap.containsKey(providerServiceDTO.getServiceName())) {
                providerServiceGroupDTOMap.put(providerServiceDTO.getServiceName(), new ProviderServiceGroupDTO(
                        providerServiceDTO.getServiceName(), providerServiceDTO.getAppCode(), 0));
            }
            int preSize = providerServiceGroupDTOMap.get(providerServiceDTO.getServiceName()).getInstanceSize();
            providerServiceGroupDTOMap.get(providerServiceDTO.getServiceName()).setInstanceSize(preSize + 1);
        }

        return new ArrayList<>(providerServiceGroupDTOMap.values());
    }

    public List<ProviderServiceDTO> getProviders(List<InstancePO> instances) {

        List<String> instanceIds = instances.stream().map(InstancePO::getInstanceId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<ProviderServicePO> lambda = new QueryWrapper<ProviderServicePO>().lambda();

        lambda.in(ProviderServicePO::getInstanceId, instanceIds);

        List<ProviderServicePO> providerServicePOList = providerServiceDao.selectList(lambda);

        List<ProviderServiceDTO> filterPoList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(providerServicePOList)) {

            List<String> providerIds = providerServicePOList.stream().map(ProviderServicePO::getInstanceId)
                    .collect(Collectors.toList());

            //通过InstanceIds，查询端口列表
            List<InstancePortPO> instancePortPOList = instancePortManager.selectListByInstanceId(providerIds);

            Map<String, InstancePO> stringInstancePOMap = instances.stream()
                    .collect(Collectors.toMap(InstancePO::getInstanceId, instancePO -> instancePO));

            //如果有重复的getInstanceId+getPort，取最后一条。
            Map<String, String> instancePortPOMap = instancePortPOList.stream()
                    .collect(Collectors
                            .toMap(InstancePortPO::getInstanceId, InstancePortPO::getPort, (key1, key2) -> key2));

            for (ProviderServicePO entity : providerServicePOList) {

                InstancePO instancePO = stringInstancePOMap.get(entity.getInstanceId());

                String port = instancePortPOMap.get(entity.getInstanceId());

                if (Objects.nonNull(instancePO) && !StringUtils.isEmpty(port)) {
                    ProviderServiceDTO providerServiceDTO = ProviderServiceConvert.toDTO(entity);
                    providerServiceDTO.setIp(instancePO.getIp());
                    providerServiceDTO.setServiceType(ServiceTypeEnum.getByCode(entity.getServiceType()));
                    providerServiceDTO.setRegisterType(RegisterTypeEnum.getByCode(entity.getRegisterType()));
                    providerServiceDTO.setAppCode(instancePO.getAppCode());
                    providerServiceDTO.setEnvType(instancePO.getEnvType());
                    providerServiceDTO.setPort(port);
                    filterPoList.add(providerServiceDTO);
                }
            }
        }

        return filterPoList;
    }

    public List<ConsumerServiceBO> getByProviderInstanceId(List<ProviderServiceDTO> providerServiceDTOS) {

        List<String> serviceKeys = providerServiceDTOS.stream()
                .filter(t -> t != null && !StringUtils.isEmpty(t.getInstanceId()))
                .map(ProviderServiceDTO::getServiceKey).distinct().collect(Collectors.toList());

        LambdaQueryWrapper<ConsumerServicePO> consumerQuery = new LambdaQueryWrapper<>();
        consumerQuery.in(ConsumerServicePO::getServiceKey, serviceKeys);
        List<ConsumerServicePO> consumerList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(serviceKeys)) {
            consumerList = consumerServiceDao.selectList(consumerQuery);
        }
        List<ConsumerServiceBO> result = new ArrayList<>();
        List<String> instanceIds = consumerList.stream()
                .filter(t -> t != null && !StringUtils.isEmpty(t.getInstanceId()))
                .map(ConsumerServicePO::getInstanceId).distinct().collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(instanceIds)) {
            //查出来机器IP、 端口
            List<InstanceBO> instanceBOs = getByInstanceIds(instanceIds);

            result = ConsumerServiceConvert.toConsumerServiceBOList(consumerList, instanceBOs, providerServiceDTOS);
        }
        return result;
    }

    /**
     * 查询IP 和端口信息
     *
     * @param instanceIds
     * @return
     */
    public List<InstanceBO> getByInstanceIds(List<String> instanceIds) {
        LambdaQueryWrapper<InstancePO> insWrapper = new LambdaQueryWrapper<>();
        insWrapper.in(InstancePO::getInstanceId, instanceIds);
        List<InstancePO> instancePOS = this.instanceDao.selectList(insWrapper);

        LambdaQueryWrapper<InstancePortPO> portWrapper = new LambdaQueryWrapper<>();
        portWrapper.in(InstancePortPO::getInstanceId, instanceIds);
        List<InstancePortPO> instancePortPOS = this.instancePortDao.selectList(portWrapper);
        return InstanceConvert.toInstanceBOList(instancePOS, instancePortPOS);
    }

    public List<ConsumerServiceDTO> getConsumerList(List<InstancePO> instances) {
        List<String> instanceIds = instances.stream().map(InstancePO::getInstanceId)
                .collect(Collectors.toList());
        Map<String, InstancePO> instancePOMap = instances.stream().collect(Collectors.toMap(InstancePO::getInstanceId, v -> v));

        LambdaQueryWrapper<ConsumerServicePO> consumerQuery = new LambdaQueryWrapper<>();
        consumerQuery.in(ConsumerServicePO::getInstanceId, instanceIds);
        List<ConsumerServicePO> consumerList = consumerServiceDao.selectList(consumerQuery);

        List<ConsumerServiceDTO> list = ConsumerServiceConvert.toDTOList(consumerList);
        for(ConsumerServiceDTO consumerItem : list) {
            consumerItem.setAppCode(instancePOMap.get(consumerItem.getInstanceId()).getAppCode());
        }

        return list;
    }
}
