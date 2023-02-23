package com.mfw.atlas.provider.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MAlertHelper {

    @Autowired
    private RestTemplate restTemplate;
    @Value("${mfw.alert.host:http://malert.mtech.svc.ab}")
    private String mAlertHost;
    @Value("${mfw.alert.uri:/malert/alertByAppCode}")
    private String mAlertUri;
    @Value("${spring.application.name}")
    private String applicationName;
    @Value("${spring.profiles.active}")
    private String envProfile;

    /**
     * 直接报警上报
     *
     * @param alertMsg 报警内容
     * @return
     */
    public boolean alertEventMap(String alertMsg) {
        Map<String, String> eventMap = new HashMap<>();
        eventMap.put("appCode", applicationName);
        String msg = String.format("{\n env: %s \n time: %s \n message: %s}",
                envProfile, LocalDateTime.now().toString(), alertMsg);
        eventMap.put("alertMsg", msg);

        ResponseEntity<String> responseEntity = restTemplate
                .postForEntity(mAlertHost + mAlertUri, eventMap, String.class);
        JSONObject response = JSON.parseObject(responseEntity.getBody());
        return responseEntity.getStatusCode() == HttpStatus.OK && (Boolean) response.get("isSuccess") && response
                .get("isSuccess").equals(HttpStatus.OK.value());
    }
}
