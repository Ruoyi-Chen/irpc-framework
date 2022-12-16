package org.idea.irpc.framework.core.common.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * RPC 编码器
 *
 * 编码器的思路：
 * 这个就很简单了，直接就是2字节的magic number + 包体的4个字节 + content字节数据即可直接塞到ByteBuf中进行发送，无半包、粘包等问题的考虑。
 * @Author : Ruoyi Chen
 * @create 2022/12/15 16:04
 */
public class RpcEncoder extends MessageToByteEncoder<RpcProtocol> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcProtocol msg, ByteBuf out) throws Exception {
        // 发送顺序： 魔数 -> 长度 -> 内容
        out.writeShort(msg.getMagicNumber());
        out.writeInt(msg.getContentLength());
        out.writeBytes(msg.getContent());
    }
}
