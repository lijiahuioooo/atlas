package com.mfw.atlas.provider.manager;

import com.alibaba.dubbo.common.URL;
import com.mfw.atlas.client.constants.GlobalCodeEnum;
import com.mfw.atlas.client.model.dto.ProviderServiceDTO;
import com.mfw.atlas.provider.constant.ExtendedParamsConstants;
import com.mfw.atlas.provider.convert.ProviderServiceConvert;
import com.mfw.atlas.provider.exceptions.BusinessException;
import com.mfw.atlas.provider.model.dto.request.ServiceInstanceRequestDTO;
import com.mfw.atlas.provider.util.Md5Utils;
import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.retry.RetryNTimes;
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
public class DubboZookeeperClientManager implements SmartLifecycle {

    private static volatile ConcurrentHashMap<String, CuratorFramework> zkRegisterClientMap = new ConcurrentHashMap();

    private volatile boolean isRunning = false;

    private static final String DEFAULT_GROUP = "/dubbo/";

    private static final String DEFAULT_SERVICE = "/providers";

    //zk-RetryNTimes
    private static final int n = 1;

    //zk-RetryNTimes
    private static final int sleepMsBetweenRetries = 100;


    /**
     * 通过curator连接zookeeper
     *
     * @return
     */
    private CuratorFramework createZKService(String serverAddr) {
        CuratorFramework curatorFramework = null;
        try {
            if (StringUtils.isNotEmpty(serverAddr)) {
                // 初始化客户端
                RetryNTimes retryNTimes = new RetryNTimes(n, sleepMsBetweenRetries);
                curatorFramework = CuratorFrameworkFactory.newClient(serverAddr, retryNTimes);
                curatorFramework.start();
            }
        } catch (Exception e) {
//            log.error("createZK error :{},addr:{}", e.getMessage(), serverAddr);
            throw new RuntimeException("createZK error :" + e.getMessage() + ",addr:" + serverAddr);
        }

        return curatorFramework;
    }

    /**
     * 拉取原注册中心
     *
     * @param queryInfo
     */
    public List<ProviderServiceDTO> originalRegister(ServiceInstanceRequestDTO queryInfo) {

        List<ProviderServiceDTO> originalData = new ArrayList<>();
        if (Objects.nonNull(queryInfo.getExtendedMFWParams(ExtendedParamsConstants.ORIGINAL_TYPE)) && Objects
                .nonNull(queryInfo.getExtendedMFWParams(ExtendedParamsConstants.ORIGINAL_ADDRESS))) {

            String originalRegisterType = String
                    .valueOf(queryInfo.getExtendedMFWParams(ExtendedParamsConstants.ORIGINAL_TYPE));
            String originalRegisterAddress = String
                    .valueOf(queryInfo.getExtendedMFWParams(ExtendedParamsConstants.ORIGINAL_ADDRESS));

            CuratorFramework registerClient = getRegisterClient(originalRegisterAddress, originalRegisterType);
            if (Objects.nonNull(registerClient)) {
                String[] split = queryInfo.getServiceName().split(":");

                if (split.length > 1 && StringUtils.isNotEmpty(split[1])) {
                    originalData = getZKData(registerClient, split[1]);
                }
            }
        } else {
            throw new BusinessException(GlobalCodeEnum.GL_ORIGINAL_ADDRESS_FAIL_9006.getCode(),
                    queryInfo.getServiceName() + GlobalCodeEnum.GL_ORIGINAL_ADDRESS_FAIL_9006.getDesc());
        }
        return originalData;
    }

    /**
     * 获取zk数据并转成DTO
     *
     * @param serviceName
     * @return
     */
    private List<ProviderServiceDTO> getZKData(CuratorFramework client, String serviceName) {
        List<ProviderServiceDTO> providerServiceDTOS = new ArrayList<>();
        try {
            List<String> instanceIds = client.getChildren().forPath(DEFAULT_GROUP + serviceName + DEFAULT_SERVICE);

            if (!CollectionUtils.isEmpty(instanceIds)) {
                List<URL> zkDatas = toUrlsWithoutEmpty(instanceIds);
                providerServiceDTOS = ProviderServiceConvert.zksToDTOList(zkDatas);
            }
            return providerServiceDTOS;
        } catch (Exception e) {
//            log.error("getZKData error", e.getMessage());
            throw new BusinessException(GlobalCodeEnum.GL_ZOOKEEPER_CLIENT_FAIL_9010.getCode(),
                    GlobalCodeEnum.GL_ZOOKEEPER_CLIENT_FAIL_9010.getDesc() +
                            ",serviceName:" + serviceName +",errorinfo：" +e.getMessage());

        }
    }


    /**
     * zk原始数据转成URL
     *
     * @param providers
     * @return
     */
    private static List<URL> toUrlsWithoutEmpty(List<String> providers) {
        List<URL> urls = new ArrayList<URL>();
        if (providers != null && !providers.isEmpty()) {
            for (String provider : providers) {
                provider = URL.decode(provider);
                if (provider.contains("://")) {
                    URL url = URL.valueOf(provider);
                    urls.add(url);
                }
            }
        }
        return urls;
    }

    /**
     * 获取servicekey的hashcode
     *
     * @param registerAddr
     * @return
     */
    public String getOriginalAddrMD5(String registerAddr) {
        return Md5Utils.getMD5(registerAddr.getBytes(Charset.forName("UTF-8")));
    }

    @Override
    public void start() {
        isRunning = true;
    }

    @Override
    public void stop() {
        //服务停止前,方法调用
        isRunning = false;
        if (!CollectionUtils.isEmpty(zkRegisterClientMap)) {
            zkRegisterClientMap.forEach((k, v) -> {
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

    private CuratorFramework getRegisterClient(String registerAddr, String registerType) {

        CuratorFramework registerClient = null;

        String registerAddrMD5 = getOriginalAddrMD5(registerAddr);

        if (registerType.equals("zookeeper")) {
            registerClient = zkRegisterClientMap.get(registerAddrMD5);
        }

        if (Objects.isNull(registerClient) || !registerClient.isStarted()) {
            registerClient = createClient(registerAddr, registerType, registerAddrMD5);
        }

        return registerClient;
    }

    private synchronized CuratorFramework createClient(String registerAddr, String registerType,
            String registerAddrMD5) {
        CuratorFramework registerClient = null;
        try {

            registerClient = zkRegisterClientMap.get(registerAddrMD5);
            if (Objects.isNull(registerClient) || !registerClient.isStarted()) {
                registerClient = createZKService(registerAddr);
                zkRegisterClientMap.putIfAbsent(registerAddrMD5, registerClient);
            }
        } catch (Exception e) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("连接原注册中心失败:")
                    .append(",registerType:")
                    .append(registerType)
                    .append(",getMessage:")
                    .append(e.getMessage());

            throw new BusinessException(GlobalCodeEnum.GL_CONNECTION_REGISTER_FAIL_9007.getCode(),
                    buffer.toString());
        }
        return registerClient;
    }

}
