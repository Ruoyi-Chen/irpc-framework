package org.idea.irpc.framework.core.config;

import lombok.Data;

@Data
public class ClientConfig {
    private String applicationName;
    private String registerAddr;
    private String proxyType;
    private String routerStrategy;
    private String clientSerialize;
}
