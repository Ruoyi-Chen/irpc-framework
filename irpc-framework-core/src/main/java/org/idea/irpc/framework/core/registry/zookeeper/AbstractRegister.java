package org.idea.irpc.framework.core.registry.zookeeper;

import org.idea.irpc.framework.core.registry.RegistryService;
import org.idea.irpc.framework.core.registry.URL;

import java.util.List;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.SUBSCRIBE_SERVICE_LIST;
import static org.idea.irpc.framework.core.common.cache.CommonServerCache.PROVIDER_URL_SET;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/16 19:18
 */
public abstract class AbstractRegister implements RegistryService {
    @Override
    public void register(URL url) {
        PROVIDER_URL_SET.add(url);
    }

    @Override
    public void unRegister(URL url) {
        PROVIDER_URL_SET.remove(url);
    }

    @Override
    public void subscribe(URL url) {
        SUBSCRIBE_SERVICE_LIST.add(url.getServiceName());
    }

    @Override
    public void doUnSubScribe(URL url) {
        SUBSCRIBE_SERVICE_LIST.remove(url.getServiceName());
    }

    /**
     * 留给子类扩展
     */
    public abstract void doAfterSubscribe(URL url);
    public abstract void doBeforeSubscribe(URL url);
    public abstract List<String> getProviderIps(String serviceName);

}
