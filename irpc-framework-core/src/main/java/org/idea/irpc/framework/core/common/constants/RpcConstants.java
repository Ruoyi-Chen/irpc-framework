package org.idea.irpc.framework.core.common.constants;

public class RpcConstants {
    public static final short MAGIC_NUMBER=19812;

    /**
     * 代理
     */
    public static final String JDK_PROXY_TYPE = "jdk";
    public static final String JAVASSIST_PROXY_TYPE = "javassist";

    /**
     * 路由
     */
    public static final String RANDOM_ROUTER_TYPE = "random";
    public static final String ROTATE_ROUTER_TYPE = "rotate";

    /**
     * 序列化
     */
    public static final String JDK_SERIALIZE_TYPE = "jdk";
    public static final String FAST_JSON_SERIALIZE_TYPE = "fastJson";
    public static final String HESSIAN2_SERIALIZE_TYPE = "hessian2";
    public static final String KRYO_SERIALIZE_TYPE = "kryo";

    public static final Integer DEFAULT_TIMEOUT = 3000;
    public static final Integer DEFAULT_THREAD_NUMS = 256;
    public static final Integer DEFAULT_QUEUE_SIZE = 512;
}
