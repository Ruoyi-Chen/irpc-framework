package org.idea.irpc.framework.core.common.exception;

import org.idea.irpc.framework.core.common.protocol.RpcInvocation;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/27 22:57
 */
public class MaxServiceLimitRequestException extends IRpcException{

    public MaxServiceLimitRequestException(RpcInvocation rpcInvocation) {
        super(rpcInvocation);
    }
}
