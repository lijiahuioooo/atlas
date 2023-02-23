package com.mfw.atlas.sdk.spring.cloud.discovery;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.mfw.atlas.sdk.spring.cloud.utils.Chooser;
import com.mfw.atlas.sdk.spring.cloud.utils.LogUtils;
import com.mfw.atlas.sdk.spring.cloud.utils.Pair;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class MfwLoadBalanceRule extends AbstractLoadBalancerRule {

    @Override
    public Server choose(Object key) {
        DynamicServerListLoadBalancer loadBalancer = (DynamicServerListLoadBalancer) getLoadBalancer();
        List<Server> serverList = loadBalancer.getAllServers();
        List<Instance> instanceList = new ArrayList<>();

        for(Server s:serverList) {
            MfwServer mfwServer = (MfwServer)s;
            instanceList.add(mfwServer.getInstance());
        }
        try {
            Instance instance = getHostByRandomWeight(instanceList);
            log.debug("MFWLB 选中的instance = {}", instance);
            if(Objects.isNull(instance)) {
                log.warn("无可用实例选择 name:{} servers:{}", loadBalancer.getName(), serverList);
                return null;
            }
            return new MfwServer(instance);
        } catch (Exception e) {
            log.error("MFWLB选择发生异常", e);
            return null;
        }
    }

    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {
    }

    private static Instance getHostByRandomWeight(List<Instance> hosts) {
        log.debug("entry randomWithWeight");
        if (hosts == null || hosts.size() == 0) {
            LogUtils.NAMING_LOGGER.debug("hosts == null || hosts.size() == 0");
            return null;
        }

        Chooser<String, Instance> vipChooser = new Chooser<String, Instance>("www.mafengwo.cn");

        log.debug("new Chooser");

        List<Pair<Instance>> hostsWithWeight = new ArrayList<Pair<Instance>>();
        for (Instance host : hosts) {
            if (host.isHealthy()) {
                hostsWithWeight.add(new Pair<Instance>(host, host.getWeight()));
            }
        }
        log.debug("for (Host host : hosts)");
        vipChooser.refresh(hostsWithWeight);
        log.debug("vipChooser.refresh");
        return vipChooser.randomWithWeight();
    }
}