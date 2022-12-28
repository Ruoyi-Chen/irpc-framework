package org.idea.irpc.framework.core.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import org.idea.irpc.framework.core.common.SPI;
import org.idea.irpc.framework.core.common.ServerServiceSemaphoreWrapper;
import org.idea.irpc.framework.core.common.event.IRpcListenerLoader;
import org.idea.irpc.framework.core.common.protocol.RpcDecoder;
import org.idea.irpc.framework.core.common.protocol.RpcEncoder;
import org.idea.irpc.framework.core.common.utils.CommonUtils;
import org.idea.irpc.framework.core.config.PropertiesBootstrap;
import org.idea.irpc.framework.core.config.ServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.filter.IServerFilter;
import org.idea.irpc.framework.core.filter.server.ServerFilterChain;
import org.idea.irpc.framework.core.filter.server.ServerLogFilterImpl;
import org.idea.irpc.framework.core.filter.server.ServerTokenFilterImpl;
import org.idea.irpc.framework.core.registy.RegistryService;
import org.idea.irpc.framework.core.registy.URL;
import org.idea.irpc.framework.core.registy.zookeeper.ZookeeperRegister;
import org.idea.irpc.framework.core.serialize.SerializeFactory;
import org.idea.irpc.framework.core.serialize.fastjson.FastJsonSerializeFactory;
import org.idea.irpc.framework.core.serialize.hessian.HessianSerializeFactory;
import org.idea.irpc.framework.core.serialize.jdk.JdkSerializeFactory;
import org.idea.irpc.framework.core.serialize.kryo.KryoSerializeFactory;

import java.io.IOException;
import java.util.LinkedHashMap;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.EXTENSION_LOADER;
import static org.idea.irpc.framework.core.common.cache.CommonServerCache.*;
import static org.idea.irpc.framework.core.common.cache.CommonServerCache.SERVER_BEFORE_FILTER_CHAIN;
import static org.idea.irpc.framework.core.common.constants.RpcConstants.*;
import static org.idea.irpc.framework.core.common.constants.RpcConstants.KRYO_SERIALIZE_TYPE;
import static org.idea.irpc.framework.core.spi.ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE;

@Slf4j
public class Server {

    private ServerConfig serverConfig;

    private static IRpcListenerLoader iRpcListenerLoader;


    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    /**
     * 用于接收客户端请求的线程池职责如下：
     * 接收客户端TCP连接，初始化Channel参数；
     * 将链路状态变更事件通知给ChannelPipeline；
     */
    private static EventLoopGroup bossGroup = null;

    /**
     * https://www.cnblogs.com/duanxz/p/3724395.html
     * 处理IO操作的线程池职责如下：
     * 异步读取远端数据，发送读事件到ChannelPipeline；
     * 异步发送数据到远端，调用ChannelPipeline的发送消息接口；
     * 执行系统调用Task；
     * 执行定时任务Task，如空闲链路检测等；
     */
    private static EventLoopGroup workerGroup = null;

    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        // 1. 初始化server
        Server server = new Server();

        // 2. 配置server
        server.initServerConfig();

//        // 3. 注册server服务
//        server.registyService(new DataServiceImpl());
        // 初始化监听器
        iRpcListenerLoader = new IRpcListenerLoader();
        iRpcListenerLoader.init();

        // 3. 向外暴露服务
        ServiceWrapper dataServiceServiceWrapper = new ServiceWrapper(new DataServiceImpl(), "dev");
        dataServiceServiceWrapper.setServiceToken("token-a");
        dataServiceServiceWrapper.setLimit(2);
        ServiceWrapper userServiceServiceWrapper = new ServiceWrapper(new UserServiceImpl(), "dev");
        userServiceServiceWrapper.setServiceToken("token-b");
        userServiceServiceWrapper.setLimit(2);

        server.exportService(dataServiceServiceWrapper);
        server.exportService(userServiceServiceWrapper);
        ApplicationShutdownHook.registryShutdownHook();

        // 4. 启动应用
        server.startApplication();
    }

    public void exportService(ServiceWrapper serviceWrapper) {
        Object serviceBean = serviceWrapper.getServiceObj();
        if (serviceBean.getClass().getInterfaces().length == 0) {
            throw new RuntimeException("service must had interfaces!");
        }
        Class<?>[] interfaces = serviceBean.getClass().getInterfaces();
        if (interfaces.length > 1) {
            throw new RuntimeException("service must only had one interface");
        }
        if (REGISTRY_SERVICE == null) {
            try {
                EXTENSION_LOADER.loadExtension(RegistryService.class);
                LinkedHashMap<String, Class> registryClassMap = EXTENSION_LOADER_CLASS_CACHE.get(RegistryService.class.getName());
                Class registryClass = registryClassMap.get(serverConfig.getRegisterType());
                REGISTRY_SERVICE = (RegistryService) registryClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("registryServiceType unKnow,error is ", e);
            }
        }

        // 默认选择该对象的第一个实现接口
        Class<?> interfaceClass = interfaces[0];
        PROVIDER_CLASS_MAP.put(interfaceClass.getName(), serviceBean);

        URL url = new URL();
        url.setServiceName(interfaceClass.getName());
        url.setApplicationName(serverConfig.getApplicationName());
        url.addParameter("host", CommonUtils.getIpAddress());
        url.addParameter("port", String.valueOf(serverConfig.getServerPort()));
        url.addParameter("group", String.valueOf(serviceWrapper.getGroup()));
        url.addParameter("limit", String.valueOf(serviceWrapper.getLimit()));

        //设置服务端的限流器
        SERVER_SERVICE_SEMAPHORE_MAP.put(interfaceClass.getName(), new ServerServiceSemaphoreWrapper(serviceWrapper.getLimit()));

        PROVIDER_URL_SET.add(url);

        if (!CommonUtils.isEmpty(serviceWrapper.getServiceToken())) {
            PROVIDER_SERVICE_WRAPPER_MAP.put(interfaceClass.getName(), serviceWrapper);
        }
    }


    public void initServerConfig() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        ServerConfig serverConfig = PropertiesBootstrap.loadServerConfigFromLocal();
        this.setServerConfig(serverConfig);
        SERVER_CONFIG = serverConfig;

        // 初始化线程池和队列的配置
        SERVER_CHANNEL_DISPATCHER.init(SERVER_CONFIG.getServerQueueSize(), SERVER_CONFIG.getServerBizThreadNums());

        // 序列化技术初始化
        String serverSerialize = serverConfig.getServerSerialize();
        EXTENSION_LOADER.loadExtension(SerializeFactory.class);
        LinkedHashMap<String, Class> serializeFactoryClassMap = EXTENSION_LOADER_CLASS_CACHE.get(SerializeFactory.class.getName());
        Class serializeFactoryClass = serializeFactoryClassMap.get(serverSerialize);
        if (serializeFactoryClass == null) {
            throw new RuntimeException("no match serialize type for " + serverSerialize);
        }
        SERVER_SERIALIZE_FACTORY = (SerializeFactory) serializeFactoryClass.newInstance();

        // 过滤链技术初始化
        EXTENSION_LOADER.loadExtension(IServerFilter.class);
        LinkedHashMap<String, Class> iServerFilterClassMap = EXTENSION_LOADER_CLASS_CACHE.get(IServerFilter.class.getName());
//        ServerFilterChain serverFilterChain = new ServerFilterChain();
        ServerBeforeFilterChain serverBeforeFilterChain = new ServerBeforeFilterChain();
        ServerAfterFilterChain serverAfterFilterChain = new ServerAfterFilterChain();
        for (String iServerFilterKey : iServerFilterClassMap.keySet()) {
            Class iServerFilterClass = iServerFilterClassMap.get(iServerFilterKey);
            if (iServerFilterClass == null) {
                throw new RuntimeException("no match iServerFilter type for " + iServerFilterKey);
            }
            SPI spi = (SPI) iServerFilterClass.getDeclaredAnnotation(SPI.class);
            if (spi != null && "before".equals(spi.value())) {
                serverBeforeFilterChain.addServerFilter((IServerFilter) iServerFilterClass.newInstance());
            } else if (spi != null && "after".equals(spi.value())) {
                serverAfterFilterChain.addServerFilter((IServerFilter) iServerFilterClass.newInstance());
            }
            SERVER_AFTER_FILTER_CHAIN = serverAfterFilterChain;
            SERVER_BEFORE_FILTER_CHAIN = serverBeforeFilterChain;
//            serverFilterChain.addServerFilter((IServerFilter) iServerFilterClass.newInstance());
        }
//        SERVER_FILTER_CHAIN = serverFilterChain;
//        ServerConfig serverConfig = PropertiesBootstrap.loadServerConfigFromLocal();
//        this.setServerConfig(serverConfig);
//        String serverSerialize = serverConfig.getServerSerialize();
//        switch (serverSerialize) {
//            case JDK_SERIALIZE_TYPE:
//                SERVER_SERIALIZE_FACTORY = new JdkSerializeFactory();
//                break;
//            case FAST_JSON_SERIALIZE_TYPE:
//                SERVER_SERIALIZE_FACTORY = new FastJsonSerializeFactory();
//                break;
//            case HESSIAN2_SERIALIZE_TYPE:
//                SERVER_SERIALIZE_FACTORY = new HessianSerializeFactory();
//                break;
//            case KRYO_SERIALIZE_TYPE:
//                SERVER_SERIALIZE_FACTORY = new KryoSerializeFactory();
//                break;
//            default:
//                throw new RuntimeException("no match serialize type for" + serverSerialize);
//        }
////        System.out.println("serverSerialize is "+serverSerialize);
//        SERVER_CONFIG = serverConfig;
//        ServerFilterChain serverFilterChain = new ServerFilterChain();
//        serverFilterChain.addServerFilter(new ServerLogFilterImpl());
//        serverFilterChain.addServerFilter(new ServerTokenFilterImpl());
//        SERVER_FILTER_CHAIN = serverFilterChain;
    }

    /**
     * 将服务注册到服务器
     * @param serviceBean
     */
//    private void registyService(Object serviceBean) {
//        // 如果这个service实现类没有接口，抛出异常
//        if (serviceBean.getClass().getInterfaces().length == 0) {
//            throw new RuntimeException("service must had interfaces! ");
//        }
//
//        // service实现类不能实现多个接口
//        Class[] interfaces = serviceBean.getClass().getInterfaces();
//        if (interfaces.length > 1) {
//            throw new RuntimeException("service must only have one interfaces!");
//        }
//
//        Class interfaceClass = interfaces[0];
//        // 需要注册的对象统一放在一个MAP集合中进行管理 接口名 -> 实现类
//        PROVIDER_CLASS_MAP.put(interfaceClass.getName(), serviceBean);
//    }

    /**
     * 启动服务
     * 在创建ServerBootstrap类实例前，
     * 先创建两个EventLoopGroup，它们实际上是两个独立的Reactor线程池，
     * bossGroup负责接收客户端的连接，
     * workerGroup负责处理IO相关的读写操作，或者执行系统task、定时task等。
     * <p>
     * ChannelOptions: https://netty.io/4.1/api/io/netty/channel/ChannelOption.html
     */
    public void startApplication() throws InterruptedException {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.option(ChannelOption.SO_SNDBUF, 16 * 1024)
                .option(ChannelOption.SO_RCVBUF, 16 * 1024)
                .option(ChannelOption.SO_KEEPALIVE, true);

        // 服务端采用单一长连接的模式，这里所支持的最大连接数应该和机器本身的性能有关
        // 连接防护的handler应该绑定在Main-Reactor上
        bootstrap.handler(new MaxConnectionLimitHandler(serverConfig.getMaxConnections()));
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                System.out.println("初始化provider过程");
                ByteBuf delimiter = Unpooled.copiedBuffer(DEFAULT_DECODE_CHAR.getBytes());
                ch.pipeline().addLast(new DelimiterBasedFrameDecoder(serverConfig.getMaxServerRequestData(), delimiter));
                ch.pipeline().addLast(new RpcEncoder());
                ch.pipeline().addLast(new RpcDecoder());
                // 这里面需要注意出现堵塞的情况发生，建议将核心业务内容分配给业务线程池处理
                ch.pipeline().addLast(new ServerHandler());
            }
        });
        this.batchExportUrl();
        //开始准备接收请求的任务
        SERVER_CHANNEL_DISPATCHER.startDataConsume();
        bootstrap.bind(serverConfig.getServerPort()).sync();
        IS_STARTED = true;
        log.info("[startApplication] server is started!");
    }

    /**
     * 为了将服务端的具体服务都暴露到注册中心，方便客户端进行调用。
     */
    public void batchExportUrl() {
        Thread task = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (URL url : PROVIDER_URL_SET) {
                    REGISTRY_SERVICE.register(url);
                }
            }
        });
        task.start();
    }
}