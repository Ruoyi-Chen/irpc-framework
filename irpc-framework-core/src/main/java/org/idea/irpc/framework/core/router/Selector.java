package org.idea.irpc.framework.core.router;

import lombok.Data;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/21 16:00
 */
@Data
public class Selector {
    /**
     * 服务命名
     * eg: com.sise.test.DataService
     */
    private String providerServiceName;
}
