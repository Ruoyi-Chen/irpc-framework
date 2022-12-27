package org.idea.irpc.framework.core.proxy.javassist;

import com.sun.deploy.security.CredentialInfo;
import org.idea.irpc.framework.core.client.RpcReferenceWrapper;
import org.idea.irpc.framework.core.common.protocol.RpcInvocation;
import org.idea.irpc.framework.core.filter.IFilter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.RESP_MAP;
import static org.idea.irpc.framework.core.common.cache.CommonClientCache.SEND_QUEUE;
import static org.idea.irpc.framework.core.common.constants.RpcConstants.DEFAULT_TIMEOUT;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/15 19:07
 */
public class JavassistInvocationHandler implements InvocationHandler {


    private final static Object OBJECT = new Object();
    private Long timeOut = Long.valueOf(DEFAULT_TIMEOUT);
    private RpcReferenceWrapper rpcReferenceWrapper;

    public JavassistInvocationHandler(RpcReferenceWrapper rpcReferenceWrapper) {
        this.rpcReferenceWrapper = rpcReferenceWrapper;
        timeOut = Long.valueOf(String.valueOf(rpcReferenceWrapper.getAttatchments().get("timeOut")));
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcInvocation rpcInvocation = new RpcInvocation();
        rpcInvocation.setArgs(args);
        rpcInvocation.setTargetMethod(method.getName());
        rpcInvocation.setTargetServiceName(rpcReferenceWrapper.getAimClass().getName());
        rpcInvocation.setAttachments(rpcReferenceWrapper.getAttatchments());
        rpcInvocation.setUuid(UUID.randomUUID().toString());
        rpcInvocation.setRetry(rpcReferenceWrapper.getRetry());

        //代理类内部将请求放入到发送队列中，等待发送队列发送请求
        SEND_QUEUE.add(rpcInvocation);

        // 既然是异步请求，就没有必要在RESP_MAP中判断是否有响应结果了
        if (rpcReferenceWrapper.isAsync()) {
            return null;
        }
        RESP_MAP.put(rpcInvocation.getUuid(), OBJECT);

        long beginTime = System.currentTimeMillis();
        int retryTimes = 0;
        //如果请求数据在指定时间内返回则返回给客户端调用方
        while (System.currentTimeMillis() - beginTime < timeOut || rpcInvocation.getRetry() > 0) {
            Object object = RESP_MAP.get(rpcInvocation.getUuid());
            if (object instanceof RpcInvocation) {
                RpcInvocation rpcInvocationResp = (RpcInvocation) object;
                // 正常结果
                if (rpcInvocationResp.getRetry() == 0 && rpcInvocationResp.getE() == null) {
                    return rpcInvocationResp.getResponse();
                } else if (rpcInvocationResp.getE() != null) {
                    // 每次重试之后会将retry值扣减1
                    if (rpcInvocationResp.getRetry() == 0) {
                        return rpcInvocationResp.getResponse();
                    }
                    // 如果是因为超时的情况，才会触发重试规则，否则重试机制不生效
                    if (System.currentTimeMillis() - beginTime > timeOut) {
                        retryTimes++;
                        // 重新请求
                        rpcInvocation.setResponse(null);
                        rpcInvocation.setRetry(rpcInvocationResp.getRetry() - 1);
                        RESP_MAP.put(rpcInvocation.getUuid(), OBJECT);
                        SEND_QUEUE.add(rpcInvocation);
                    }
                }
            }
        }
//        while (System.currentTimeMillis() - beginTime < timeOut) {
//            Object object = RESP_MAP.get(rpcInvocation.getUuid());
//            if (object instanceof RpcInvocation) {
//                return ((RpcInvocation)object).getResponse();
//            }
//        }
        throw new TimeoutException("Wait for response from server on client " + timeOut + "ms,retry times is " + retryTimes + ",service's name is " + rpcInvocation.getTargetServiceName() + "#" + rpcInvocation.getTargetMethod());
    }
}