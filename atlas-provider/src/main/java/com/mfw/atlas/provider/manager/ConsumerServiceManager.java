package com.mfw.atlas.provider.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mfw.atlas.client.model.dto.ConsumerServiceDTO;
import com.mfw.atlas.provider.constant.InstanceStatusEnum;
import com.mfw.atlas.provider.convert.ConsumerServiceConvert;
import com.mfw.atlas.provider.dao.ConsumerServiceDao;
import com.mfw.atlas.provider.dao.ProviderServiceDao;
import com.mfw.atlas.provider.model.bo.ConsumerServiceBO;
import com.mfw.atlas.provider.model.bo.InstanceBO;
import com.mfw.atlas.provider.model.po.ConsumerServicePO;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * 消费方服务管理
 *
 * @author huangrui
 */
@Slf4j
@Component
public class ConsumerServiceManager {


    @Autowired
    private ProviderServiceDao providerServiceDao;

    @Autowired
    private ConsumerServiceDao consumerServiceDao;

    @Autowired
    private InstanceManager instanceManager;

    /**
     * 批量添加
     *
     * @param consumerServiceDTOS
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveBatch(List<ConsumerServiceDTO> consumerServiceDTOS) {
        if (CollectionUtils.isEmpty(consumerServiceDTOS)) {
            return false;
        }

        List<ConsumerServicePO> insertList = new ArrayList<>();
        List<ConsumerServicePO> updateList = new ArrayList<>();

        List<String> serviceKeys = new ArrayList<>();

        for (ConsumerServiceDTO entity : consumerServiceDTOS) {
            String serviceKey = getServiceKeyHashCode(entity);
            if (StringUtils.isNotEmpty(entity.getInstanceId()) && StringUtils.isNotEmpty(entity.getServiceName())
                    && !serviceKeys.contains(serviceKey)) {
                QueryWrapper<ConsumerServicePO> queryWrapper = new QueryWrapper<>();

                entity.setServiceKey(serviceKey);

                queryWrapper.lambda().eq(ConsumerServicePO::getServiceKey, serviceKey);
                queryWrapper.lambda().eq(ConsumerServicePO::getInstanceId, entity.getInstanceId());

                ConsumerServicePO consumerServicePO = consumerServiceDao.selectOne(queryWrapper);

                if (Objects.nonNull(consumerServicePO)) {
                    entity.setId(consumerServicePO.getId());
                    updateList.add(ConsumerServiceConvert.toPO(entity));
                    serviceKeys.add(serviceKey);
                } else {
                    insertList.add(ConsumerServiceConvert.toPO(entity));
                }
            } else {
                log.warn("订阅数据为空的entity=:{}", entity);
            }
        }

        //批量添加
        if (!CollectionUtils.isEmpty(insertList)) {
            consumerServiceDao.insert(insertList);
        }
        //批量更新
        if (!CollectionUtils.isEmpty(updateList)) {
            consumerServiceDao.updateBatch(updateList);
        }
        return true;
    }

    /**
     * 根据ID删除数据
     *
     * @param ids
     * @return
     */
    public Boolean deleteById(Long ids) {
        int effectRow = consumerServiceDao.deleteById(ids);
        return effectRow == 1;
    }

    /**
     * 根据instanceId 查询服务订阅信息与实例信息
     *
     * @param instanceId
     * @return
     */
    public List<ConsumerServiceBO> getByInstanceId(String instanceId) {
        LambdaQueryWrapper<ConsumerServicePO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsumerServicePO::getInstanceId, instanceId);

        List<ConsumerServicePO> consumerList = consumerServiceDao.selectList(queryWrapper);

        List<String> instanceIds = consumerList.stream()
                .filter(t -> t != null && StringUtils.isNotEmpty(t.getInstanceId()))
                .map(ConsumerServicePO::getInstanceId).distinct().collect(Collectors.toList());
        List<ConsumerServiceBO> result = new ArrayList<>();
        if (!CollectionUtils.isEmpty(instanceIds)) {
            //查出来机器IP、 端口
            List<InstanceBO> instanceBOs = this.instanceManager.getByInstanceIds(instanceIds);
            result = ConsumerServiceConvert.toConsumerServiceBOList(consumerList, instanceBOs);
        }

        //组装后返回数据
        return result;
    }

    /**
     * 根据服务提供方的instanceId查询服务订阅信息与实例信息
     *
     * @param instanceId
     * @return
     */
    public List<ConsumerServiceBO> getByProviderInstanceId(String instanceId) {
        LambdaQueryWrapper<ProviderServicePO> providerQuery = new LambdaQueryWrapper<>();
        providerQuery.eq(ProviderServicePO::getInstanceId, instanceId);
        List<ProviderServicePO> providerServicePOS = providerServiceDao.selectList(providerQuery);

        List<String> serviceKeys = providerServicePOS.stream()
                .filter(t -> t != null && StringUtils.isNotEmpty(t.getInstanceId()))
                .map(ProviderServicePO::getServiceKey).distinct().collect(Collectors.toList());

        LambdaQueryWrapper<ConsumerServicePO> consumerQuery = new LambdaQueryWrapper<>();
        consumerQuery.in(ConsumerServicePO::getServiceKey, serviceKeys);
        List<ConsumerServicePO> consumerList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(serviceKeys)) {
            consumerList = consumerServiceDao.selectList(consumerQuery);
        }
        List<ConsumerServiceBO> result = new ArrayList<>();
        List<String> instanceIds = consumerList.stream()
                .filter(t -> t != null && StringUtils.isNotEmpty(t.getInstanceId()))
                .map(ConsumerServicePO::getInstanceId).distinct().collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(instanceIds)) {
            //查出来机器IP、 端口
            List<InstanceBO> instanceBOs = this.instanceManager.getByInstanceIds(instanceIds);

            result = ConsumerServiceConvert.toConsumerServiceBOList(consumerList, instanceBOs);
        }
        return result;
    }

    /**
     * 获取servicekey的hashcode
     *
     * @param consumerServiceDTO
     * @return
     */
    public String getServiceKeyHashCode(ConsumerServiceDTO consumerServiceDTO) {
//        if (StringUtils.isEmpty(consumerServiceDTO.getEnvType())) {
//            consumerServiceDTO.setEnvType("default");
//        }

        StringBuffer buffer = new StringBuffer();
        buffer.append(consumerServiceDTO.getServiceType().getCode()).append(".");
        buffer.append(consumerServiceDTO.getServiceName()).append(".");
        buffer.append(consumerServiceDTO.getServiceGroup()).append(".");
        buffer.append(consumerServiceDTO.getServiceVersion()).append(".");
        buffer.append(consumerServiceDTO.getEnvType());

        return Md5Utils.getMD5(buffer.toString().getBytes(Charset.forName("UTF-8")));
    }

    /**
     * 删除consumer已经摘除的实例 （status=2）
     *
     * @return
     */
    public boolean removeOfflineConsumerInstances(int limit, String ctime) {

        List<ConsumerServicePO> consumerServicePOS = consumerServiceDao.selectList(
                new LambdaQueryWrapper<ConsumerServicePO>()
                        .select(ConsumerServicePO::getInstanceId, ConsumerServicePO::getId)
                        .orderByAsc(ConsumerServicePO::getId)
                        .last("limit " + limit).lt(ConsumerServicePO::getCtime, ctime));

        XxlJobLogger.log("consumer匹配的信息:{}", consumerServicePOS);
        if (!CollectionUtils.isEmpty(consumerServicePOS)) {

            List<String> instanceIds = consumerServicePOS.stream().map(ConsumerServicePO::getInstanceId)
                    .collect(Collectors.toList());

            //查看下线实例
            List<InstancePO> removeInstances = instanceManager
                    .getInstancesByStatus(instanceIds, InstanceStatusEnum.OFFLINE.getCode());

            if (!CollectionUtils.isEmpty(removeInstances)) {
                List<String> removeInstancesIds = removeInstances.stream().map(InstancePO::getInstanceId)
                        .collect(Collectors.toList());

                //有重复的instanceId
                Map<String, ConsumerServicePO> providerIds = consumerServicePOS.stream()
                        .collect(Collectors.toMap(k -> k.getInstanceId() + k.getId(), provider -> provider));

                providerIds.forEach((k, v) -> {

                    if (removeInstancesIds.contains(v.getInstanceId())) {
                        consumerServiceDao.deleteById(v.getId());
                        XxlJobLogger.log("Consumer删除的instanceId:{}", v.getId());
                    }
                });

            }
        }
        return true;
    }

    public boolean removeConsumerByOfflineInstance(List<String> instanceIds) {
        if (!CollectionUtils.isEmpty(instanceIds)) {
            LambdaQueryWrapper<ConsumerServicePO> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(ConsumerServicePO::getInstanceId, instanceIds);
            int num = consumerServiceDao.delete(wrapper);
            XxlJobLogger.log("remove consumer num:" + num);
        }
        return true;
    }
}
