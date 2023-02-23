package com.mfw.atlas.provider.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.mfw.atlas.client.constants.GlobalCodeEnum;
import com.mfw.atlas.client.model.dto.ProviderServiceDTO;
import com.mfw.atlas.provider.constant.ExtendedParamsConstants;
import com.mfw.atlas.provider.exceptions.BusinessException;
import com.mfw.atlas.provider.model.dto.request.ServiceInstanceRequestDTO;
import com.mfw.atlas.provider.util.Md5Utils;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @author huangrui
 * @Time 2020/11/19 10:18 上午
 */
@Slf4j
@Component
public class NacosClientManager implements SmartLifecycle {

    private static volatile ConcurrentHashMap<String, MFWNacosProxyManager> registerClientMap = new ConcurrentHashMap();

    private volatile boolean isRunning = false;

    private static String defaultNamespaceId = "public";

    /**
     * 拉取原注册中心
     *
     * @param queryInfo
     */
    public List<ProviderServiceDTO> originalRegister(ServiceInstanceRequestDTO queryInfo) {

        List<ProviderServiceDTO> originalData = new ArrayList<>();
        String namespaceId = "public";
        if (queryInfo.getExtendedMFWParams(ExtendedParamsConstants.ORIGINAL_NAMESPACE) != null) {
            namespaceId = String.valueOf(queryInfo.getExtendedMFWParams(ExtendedParamsConstants.ORIGINAL_NAMESPACE));
        }

        MFWNacosProxyManager registerClient = getRegisterClient(queryInfo);

        if (Objects.nonNull(registerClient)) {
            originalData = getNaocsData(registerClient, queryInfo.getServiceName(), namespaceId);
        }
        return originalData;
    }

    /**
     * 获取nacos数据并转成DTO
     * @param mfwNamingProxyManager
     * @param serviceName
     * @param namespaceId
     * @return
     */
    private List<ProviderServiceDTO> getNaocsData(MFWNacosProxyManager mfwNamingProxyManager, String serviceName,
            String namespaceId) {
        List<ProviderServiceDTO> serviceInstanceList = new ArrayList<>();
        try {
            String result = mfwNamingProxyManager.queryList(serviceName, namespaceId);

            ServiceInfo serviceInfo = null;
            if (StringUtils.isNotEmpty(result)) {
                serviceInfo = JSON.parseObject(result, ServiceInfo.class);
            }

            List<Instance> list = null;
            if (serviceInfo == null || CollectionUtils.isEmpty(list = serviceInfo.getHosts())) {
                return new ArrayList<>();
            }

            list.forEach(instance -> {
                serviceInstanceList.add(
                        new ProviderServiceDTO()
                                .setInstanceId(instance.getInstanceId())
                                .setServiceName(instance.getServiceName())
                                .setIp(instance.getIp())
                                .setPort(String.valueOf(instance.getPort()))
                                .setMetadata(JSON.toJSONString(instance.getMetadata()))
                );
            });

            return serviceInstanceList;
        } catch (Exception e) {
//            log.error("获取nacos失败:{}", e.getMessage());
            throw new BusinessException(GlobalCodeEnum.GL_NACOS_API_FAIL_9009.getCode(),
                    "获取nacos数据失败，serviceName:"+serviceName +",errorinfo："+ e.getMessage());
        }
    }



    @Override
    public void start() {
        isRunning = true;
    }

    @Override
    public void stop() {
        //服务停止前,方法调用
        isRunning = false;
        if (!CollectionUtils.isEmpty(registerClientMap)) {
            registerClientMap.forEach((k, v) -> {
                if (Objects.nonNull(v)) {
                    try {
                        v.close();
                    } catch (Exception e) {
                        //do nothing
                    }
                }
            });
        }
    }


    @Override
    public boolean isRunning() {
        return isRunning;
    }


    private MFWNacosProxyManager getRegisterClient(ServiceInstanceRequestDTO queryInfo) {

        String endpoint = null;
        String registerAddr = null;
        String registerClientKey =null;
        if(Objects.nonNull(queryInfo.getExtendedMFWParams(ExtendedParamsConstants.ORIGINAL_ADDRESS))){
            registerAddr = String.valueOf(queryInfo.getExtendedMFWParams(ExtendedParamsConstants.ORIGINAL_ADDRESS));
            registerClientKey = getOriginalAddrMD5(registerAddr);
        }  else if (Objects.nonNull(queryInfo.getExtendedMFWParams(ExtendedParamsConstants.ORIGINAL_ENDPOINT))) {
            endpoint = String.valueOf(queryInfo.getExtendedMFWParams(ExtendedParamsConstants.ORIGINAL_ENDPOINT));
            registerClientKey = getOriginalAddrMD5(endpoint);
        }

        MFWNacosProxyManager registerClient = registerClientMap.get(registerClientKey);

        if (Objects.isNull(registerClient)) {
            registerClient = createClient(registerClientKey,endpoint, registerAddr);
        }

        return registerClient;
    }

    private synchronized MFWNacosProxyManager createClient(String registerClientKey, String endpoint,String registerAddr) {
        MFWNacosProxyManager mfwNamingProxyManager = null;
        try{
            if (StringUtils.isNotEmpty(endpoint) || StringUtils.isNotEmpty(registerAddr)) {
                mfwNamingProxyManager = registerClientMap.get(registerClientKey);
                if (mfwNamingProxyManager == null) {
                    mfwNamingProxyManager = new MFWNacosProxyManager(endpoint, registerAddr);
                    registerClientMap.putIfAbsent(registerClientKey, mfwNamingProxyManager);
                }
            }
        }catch (Exception e){
            throw new BusinessException(GlobalCodeEnum.GL_CREATE_NACOS_CLIENT_FAIL_9008.getCode(),
                    "创建nacosClient错误，registerAddr:"+registerAddr+",errorinfo："+e.getMessage());
        }

        return mfwNamingProxyManager;
    }

    public String getOriginalAddrMD5(String registerAddr) {
        return Md5Utils.getMD5(registerAddr.getBytes(Charset.forName("UTF-8")));
    }
}
