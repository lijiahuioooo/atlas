package com.mfw.atlas.sdk.dubbo.mfwdiscovery.registry;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.common.utils.UrlUtils;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.support.FailbackRegistry;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

import static com.alibaba.dubbo.common.Constants.*;
import static com.alibaba.dubbo.common.Constants.CONFIGURATORS_CATEGORY;

public class MFWDiscoveryRegistry extends FailbackRegistry {

    /**
     * All supported categories
     */
    private static final String[] ALL_SUPPORTED_CATEGORIES = of(
            PROVIDERS_CATEGORY,
            CONSUMERS_CATEGORY,
            ROUTERS_CATEGORY,
            CONFIGURATORS_CATEGORY
    );

    /**
     * The separator for service name
     *
     * @revert change a constant to be configurable, it's designed for Windows file name that is compatible with old
     * Nacos binary release(< 0.6.1)
     */
    private static final String SERVICE_NAME_SEPARATOR = System.getProperty("nacos.service.name.separator", ":");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final NamingService namingService;

    private final ConcurrentMap<String, EventListener> nacosListeners;

    private static List<URL> subscribeUrls = new ArrayList<>();

    private static List<URL> registerUrls = new ArrayList<>();

    public static List<URL> getSubscribeUrls() {
        return subscribeUrls;
    }

    public static List<URL> getRegisterUrls() {
        return registerUrls;
    }

    public MFWDiscoveryRegistry(URL url, NamingService namingService) {
        super(url);
        this.namingService = namingService;
        this.nacosListeners = new ConcurrentHashMap<>();
    }

    @Override
    public boolean isAvailable() {
        return "UP".equals(namingService.getServerStatus());
    }

    @Override
    public List<URL> lookup(final URL url) {
        final List<URL> urls = new LinkedList<>();
        return urls;
    }

    @Override
    protected void doRegister(URL url) {
        final String serviceName = getServiceName(url);
        final Instance instance = createInstance(url);
        execute(namingService -> namingService.registerInstance(serviceName, instance));
        if(url.getProtocol().equals(DEFAULT_PROTOCOL)) {
            registerUrls.add(url);
        }
    }

    @Override
    protected void doUnregister(final URL url) {
        execute(namingService -> {
            String serviceName = getServiceName(url);
            Instance instance = createInstance(url);
            namingService.deregisterInstance(serviceName, instance.getIp(), instance.getPort());
        });
    }

    @Override
    protected void doSubscribe(final URL url, final NotifyListener listener) {
        List<String> serviceNames = getServiceNames(url, listener);
        doSubscribe(url, listener, serviceNames);
        if(url.getProtocol().equals(CONSUMER_PROTOCOL)) {
            subscribeUrls.add(url);
        }
    }

    private void doSubscribe(final URL url, final NotifyListener listener, final List<String> serviceNames) {
        execute(namingService -> {
            for (String serviceName : serviceNames) {
                List<Instance> instances = namingService.getAllInstances(serviceName);
                notifySubscriber(url, listener, instances);
                subscribeEventListener(serviceName, url, listener);
            }
        });
    }

    @Override
    protected void doUnsubscribe(URL url, NotifyListener listener) {
    }

    /**
     * Get the service names from the specified {@link URL url}
     *
     * @param url      {@link URL}
     * @param listener {@link NotifyListener}
     * @return non-null
     */
    private List<String> getServiceNames(URL url, NotifyListener listener) {
        return doGetServiceNames(url);
    }

    private <T> void filterData(Collection<T> collection, NacosDataFilter<T> filter) {
        Iterator<T> iterator = collection.iterator();
        while (iterator.hasNext()) {
            T data = iterator.next();
            if (!filter.accept(data)) {
                iterator.remove();
            }
        }
    }

    private List<String> doGetServiceNames(URL url) {
        String[] categories = getCategories(url);
        List<String> serviceNames = new ArrayList<String>(categories.length);
        for (String category : categories) {
            final String serviceName = getServiceName(url, category);
            serviceNames.add(serviceName);
        }
        return serviceNames;
    }

    private List<URL> buildURLs(URL consumerURL, Collection<Instance> instances) {
        if (instances.isEmpty()) {
            return Collections.emptyList();
        }
        List<URL> urls = new LinkedList<URL>();
        for (Instance instance : instances) {
            URL url = buildURL(instance);
            if (UrlUtils.isMatch(consumerURL, url)) {
                urls.add(url);
            }
        }
        return urls;
    }

    private void subscribeEventListener(String serviceName, final URL url, final NotifyListener listener)
            throws NacosException {
        if (!nacosListeners.containsKey(serviceName)) {
            EventListener eventListener = event -> {
                if (event instanceof NamingEvent) {
                    NamingEvent e = (NamingEvent) event;
                    notifySubscriber(url, listener, e.getInstances());
                }
            };
            namingService.subscribe(serviceName, eventListener);
            nacosListeners.put(serviceName, eventListener);
        }
    }

    /**
     * Notify the Healthy {@link Instance instances} to subscriber.
     *
     * @param url       {@link URL}
     * @param listener  {@link NotifyListener}
     * @param instances all {@link Instance instances}
     */
    private void notifySubscriber(URL url, NotifyListener listener, Collection<Instance> instances) {
        List<Instance> healthyInstances = new LinkedList<>(instances);
        // Healthy Instances
        filterHealthyInstances(healthyInstances);
        List<URL> urls = buildURLs(url, healthyInstances);
        if (logger.isInfoEnabled()) {
            logger.info("The URLs[size : {}] are about to be notified from instances : {}", urls.size(), instances);
        }
        MFWDiscoveryRegistry.this.notify(url, listener, urls);
    }

    /**
     * Get the categories from {@link URL}
     *
     * @param url {@link URL}
     * @return non-null array
     */
    private String[] getCategories(URL url) {
        return Constants.ANY_VALUE.equals(url.getServiceInterface()) ?
                ALL_SUPPORTED_CATEGORIES : of(Constants.DEFAULT_CATEGORY);
    }

    private URL buildURL(Instance instance) {
        URL url = new URL(instance.getMetadata().get(Constants.PROTOCOL_KEY),
                instance.getIp(),
                instance.getPort(),
                instance.getMetadata());
        return url;
    }

    private Instance createInstance(URL url) {
        // Append default category if absent
        String category = url.getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY);
        URL newURL = url.addParameter(Constants.CATEGORY_KEY, category);
        newURL = newURL.addParameter(Constants.PROTOCOL_KEY, url.getProtocol());
        String ip = NetUtils.getLocalHost();
        int port = newURL.getParameter(Constants.BIND_PORT_KEY, url.getPort());
        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(port);
        instance.setMetadata(new HashMap<>(newURL.getParameters()));
        return instance;
    }

    private String getServiceName(URL url) {
        String category = url.getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY);
        return getServiceName(url, category);
    }

    private String getServiceName(URL url, String category) {
        StringBuilder serviceNameBuilder = new StringBuilder(category);
        appendIfPresent(serviceNameBuilder, url, Constants.INTERFACE_KEY);
        appendIfPresent(serviceNameBuilder, url, Constants.VERSION_KEY);
        appendIfPresent(serviceNameBuilder, url, Constants.GROUP_KEY);
        return serviceNameBuilder.toString();
    }

    private void appendIfPresent(StringBuilder target, URL url, String parameterName) {
        String parameterValue = url.getParameter(parameterName);
        if (!StringUtils.isBlank(parameterValue)) {
            target.append(SERVICE_NAME_SEPARATOR).append(parameterValue);
        }
    }

    private void execute(NamingServiceCallback callback) {
        try {
            callback.callback(namingService);
        } catch (NacosException e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.getErrMsg(), e);
            }
        }
    }

    private void filterHealthyInstances(Collection<Instance> instances) {
        filterData(instances, data -> data.isEnabled());
    }

    private static <T> T[] of(T... values) {
        return values;
    }


    /**
     * A filter for Nacos data
     *
     * @since 2.6.5
     */
    private interface NacosDataFilter<T> {

        /**
         * Tests whether or not the specified data should be accepted.
         *
         * @param data The data to be tested
         * @return <code>true</code> if and only if <code>data</code>
         * should be accepted
         */
        boolean accept(T data);

    }

    /**
     * {@link NamingService} Callback
     *
     * @since 2.6.5
     */
    interface NamingServiceCallback {

        /**
         * Callback
         *
         * @param namingService {@link NamingService}
         * @throws NacosException
         */
        void callback(NamingService namingService) throws NacosException;

    }
}
