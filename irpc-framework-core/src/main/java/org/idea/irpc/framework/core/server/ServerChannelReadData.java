package org.idea.irpc.framework.core.server;

import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import org.idea.irpc.framework.core.common.protocol.RpcProtocol;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/25 19:53
 */
@Data
public class ServerChannelReadData {
    private RpcProtocol rpcProtocol;
    private ChannelHandlerContext channelHandlerContext;
}
