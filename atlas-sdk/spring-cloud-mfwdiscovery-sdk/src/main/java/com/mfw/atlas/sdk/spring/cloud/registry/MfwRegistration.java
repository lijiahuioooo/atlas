package com.mfw.atlas.sdk.spring.cloud.registry;

import com.alibaba.nacos.api.naming.NamingService;
import com.mfw.atlas.sdk.spring.cloud.MfwDiscoveryProperties;
import org.springframework.cloud.client.discovery.ManagementServerPortUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.Map;

/**
 * @author xiaojing
 */
public class MfwRegistration {

	public static final String MANAGEMENT_PORT = "management.port";
	public static final String MANAGEMENT_CONTEXT_PATH = "management.context-path";
	public static final String MANAGEMENT_ADDRESS = "management.address";
	public static final String MANAGEMENT_ENDPOINT_BASE_PATH = "management.endpoints.web.base-path";

	private MfwDiscoveryProperties mfwDiscoveryProperties;

	private ApplicationContext context;


	public MfwRegistration(MfwDiscoveryProperties mfwDiscoveryProperties,
                           ApplicationContext context) {
		this.mfwDiscoveryProperties = mfwDiscoveryProperties;
		this.context = context;
	}

	@PostConstruct
	public void init() {

		Map<String, String> metadata = mfwDiscoveryProperties.getMetadata();
		Environment env = context.getEnvironment();

		String endpointBasePath = env.getProperty(MANAGEMENT_ENDPOINT_BASE_PATH);
		if (!StringUtils.isEmpty(endpointBasePath)) {
			metadata.put(MANAGEMENT_ENDPOINT_BASE_PATH, endpointBasePath);
		}

		Integer managementPort = ManagementServerPortUtils.getPort(context);
		if (null != managementPort) {
			metadata.put(MANAGEMENT_PORT, managementPort.toString());
			String contextPath = env
					.getProperty("management.server.servlet.context-path");
			String address = env.getProperty("management.server.address");
			if (!StringUtils.isEmpty(contextPath)) {
				metadata.put(MANAGEMENT_CONTEXT_PATH, contextPath);
			}
			if (!StringUtils.isEmpty(address)) {
				metadata.put(MANAGEMENT_ADDRESS, address);
			}
		}
	}

	public String getServiceId() {
		return mfwDiscoveryProperties.getService();
	}

	public String getHost() {
		return mfwDiscoveryProperties.getIp();
	}

	public int getPort() {
		return mfwDiscoveryProperties.getPort();
	}

	public boolean isSecure() {
		return false;
	}

	public void setPort(int port) {
		this.mfwDiscoveryProperties.setPort(port);
	}

	public URI getUri() {
		String scheme = (this.isSecure()) ? "https" : "http";
		String uri = String.format("%s://%s:%s", scheme, this.getHost(),
				this.getPort());
		return URI.create(uri);
	}

	public Map<String, String> getMetadata() {
		return mfwDiscoveryProperties.getMetadata();
	}

	public String getScheme() {
		return null;
	}

	public MfwDiscoveryProperties getMfwDiscoveryProperties() {
		return mfwDiscoveryProperties;
	}

	public NamingService getNacosNamingService() {
		return mfwDiscoveryProperties.namingServiceInstance();
	}

	@Override
	public String toString() {
		return "MfwRegistration{" + "mfwDiscoveryProperties="
				+ mfwDiscoveryProperties + '}';
	}
}
