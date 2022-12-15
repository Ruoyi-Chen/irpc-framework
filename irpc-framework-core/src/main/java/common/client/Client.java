package common.client;

import com.alibaba.fastjson.JSON;
import common.protocol.RpcDecoder;
import common.protocol.RpcEncoder;
import common.protocol.RpcInvocation;
import common.protocol.RpcProtocol;
import config.ClientConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import proxy.jdk.JDKProxyFactory;
import service.DataService;

import static common.cache.CommonClientCache.SEND_QUEUE;

/**
 * 客户端
 *
 * 客户端首先需要通过一个代理工厂获取被调用对象的代理对象，
 * 然后通过代理对象将数据放入发送队列，
 * 最后会有一个异步线程将发送队列内部的数据一个个地发送给到服务端，
 * 并且等待服务端响应对应的数据结果。
 * @Author : Ruoyi Chen
 * @create 2022/12/15 16:40
 */
@Data
@Slf4j
public class Client {
    private ClientConfig clientConfig;

    public static EventLoopGroup clientGroup = new NioEventLoopGroup();

    public static void main(String[] args) throws Throwable {
        // 1. 初始化客户端，配置参数
        Client client = new Client();
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setPort(9090);
        clientConfig.setServerAddr("localhost");
        client.setClientConfig(clientConfig);
        // 2. 启动客户端
        RpcReference rpcReference = client.startClientApplication();
        DataService dataService = rpcReference.get(DataService.class);

        for (int i = 0; i < 100; i++) {
            String result = dataService.sendData("test" + i);
            log.info("客户端发送数据--> " + result);
        }
    }

    private RpcReference startClientApplication() throws InterruptedException {
        NioEventLoopGroup clientGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel channel) throws Exception {
                // 管道中初始化了解码器、编码器、客户端相应类
                channel.pipeline().addLast(new RpcEncoder());
                channel.pipeline().addLast(new RpcDecoder());
                channel.pipeline().addLast(new ClientHandler());
            }
        });

        // 常规地连接netty服务端
        ChannelFuture channelFuture = bootstrap.connect(clientConfig.getServerAddr(), clientConfig.getPort()).sync();
        log.info("====== Client 服务启动 ======");
        // 注入一个代理工厂
        RpcReference rpcReference = new RpcReference(new JDKProxyFactory());
        return rpcReference;
    }

    /**
     * 开启发送线程，专门从事将数据包发送给服务端，起到一个解耦的效果
     * 将请求发送任务交给单独的IO线程区负责，实现异步化，提升发送性能。
     * @param channelFuture
     */
    private void startClient(ChannelFuture channelFuture) {
        Thread asyncSendJob = new Thread(new AsyncSendJob(channelFuture));
        asyncSendJob.start();
    }


    private class AsyncSendJob implements Runnable {
        private ChannelFuture channelFuture;

        public AsyncSendJob(ChannelFuture channelFuture) {
            this.channelFuture = channelFuture;
        }

        public void run() {
            while (true) {

                try {
                    // 阻塞模式，从发送队列中取出一个对象
                    RpcInvocation data = SEND_QUEUE.take();
                    // 将RpcInvocation封装到RpcProtocol对象中，发送给服务端，这里正好对应了ServerHandler
                    String json = JSON.toJSONString(data);
                    RpcProtocol rpcProtocol = new RpcProtocol(json);

                    // netty的通道负责发送数据到服务端
                    channelFuture.channel().writeAndFlush(rpcProtocol);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
