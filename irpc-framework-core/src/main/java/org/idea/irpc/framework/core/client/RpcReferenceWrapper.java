package org.idea.irpc.framework.core.client;

import lombok.Data;

/**
 * rpc远程调用包装类
 *
 * @Author Chen Ruoyi
 * @Date created in 11:28 上午 2022/1/29
 */
@Data
public class RpcReferenceWrapper<T> {
    private Class<T> aimClass;
    private String group;
}
