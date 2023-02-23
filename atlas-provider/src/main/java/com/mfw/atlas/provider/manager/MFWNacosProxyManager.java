package com.mfw.atlas.provider.manager;

import com.mfw.atlas.client.constants.GlobalCodeEnum;
import com.mfw.atlas.provider.exceptions.BusinessException;
import com.mfw.atlas.provider.util.GsonUtils;
import com.mfw.atlas.provider.util.IoUtils;
import com.mfw.atlas.provider.util.RestTempUtils;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

/**
 * 消费方服务管理
 *
 * @author huangrui
 */
@Slf4j
public class MFWNacosProxyManager {

    private static final int DEFAULT_SERVER_PORT = 8848;

    private int serverPort = DEFAULT_SERVER_PORT;

    private String endpoint;

    private List<String> serverList;

    private volatile List<String> serversFromEndpoint = new ArrayList<String>();

    private long lastSrvRefTime = 0L;

    private long vipSrvRefInterMillis = TimeUnit.SECONDS.toMillis(30);

    private ScheduledExecutorService executorService;

    public static String NACOS_URL_BASE = "/nacos/v1/ns";
    public static String SERVER_ADDR_IP_SPLITER = ":";
    public static String HTTP_PREFIX = "http://";

    /**
     * @param endpoint
     * @param serverList 优先级比 endpoint 高
     */
    public MFWNacosProxyManager(String endpoint, String serverList) {
        this.endpoint = endpoint;
        if (StringUtils.isNotEmpty(serverList)) {
            this.serverList = Arrays.asList(serverList.split(","));
        }
        initRefreshSrvIfNeed();
    }


    private void initRefreshSrvIfNeed() {
        if (StringUtils.isEmpty(endpoint)) {
            return;
        }

        executorService = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("MFWNamingProxyManager.endpoint.serverList.updater");
                t.setDaemon(true);
                return t;
            }
        });

        executorService.scheduleWithFixedDelay(() -> {
            refreshSrvIfNeed();
        }, 0, vipSrvRefInterMillis, TimeUnit.MILLISECONDS);

        refreshSrvIfNeed();
    }

    private void refreshSrvIfNeed() {
        try {

            if (!CollectionUtils.isEmpty(serverList)) {
                log.debug("[ENDPOINT-SERVER-LIST] server list provided by use: " + serverList);
                return;
            }

            if (System.currentTimeMillis() - lastSrvRefTime < vipSrvRefInterMillis) {
                return;
            }

            List<String> list = getServerListFromEndpoint();

            if (CollectionUtils.isEmpty(list)) {
                throw new Exception("[ENDPOINT-SERVER-LIST] Can not acquire Nacos list");
            }

            serversFromEndpoint = list;
            lastSrvRefTime = System.currentTimeMillis();
        } catch (Throwable e) {
            log.error("[ENDPOINT-SERVER-LIST] failed to update server list", e);
        }
    }

    public List<String> getServerListFromEndpoint() throws IOException {

        String urlString = HTTP_PREFIX + endpoint + "/nacos/serverlist";
        Map<String, String> headerMap = builderHeaders();
        ResponseEntity<String> result = RestTempUtils.get(urlString, headerMap, new HashMap<>());
        List<String> list = new ArrayList<String>();
        if (HttpStatus.OK == result.getStatusCode()) {
            String content = result.getBody();
            if (StringUtils.isNoneBlank(content)) {
                for (String line : IoUtils.readLines(new StringReader(content))) {
                    if (!line.trim().isEmpty()) {
                        list.add(line.trim());
                    }
                }
            }
        }
        return list;

    }

    public Map<String, String> builderHeaders() {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("User-Agent", "atlas-provider");
        return headerMap;
    }

    public String queryList(String serviceName, String namespaceId) {
        final Map<String, Object> params = new HashMap<>(8);
        params.put("namespaceId", namespaceId);
        params.put("serviceName", serviceName);
//        params.put("clusters", clusters);
//        params.put("groupName", groupName);
//        params.put("clientIP", );
        params.put("healthyOnly", true);

        return reqAPI(NACOS_URL_BASE
                        + "/instance/list?serviceName={serviceName}&serviceName={serviceName}&healthyOnly={healthyOnly}",
                params, HttpMethod.GET);
    }

    public String reqAPI(String api, Map<String, Object> params, HttpMethod method) {
        List<String> snapshot = serversFromEndpoint;
        if (!CollectionUtils.isEmpty(serverList)) {
            snapshot = serverList;
        }
        return reqAPI(api, params, snapshot, HttpMethod.GET);
    }


    public String reqAPI(String api, Map<String, Object> params, List<String> servers, HttpMethod method) {

        if (CollectionUtils.isEmpty(servers) && method != HttpMethod.GET) {
            throw new IllegalArgumentException("no server available");
        }

        Exception exception = new Exception();

        if (servers != null && !servers.isEmpty()) {
            Random random = new Random(System.currentTimeMillis());
            int index = random.nextInt(servers.size());
            for (int i = 0; i < servers.size(); i++) {
                String server = servers.get(index);
                try {
                    return callServer(api, params, server);
                } catch (Exception e) {
                    exception = e;
//                    log.error("request {} failed.", server, e);
                }
                index = (index + 1) % servers.size();
            }
            throw new IllegalStateException("failed to req API:" + api + " after all servers(" + servers + ") tried: "
                    + exception.getMessage());
        }
        throw new IllegalStateException("failed to req API:/api/" + api + " after all servers(" + servers + ") tried: "
                + exception.getMessage());
    }

    public String callServer(String api, Map<String, Object> params, String curServer) {
        long start = System.currentTimeMillis();
        long end = 0;
        String url;
        if (curServer.startsWith(HTTP_PREFIX)) {
            url = curServer + api;
        } else {
            if (!curServer.contains(SERVER_ADDR_IP_SPLITER)) {
                curServer = curServer + SERVER_ADDR_IP_SPLITER + serverPort;
            }
            url = HTTP_PREFIX + curServer + api;
        }
        Map<String, String> headerMap = builderHeaders();
        ResponseEntity<String> result = RestTempUtils.get(url, headerMap, params);
        end = System.currentTimeMillis();
        log.info("callServer api : {} ,params: {} ,curServer: {} ,cost: {} ms", api, GsonUtils.toJsonString(params),
                curServer, end - start);
        if (HttpStatus.OK == result.getStatusCode()) {
            return result.getBody();
        }
        throw new BusinessException(GlobalCodeEnum.GL_FAIL_9995.getCode(), "failed to req API:"
                + curServer + api + ". code:"
                + result.getStatusCode() + " msg: " + result.getBody());
    }

    public void close() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }
}
