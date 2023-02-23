package com.mfw.atlas.admin.controller;

import com.alibaba.druid.util.StringUtils;
import com.mfw.atlas.admin.manager.InstanceManager;
import com.mfw.atlas.admin.model.bo.ConsumerServiceBO;
import com.mfw.atlas.admin.model.dto.ConsumerServiceDTO;
import com.mfw.atlas.admin.model.dto.ProviderServiceDTO;
import com.mfw.atlas.admin.model.dto.ProviderServiceGroupDTO;
import com.mfw.atlas.admin.model.po.InstancePO;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/discovery")
public class ServiceMangerController {

    @Autowired
    private InstanceManager instanceManager;


    @RequestMapping("/serviceList")
    public String list(HttpServletRequest request, @RequestParam(value = "appcode", required = false, defaultValue = "") String appcode) {


        String regex = "[_.]+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(appcode);
        //将字符串中所有命中的特殊字符替换为空
        appcode = matcher.replaceAll("-");

        List<InstancePO> instances = instanceManager.getInstances(appcode);

        List<ProviderServiceDTO> providerServiceDTOS = new ArrayList<>();
        List<ProviderServiceGroupDTO> providerServiceGroupDTOS = new ArrayList<>();
        List<ConsumerServiceDTO> subscribe = new ArrayList<>();
        List<ConsumerServiceBO> consumerSubscribe = new ArrayList<>();


        if(!StringUtils.isEmpty(appcode)){
            if(!CollectionUtils.isEmpty(instances)){
                providerServiceDTOS = instanceManager.getProviders(instances);
                providerServiceGroupDTOS = instanceManager.getProviderGroups(providerServiceDTOS);

                if(!CollectionUtils.isEmpty(providerServiceDTOS)){

                    //被订阅列表
                    consumerSubscribe = instanceManager.getByProviderInstanceId(providerServiceDTOS);
                }
                //订阅列表
                subscribe = instanceManager.getConsumerList(instances);
            }
        }

        if(CollectionUtils.isEmpty(providerServiceDTOS)){
            consumerSubscribe = new ArrayList<>();
            subscribe = new ArrayList<>();
        }

        request.setAttribute("instances", instances);
        request.setAttribute("providers", providerServiceDTOS);
        request.setAttribute("serviceGroups", providerServiceGroupDTOS);
        request.setAttribute("consumers", consumerSubscribe);
        request.setAttribute("subscribe", subscribe);
        request.setAttribute("appcode", appcode);

        return "serviceList";
    }
}
