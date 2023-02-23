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

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.mfw.atlas.sdk.spring.cloud.MfwDiscoveryProperties;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaojing
 * @author renhaojun
 */
public class MfwServerList extends AbstractServerList<MfwServer> {

	private MfwDiscoveryProperties discoveryProperties;

	private String serviceId;

	public MfwServerList(MfwDiscoveryProperties discoveryProperties) {
		this.discoveryProperties = discoveryProperties;
	}

	@Override
	public List<MfwServer> getInitialListOfServers() {
		return getServers();
	}

	@Override
	public List<MfwServer> getUpdatedListOfServers() {
		return getServers();
	}

	private List<MfwServer> getServers() {
		try {
			List<Instance> instances = discoveryProperties.namingServiceInstance()
					.selectInstances(serviceId, true);
			return instancesToServerList(instances);
		}
		catch (Exception e) {
			throw new IllegalStateException(
					"Can not get service instances from nacos, serviceId=" + serviceId,
					e);
		}
	}

	private List<MfwServer> instancesToServerList(List<Instance> instances) {
		List<MfwServer> result = new ArrayList<>();
		if (null == instances) {
			return result;
		}
		for (Instance instance : instances) {
			result.add(new MfwServer(instance));
		}

		return result;
	}

	public String getServiceId() {
		return serviceId;
	}

	@Override
	public void initWithNiwsConfig(IClientConfig iClientConfig) {
		this.serviceId = iClientConfig.getClientName();
	}
}