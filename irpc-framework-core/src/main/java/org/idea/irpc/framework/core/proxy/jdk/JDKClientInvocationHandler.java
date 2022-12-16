package org.idea.irpc.framework.core.proxy.jdk;

import org.idea.irpc.framework.core.common.protocol.RpcInvocation;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.RESP_MAP;
import static org.idea.irpc.framework.core.common.cache.CommonClientCache.SEND_QUEUE;

/**
 * 每一个动态代理类都必须要实现InvocationHandler这个接口，
 * 并且每个代理类的实例都关联到了一个handler，
 * 当我们通过代理对象调用一个方法的时候，这个方法的调用就会被转发为由InvocationHandler这个接口的 invoke 方法来进行调用。
 * @Author : Ruoyi Chen
 * @create 2022/12/15 17:32
 */
@Slf4j
public class JDKClientInvocationHandler implements InvocationHandler {
    private final static Object OBJECT = new Object();
    private Class<?> clazz;
    public JDKClientInvocationHandler(Class<?> clazz) {
        this.clazz = clazz;
    }

    /**
     * 对每次请求都增加一个uuid进行区分，这样可以将请求和响应进行关联匹配，方便我们在客户端接收数据的时候进行识别。
     * @return
     * @throws Throwable
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcInvocation rpcInvocation = new RpcInvocation();
        rpcInvocation.setArgs(args);
        rpcInvocation.setTargetMethod(method.getName());
        rpcInvocation.setTargetServiceName(clazz.getName());

        // 注入uuid，对每一次请求都做单独区分
        rpcInvocation.setUuid(UUID.randomUUID().toString());
        RESP_MAP.put(rpcInvocation.getUuid(), OBJECT);

        // 将请求参数放入到发送队列
        SEND_QUEUE.add(rpcInvocation);
        log.info("Sending: {}", rpcInvocation);
        // 客户端请求超时的判断依据
        long beginTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - beginTime < 3*1000) {
            Object object = RESP_MAP.get(rpcInvocation.getUuid());
            if (object instanceof RpcInvocation) {
                return ((RpcInvocation)object).getResponse();
            }
        }
        throw new TimeoutException("client wait server's response timeout!");
    }
}
