package common.protocol;

import common.constants.RpcConstants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * RPC解码器
 *
 * 解码器的思路：
     * 服务器不断将收到的客户端的字节流数据放到ByteBuf这个字节数组容器中，
     * 只不过考虑到有粘包(多个包一起发送了)和半包(1个包被分成了多部分发送)，
     * 因此需要记录初始的readerIndex，后面判断如果解析不出来一个完整的数据包，则读取指针归位即可，
     *
     * 否则就是截取出一个完整的客户端数据包，
     * 同时移动了readerIndex指针，等待不断地收取数据截取下一个数据包。
         * 没有收到一个完整的数据包时，下一个ServerHandler中的Object msg也不会有东西，
         * 收到了一个完整的数据包，则强转为RpcProtocol，就可以进行业务处理了。

 * @Author : Ruoyi Chen
 * @create 2022/12/15 16:09
 */
public class RpcDecoder extends ByteToMessageDecoder {

    /**
     * 协议的开头部分的标准长度
     */
    public final int BASE_LENGTH = 2 + 4;

    private final long MAX_ALLOWED_SIZE = 1000;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        if (byteBuf.readableBytes() >= BASE_LENGTH) {
            // 防止收到一些体积过大的数据包
            if (byteBuf.readableBytes() > MAX_ALLOWED_SIZE) {
                // 跳过所有可读的bytes（直接不收了）
                byteBuf.skipBytes(byteBuf.readableBytes());
            }

            int beginReader;
            while (true) {
                beginReader = byteBuf.readerIndex();
                byteBuf.markReaderIndex();
                // 读一个short，这里对应了RpcProtocol的魔数
                // 如果魔数对不上，说明是非法客户端发来的数据包，直接把channel关了
                if (byteBuf.readShort() == RpcConstants.MAGIC_NUMBER) {
                    break;
                } else {
                    ctx.close();
                    return;
                }
            }

            // 往后读一个int, 这里对应了RpcProtocol对象的contentLength字段
            int length = byteBuf.readInt();
            // 如果可读的bytes比length小，说明剩余数据包不是完整的。这里还需要重置读索引。
            if (byteBuf.readableBytes() < length) {
                byteBuf.readerIndex(beginReader);
                return;
            }

            // 读取byteBuf中的数据，这里其实就是实际RpcProtocol对象的content字段，将其放入RpcProtocol并放入out列表中
            byte[] data = new byte[length];
            byteBuf.readBytes(data);
            RpcProtocol rpcProtocol = new RpcProtocol(data);
            out.add(rpcProtocol);
        }
    }
}
