package common.protocol;


import lombok.Data;

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
}
