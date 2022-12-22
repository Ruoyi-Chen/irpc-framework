package org.idea.irpc.framework.core.proxy.javassist;

import org.idea.irpc.framework.core.client.RpcReferenceWrapper;
import org.idea.irpc.framework.core.proxy.ProxyFactory;
import org.idea.irpc.framework.core.proxy.jdk.JDKClientInvocationHandler;

import java.lang.reflect.Proxy;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/15 19:04
 */
public class JavassistProxyFactory implements ProxyFactory {
    @Override
    public <T> T getProxy(RpcReferenceWrapper rpcReferenceWrapper) throws Throwable {
        return (T) ProxyGenerator.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                rpcReferenceWrapper.getAimClass(), new JavassistInvocationHandler(rpcReferenceWrapper));
    }
}
