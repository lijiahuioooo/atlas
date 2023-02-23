package com.mfw.atlas.client.discoveryclient;

import com.mfw.atlas.client.constants.GlobalCodeEnum;
import com.mfw.atlas.client.model.ResponseResult;
import com.mfw.atlas.client.model.dto.ConsumerServiceDTO;
import com.mfw.atlas.client.model.dto.ProviderServiceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 注册服务信息和订阅服务信息
 *
 * @author fenghua
 */
public class RegisterClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterClient.class);

    private RestTemplate restTemplate;
    private String serverAddress;
    private String clientVersion = "1.0.0";
    private Map<String, String> baseParams = new HashMap<>();

    public RegisterClient(String serverAddress, String clientVersion, Map<String, String> baseParams) {
        this.serverAddress = serverAddress;
        this.clientVersion = clientVersion;
        this.restTemplate = RestTemplateFactory.getRestTemplate();
        this.baseParams = baseParams;
    }

    public RegisterClient(String serverAddress) {
        this.serverAddress = serverAddress;
        this.restTemplate = RestTemplateFactory.getRestTemplate();
    }

    public Boolean register(List<ProviderServiceDTO> providerServiceDTOS) {
        HttpEntity<List<ProviderServiceDTO>> request = new HttpEntity<>(providerServiceDTOS);
        try {
            ResponseEntity<ResponseResult> responseEntity = restTemplate.postForEntity(getUrl("/provider/metadata/report"), request, ResponseResult.class);
            if(Objects.isNull(responseEntity.getBody())) {
                throw new Exception("server response null.");
            }

            int resCode = responseEntity.getBody().getCode();
            if(!GlobalCodeEnum.GL_SUCC_0000.getCode().equals(resCode)) {
                throw new Exception("server error code:" + resCode);
            }
        } catch (Exception e) {
            LOGGER.error("[register client] report service info failed", e);
            new Thread(() -> System.exit(1)).start();
        }

        return true;
    }

    public Boolean subscribe(List<ConsumerServiceDTO> consumerServiceDTOS) {
        HttpEntity<List<ConsumerServiceDTO>> request = new HttpEntity<>(consumerServiceDTOS);
        try {
            ResponseEntity<ResponseResult> responseEntity = restTemplate.postForEntity(getUrl("/consumer/metadata/report"), request, ResponseResult.class);
            if(Objects.isNull(responseEntity.getBody())) {
                throw new Exception("server response null.");
            }

            int resCode = responseEntity.getBody().getCode();
            if(!GlobalCodeEnum.GL_SUCC_0000.getCode().equals(resCode)) {
                LOGGER.error("subscribe_server_error", consumerServiceDTOS);
                throw new Exception("server error code:" + resCode);
            }
        } catch (Exception e) {
            LOGGER.error("[register client] register subscribe info failed", e);
            new Thread(() -> System.exit(2)).start();
        }

        return true;
    }

    private String getUrl(String path) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverAddress + path);
        builder.queryParam("clientVersion", clientVersion);
        if(!baseParams.isEmpty()) {
            baseParams.forEach(builder::queryParam);
        }
        return builder.toUriString();
    }
}
