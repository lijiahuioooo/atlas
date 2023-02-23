package com.mfw.atlas.sdk.spring.cloud;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.exception.NacosException;
import com.mfw.atlas.sdk.spring.cloud.client.naming.MfwNamingService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.*;

import static com.alibaba.nacos.api.PropertyKeyConst.SERVER_ADDR;

/**
 * @author zhangyang1
 */

@ConfigurationProperties("spring.cloud.mfw.discovery")
public class MfwDiscoveryProperties {

	private ApplicationContext applicationContext;

	private String applicationName;

	private Map<String, String> metadata = new HashMap<>();

	public MfwDiscoveryProperties(ApplicationContext context) {
		applicationContext = context;
		applicationName = context.getEnvironment().getProperty("spring.application.name");
	}

	private static final Logger log = LoggerFactory
			.getLogger(MfwDiscoveryProperties.class);

	/**
	 * nacos discovery server address
	 */
	private String serverAddr;

	private String ip;

	private int port = -1;

	private List<String> consumers = new ArrayList<>();

	private Map<String, String> parameters = new HashMap<>();

	public String getServerAddr() {
		return serverAddr;
	}

	public void setServerAddr(String serverAddr) {
		this.serverAddr = serverAddr;
	}

	public List<String> getConsumers() {
		return consumers;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	public String getService() {
		return applicationName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return "MfwDiscoveryProperties{" + "serverAddr='" + serverAddr + '}';
	}

	private MfwNamingService namingService;

	public MfwNamingService namingServiceInstance() {

		if (null != namingService) {
			return namingService;
		}

		Properties properties = new Properties();
		properties.put(SERVER_ADDR, serverAddr);
		if (!CollectionUtils.isEmpty(consumers)){
			properties.put(PropertyKeyConst.CONSUMERS, JSON.toJSONString(consumers));
		}
		properties.put(PropertyKeyConst.CLIENT_VERSION, "1.0.0");

		for (String key:parameters.keySet()){
			if(StringUtils.isNotEmpty(key)){
				properties.putIfAbsent(key,parameters.get(key));
			}
		}

		try {
			namingService = createNamingService(properties);
		}
		catch (Exception e) {
			log.error("create mfw naming service error!properties={},e=,", this, e);
			return null;
		}
		return namingService;
	}

	private static MfwNamingService createNamingService(Properties properties) throws NacosException {
		try {
			Class<?> driverImplClass = Class.forName("com.mfw.atlas.sdk.spring.cloud.client.naming.MfwNamingService");
			Constructor constructor = driverImplClass.getConstructor(Properties.class);
			MfwNamingService vendorImpl = (MfwNamingService)constructor.newInstance(properties);
			return vendorImpl;
		} catch (Throwable e) {
			throw new NacosException(NacosException.CLIENT_INVALID_PARAM, e);
		}
	}


	@PostConstruct
	private void initConsumer(){
		Map<String,Object> beans = applicationContext.getBeansWithAnnotation(FeignClient.class);
		if (beans.size() > 0){
			List<String> consumerNames = new ArrayList<>();
			beans.forEach((k, v) -> {
				try {
					Annotation annotation = Class.forName(k).getDeclaredAnnotation(FeignClient.class);
					if (Proxy.isProxyClass(annotation.getClass())) {
						InvocationHandler ih =  Proxy.getInvocationHandler(annotation);
						Field memberValues = ih.getClass().getDeclaredField("memberValues");
						memberValues.setAccessible(true);
						Map<String, String> members = (Map<String, String>) memberValues.get(ih);
						if (members.containsKey("name") &&
								(!members.containsKey("url") || StringUtils.isBlank(members.get("url")))){
							String serviceName = members.get("name");
							if(!consumerNames.contains(serviceName) && !applicationName.equals(serviceName)) {
								consumers.add(applicationContext.getEnvironment().resolvePlaceholders(serviceName));
								consumerNames.add(serviceName);
							} else {
								log.debug("FeignClient:{} 重复或是服务本身", serviceName);
							}
						} else {
							log.debug("FeignClient:{} 不需要订阅", k);
						}
					}
				} catch (Exception e) {
					log.error("initConsumerException:{}", e);
				}
			});
		}
	}
}
