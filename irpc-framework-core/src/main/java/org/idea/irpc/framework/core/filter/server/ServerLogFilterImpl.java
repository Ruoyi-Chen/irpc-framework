package org.idea.irpc.framework.core.filter.server;

import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.protocol.RpcInvocation;
import org.idea.irpc.framework.core.filter.IServerFilter;

/**
 * 服务端日志记录过滤器
 * @Author : Ruoyi Chen
 * @create 2022/12/23 16:30
 */
@Slf4j
public class ServerLogFilterImpl implements IServerFilter {
    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
      log.info(rpcInvocation.getAttachments().get("c_app_name") + "do invoke ----> " + rpcInvocation.getTargetServiceName() + "#" + rpcInvocation.getTargetMethod());
    }
}
