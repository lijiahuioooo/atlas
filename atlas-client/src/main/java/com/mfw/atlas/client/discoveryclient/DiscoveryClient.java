package com.mfw.atlas.client.discoveryclient;

import com.mfw.atlas.client.constants.DiscoveryExceptionEnum;
import com.mfw.atlas.client.constants.GlobalCodeEnum;
import com.mfw.atlas.client.constants.ServiceTypeEnum;
import com.mfw.atlas.client.exceptions.DiscoveryException;
import com.mfw.atlas.client.model.ResponseResult;
import com.mfw.atlas.client.model.dto.ProviderServiceDTO;
import com.mfw.atlas.client.utils.EnvUtils;
import com.mfw.atlas.client.utils.GsonUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class DiscoveryClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryClient.class);

    private static String DEFAULT_ENV_TYPE = "dev";
    private static String DEFAULT_ENV_GROUP = "";
    private static String DEFAULT_INSTANCE_ID = "";

    private RestTemplate restTemplate;
    private String serverAddress;
    private String clientVersion = "1.0.0";
    private Map<String, String> baseParams = new HashMap<>();
    private Map<String, Object> extendedParams = new HashMap<>();

    public DiscoveryClient(String serverAddress, String clientVersion, Map<String, String> baseParams,Map<String, Object> extendedParams) {
        this.serverAddress = serverAddress;
        this.clientVersion = clientVersion;
        this.baseParams = baseParams;
        this.extendedParams = extendedParams;
        this.initRestTemplate();
    }

    public DiscoveryClient(String serverAddress) {
        this.serverAddress = serverAddress;
        this.initRestTemplate();
    }

    private void initRestTemplate() {
        restTemplate = RestTemplateFactory.getRestTemplate();
    }

    public List<ProviderServiceDTO> getServiceInstanceList(ServiceTypeEnum serviceType, String serviceName, String clusters, String serviceGroup, String serviceVersion) {
        List<ProviderServiceDTO> instanceList;

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverAddress + "/discovery/service/instanceList");
        builder.queryParam("clientVersion", clientVersion);
        if(!baseParams.isEmpty()) {
            baseParams.forEach(builder::queryParam);
        }
        if (extendedParams != null) {
            builder.queryParam("extendedMFWParams", GsonUtils.toJsonString(extendedParams));
        }
        builder.queryParam("serviceType", serviceType.getCode())
                .queryParam("serviceName", serviceName)
                .queryParam("serviceGroup", serviceGroup)
                .queryParam("serviceVersion", serviceVersion)
                .queryParam("clusters", clusters)
                .queryParam("instanceId", Optional.ofNullable(EnvUtils.getInstanceId()).orElse(DEFAULT_INSTANCE_ID))
                .queryParam("envType", Optional.ofNullable(EnvUtils.getEnvType()).orElse(DEFAULT_ENV_TYPE))
                .queryParam("envGroup", Optional.ofNullable(EnvUtils.getEnvGroup()).orElse(DEFAULT_ENV_GROUP));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<ResponseResult<List<ProviderServiceDTO>>> responseEntity;

        try {
            responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity,
                    new ParameterizedTypeReference<ResponseResult<List<ProviderServiceDTO>>>() {
                    });
        } catch (Exception e) {
            throw new DiscoveryException(DiscoveryExceptionEnum.GET_SERVICE_INSTANCE_FAILED);
        }

        // invalid null validate
        ResponseResult<List<ProviderServiceDTO>> serviceInstanceList = responseEntity.getBody();
        if(Objects.isNull(serviceInstanceList)) {
            throw new DiscoveryException(DiscoveryExceptionEnum.GET_SERVICE_INSTANCE_NULL);
        }

        // code validate
        int resCode = responseEntity.getBody().getCode();
        if(!GlobalCodeEnum.GL_SUCC_0000.getCode().equals(resCode)) {
            throw new DiscoveryException(DiscoveryExceptionEnum.GET_SERVICE_INSTANCE_ERROR, "server error code:"+resCode);
        }

        instanceList = serviceInstanceList.getData();

        if(Objects.isNull(instanceList)) {
            LOGGER.warn("[discovery client] instance of service [" + serviceName + "] is null");
            instanceList = new ArrayList<>();
        }

        return instanceList;
    }
}
