package com.mfw.atlas.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CommonConfig {

        @Bean
        public RestTemplate initRestTemplate() {
            SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
            clientHttpRequestFactory.setConnectTimeout(3000);
            clientHttpRequestFactory.setReadTimeout(3000);
            return new RestTemplate(clientHttpRequestFactory);
        }
}
