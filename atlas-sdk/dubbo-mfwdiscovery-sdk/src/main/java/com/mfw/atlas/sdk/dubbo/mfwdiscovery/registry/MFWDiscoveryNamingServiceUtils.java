package com.mfw.atlas.sdk.dubbo.mfwdiscovery.registry;

import static com.alibaba.dubbo.common.Constants.BACKUP_KEY;
import static com.alibaba.nacos.api.PropertyKeyConst.ACCESS_KEY;
import static com.alibaba.nacos.api.PropertyKeyConst.CLUSTER_NAME;
import static com.alibaba.nacos.api.PropertyKeyConst.ENDPOINT;
import static com.alibaba.nacos.api.PropertyKeyConst.NAMESPACE;
import static com.alibaba.nacos.api.PropertyKeyConst.SECRET_KEY;
import static com.alibaba.nacos.api.PropertyKeyConst.SERVER_ADDR;
import static com.mfw.atlas.client.constants.GlobalStatusConstants.MFW_PARAMETER_PREFIX;
import static com.mfw.atlas.sdk.dubbo.mfwdiscovery.utils.naming.UtilAndComs.NACOS_NAMING_LOG_NAME;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import java.util.Properties;

public class MFWDiscoveryNamingServiceUtils {


    public static NamingService createNamingService(URL url) {
        Properties nacosProperties = buildNacosProperties(url);
        NamingService namingService = null;
        try {
            namingService = new MFWDiscoveryNamingService(nacosProperties);
        } catch (NacosException e) {
            throw new IllegalStateException(e);
        }
        return namingService;
    }

    private static Properties buildNacosProperties(URL url) {
        Properties properties = new Properties();
        setServerAddr(url, properties);
        setProperties(url, properties);
        //扩展参数
        setMFParameter(url, properties);
        return properties;
    }

    private static void setServerAddr(URL url, Properties properties) {
        StringBuilder serverAddrBuilder =
                new StringBuilder(url.getHost()) // Host
                        .append(":")
                        .append(url.getPort()); // Port

        // Append backup parameter as other servers
        String backup = url.getParameter(BACKUP_KEY);
        if (backup != null) {
            serverAddrBuilder.append(",").append(backup);
        }

        String serverAddr = serverAddrBuilder.toString();
        properties.put(SERVER_ADDR, serverAddr);
    }

    private static void setProperties(URL url, Properties properties) {
        putPropertyIfAbsent(url, properties, NAMESPACE);
        putPropertyIfAbsent(url, properties, NACOS_NAMING_LOG_NAME);
        putPropertyIfAbsent(url, properties, ENDPOINT);
        putPropertyIfAbsent(url, properties, NAMESPACE);
        putPropertyIfAbsent(url, properties, ACCESS_KEY);
        putPropertyIfAbsent(url, properties, SECRET_KEY);
        putPropertyIfAbsent(url, properties, CLUSTER_NAME);
    }

    private static void setMFParameter(URL url, Properties properties) {
        for (String key: url.getParameters().keySet()){
            if(StringUtils.isNotEmpty(key) && key.startsWith(MFW_PARAMETER_PREFIX)){
                putPropertyIfAbsent(url, properties,key);
            }
        }
    }
    private static void putPropertyIfAbsent(URL url, Properties properties, String propertyName) {
        String propertyValue = url.getParameter(propertyName);
        if (StringUtils.isNotEmpty(propertyValue)) {
            properties.setProperty(propertyName, propertyValue);
        }
    }
}
