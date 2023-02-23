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

package com.mfw.atlas.sdk.spring.cloud.discovery;

import com.mfw.atlas.sdk.spring.cloud.MfwDiscoveryProperties;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.ServerList;
import com.netflix.loadbalancer.ServerListUpdater;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * integrated Ribbon by default
 * @author xiaojing
 */
@Configuration
@ConditionalOnRibbonMfw
public class MfwRibbonClientConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public ServerList<?> ribbonServerList(IClientConfig config, MfwDiscoveryProperties atlasProperties) {
		MfwServerList serverList = new MfwServerList(atlasProperties);
		serverList.initWithNiwsConfig(config);
		return serverList;
	}

	@Bean
    @ConditionalOnMissingBean
	public IRule ribbonRule() {
		return new MfwLoadBalanceRule();
	}

	@Bean
	@ConditionalOnMissingBean
	public ServerListUpdater ribbonServerListUpdater() {
		return new MfwServerListUpdater();
	}
}
