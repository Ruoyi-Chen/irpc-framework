package org.idea.irpc.framework.core.common.event.listener;

import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.client.ConnectionHandler;
import org.idea.irpc.framework.core.common.ChannelFutureWrapper;
import org.idea.irpc.framework.core.common.event.IRpcListener;
import org.idea.irpc.framework.core.common.event.data.URLChangeWrapper;
import org.idea.irpc.framework.core.common.utils.CommonUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.CONNECT_MAP;

/**
 * zk的服务提供者节点发生了变更，需要发送事件通知操作的目的是什么？
 * 客户端需要更新本地的一个目标服务列表，避免向无用的服务发送请求。
 *
 *
 * @Author : Ruoyi Chen
 * @create 2022/12/17 14:15
 */
@Slf4j
public class ServiceUpdateListener implements IRpcListener {
    @Override
    public void callback(Object t) {
        // 获取到子节点的数据信息
        URLChangeWrapper urlChangeWrapper = (URLChangeWrapper) t;
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(urlChangeWrapper.getServiceName());
        // 回调结果列表是否为空
        if (CommonUtils.isEmptyList(channelFutureWrappers)) {
            log.error("[ServiceUpdateListener] channelFutureWrappers is empty");
            return;
        } else {
            // 获取可匹配服务提供者的url
            List<String> matchProviderUrl = urlChangeWrapper.getProviderUrl();
            // 确定最终服务提供者的url
            Set<String> finalUrl = new HashSet<>();
            List<ChannelFutureWrapper> finalChannelFutureWrappers = new ArrayList<>();
            for (ChannelFutureWrapper channelFutureWrapper : channelFutureWrappers) {
                // 对于每一个通道，得到他们原来的url
                String oldServerAddress  = channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort();
                if (!matchProviderUrl.contains(oldServerAddress)) {
                    // 如果老的url已经不在当前匹配服务提供者url集合里，说明已经被移除了
                    continue;
                } else {
                    // 如果还可以提供服务，将其加入最终名单里
                    finalChannelFutureWrappers.add(channelFutureWrapper);
                    finalUrl.add(oldServerAddress);
                }
            }

            // 此时老的url已经被移除了，开始检查是否有新的url
            /**
             * ChannelFuture是java.util.concurrent.Future的子类，
             * 它除了可以拿到线程的执行结果之外，还对其进行了扩展，
             * 加入了当前任务状态判断、等待任务执行和添加listener的功能。
             * 添加的Listener会在future执行结束之后，被通知。
             * 不需要自己再去调用get等待future结束。
             *
             * ChannelFutureWrapper其实是一个自定义的包装类，
             * 将netty建立好的ChannelFuture做了一些封装【ChannelFuture+host+port】
             */
            List<ChannelFutureWrapper> newChannelFutureWrapper = new ArrayList<>();
            for (String newProviderUrl : matchProviderUrl) {
                // 如果最终url里没有，再加进去
                if (!finalUrl.contains(newProviderUrl)){
                    ChannelFutureWrapper channelFutureWrapper = new ChannelFutureWrapper();
                    String host = newProviderUrl.split(":")[0];
                    Integer port = Integer.valueOf(newProviderUrl.split(":")[1]);
                    channelFutureWrapper.setHost(host);
                    channelFutureWrapper.setPort(port);

                    ChannelFuture channelFuture = null;
                    try{
                        channelFuture = ConnectionHandler.createChannelFuture(host, port);
                        channelFutureWrapper.setChannelFuture(channelFuture);
                        newChannelFutureWrapper.add(channelFutureWrapper);
                        finalUrl.add(newProviderUrl);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            finalChannelFutureWrappers.addAll(newChannelFutureWrapper);

            // 最终更新服务
            CONNECT_MAP.put(urlChangeWrapper.getServiceName(), finalChannelFutureWrappers);
        }
    }
}
