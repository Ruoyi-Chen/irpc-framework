package org.idea.irpc.framework.core.filter.client;

import org.idea.irpc.framework.core.common.ChannelFutureWrapper;
import org.idea.irpc.framework.core.common.protocol.RpcInvocation;
import org.idea.irpc.framework.core.filter.IClientFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端过滤链
 * @Author : Ruoyi Chen
 * @create 2022/12/23 16:16
 */
public class ClientFilterChain {
    private static List<IClientFilter> iClientFilters = new ArrayList<>();

    public void addClientFilter(IClientFilter iClientFilter) {
        iClientFilters.add(iClientFilter);
    }

    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        for (IClientFilter iClientFilter : iClientFilters) {
            iClientFilter.doFilter(src, rpcInvocation);
        }
    }
}
