package org.idea.irpc.framework.core.common.cache;

import org.idea.irpc.framework.core.common.ChannelFuturePollingRef;
import org.idea.irpc.framework.core.common.ChannelFutureWrapper;
import org.idea.irpc.framework.core.common.protocol.RpcInvocation;
import org.idea.irpc.framework.core.config.ClientConfig;
import org.idea.irpc.framework.core.filter.client.ClientFilterChain;
import org.idea.irpc.framework.core.registy.URL;
import org.idea.irpc.framework.core.registy.zookeeper.AbstractRegister;
import org.idea.irpc.framework.core.router.IRouter;
import org.idea.irpc.framework.core.serialize.SerializeFactory;
import org.idea.irpc.framework.core.spi.ExtensionLoader;

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

    // provider名称 --> 该服务有哪些集群URL
    /**
     * 订阅服务列表
     */
    public static List<URL> SUBSCRIBE_SERVICE_LIST = new ArrayList<>();

    /**
     * URL列表
     */
    public static Map<String, Map<String,String>> URL_MAP = new ConcurrentHashMap<>();
//    public static Map<String, List<URL>> URL_MAP = new ConcurrentHashMap<>();

    /**
     * 服务器地址
     */
    public static Set<String> SERVER_ADDRESS = new HashSet<>();

    /**
     * 信道连接池 //每次进行远程调用的时候都是从这里面去选择服务提供者
     */
    public static Map<String, List<ChannelFutureWrapper>> CONNECT_MAP = new ConcurrentHashMap<>();

    /**
     *     随机请求的map
     */
    public static Map<String, ChannelFutureWrapper[]> SERVICE_ROUTER_MAP = new ConcurrentHashMap<>();
    public static ChannelFuturePollingRef CHANNEL_FUTURE_POLLING_REF = new ChannelFuturePollingRef();
    public static IRouter IROUTER;
    public static SerializeFactory CLIENT_SERIALIZE_FACTORY;
    public static ClientConfig CLIENT_CONFIG;
    public static ClientFilterChain CLIENT_FILTER_CHAIN;
    public static AbstractRegister ABSTRACT_REGISTER;
    public static ExtensionLoader EXTENSION_LOADER = new ExtensionLoader();
}
