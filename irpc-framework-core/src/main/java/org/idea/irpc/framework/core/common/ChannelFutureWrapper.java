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
}
