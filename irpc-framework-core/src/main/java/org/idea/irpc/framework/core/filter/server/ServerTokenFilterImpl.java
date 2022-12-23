package org.idea.irpc.framework.core.filter.server;

import org.idea.irpc.framework.core.common.protocol.RpcInvocation;
import org.idea.irpc.framework.core.common.utils.CommonUtils;
import org.idea.irpc.framework.core.filter.IServerFilter;
import org.idea.irpc.framework.core.server.ServiceWrapper;

import static org.idea.irpc.framework.core.common.cache.CommonServerCache.PROVIDER_SERVICE_WRAPPER_MAP;

/**
 * 服务端Token校验过滤器
 *
 * @Author : Ruoyi Chen
 * @create 2022/12/23 16:31
 */
public class ServerTokenFilterImpl implements IServerFilter {
    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        String token = (String) rpcInvocation.getAttachments().get("serviceToken");
        ServiceWrapper serviceWrapper = PROVIDER_SERVICE_WRAPPER_MAP.get(rpcInvocation.getTargetServiceName());
        String matchToken = serviceWrapper.getServiceToken();
        // 该服务不需要token
        if (CommonUtils.isEmpty(matchToken)) {
            return;
        }
        // token匹配
        if (!CommonUtils.isEmpty(token) && token.equals(matchToken)) {
            return;
        }
        throw new RuntimeException("token is " + token + ", verify result is false!");
    }
}
