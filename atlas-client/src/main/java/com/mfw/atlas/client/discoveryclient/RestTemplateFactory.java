package com.mfw.atlas.client.discoveryclient;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class RestTemplateFactory {
    private static int connectTimeout = 10_000;
    private static int readTimeout = 10_000;
    private static RestTemplate restTemplate;

    static {
        restTemplate = new RestTemplate(getClientHttpRequestFactory(connectTimeout, readTimeout));
    }

    private RestTemplateFactory() {}

    public static RestTemplate getRestTemplate() {
        return restTemplate;
    }

    private static SimpleClientHttpRequestFactory getClientHttpRequestFactory(int connectTimeout, int readTimeout) {
        SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(connectTimeout);
        clientHttpRequestFactory.setReadTimeout(readTimeout);
        return clientHttpRequestFactory;
    }
}
