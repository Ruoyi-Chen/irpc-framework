package proxy.jdk;


import proxy.ProxyFactory;

import java.lang.reflect.Proxy;

/**
 *
 * 这个代理工厂的内部注入了一个叫做JDK的代理处理器，JDKClientInvocationHandler，
 * 它的核心任务就是将 [需要调用的方法名称、服务名称，参数] 统统都封装好到RpcInvocation当中，
 * 然后塞入到一个队列里，并且等待服务端的数据返回。
 * @Author : Ruoyi Chen
 * @create 2022/12/15 17:30
 */
public class JDKProxyFactory implements ProxyFactory {
    public <T> T getProxy(Class clazz) throws Throwable {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
                new JDKClientInvocationHandler(clazz));
    }
}
