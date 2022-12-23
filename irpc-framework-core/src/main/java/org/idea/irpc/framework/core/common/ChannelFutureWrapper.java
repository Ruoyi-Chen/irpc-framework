package org.idea.irpc.framework.core.common;

import io.netty.channel.ChannelFuture;
import lombok.Data;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/16 19:24
 */
@Data
public class ChannelFutureWrapper {
    private ChannelFuture channelFuture;
    private String host;
    private Integer port;
    /**
     * [路由层]
     * ChannelFutureWrapper对象的则是将host，port，channelFuture对象进行了相关的包装。
     * 为了能够实现按照权重的配置，这里可以加入一个weight字段的设计。
     */
    private Integer weight;
    private String group;

    public ChannelFutureWrapper(String host, Integer port,Integer weight) {
        this.host = host;
        this.port = port;
        this.weight = weight;
    }


    public ChannelFutureWrapper() {
    }
}
