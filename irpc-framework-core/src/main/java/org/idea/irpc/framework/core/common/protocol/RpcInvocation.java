package org.idea.irpc.framework.core.common.protocol;


import lombok.Data;
import lombok.ToString;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class RpcInvocation {
    private String targetMethod;
    private String targetServiceName;
    private Object[] args;
    /**
     * uuid主要是用于匹配请求和响应的一个关键值。
     * 当请求从客户端发出的时候，会有一个uuid用于记录发出的请求，
     * 待数据返回的时候通过uuid来匹配对应的请求线程，并且返回给调用线程。
     */
    private String uuid;
    private Object response;
    private Map<String, Object> attachments = new ConcurrentHashMap<>();
    // 记录服务端抛出的异常信息
    private Throwable e;
    // 重试次数
    private int retry;
}
