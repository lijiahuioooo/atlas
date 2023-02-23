package com.mfw.atlas.provider.util;

import java.nio.charset.Charset;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * @author KL
 * @Time 2020/7/18 3:54 下午
 */
@Slf4j
public class RestTempUtils {

    private RestTempUtils() {
    }

    private static RestTemplate restTemplate;

    static {
        SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(3000);
        clientHttpRequestFactory.setReadTimeout(3000);
        restTemplate = new RestTemplate(clientHttpRequestFactory);
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
        restTemplate.setRequestFactory(clientHttpRequestFactory);
    }

    public static RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public static ResponseEntity<String> get(String url, Map<String, String> headers,
            Map<String, Object> uriVariables) {
        HttpHeaders requestHeaders = new HttpHeaders();
        headers.entrySet().stream().filter(set -> set != null).forEach(set -> {
            requestHeaders.add(set.getKey(), set.getValue());
        });
        requestHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return getRestTemplate().exchange(url, HttpMethod.GET, new HttpEntity<String>(null, requestHeaders),
                String.class, uriVariables);
    }

}

