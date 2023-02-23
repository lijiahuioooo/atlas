package com.mfw.atlas.provider.test.nacos;

import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.mfw.atlas.provider.AtlasApplication;
import com.mfw.atlas.provider.manager.MFWNacosProxyManager;
import com.mfw.atlas.provider.util.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AtlasApplication.class)
@WebAppConfiguration
public class nacosServerTest {

    @Test
    public void testNacosAddr() {

        MFWNacosProxyManager client = new MFWNacosProxyManager(null, "172.16.129.173:8848");

        String result = client.queryList(
                "providers:com.mfw.atlas.sdk.demo.dubbo.singleprovider.service.SingleProviderService2:1.0.0:default",
                "");
        ServiceInfo serviceInfo = null;
        if (StringUtils.isNotEmpty(result)) {
            serviceInfo = GsonUtils.fromJson(result, ServiceInfo.class);
        }
        log.info(GsonUtils.toJsonString(serviceInfo));
    }


    @Test
    public void testNacosEndPoint() {

        MFWNacosProxyManager client = new MFWNacosProxyManager("nacosendpoint.mtech.svc.ab:80", null);

        String result = client.queryList(
                "providers:com.mfw.atlas.sdk.demo.dubbo.singleprovider.service.SingleProviderService2:1.0.0:default",
                "");
        ServiceInfo serviceInfo = null;
        if (StringUtils.isNotEmpty(result)) {
            serviceInfo = GsonUtils.fromJson(result, ServiceInfo.class);
        }
        log.info(GsonUtils.toJsonString(serviceInfo));
    }
}
