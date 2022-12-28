package org.idea.irpc.framework.core.client;

import lombok.Data;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/28 18:52
 */
@Data
public class RpcReferenceFuture<T> {
    private RpcReferenceWrapper rpcReferenceWrapper;
    private Object response;
}
