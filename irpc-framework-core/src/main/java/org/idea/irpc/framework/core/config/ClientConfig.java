package org.idea.irpc.framework.core.config;

import lombok.Data;

@Data
public class ClientConfig {
    private String applicationName;
    private String registerAddr;
    private String proxyType;
    private String routerStrategy;
    private String clientSerialize;
    private String registerType;
    /**
     * 客户端发数据的超时时间
     */
    private Integer timeOut;
    /**
     * 客户端最大响应数据体积
     */
    private Integer maxServerRespDataSize;
}
