package com.mfw.atlas.provider.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mfw.atlas.client.constants.RegisterTypeEnum;
import com.mfw.atlas.client.constants.ServiceTypeEnum;
import com.mfw.atlas.client.model.dto.ProviderServiceDTO;
import com.mfw.atlas.provider.constant.ExtendedParamsConstants;
import com.mfw.atlas.provider.constant.ProtocolEnum;
import com.mfw.atlas.provider.convert.ProviderServiceConvert;
import com.mfw.atlas.provider.dao.ProviderServiceDao;
import com.mfw.atlas.provider.manager.DubboZookeeperClientManager;
import com.mfw.atlas.provider.manager.InstanceManager;
import com.mfw.atlas.provider.manager.InstancePortManager;
import com.mfw.atlas.provider.manager.NacosClientManager;
import com.mfw.atlas.provider.model.bo.InstanceLbBO;
import com.mfw.atlas.provider.model.dto.request.ServiceInstanceRequestDTO;
import com.mfw.atlas.provider.model.po.InstancePO;
import com.mfw.atlas.provider.model.po.InstancePortPO;
import com.mfw.atlas.provider.model.po.ProviderServicePO;
import com.mfw.atlas.provider.util.GsonUtils;
import com.mfw.atlas.provider.util.Md5Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author KL
 * @Time 2020/11/19 9:52 上午
 */
@Slf4j
@Service
public class ProviderService {

    @Autowired
    private ProviderServiceDao providerServiceDao;

    @Autowired
    private InstanceManager instanceManager;

    @Autowired
    private InstancePortManager instancePortManager;
    @Autowired
    private DubboZookeeperClientManager dubboZookeeperClientManager;
    @Autowired
    private NacosClientManager nacosClientManager;

    /**
     * 获取服务实例列表(含负载均衡权重)
     *
     * @param queryInfo
     * @return
     */
    public List<ProviderServiceDTO> getServiceInstances(ServiceInstanceRequestDTO queryInfo) {
        String serviceKey = getServiceKeyHashCode(ProviderServiceConvert.serviceDTOToProviderDTO(queryInfo));

        //根据serviceKey查询Provider列表
        List<ProviderServicePO> providerServicePOList = getServiceKeys(serviceKey);

        List<String> instanceIds = providerServicePOList.stream().map(ProviderServicePO::getInstanceId)
                .collect(Collectors.toList());

        //通过InstanceIds和筛选参数，查询含有负载均衡信息的实例列表
        List<InstanceLbBO> serviceInstances = instanceManager.getServiceLbInstances(instanceIds, queryInfo);

        List<String> ids = serviceInstances.stream().map(InstanceLbBO::getInstanceId)
                .collect(Collectors.toList());

        //通过InstanceIds，查询端口列表
        List<InstancePortPO> instancePortPOList = instancePortManager
                .selectListByInstanceIds(ids, ProtocolEnum.getEnumByType(queryInfo.getServiceType()).getProtocol());

        //拉取原注册中心
        if (CollectionUtils.isEmpty(serviceInstances) || CollectionUtils.isEmpty(instancePortPOList)) {
            List<ProviderServiceDTO> originalRegister = getOriginalRegister(queryInfo);

//            log.info("拉取原注册中心：parameter:{}，res:{}", queryInfo, originalRegister);
            return originalRegister;
        }

        Map<String, InstancePO> stringInstancePOMap = serviceInstances.stream()
                .collect(Collectors.toMap(InstanceLbBO::getInstanceId, InstanceLbBO::getInstancePO));
        Map<String, Integer> instanceWeightMap = serviceInstances.stream()
                .collect(Collectors.toMap(InstanceLbBO::getInstanceId, InstanceLbBO::getWeight));

        //如果有重复的getInstanceId+getPort，取最后一条。
        Map<String, String> instancePortPOMap = instancePortPOList.stream()
                .collect(Collectors.toMap(InstancePortPO::getInstanceId, InstancePortPO::getPort, (key1, key2) -> key2));

        List<ProviderServiceDTO> filterPoList = new ArrayList<>();
        for (ProviderServicePO entity : providerServicePOList) {

            InstancePO instancePO = stringInstancePOMap.get(entity.getInstanceId());

            String port = instancePortPOMap.get(entity.getInstanceId());

            if (Objects.nonNull(instancePO) && StringUtils.isNotEmpty(port)) {
                ProviderServiceDTO providerServiceDTO = ProviderServiceConvert.toDTO(entity);
                providerServiceDTO.setIp(instancePO.getIp());
                providerServiceDTO.setAppCode(instancePO.getAppCode());
                providerServiceDTO.setEnvType(instancePO.getEnvType());
                providerServiceDTO.setPort(port);
                Map<String, Object> metaMap = (Map<String, Object>)GsonUtils.fromJson(providerServiceDTO.getMetadata(), Map.class);
                if(Objects.nonNull(instanceWeightMap.get(entity.getInstanceId()))) {
                    metaMap.put("weight", instanceWeightMap.get(entity.getInstanceId()).toString());
                }
                String metaData = GsonUtils.toJsonString(metaMap);
                providerServiceDTO.setMetadata(metaData);
                filterPoList.add(providerServiceDTO);
            }
        }

        return filterPoList;
    }

    /**
     * 根据serviceKey查询数据
     *
     * @param serviceKey
     * @return
     */
    public List<ProviderServicePO> getServiceKeys(String serviceKey) {
        LambdaQueryWrapper<ProviderServicePO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProviderServicePO::getServiceKey, serviceKey);

        List<ProviderServicePO> providerServicePOs = providerServiceDao.selectList(queryWrapper);

        return providerServicePOs;
    }


    /**
     * 获取servicekey的hashcode
     *
     * @param providerServiceDTO
     * @return
     */
    public String getServiceKeyHashCode(ProviderServiceDTO providerServiceDTO) {
//        if (StringUtils.isEmpty(providerServiceDTO.getEnvType())) {
//            providerServiceDTO.setEnvType("default");
//        }

        StringBuffer buffer = new StringBuffer();
        buffer.append(providerServiceDTO.getServiceType().getCode()).append(".");
        buffer.append(providerServiceDTO.getServiceName()).append(".");
        buffer.append(providerServiceDTO.getServiceGroup()).append(".");
        buffer.append(providerServiceDTO.getServiceVersion()).append(".");
        buffer.append(providerServiceDTO.getEnvType());

        return Md5Utils.getMD5(buffer.toString().getBytes(Charset.forName("UTF-8")));
    }

    private List<ProviderServiceDTO> getOriginalRegister(ServiceInstanceRequestDTO queryInfo) {
        List<ProviderServiceDTO> providerServiceDTOS = new ArrayList<>();

        String registerType = "";
        if (Objects.nonNull(queryInfo.getExtendedMFWParams(ExtendedParamsConstants.ORIGINAL_TYPE))) {
            registerType = String.valueOf(queryInfo.getExtendedMFWParams(ExtendedParamsConstants.ORIGINAL_TYPE));
        }

        if (queryInfo.getServiceType() == ServiceTypeEnum.DUBBO.getCode() && registerType
                .equals(RegisterTypeEnum.ZOOKEEPER.getName())) {
            //给其他服务预留zk实现的位置。
            registerType = "dubbo-zk";
        }

        switch (registerType) {
            case "dubbo-zk":
                providerServiceDTOS = this.dubboZookeeperClientManager.originalRegister(queryInfo);
                break;
            case "springcloud-zk":
                break;
            case "nacos":
                providerServiceDTOS = this.nacosClientManager.originalRegister(queryInfo);
                break;
            default:
        }
        return providerServiceDTOS;
    }

}
