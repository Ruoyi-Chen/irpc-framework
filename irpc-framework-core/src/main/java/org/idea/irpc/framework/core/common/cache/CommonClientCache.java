package org.idea.irpc.framework.core.common.cache;

import org.idea.irpc.framework.core.common.ChannelFuturePollingRef;
import org.idea.irpc.framework.core.common.ChannelFutureWrapper;
import org.idea.irpc.framework.core.common.protocol.RpcInvocation;
import org.idea.irpc.framework.core.config.ClientConfig;
import org.idea.irpc.framework.core.registry.URL;
import org.idea.irpc.framework.core.router.IRouter;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/15 17:02
 */
public class CommonClientCache {
    /**
     * 发送队列
     */
    public static BlockingQueue<RpcInvocation> SEND_QUEUE = new ArrayBlockingQueue<RpcInvocation>(100);
    /**
     * 响应队列
     */
    public static Map<String, Object> RESP_MAP = new ConcurrentHashMap<String, Object>();

    /**
     * 客户端配置信息
     */
    public static ClientConfig CLIENT_CONFIG;

    // provider名称 --> 该服务有哪些集群URL
    /**
     * 订阅服务列表
     */
    public static List<String> SUBSCRIBE_SERVICE_LIST = new ArrayList<>();

    /**
     * URL列表
     */
    public static Map<String, List<URL>> URL_MAP = new ConcurrentHashMap<>();

    /**
     * 服务器地址
     */
    public static Set<String> SERVER_ADDRESS = new HashSet<>();

    /**
     * 信道连接池
     */
    public static Map<String, List<ChannelFutureWrapper>> CONNECT_MAP = new ConcurrentHashMap<>();

    public static Map<String, ChannelFutureWrapper[]> SERVICE_ROUTER_MAP = new ConcurrentHashMap<>();
    public static ChannelFuturePollingRef CHANNEL_FUTURE_POLLING_REF = new ChannelFuturePollingRef();
    public static IRouter IROUTER;
}
