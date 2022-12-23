package org.idea.irpc.framework.core.registry.zookeeper;

import lombok.Data;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/16 19:15
 */
@Data
public class ProviderNodeInfo {
    private String applicationName;
    private String serviceName;
    private String address;
    private Integer weight;
    private String registryTime;
    private String group;
}
