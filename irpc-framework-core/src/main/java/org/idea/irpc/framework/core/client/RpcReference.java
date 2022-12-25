package org.idea.irpc.framework.core.client;

import org.idea.irpc.framework.core.proxy.ProxyFactory;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.CLIENT_CONFIG;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/15 16:44
 */
public class RpcReference {
    public ProxyFactory proxyFactory;

    public RpcReference(ProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }


    /**
     * 根据接口类型获取代理对象
     *
     * @param rpcReferenceWrapper
     * @param <T>
     * @return
     */
    public <T> T get(RpcReferenceWrapper<T> rpcReferenceWrapper) throws Throwable {
        initGlobalRpcReferenceWrapperConfig(rpcReferenceWrapper);
        return proxyFactory.getProxy(rpcReferenceWrapper);
    }

    private void initGlobalRpcReferenceWrapperConfig(RpcReferenceWrapper rpcReferenceWrapper) {
        if (rpcReferenceWrapper.getTimeOUt() == null) {
            rpcReferenceWrapper.setTimeOut(CLIENT_CONFIG.getTimeOut());
        }
    }
}
