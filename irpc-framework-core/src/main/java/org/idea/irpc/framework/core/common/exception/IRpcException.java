package org.idea.irpc.framework.core.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.idea.irpc.framework.core.common.protocol.RpcInvocation;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/27 21:24
 */
@Data
@AllArgsConstructor
public class IRpcException extends RuntimeException{
    private RpcInvocation rpcInvocation;
}
