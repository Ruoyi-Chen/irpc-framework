package org.idea.irpc.framework.core.proxy.javassist;

import org.idea.irpc.framework.core.proxy.ProxyFactory;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/15 19:04
 */
public class JavassistProxyFactory implements ProxyFactory {
    public <T> T getProxy(Class clazz) throws Throwable {
        return (T) ProxyGenerator.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                clazz, new JavassistInvocationHandler(clazz));
    }
}
