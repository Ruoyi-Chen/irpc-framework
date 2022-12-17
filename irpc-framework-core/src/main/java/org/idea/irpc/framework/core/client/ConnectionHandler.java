package org.idea.irpc.framework.core.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import org.idea.irpc.framework.core.common.ChannelFutureWrapper;
import org.idea.irpc.framework.core.common.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.CONNECT_MAP;
import static org.idea.irpc.framework.core.common.cache.CommonClientCache.SERVER_ADDRESS;

/**
 * 当注册中心的节点新增或者移除或者权重变化的时候，这个类主要负责对内存中的url做变更
 *
 * 将【连接的建立，断开，按照服务名筛选】等功能都封装在了一起，
 * 按照单一职责的设计原则，将与连接有关的功能都统一封装在了一起。
 *
 * @Author : Ruoyi Chen
 * @create 2022/12/17 14:52
 */
public class ConnectionHandler {
    /**
     * 核心的连接处理器
     * 专门负责和服务端构建连接通信
     */
    private static Bootstrap bootstrap;

    public static void setBootstrap(Bootstrap bootstrap) {
        ConnectionHandler.bootstrap = bootstrap;
    }

    /**
     * 构建单个连接通道 元操作，既要处理连接，还要将连接进行内存存储管理
     *
     * @param providerServiceName
     * @param providerIp
     */
    public static void connect(String providerServiceName, String providerIp) throws InterruptedException {
        if (bootstrap == null) {
            throw new RuntimeException("boostrap can not be null");
        }

        // 格式错误类型的信息
        if (!providerIp.contains(":")) {
            return;
        }

        String[] providerAddress = providerIp.split(":");
        String ip = providerAddress[0];
        Integer port = Integer.parseInt(providerAddress[1]);
        // 将boostrap连接得到的channelFuture封装成ChannelFutureWrapper
        ChannelFuture channelFuture = bootstrap.connect(ip, port).sync();
        ChannelFutureWrapper channelFutureWrapper = new ChannelFutureWrapper();
        channelFutureWrapper.setChannelFuture(channelFuture);
        channelFutureWrapper.setHost(ip);
        channelFutureWrapper.setPort(port);

        // 将服务提供者ip放进SERVER_ADDRESS
        SERVER_ADDRESS.add(providerIp);
        // 将服务提供者future放入CONNECT_MAP
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(providerServiceName);
        if (CommonUtils.isEmptyList(channelFutureWrappers)) {
            channelFutureWrappers = new ArrayList<>();
        }
        channelFutureWrappers.add(channelFutureWrapper);
        CONNECT_MAP.put(providerServiceName, channelFutureWrappers);
    }

    /**
     * 构建ChannelFuture
     * @param host
     * @param port
     * @return
     * @throws InterruptedException
     */
    public static ChannelFuture createChannelFuture(String host, Integer port) throws InterruptedException {
        ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
        return channelFuture;
    }

    public static void disConnect(String providerServiceName, String providerIp) {
        SERVER_ADDRESS.remove(providerIp);
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(providerServiceName);
        if (CommonUtils.isNotEmptyList(channelFutureWrappers)) {
            channelFutureWrappers = channelFutureWrappers.stream().filter(channelFutureWrapper -> {
                return (!providerIp.equals(channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort()));
            }).collect(Collectors.toList());
        }
//        Iterator<ChannelFutureWrapper> iterator = channelFutureWrappers.iterator();
//        while (iterator.hasNext()) {
//            ChannelFutureWrapper channelFutureWrapper = iterator.next();
//            if (providerIp.equals(channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort())) {
//                iterator.remove();
//            }
//        }
    }

    /**
     * 默认走随机策略获取ChannelFuture
     *
     * @param providerServiceName
     * @return
     */
    public static ChannelFuture getChannelFuture(String providerServiceName) {
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(providerServiceName);
        if (CommonUtils.isEmptyList(channelFutureWrappers)) {
            throw new RuntimeException("no provider exist for " + providerServiceName);
        }
        return channelFutureWrappers.get(new Random().nextInt(channelFutureWrappers.size())).getChannelFuture();
    }

}
