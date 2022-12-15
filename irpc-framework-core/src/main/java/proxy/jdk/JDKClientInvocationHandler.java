package proxy.jdk;

import common.protocol.RpcInvocation;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static common.cache.CommonClientCache.RESP_MAP;
import static common.cache.CommonClientCache.SEND_QUEUE;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/15 17:32
 */
public class JDKClientInvocationHandler implements InvocationHandler {
    private final static Object OBJECT = new Object();
    private final static long TIMEOUT = 3 * 1000;
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
        long beginTime = System.currentTimeMillis();
        // 客户端请求超时的判断依据
        while (System.currentTimeMillis() - beginTime < TIMEOUT) {
            Object obj = RESP_MAP.get(rpcInvocation.getUuid());
            if (obj instanceof RpcInvocation) {
                return ((RpcInvocation) obj).getResponse();
            }
        }
        throw new TimeoutException("client wait server's response timeout!");
    }
}
