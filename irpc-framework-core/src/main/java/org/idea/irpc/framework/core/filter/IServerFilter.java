package org.idea.irpc.framework.core.filter;

import org.idea.irpc.framework.core.common.protocol.RpcInvocation;

/**
 * 服务端过滤链
 *
 * @Author : Ruoyi Chen
 * @create 2022/12/23 16:13
 */
public interface IServerFilter extends IFilter{
    /**
     * 执行核心过滤逻辑
     * @param rpcInvocation
     */
    void doFilter(RpcInvocation rpcInvocation);
}
