package org.idea.irpc.framework.core.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import org.idea.irpc.framework.core.common.ChannelFutureWrapper;
import org.idea.irpc.framework.core.common.utils.CommonUtils;
import org.idea.irpc.framework.core.router.Selector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.*;

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
        // 将服务名放入CONNECT_MAP
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
     * 之前的调用逻辑设计思路：
     * 默认走随机策略获取ChannelFuture
     * 从注册中心获取服务的地址信息，并且缓存在一个MAP集合中。
     * 从缓存的MAP集合中根据服务名称查询到对应的通道List集合。
     * 从List集合中随机筛选一个Channel通道，发送数据包。
     *
     * 存在以下缺陷：
     * 假设目标机器的性能不一，如何对机器进行权重分配？
     * 每次都要执行Random函数，在高并发情况下对CPU的消耗会比较高。
     * 如何基于路由策略做ABTest？
     *
     * 【为什么用Random不好？】
     * JDK内部的Random类的底层设计基本思路是：
     * 先通过一个初始化种子的函数，
     * 然后和181783497276652981这个数字做乘法，
     * 再将其与System.nanoTime()做与运算得倒一个随机的种子。
     * 注意 System.nanoTime()) 是一个本地方法，
     * 调用本地方法的时候需要涉及到系统的上下文切换会比较消耗性能。
     * 生成了随机种子之后，会结合线性同余算法去做计算，从而得到随机数
     *
     * 既然Random函数的计算比较消耗性能，那么为何不尝试换种思路来实现随机调用呢？
     * 假设在进行远程调用的时候，预先产生一个随机数组，该数组定义好了需要调用的服务提供者顺序，
     * 接下来按照这个随机顺序去做轮询，其实达成的效果也是一样的。
     *
     * 所以我的设计思路是：
     * 在客户端和服务提供者进行连接建立的环节会触发路由层的一个refreshRouterArr函数，
     * 生成对应先后顺序的随机数组，并且将其存放在一个map集合中。
     *
     * @param providerServiceName
     * @return
     */
    public static ChannelFuture getChannelFuture(String providerServiceName) {
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(providerServiceName);
        if (CommonUtils.isEmptyList(channelFutureWrappers)) {
            throw new RuntimeException("no provider exist for " + providerServiceName);
        }
//        return channelFutureWrappers.get(new Random().nextInt(channelFutureWrappers.size())).getChannelFuture();
        Selector selector = new Selector();
        selector.setProviderServiceName(providerServiceName);
        ChannelFuture channelFuture = IROUTER.select(selector).getChannelFuture();
        return channelFuture;
    }

}
