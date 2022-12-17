package org.idea.irpc.framework.core.client;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.idea.irpc.framework.core.common.event.IRpcListenerLoader;
import org.idea.irpc.framework.core.common.protocol.RpcDecoder;
import org.idea.irpc.framework.core.common.protocol.RpcEncoder;
import org.idea.irpc.framework.core.common.protocol.RpcInvocation;
import org.idea.irpc.framework.core.common.protocol.RpcProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.utils.CommonUtils;
import org.idea.irpc.framework.core.config.PropertiesBootstrap;
import org.idea.irpc.framework.core.proxy.javassist.JavassistProxyFactory;
import org.idea.irpc.framework.core.proxy.jdk.JDKProxyFactory;
import org.idea.irpc.framework.core.registry.URL;
import org.idea.irpc.framework.core.registry.zookeeper.AbstractRegister;
import org.idea.irpc.framework.core.registry.zookeeper.ZookeeperRegister;
import org.idea.irpc.framework.interfaces.DataService;
import org.idea.irpc.framework.core.config.ClientConfig;

import java.util.List;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.SEND_QUEUE;
import static org.idea.irpc.framework.core.common.cache.CommonClientCache.SUBSCRIBE_SERVICE_LIST;

/**
 * 客户端
 * <p>
 * 客户端首先需要通过一个代理工厂获取被调用对象的代理对象，
 * 然后通过代理对象将数据放入发送队列，
 * 最后会有一个异步线程将发送队列内部的数据一个个地发送给到服务端，
 * 并且等待服务端响应对应的数据结果。
 *
 * @Author : Ruoyi Chen
 * @create 2022/12/15 16:40
 */
@Slf4j
public class Client {
    private ClientConfig clientConfig;

    private AbstractRegister abstractRegister;

    private IRpcListenerLoader iRpcListenerLoader;

    private Bootstrap bootstrap = new Bootstrap();

    public static EventLoopGroup clientGroup = new NioEventLoopGroup();

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    public void setClientConfig(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    public static void main(String[] args) throws Throwable {
        // 1. 初始化客户端，配置参数
        Client client = new Client();
//        ClientConfig clientConfig = new ClientConfig();
//        clientConfig.setPort(9091);
//        clientConfig.setServerAddr("localhost");
//        client.setClientConfig(clientConfig);
        // 2. 启动客户端
        RpcReference rpcReference = client.initClientApplication();
        DataService dataService = rpcReference.get(DataService.class);

        client.doSubscribeService(DataService.class);
        ConnectionHandler.setBootstrap(client.getBootstrap());
        client.doConnectServer();
        client.startClient();

        for (int i = 0; i < 100; i++) {
            String result = dataService.sendData("test");
            log.info("客户端收到结果--> " + result);
            Thread.sleep(1000);
        }
    }

    private RpcReference initClientApplication() {
        NioEventLoopGroup clientGroup = new NioEventLoopGroup();
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

        iRpcListenerLoader = new IRpcListenerLoader();
        iRpcListenerLoader.init();
        this.clientConfig = PropertiesBootstrap.loadClientConfigFromLocal();
        RpcReference rpcReference;
        if ("javassist".equals(clientConfig.getProxyType())) {
            rpcReference = new RpcReference(new JavassistProxyFactory());
        } else {
            rpcReference = new RpcReference(new JDKProxyFactory());
        }
        return rpcReference;
    }

    /**
     * 开始和各个provider建立连接
     */
    private void doConnectServer() {
        for (String providerServiceName : SUBSCRIBE_SERVICE_LIST) {
            List<String> providerIps = abstractRegister.getProviderIps(providerServiceName);
            for (String providerIp : providerIps) {
                try {
                    ConnectionHandler.connect(providerServiceName, providerIp);
                } catch (InterruptedException e) {
                    log.error("[doConnectServer] connect fail ", e);
                }
            }
            URL url = new URL();
            url.setServiceName(providerServiceName);
            // 客户端在此新增了一个订阅的功能
            abstractRegister.doAfterSubscribe(url);
        }
    }

    /**
     * 开启服务之前需要预先订阅对应的服务
     *
     * @param serviceBean
     */
    private void doSubscribeService(Class serviceBean) {
        if (abstractRegister == null) {
            abstractRegister = new ZookeeperRegister(clientConfig.getRegisterAddr());
        }

        URL url = new URL();
        url.setApplicationName(clientConfig.getApplicationName());
        url.setServiceName(serviceBean.getName());
        url.addParameter("host", CommonUtils.getIpAddress());
        abstractRegister.subscribe(url);
    }

//    private RpcReference startClientApplication() throws InterruptedException {
//        NioEventLoopGroup clientGroup = new NioEventLoopGroup();
//        Bootstrap bootstrap = new Bootstrap();
//        bootstrap.group(clientGroup);
//        bootstrap.channel(NioSocketChannel.class);
//        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
//            protected void initChannel(SocketChannel channel) throws Exception {
//                // 管道中初始化了解码器、编码器、客户端相应类
//                channel.pipeline().addLast(new RpcEncoder());
//                channel.pipeline().addLast(new RpcDecoder());
//                channel.pipeline().addLast(new ClientHandler());
//            }
//        });
//
//        // 常规地连接netty服务端
//        ChannelFuture channelFuture = bootstrap.connect(clientConfig.getServerAddr(), clientConfig.getPort()).sync();
//        log.info("====== Client 服务启动 ======");
//        this.startClient(channelFuture);
//        // 注入一个代理工厂
//        RpcReference rpcReference = new RpcReference(new JDKProxyFactory());
//        return rpcReference;
//    }

    /**
     * 开启发送线程，专门从事将数据包发送给服务端，起到一个解耦的效果
     * 将请求发送任务交给单独的IO线程区负责，实现异步化，提升发送性能。
     *
     * @param
     */
    private void startClient() {
        Thread asyncSendJob = new Thread(new AsyncSendJob());
        asyncSendJob.start();
    }


    private class AsyncSendJob implements Runnable {
        private ChannelFuture channelFuture;

        public AsyncSendJob() {
        }

        public void run() {
            while (true) {

                try {
                    // 阻塞模式，从发送队列中取出一个对象
                    RpcInvocation data = SEND_QUEUE.take();
                    // 将RpcInvocation封装到RpcProtocol对象中，发送给服务端，这里正好对应了ServerHandler
                    String json = JSON.toJSONString(data);
                    RpcProtocol rpcProtocol = new RpcProtocol(json.getBytes());

                    ChannelFuture channelFuture = ConnectionHandler.getChannelFuture(data.getTargetServiceName());
                    // netty的通道负责发送数据到服务端
                    channelFuture.channel().writeAndFlush(rpcProtocol);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
