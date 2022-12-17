package org.idea.irpc.framework.core.config;


import lombok.Data;

@Data
public class ServerConfig {
    private Integer serverPort;
    private String registerAddr;
    private String  applicationName;
}
