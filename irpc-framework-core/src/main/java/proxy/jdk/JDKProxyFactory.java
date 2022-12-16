package proxy.jdk;


import proxy.ProxyFactory;

import java.lang.reflect.Proxy;

/**
 * 得到一个动态的代理对象
 *
 * 这个代理工厂的内部注入了一个叫做JDK的代理处理器，JDKClientInvocationHandler，
 * 它的核心任务就是将 [需要调用的方法名称、服务名称，参数] 统统都封装好到RpcInvocation当中，
 * 然后塞入到一个队列里，并且等待服务端的数据返回。
 * @Author : Ruoyi Chen
 * @create 2022/12/15 17:30
 */
public class JDKProxyFactory implements ProxyFactory {
    /**
     * public static Object newProxyInstance(ClassLoader loader, Class<?>[] interfaces, InvocationHandler h) throws IllegalArgumentException
     * loader:　　    一个ClassLoader对象，定义了由哪个ClassLoader对象来对生成的代理对象进行加载
     * interfaces:　　一个Interface对象的数组，表示的是我将要给我需要代理的对象提供一组什么接口，如果我提供了一组接口给它，那么这个代理对象就宣称实现了该接口(多态)，这样我就能调用这组接口中的方法了
     * h:　　         一个InvocationHandler对象，表示的是当我这个动态代理对象在调用方法的时候，会关联到哪一个InvocationHandler对象上
     */
    public <T> T getProxy(Class clazz) throws Throwable {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
                new JDKClientInvocationHandler(clazz));
    }
}
