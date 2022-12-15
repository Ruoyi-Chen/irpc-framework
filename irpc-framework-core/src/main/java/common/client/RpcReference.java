package common.client;

import lombok.Data;
import proxy.ProxyFactory;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/15 16:44
 */
public class RpcReference {
    public ProxyFactory proxyFactory;

    public RpcReference(ProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

    public <T> T get(Class<T> tClass) throws Throwable {
        return proxyFactory.getProxy(tClass);
    }
}
