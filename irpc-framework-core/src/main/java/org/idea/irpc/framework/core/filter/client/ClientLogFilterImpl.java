package org.idea.irpc.framework.core.filter.client;

import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.ChannelFutureWrapper;
import org.idea.irpc.framework.core.common.protocol.RpcInvocation;
import org.idea.irpc.framework.core.filter.IClientFilter;

import java.util.List;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.CLIENT_CONFIG;

/**
 * 客户端调用日志过滤器
 *
 * 客户端发起调用的日志记录过滤器部分，主要用于记录请求链路的信息
 *
 * @Author : Ruoyi Chen
 * @create 2022/12/23 16:20
 */
@Slf4j
public class ClientLogFilterImpl implements IClientFilter {
    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        rpcInvocation.getAttachments().put("c_app_name", CLIENT_CONFIG.getApplicationName());
        log.info(rpcInvocation.getAttachments().get("c_app_name") + "do invoke -----> " + rpcInvocation.getTargetServiceName());
    }
}
