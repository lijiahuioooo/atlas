package com.mfw.atlas.provider.test.zk;

import com.alibaba.dubbo.common.URL;
import com.alibaba.fastjson.JSON;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.retry.RetryNTimes;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZookeeperServerTest {

    static com.netflix.curator.framework.CuratorFramework client = null;

    public static void main(String[] args)  {
        try {
            // 初始化客户端
            // 总共重试三次,每次一秒
            RetryNTimes retryNTimes = new RetryNTimes(1, 100);

            client = CuratorFrameworkFactory.newClient("127.0.0.1:2181",retryNTimes);

            client.start();

            List<String> instanceIds = client.getChildren().forPath("/dubbo/com.mfw.atlas.sdk.demo.dubbo.singleprovider.service.SingleProviderService2/providers");

            System.out.print(JSON.toJSONString(toUrlsWithoutEmpty(instanceIds)));

        } catch (IOException e) {
//            throw new IllegalStateException(e);
            log.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
}
