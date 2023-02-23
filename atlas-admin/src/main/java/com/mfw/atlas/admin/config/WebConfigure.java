package com.mfw.atlas.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * Created by zxin on 2019-09-05.
 */
@Configuration
public class WebConfigure implements WebMvcConfigurer {


    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");     //1允许任何域名使用
        corsConfiguration.setAllowCredentials(true); //2允许cookie
        corsConfiguration.addAllowedHeader("*");     //3允许任何头
        corsConfiguration.addAllowedMethod("*");     //4允许任何方法（post、get等）
        return corsConfiguration;
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", buildConfig()); // 4
        return new CorsFilter(source);
    }


}
