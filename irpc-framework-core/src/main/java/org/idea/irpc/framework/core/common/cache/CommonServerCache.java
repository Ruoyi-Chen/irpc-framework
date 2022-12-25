package org.idea.irpc.framework.core.common.cache;

import org.idea.irpc.framework.core.config.ServerConfig;
import org.idea.irpc.framework.core.dispatcher.ServerChannelDispatcher;
import org.idea.irpc.framework.core.filter.server.ServerFilterChain;
import org.idea.irpc.framework.core.registy.RegistryService;
import org.idea.irpc.framework.core.registy.URL;
import org.idea.irpc.framework.core.serialize.SerializeFactory;
import org.idea.irpc.framework.core.server.ServiceWrapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CommonServerCache {
    /**
     * 服务提供的类
     */
    public static final Map<String, Object> PROVIDER_CLASS_MAP = new HashMap<String, Object>();

    /**
     * 服务提供者URL列表
     */
    public static final Set<URL> PROVIDER_URL_SET = new HashSet<>();

    public static RegistryService REGISTRY_SERVICE;
    public static SerializeFactory SERVER_SERIALIZE_FACTORY;

    public static ServerConfig SERVER_CONFIG;
    public static ServerFilterChain SERVER_FILTER_CHAIN;
    public static final Map<String, ServiceWrapper> PROVIDER_SERVICE_WRAPPER_MAP = new ConcurrentHashMap<>();
    public static Boolean IS_STARTED = false;

    /**
     * 分发器对象
     *
     * SERVER_CHANNEL_DISPATCHER实际上就是org.idea.irpc.framework.core.dispatcher.ServerChannelDispatcher对象，
     * 只不过将它设置为了一个静态对象存在了org.idea.irpc.framework.core.common.cache.CommonServerCache当中。
     */
    public static ServerChannelDispatcher SERVER_CHANNEL_DISPATCHER = new ServerChannelDispatcher();
}
