package org.idea.irpc.framework.core.server;

import lombok.Data;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/22 17:49
 */
@Data
public class ServiceWrapper {
    /**
     * 对外暴露的具体服务对象
     */
    private Object serviceObj;

    /**
     * 具体暴露服务的分组
     */
    private String group = "default";

    /**
     * 整个应用的token校验
     */
    private String serviceToken = "";

    /**
     * 限流策略
     */
    private Integer limit = -1;


    public ServiceWrapper(Object serviceObj) {
        this.serviceObj = serviceObj;
    }

    public ServiceWrapper(Object serviceObj, String group) {
        this.serviceObj = serviceObj;
        this.group = group;
    }
}
