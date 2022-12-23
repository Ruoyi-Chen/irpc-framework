package org.idea.irpc.framework.core.filter;

import org.idea.irpc.framework.core.common.ChannelFutureWrapper;
import org.idea.irpc.framework.core.common.protocol.RpcInvocation;

import java.util.List;

/**
 * 客户端过滤器
 * @Author : Ruoyi Chen
 * @create 2022/12/23 16:12
 */
public interface IClientFilter extends IFilter{
    /**
     * 执行过滤链
     *
     * @param src
     * @param rpcInvocation
     */
    void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation);
}
