package com.mfw.atlas.provider.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mfw.atlas.client.model.dto.ProviderServiceDTO;
import com.mfw.atlas.provider.constant.InstanceStatusEnum;
import com.mfw.atlas.provider.convert.ProviderServiceConvert;
import com.mfw.atlas.provider.dao.ProviderServiceDao;
import com.mfw.atlas.provider.model.po.InstancePO;
import com.mfw.atlas.provider.model.po.ProviderServicePO;
import com.mfw.atlas.provider.util.Md5Utils;
import com.xxl.job.core.log.XxlJobLogger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


/**
 * 提供方服务管理
 *
 * @author huangrui
 */
@Slf4j
@Component
@Configuration
public class ProviderServiceManager {

    @Autowired
    private ProviderServiceDao providerServiceDao;

    @Autowired
    private InstanceManager instanceManager;

    /**
     * 添加
     *
     * @param providerServiceDTO
     * @return
     */
    public ProviderServiceDTO insert(ProviderServiceDTO providerServiceDTO) {
        if (!Objects.nonNull(providerServiceDTO)) {
            return new ProviderServiceDTO();
        }

        ProviderServicePO po = ProviderServiceConvert.toPO(providerServiceDTO);

        providerServiceDao.insert(po);
        return providerServiceDTO;
    }

    /**
     * 元数据上报-批量添加
     *
     * @param providerServiceDTOS
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveBatch(List<ProviderServiceDTO> providerServiceDTOS) {
        if (CollectionUtils.isEmpty(providerServiceDTOS)) {
            return false;
        }
        List<ProviderServicePO> insertList = new ArrayList<>();
        List<ProviderServicePO> updateList = new ArrayList<>();

        List<String> serviceKeys = new ArrayList<>();

        for (ProviderServiceDTO entity : providerServiceDTOS) {
            String serviceKey = getServiceKeyHashCode(entity);
            if (StringUtils.isNotEmpty(entity.getInstanceId()) && StringUtils.isNotEmpty(entity.getServiceName())
                    && !serviceKeys.contains(serviceKey)) {
                QueryWrapper<ProviderServicePO> queryWrapper = new QueryWrapper<>();

                entity.setServiceKey(serviceKey);

                queryWrapper.lambda().eq(ProviderServicePO::getServiceKey, serviceKey);
                queryWrapper.lambda().eq(ProviderServicePO::getInstanceId, entity.getInstanceId());

                ProviderServicePO providerServicePO = providerServiceDao.selectOne(queryWrapper);

                if (Objects.nonNull(providerServicePO)) {
                    entity.setId(providerServicePO.getId());
                    updateList.add(ProviderServiceConvert.toPO(entity));
                    serviceKeys.add(serviceKey);
                } else {
                    insertList.add(ProviderServiceConvert.toPO(entity));
                }


            } else {
                log.warn("元数据上报为空的entity:{}", entity);
            }
        }

        //批量添加
        if (!CollectionUtils.isEmpty(insertList)) {
            providerServiceDao.insert(insertList);
        }
        //批量更新
        if (!CollectionUtils.isEmpty(updateList)) {
            providerServiceDao.updateBatch(updateList);
        }
        return true;
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

    /**
     * 删除provider已经摘除的实例 （status=2）
     *
     * @return
     */
    public boolean removeOfflineProviderInstances(int limit, String ctime) {

        List<ProviderServicePO> providerServicePOList = providerServiceDao.selectList(
                new LambdaQueryWrapper<ProviderServicePO>()
                        .select(ProviderServicePO::getInstanceId, ProviderServicePO::getId)
                        .orderByAsc(ProviderServicePO::getId)
                        .last("limit " + limit).lt(ProviderServicePO::getCtime, ctime));

        XxlJobLogger.log("prodiver匹配的信息:{}", providerServicePOList);
        if (!CollectionUtils.isEmpty(providerServicePOList)) {

            List<String> instanceIds = providerServicePOList.stream().map(ProviderServicePO::getInstanceId)
                    .collect(Collectors.toList());

            //查询已删除的实例
            List<InstancePO> removeInstances = instanceManager
                    .getInstancesByStatus(instanceIds, InstanceStatusEnum.OFFLINE.getCode());

            if (!CollectionUtils.isEmpty(removeInstances)) {

                List<String> removeInstancesIds = removeInstances.stream().map(InstancePO::getInstanceId)
                        .collect(Collectors.toList());

                //有重复的instanceId
                Map<String, ProviderServicePO> providerIds = providerServicePOList.stream()
                        .collect(Collectors.toMap(k -> k.getInstanceId() + k.getId(), provider -> provider));

                providerIds.forEach((k, v) -> {

                    if (removeInstancesIds.contains(v.getInstanceId())) {
                        providerServiceDao.deleteById(v.getId());
                        XxlJobLogger.log("Provider删除的instanceId:{}", v.getId());
                    }
                });
            }
        }
        return true;
    }

    public boolean removeProviderByOfflineInstance(List<String> instanceIds) {
        if (!CollectionUtils.isEmpty(instanceIds)) {
            LambdaQueryWrapper<ProviderServicePO> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(ProviderServicePO::getInstanceId, instanceIds);
            int num = providerServiceDao.delete(wrapper);
            XxlJobLogger.log("provider remove num:" + num);
        }
        return true;
    }

}
