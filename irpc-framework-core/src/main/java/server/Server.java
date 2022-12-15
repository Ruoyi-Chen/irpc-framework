package server;

import common.protocol.RpcDecoder;
import common.protocol.RpcEncoder;
import config.ServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import service.impl.DataServiceImpl;

import static common.cache.CommonServerCache.PROVIDER_CLASS_MAP;

@Data
@Slf4j
public class Server {

    private ServerConfig serverConfig;

    /**
     * 用于接收客户端请求的线程池职责如下：
     *      接收客户端TCP连接，初始化Channel参数；
     *      将链路状态变更事件通知给ChannelPipeline；
     */
    private static EventLoopGroup bossGroup = null;

    /**
     * https://www.cnblogs.com/duanxz/p/3724395.html
     * 处理IO操作的线程池职责如下：
     *      异步读取远端数据，发送读事件到ChannelPipeline；
     *      异步发送数据到远端，调用ChannelPipeline的发送消息接口；
     *      执行系统调用Task；
     *      执行定时任务Task，如空闲链路检测等；
     */
    private static EventLoopGroup workerGroup = null;

    public static void main(String[] args) throws InterruptedException {
        // 1. 初始化server
        Server server = new Server();

        // 2. 配置server
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(9090);
        server.setServerConfig(serverConfig);

        // 3. 注册server服务
        server.registyService(new DataServiceImpl());

        // 4. 启动应用
        server.startApplication();
    }

    /**
     * 将服务注册到服务器
     * @param serviceBean
     */
    private void registyService(Object serviceBean) {
        // 如果这个service实现类没有接口，抛出异常
        if (serviceBean.getClass().getInterfaces().length == 0) {
            throw new RuntimeException("service must had interfaces! ");
        }

        // service实现类不能实现多个接口
        Class<?>[] interfaces = serviceBean.getClass().getInterfaces();
        if (interfaces.length > 1) {
            throw new RuntimeException("service must only have one interfaces!");
        }

        Class interfaceClass = interfaces[0];
        // 需要注册的对象统一放在一个MAP集合中进行管理 接口名 -> 实现类
        PROVIDER_CLASS_MAP.put(interfaceClass.getName(), serviceBean);
    }

    /**
     * 启动服务
     *  在创建ServerBootstrap类实例前，
     *  先创建两个EventLoopGroup，它们实际上是两个独立的Reactor线程池，
     *  bossGroup负责接收客户端的连接，
     *  workerGroup负责处理IO相关的读写操作，或者执行系统task、定时task等。
     *
     * ChannelOptions: https://netty.io/4.1/api/io/netty/channel/ChannelOption.html
     *
     */
    private void startApplication() throws InterruptedException {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_SNDBUF, 16 * 1024)
                .option(ChannelOption.SO_RCVBUF, 16 * 1024)
                .option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                log.info("初始化provider过程..");
                socketChannel.pipeline().addLast(new RpcEncoder());
                socketChannel.pipeline().addLast(new RpcDecoder());
                socketChannel.pipeline().addLast(new ServerHandler());
            }
        });
        bootstrap.bind(serverConfig.getPort()).sync();
    }
}