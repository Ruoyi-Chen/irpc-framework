package org.idea.irpc.framework.core.common;

import java.util.concurrent.atomic.AtomicLong;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.SERVICE_ROUTER_MAP;

/**
 * Map集合中只需要按照服务的key查询到对应的服务调用顺序数组，
 * 接下来就是对该数组进行轮询即可，
 * ChannelFutureRefWrapper类就是专门实现轮训效果，
 * 它的本质就是通过取模计算。
 *
 * @Author : Ruoyi Chen
 * @create 2022/12/21 16:13
 */
public class ChannelFuturePollingRef {
    private AtomicLong referenceTimes = new AtomicLong(0);

    public ChannelFutureWrapper getChannelFutureWrapper(String serviceName) {
        ChannelFutureWrapper[] wrappers = SERVICE_ROUTER_MAP.get(serviceName);
        long i = referenceTimes.getAndIncrement();
        int idx = (int) (i % wrappers.length);
        return wrappers[idx];
    }
}
