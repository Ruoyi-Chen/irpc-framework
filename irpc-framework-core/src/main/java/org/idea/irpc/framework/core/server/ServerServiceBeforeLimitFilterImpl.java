package org.idea.irpc.framework.core.server;

import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.SPI;
import org.idea.irpc.framework.core.common.ServerServiceSemaphoreWrapper;
import org.idea.irpc.framework.core.common.exception.MaxServiceLimitRequestException;
import org.idea.irpc.framework.core.common.protocol.RpcInvocation;
import org.idea.irpc.framework.core.filter.IServerFilter;

import java.util.concurrent.Semaphore;

import static org.idea.irpc.framework.core.common.cache.CommonServerCache.SERVER_SERVICE_SEMAPHORE_MAP;

/**
 * 服务端方法限流过滤器
 *
 * 前置过滤器：
 * 请求数据在执行实际业务函数之前需要会经过前置过滤器的逻辑，
 * 而限流组件则是在前置过滤器的最后一环，主要负责tryAcquire环节。
 *
 * @Author : Ruoyi Chen
 * @create 2022/12/27 22:51
 */
@Slf4j
@SPI("before")
public class ServerServiceBeforeLimitFilterImpl implements IServerFilter {
    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        String serviceName = rpcInvocation.getTargetServiceName();
        ServerServiceSemaphoreWrapper serverServiceSemaphoreWrapper = SERVER_SERVICE_SEMAPHORE_MAP.get(serviceName);
        // 从缓存中提取semaphore对象
        Semaphore semaphore = serverServiceSemaphoreWrapper.getSemaphore();
        boolean tryResult = semaphore.tryAcquire();
        // 如果没拿到semaphore，抛出MaxServiceLimitRequestException异常
        if (!tryResult) {
            log.error("[ServerServiceBeforeLimitFilterImpl] {}'s max request is {},reject now", rpcInvocation.getTargetServiceName(), serverServiceSemaphoreWrapper.getMaxNums());
            MaxServiceLimitRequestException iRpcException = new MaxServiceLimitRequestException(rpcInvocation);
            rpcInvocation.setE(iRpcException);
            throw iRpcException;
        }
    }
}
