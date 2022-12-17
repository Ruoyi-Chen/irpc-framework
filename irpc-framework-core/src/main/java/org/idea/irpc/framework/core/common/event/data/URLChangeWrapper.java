package org.idea.irpc.framework.core.common.event.data;

import lombok.Data;

import java.util.List;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/17 14:07
 */
@Data
public class URLChangeWrapper {
    private String serviceName;
    private List<String> providerUrl;
}
