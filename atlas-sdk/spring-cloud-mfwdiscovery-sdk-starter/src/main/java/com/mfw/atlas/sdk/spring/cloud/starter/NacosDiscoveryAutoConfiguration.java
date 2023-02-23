package com.mfw.atlas.sdk.spring.cloud.starter;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.registry.NacosAutoServiceRegistration;
import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: zhangyang1
 */

@Configuration
@EnableConfigurationProperties
@ConditionalOnClass(NacosAutoServiceRegistration.class)
@ConditionalOnProperty(value = "spring.cloud.service-registry.auto-registration.enabled", matchIfMissing = true)
@AutoConfigureAfter({ AutoServiceRegistrationConfiguration.class,
        AutoServiceRegistrationAutoConfiguration.class})
public class NacosDiscoveryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public NacosDiscoveryProperties nacosProperties() {
        return new NacosDiscoveryProperties();
    }


    @Bean
    public NacosServiceRegistry nacosServiceRegistry(
            NacosDiscoveryProperties nacosDiscoveryProperties) {
        return new NacosServiceRegistry(nacosDiscoveryProperties);
    }

    @Bean
    @ConditionalOnBean(AutoServiceRegistrationProperties.class)
    public NacosRegistration nacosRegistration(
            NacosDiscoveryProperties nacosDiscoveryProperties,
            ApplicationContext context) {
        return new NacosRegistration(nacosDiscoveryProperties, context);
    }

    @Bean
    @ConditionalOnBean(AutoServiceRegistrationProperties.class)
    public NacosAutoServiceRegistration nacosAutoServiceRegistration(
            NacosServiceRegistry registry,
            AutoServiceRegistrationProperties autoServiceRegistrationProperties,
            NacosRegistration registration) {
        return new NacosAutoServiceRegistration(registry,
                autoServiceRegistrationProperties, registration);
    }
}
