package org.idea.irpc.framework.core.client;

import org.idea.irpc.framework.core.proxy.ProxyFactory;

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
        return proxyFactory.getProxy(rpcReferenceWrapper);
    }
}
