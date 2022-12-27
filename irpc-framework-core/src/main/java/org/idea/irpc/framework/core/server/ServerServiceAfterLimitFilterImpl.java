package org.idea.irpc.framework.core.server;

import org.idea.irpc.framework.core.common.SPI;
import org.idea.irpc.framework.core.common.ServerServiceSemaphoreWrapper;
import org.idea.irpc.framework.core.common.protocol.RpcInvocation;
import org.idea.irpc.framework.core.filter.IServerFilter;

import static org.idea.irpc.framework.core.common.cache.CommonServerCache.SERVER_SERVICE_SEMAPHORE_MAP;

/**
 * 后置过滤器:
 * 当业务核心逻辑执行完毕之后，会进入到后置过滤器中，这里面可以执行relase操作。
 * @Author : Ruoyi Chen
 * @create 2022/12/27 23:01
 */
@SPI("after")
public class ServerServiceAfterLimitFilterImpl implements IServerFilter {
    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        String targetServiceName = rpcInvocation.getTargetServiceName();
        ServerServiceSemaphoreWrapper serverServiceSemaphoreWrapper = SERVER_SERVICE_SEMAPHORE_MAP.get(targetServiceName);
        serverServiceSemaphoreWrapper.getSemaphore().release();
    }
}
