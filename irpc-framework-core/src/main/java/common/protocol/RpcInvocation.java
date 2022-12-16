package common.protocol;


import lombok.Data;
import lombok.ToString;

import java.util.Arrays;

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


    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTargetMethod() {
        return targetMethod;
    }

    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }

    public String getTargetServiceName() {
        return targetServiceName;
    }

    public void setTargetServiceName(String targetServiceName) {
        this.targetServiceName = targetServiceName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }


    @Override
    public String toString() {
        return "RpcInvocation{" +
                "targetMethod='" + targetMethod + '\'' +
                ", targetServiceName='" + targetServiceName + '\'' +
                ", args=" + Arrays.toString(args) +
                ", uuid='" + uuid + '\'' +
                ", response=" + response +
                '}';
    }
}
