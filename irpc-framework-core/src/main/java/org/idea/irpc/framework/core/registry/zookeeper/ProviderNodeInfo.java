package org.idea.irpc.framework.core.registry.zookeeper;

import lombok.Data;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/16 19:15
 */
@Data
public class ProviderNodeInfo {
    private String serviceName;
    private String address;
}
