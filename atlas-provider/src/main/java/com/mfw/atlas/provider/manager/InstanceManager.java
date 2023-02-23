package com.mfw.atlas.provider.manager;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.JsonObject;
import com.mfw.atlas.provider.constant.InstanceStatusEnum;
import com.mfw.atlas.provider.convert.InstanceConvert;
import com.mfw.atlas.provider.dao.InstanceDao;
import com.mfw.atlas.provider.dao.InstancePortDao;
import com.mfw.atlas.provider.model.bo.InstanceBO;
import com.mfw.atlas.provider.model.bo.InstanceLbBO;
import com.mfw.atlas.provider.model.dto.request.GetAllInstancesRequestDTO;
import com.mfw.atlas.provider.model.dto.request.QueryInstanceRequestDTO;
import com.mfw.atlas.provider.model.dto.request.ServiceInstanceRequestDTO;
import com.mfw.atlas.provider.model.dto.response.InstanceKubsDTO;
import com.mfw.atlas.provider.model.po.DiscoveryExtensionServicePO;
import com.mfw.atlas.provider.model.po.InstancePO;
import com.mfw.atlas.provider.model.po.InstancePortPO;
import com.mfw.atlas.provider.util.GsonUtils;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private InstancePortDao instancePortDao;
    @Autowired
    private DiscoveryExtensionServiceManager discoveryExtensionServiceManager;

    /**
     * 新增
     *
     * @param instanceBO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean insert(InstanceBO instanceBO) {
        if (Objects.nonNull(instanceBO) && StringUtils.isNotEmpty(instanceBO.getInstanceId())) {
            //实例
            instanceDao.insert(instanceBO.getInstancePO());

            //实例端口
            if (null != instanceBO.getInstancePortPOS() && !instanceBO.getInstancePortPOS().isEmpty()) {
                for (InstancePortPO port : instanceBO.getInstancePortPOS()) {
                    instancePortDao.insert(port);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 添加实例、实例端口
     *
     * @param instanceBO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertOrUpdate(InstanceBO instanceBO) {
        if (instanceBO == null) {
            return false;
        }
        return generalInsertOrUpdate(instanceBO);
    }

    /**
     * 批量添加or更新 (添加实例、实例端口、实例日志)
     *
     * @param instanceBOS
     * @return
     */
    public Boolean insertOrUpdateBatch(List<InstanceBO> instanceBOS) {
        if (instanceBOS == null) {
            return false;
        }

        for (InstanceBO entiy : instanceBOS) {
            generalInsertOrUpdate(entiy);
        }

        return true;
    }

    private boolean doInsertInstance(InstanceBO entity) {
        try{
            instanceDao.insert(entity.getInstancePO());
        } catch(DuplicateKeyException e) {
            log.warn("insert instance:{} error:{}", entity.getInstancePO(), e.getMessage());
            return false;
        }
        return true;
    }

    private boolean doInsertInstancePort(InstancePortPO port) {
        try{
            instancePortDao.insert(port);
        } catch(DuplicateKeyException e) {
            log.warn("insert port:{} error:{}", port, e.getMessage());
            return false;
        }
        return true;
    }

    private boolean doUpdateInstance(InstanceBO entity) {
        //实例变化的版本号,是用来保证数据不被回滚
        LambdaQueryWrapper<InstancePO> instancePOWrapper = new QueryWrapper<InstancePO>().lambda();
        instancePOWrapper.eq(InstancePO::getInstanceId, entity.getInstanceId());
        instancePOWrapper.le(InstancePO::getReversion, entity.getInstancePO().getReversion());
        int update = instanceDao.update(entity.getInstancePO(), instancePOWrapper);
        return update > 0;
    }


    private Boolean generalInsertOrUpdate(InstanceBO entity) {
        boolean bSuccess = false;
        if (StringUtils.isNotEmpty(entity.getInstanceId())) {
            //实例
            LambdaQueryWrapper<InstancePO> instancePOWrapper = new QueryWrapper<InstancePO>().lambda();
            instancePOWrapper.eq(InstancePO::getInstanceId, entity.getInstanceId());

            if (Objects.isNull(instanceDao.selectOne(instancePOWrapper))) {
                if(!(bSuccess = doInsertInstance(entity))) {
                    bSuccess = doUpdateInstance(entity);
                }
            } else {
                bSuccess = doUpdateInstance(entity);
            }

            if(bSuccess) {
                //实例端口
                for (InstancePortPO port : entity.getInstancePortPOS()) {
                    LambdaQueryWrapper<InstancePortPO> instancePortWrapper = new QueryWrapper<InstancePortPO>().lambda();
                    instancePortWrapper.eq(InstancePortPO::getInstanceId, entity.getInstanceId());
                    instancePortWrapper.eq(InstancePortPO::getPort, port.getPort());
                    if (Objects.isNull(instancePortDao.selectOne(instancePortWrapper))) {
                        doInsertInstancePort(port);
                    } else {
                        instancePortDao.update(port, instancePortWrapper);
                    }
                }
            }
        }
        //未执行数据操作
        return bSuccess;
    }

    /**
     * 网关-更具enabled获取所有
     *
     * @return
     */
    public List<InstanceKubsDTO> queryAll(Integer enabled,String envTypes) {

        LambdaQueryWrapper<InstancePO> where = new LambdaQueryWrapper<>();
        where.eq(InstancePO::getEnabled, enabled);
        if(!StringUtils.isEmpty(envTypes)){
            String[] split = envTypes.split("@");
            where.in(InstancePO::getEnvType, split);
        }

        List<InstancePO> instancePOS = this.instanceDao.selectList(where);

        List<InstanceKubsDTO> instanceKubsDTOS = new ArrayList<>();

        if (!CollectionUtils.isEmpty(instancePOS)) {
            List<String> instanceIds = instancePOS.stream().map(InstancePO::getInstanceId).distinct()
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(instanceIds)) {
                List<InstancePortPO> instancePortPOS = this.instancePortDao.selectList(
                        new LambdaQueryWrapper<InstancePortPO>().in(InstancePortPO::getInstanceId, instanceIds));

                if (!CollectionUtils.isEmpty(instancePortPOS)) {
                    instanceKubsDTOS = InstanceConvert
                            .toInstanceOuterList(instancePOS, instancePortPOS);
                }
            }
        }

        return instanceKubsDTOS;

    }

    /**
     * 网关-获取指定实例信息
     *
     * @return
     */
    public InstanceKubsDTO gateWayFindInstance(String instanceId) {

        InstanceKubsDTO instanceKubsDTO = new InstanceKubsDTO();
        if (StringUtils.isNotEmpty(instanceId)) {

            LambdaQueryWrapper<InstancePO> lambda = new QueryWrapper<InstancePO>().lambda();
            lambda.eq(InstancePO::getInstanceId, instanceId);

            InstancePO instancePO = instanceDao.selectOne(lambda);

            if (Objects.nonNull(instancePO)) {
                LambdaQueryWrapper<InstancePortPO> portWrapper = new LambdaQueryWrapper<>();
                portWrapper.eq(InstancePortPO::getInstanceId, instanceId);
                List<InstancePortPO> instancePortPOS = this.instancePortDao.selectList(portWrapper);
                if (!CollectionUtils.isEmpty(instancePortPOS)) {
                    instanceKubsDTO = InstanceConvert.toInstanceKubsDTO(instancePO, instancePortPOS);
                }
            }
        }
        return instanceKubsDTO;
    }

    /**
     * 获取服务列表
     *
     * @return
     */
    public List<InstanceLbBO> getServiceLbInstances(List<String> instanceIds, ServiceInstanceRequestDTO queryInfo) {
        List<InstancePO> poList = getEnableInstances(instanceIds);

        List<InstanceLbBO> filterPoList = new ArrayList<>();
        //无环境或者没数据，返回全部
        if (!CollectionUtils.isEmpty(poList) && Objects.nonNull(queryInfo) && StringUtils.isNotEmpty(queryInfo.getEnvType())) {
            if(StringUtils.isBlank(queryInfo.getEnvGroup())){
                //非迭代
                filterPoList = getDefaultLbVersion(poList,queryInfo);
            }else{

                String envCode = queryInfo.getEnvType()+"#"+queryInfo.getEnvGroup();

                //在当前容器过滤
                List<InstancePO> iterationPoList = poList.stream()
                        .filter(po -> po.getEnvCode().equals(envCode))
                        .collect(Collectors.toList());

                if(CollectionUtils.isEmpty(iterationPoList)){
                    filterPoList =  getDefaultLbVersion(poList,queryInfo);
                } else {
                    filterPoList = InstanceConvert.toInstanceLbBOList(iterationPoList);
                }
            }
        }

        return filterPoList;
    }

    /**
     * 获取服务列表
     *
     * @return
     */
    public List<InstancePO> getServiceInstances(List<String> instanceIds, ServiceInstanceRequestDTO queryInfo) {
        List<InstancePO> poList = getEnableInstances(instanceIds);

        List<InstancePO> filterPoList = new ArrayList<>();
        //无环境或者没数据，返回全部
        if (!CollectionUtils.isEmpty(poList) && Objects.nonNull(queryInfo) && StringUtils.isNotEmpty(queryInfo.getEnvType())) {
            if(StringUtils.isBlank(queryInfo.getEnvGroup())){
                //非迭代
                filterPoList = getDefaultVersion(poList,queryInfo);
            }else{

                String envCode = queryInfo.getEnvType()+"#"+queryInfo.getEnvGroup();

                //在当前容器过滤
                filterPoList = poList.stream()
                        .filter(po -> po.getEnvCode().equals(envCode))
                        .collect(Collectors.toList());

                if(CollectionUtils.isEmpty(filterPoList)){
                    filterPoList =  getDefaultVersion(poList,queryInfo);
                }
            }
        }

        return filterPoList;
    }

    private List<InstancePO> getEnableInstances(List<String> instanceIds) {
        if (CollectionUtils.isEmpty(instanceIds)) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<InstancePO> lambda = new QueryWrapper<InstancePO>().lambda();

        lambda.in(InstancePO::getInstanceId, instanceIds);
        lambda.eq(InstancePO::getStatus, InstanceStatusEnum.ENABLE.getCode());
        return instanceDao.selectList(lambda);
    }

    private List<InstanceLbBO> getDefaultLbVersion(List<InstancePO> poList, ServiceInstanceRequestDTO queryInfo) {
        DiscoveryExtensionServicePO discoveryExtensionServicePOS = discoveryExtensionServiceManager
                .getExtensionByAppcodeAndEnvtype(poList.get(0).getAppCode(),queryInfo.getEnvType());

        List<InstanceLbBO> lbInstanceList = new ArrayList<>();
        Map<String, Integer> versionWeightMap= new HashMap<>();
        int multiple = 1;

        if (Objects.nonNull(discoveryExtensionServicePOS)) {
            if(StringUtils.isNotEmpty(discoveryExtensionServicePOS.getVersions())){
                List<InstancePO> filterPoList = new ArrayList<>();

                String versions = discoveryExtensionServicePOS.getVersions();
                JsonObject versionsWeightJson = GsonUtils.toJsonObject(versions);
                if(Objects.nonNull(versionsWeightJson)){
                    for(InstancePO po:poList) {
                        if(Objects.nonNull(versionsWeightJson.get(po.getVersion()))){
                            String version = po.getVersion();
                            versionWeightMap.put(version, versionWeightMap.getOrDefault(version, 0) + 1);
                            //刨除version weight=0对应的实例 同时保持versionWeightMap信息完整
                            //以达到在versions含有的version实例 metadata即使存在也不算做可用实例返回
                            if(versionsWeightJson.get(version).getAsDouble() != 0) {
                                filterPoList.add(po);
                            }
                        }
                    }
                    //multiple等于不同版本的实例数的乘积，返回的实例总的weight = multiple * 100（所有版本的weight之和）
                    //通过multiple来保证单个实例的weight为整数，以保证负载准确
                    multiple = versionWeightMap.values().stream().reduce(1, Math::multiplyExact);
                    for(InstancePO po:filterPoList) {
                        float vweight = versionsWeightJson.get(po.getVersion()).getAsFloat() * multiple;
                        float pweight = vweight/versionWeightMap.get(po.getVersion());
                        Integer weight = Math.round(pweight);
                        InstanceLbBO lbInstance = InstanceConvert.toInstanceLbWeightBO(po, weight);
                        lbInstanceList.add(lbInstance);
                    }
                }

            }
            //合并AOS设置的线上版本实例
            if(StringUtils.isNotEmpty(discoveryExtensionServicePOS.getMetadata())) {
                List<InstancePO> enablePoList = new ArrayList<>();
                String metadata = discoveryExtensionServicePOS.getMetadata();
                JsonObject metaDataJson = GsonUtils.toJsonObject(metadata);
                if(Objects.nonNull(metaDataJson) && Objects.nonNull(metaDataJson.get("online_version"))){
                    String version = metaDataJson.get("online_version").getAsString();
                    //如果versions中已经含有对应的版本，则不在追加相同实例以避免重复
                    if(Objects.isNull(versionWeightMap.get(version))) {
                        for(InstancePO po:poList) {
                            if(version.equals(po.getVersion())){
                                enablePoList.add(po);
                            }
                        }
                        if(!CollectionUtils.isEmpty(enablePoList)) {
                            //总的weight = multiple * 100，以保证在versions有效时与versions里的版本权重相等
                            //同时此处可能存在weight不是整数的情况（除不尽），分摊的权重进行了均衡处理
                            float pweight = (float)Math.round(100*multiple/enablePoList.size());
                            float totalWeight = 0;
                            float realWeight = 0;
                            for(InstancePO po:enablePoList) {
                                totalWeight += pweight;
                                float curWeight = totalWeight - realWeight;
                                Integer weight = Math.round(curWeight);
                                realWeight += weight;
                                InstanceLbBO lbInstance = InstanceConvert.toInstanceLbWeightBO(po, weight);
                                lbInstanceList.add(lbInstance);
                            }
                        }
                    }
                }
            }
        }


        return lbInstanceList;
    }

    public List<InstancePO> getDefaultVersion(List<InstancePO> poList, ServiceInstanceRequestDTO queryInfo) {
        DiscoveryExtensionServicePO discoveryExtensionServicePOS = discoveryExtensionServiceManager
                .getExtensionByAppcodeAndEnvtype(poList.get(0).getAppCode(),queryInfo.getEnvType());
        List<InstancePO> filterPoList = new ArrayList<>();

        if (Objects.nonNull(discoveryExtensionServicePOS)) {

            String metadata = discoveryExtensionServicePOS.getMetadata();
            if(StringUtils.isNotEmpty(metadata)){
                for (int i = 0; i < poList.size(); i++) {
                    JsonObject jsonObject = GsonUtils.toJsonObject(metadata);
                    if(Objects.nonNull(jsonObject) && Objects.nonNull(jsonObject.get("online_version"))){
                        String version = jsonObject.get("online_version").getAsString();
                        if(version.equals(poList.get(i).getVersion())){
                            filterPoList.add(poList.get(i));
                        }
                    }
                }
            }
        }
        return filterPoList;
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

    /**
     * 查询单个实例的IP 和端口信息
     *
     * @param instanceId
     * @return
     */
    public InstanceBO getByInstanceId(String instanceId) {
        LambdaQueryWrapper<InstancePO> insWrapper = new QueryWrapper<InstancePO>().lambda();
        insWrapper.eq(InstancePO::getInstanceId, instanceId);
        InstancePO instancePO = this.instanceDao.selectOne(insWrapper);
        if(Objects.isNull(instancePO)) {
            return InstanceBO.builder().build();
        }

        LambdaQueryWrapper<InstancePortPO> instancePortWrapper = new QueryWrapper<InstancePortPO>().lambda();
        instancePortWrapper.eq(InstancePortPO::getInstanceId, instanceId);
        List<InstancePortPO> instancePortPOS = this.instancePortDao.selectList(instancePortWrapper);
        return InstanceConvert.toInstanceBO(instancePO, instancePortPOS);
    }

    /**
     * 获取所有的应用实例信息
     *
     * @param requestDTO
     * @return
     */
    public List<InstanceBO> getAllInstance(GetAllInstancesRequestDTO requestDTO) {
        LambdaQueryWrapper<InstancePO> insWrapper = new LambdaQueryWrapper<>();
        insWrapper.eq(InstancePO::getStatus, requestDTO.getStatus());
        if(StringUtils.isNotEmpty(requestDTO.getProvider())) {
            insWrapper.eq(InstancePO::getProvider, requestDTO.getProvider());
        }
        List<InstancePO> instancePOS = this.instanceDao.selectList(insWrapper);
        List<InstancePortPO> instancePortPOS = new ArrayList<>();
        List<String> instanceIds = instancePOS.stream().map(InstancePO::getInstanceId).distinct()
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(instanceIds)) {
            LambdaQueryWrapper<InstancePortPO> portWrapper = new LambdaQueryWrapper<>();
            portWrapper.in(InstancePortPO::getInstanceId, instanceIds);
            instancePortPOS = this.instancePortDao.selectList(portWrapper);
        }
        return InstanceConvert.toInstanceBOList(instancePOS, instancePortPOS);

    }

    public List<InstancePO> getInstanceByAppcode(String appcode,int status){
        LambdaQueryWrapper<InstancePO> insWrapper = new LambdaQueryWrapper<>();
        insWrapper.eq(InstancePO::getStatus, status);
        insWrapper.eq(InstancePO::getAppCode, appcode);
        List<InstancePO> instancePOS = this.instanceDao.selectList(insWrapper);
        return instancePOS;
    }

    public List<InstancePO> queryInstance(QueryInstanceRequestDTO queryInfo) {
        if(Objects.isNull(queryInfo)) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<InstancePO> insWrapper = new LambdaQueryWrapper<>();
        insWrapper.eq(InstancePO::getAppCode, queryInfo.getAppcode());
        insWrapper.eq(InstancePO::getEnvType, queryInfo.getEnvType());
        if(Objects.nonNull(queryInfo.getStatus())){
            insWrapper.eq(InstancePO::getStatus, queryInfo.getStatus());
        }
        if(StringUtils.isNotEmpty(queryInfo.getEnvGroup())) {
            insWrapper.eq(InstancePO::getEnvGroup, queryInfo.getEnvGroup());
        }
        if(StringUtils.isNotEmpty(queryInfo.getVersion())) {
            insWrapper.eq(InstancePO::getVersion, queryInfo.getVersion());
        }
        if(StringUtils.isNotEmpty(queryInfo.getCluster())) {
            insWrapper.eq(InstancePO::getCluster, queryInfo.getCluster());
        }
        if(StringUtils.isNotEmpty(queryInfo.getIdc())) {
            insWrapper.eq(InstancePO::getIdc, queryInfo.getIdc());
        }
        List<InstancePO> instancePOS = this.instanceDao.selectList(insWrapper);
        return instancePOS;
    }

    public List<InstancePO> getOfflineInstance(int days) {
        LambdaQueryWrapper<InstancePO> insWrapper = new LambdaQueryWrapper<>();
        insWrapper.eq(InstancePO::getStatus, InstanceStatusEnum.OFFLINE.getCode());
        insWrapper.le(InstancePO::getMtime, LocalDateTime.now().minusDays(days));
        return this.instanceDao.selectList(insWrapper);
    }

    public List<InstancePO> getDirtyInstance(int days) {
        LambdaQueryWrapper<InstancePO> insWrapper = new LambdaQueryWrapper<>();
        insWrapper.eq(InstancePO::getStatus, InstanceStatusEnum.NOTREADY.getCode());
        LocalDateTime dateTime = LocalDateTime.now().minusDays(days);
        insWrapper.le(InstancePO::getMtime, dateTime);
        insWrapper.orderByAsc(InstancePO::getMtime);
        insWrapper.last("limit " + 500);
        return this.instanceDao.selectList(insWrapper);
    }

    public boolean removeOfflineInstance(List<Long> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            int num = instanceDao.deleteBatchIds(ids);
            XxlJobLogger.log("remove instances num:" + num);
        }

        return true;
    }
}
