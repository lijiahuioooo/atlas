/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mfw.atlas.sdk.spring.cloud.starter;

import com.mfw.atlas.client.udp.PushEventDispatcher;
import com.mfw.atlas.client.udp.PushReceiver;
import com.mfw.atlas.sdk.spring.cloud.MfwDiscoveryProperties;
import com.mfw.atlas.sdk.spring.cloud.ServerPushEventListener;
import com.mfw.atlas.sdk.spring.cloud.discovery.ConditionalOnRibbonMfw;
import com.mfw.atlas.sdk.spring.cloud.discovery.MfwRibbonClientConfiguration;
import com.mfw.atlas.sdk.spring.cloud.registry.MfwRegistration;
import com.mfw.atlas.sdk.spring.cloud.registry.MfwServiceRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * Auto-configuration}
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnBean(SpringClientFactory.class)
@ConditionalOnRibbonMfw
@AutoConfigureAfter({RibbonAutoConfiguration.class})
@RibbonClients(defaultConfiguration = MfwRibbonClientConfiguration.class)
public class MfwDiscoveryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MfwDiscoveryProperties mfwDiscoveryProperties(ApplicationContext context) {
        return new MfwDiscoveryProperties(context);
    }

    @Bean
    @ConditionalOnMissingBean
    public MfwServiceRegistry mfwServiceRegistry(
            MfwDiscoveryProperties mfwDiscoveryProperties, MfwRegistration registration){
        return new MfwServiceRegistry(mfwDiscoveryProperties, registration);
    }

    @Bean
    @ConditionalOnMissingBean
    public MfwRegistration mfwRegistration(
            MfwDiscoveryProperties mfwDiscoveryProperties,
            ApplicationContext context) {
        return new MfwRegistration(mfwDiscoveryProperties, context);
    }

    @Bean
    @ConditionalOnMissingBean
    public PushEventDispatcher pushEventDispatcher(MfwDiscoveryProperties mfwDiscoveryProperties){
        PushEventDispatcher dispatcher = new PushEventDispatcher();
        dispatcher.addListener(new ServerPushEventListener(mfwDiscoveryProperties));
        PushReceiver receiver = new PushReceiver();
        receiver.setPushEventDispatcher(dispatcher);
        return dispatcher;
    }
}
