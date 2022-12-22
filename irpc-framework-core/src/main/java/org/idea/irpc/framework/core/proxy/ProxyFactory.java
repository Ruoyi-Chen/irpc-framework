package org.idea.irpc.framework.core.proxy;

import org.idea.irpc.framework.core.client.RpcReferenceWrapper;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/15 16:47
 */
public interface ProxyFactory {
    <T> T getProxy(RpcReferenceWrapper rpcReferenceWrapper) throws Throwable;
}
