package org.idea.irpc.framework.core.dispatcher;

import org.idea.irpc.framework.core.common.exception.IRpcException;
import org.idea.irpc.framework.core.common.protocol.RpcInvocation;
import org.idea.irpc.framework.core.common.protocol.RpcProtocol;
import org.idea.irpc.framework.core.server.ServerChannelReadData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.*;

import static org.idea.irpc.framework.core.common.cache.CommonServerCache.*;

/**
 * 定义了一个请求分发器ServerChannelDispatcher，
 * 这个分发器的内部存有一条阻塞队列 RPC_DATA_QUEUE。
 * 另外分发器的内部还有一个线程对象ServerJobCoreHandle专门负责将队列的数据读出，然后提交到业务线程池去执行。
 *
 * @Author : Ruoyi Chen
 * @create 2022/12/25 19:52
 */
public class ServerChannelDispatcher {
    private BlockingQueue<ServerChannelReadData> RPC_DATA_QUEUE;
    private ExecutorService executorService;

    public ServerChannelDispatcher() {
    }

    public void init(int queueSize, int bizThreadNums) {
        RPC_DATA_QUEUE = new ArrayBlockingQueue<>(queueSize);
        executorService = new ThreadPoolExecutor(
                bizThreadNums, bizThreadNums,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(512));
    }

    public void add(ServerChannelReadData serverChannelReadData) {
        RPC_DATA_QUEUE.add(serverChannelReadData);
    }

    class ServerJobCoreHandle implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    // 1. 从等待队列中取出待处理数据交给线程池
                    ServerChannelReadData serverChannelReadData = RPC_DATA_QUEUE.take();
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            RpcProtocol rpcProtocol = serverChannelReadData.getRpcProtocol();
                            RpcInvocation rpcInvocation = SERVER_SERIALIZE_FACTORY.deserialize(rpcProtocol.getContent(), RpcInvocation.class);
                            try {
                                //前置过滤器
                                // 2. 取出rpcProtocol，并反序列化成RpcInvocation，并放入过滤链过滤
                                SERVER_FILTER_CHAIN.doFilter(rpcInvocation);
                            } catch (Exception cause) {
                                // 针对自定义异常进行捕获，并且直接返回异常信息给到客户端，然后打印结果
                                if (cause instanceof IRpcException) {
                                    // 将异常转为自定义异常，将其中的RpcInvocation发出去
                                    IRpcException rpcException = (IRpcException) cause;
                                    RpcInvocation reqParam = rpcException.getRpcInvocation();
                                    rpcInvocation.setE(rpcException);
                                    byte[] body = SERVER_SERIALIZE_FACTORY.serialize(reqParam);
                                    RpcProtocol respRpcProtocol = new RpcProtocol(body);
                                    serverChannelReadData.getChannelHandlerContext().writeAndFlush(respRpcProtocol);
                                    return;
                                }
                            }
                            // 3. 获取该任务需要的服务，执行其需要的方法
                            Object aimObject = PROVIDER_CLASS_MAP.get(rpcInvocation.getTargetServiceName());
                            Method[] methods = aimObject.getClass().getDeclaredMethods();
                            Object result = null;
                            for (Method method : methods) {
                                if (method.getName().equals(rpcInvocation.getTargetMethod())) {
                                    if (method.getReturnType().equals(Void.TYPE)) {
                                        try {
                                            method.invoke(aimObject, rpcInvocation.getArgs());
                                        } catch (Exception e) {
                                            // 业务异常
                                            rpcInvocation.setE(e);
                                        }
                                    } else {
                                        try {
                                            result = method.invoke(aimObject, rpcInvocation.getArgs());
                                        } catch (Exception e) {
                                            // 业务异常
                                            rpcInvocation.setE(e);
                                        }
                                    }
                                    break;
                                }
                            }
                            // 4. 将结果写入rpcInvocation的响应中
                            rpcInvocation.setResponse(result);
                            // 后置过滤器
                            SERVER_FILTER_CHAIN.doFilter(rpcInvocation);
                            // 5. 将包含响应结果的数据序列化成RpcProtocol，并发回去
                            RpcProtocol respRpcProtocol = new RpcProtocol(SERVER_SERIALIZE_FACTORY.serialize(rpcInvocation));
                            serverChannelReadData.getChannelHandlerContext().writeAndFlush(respRpcProtocol);

                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void startDataConsume() {
        Thread thread = new Thread(new ServerJobCoreHandle());
        thread.start();
    }

}
