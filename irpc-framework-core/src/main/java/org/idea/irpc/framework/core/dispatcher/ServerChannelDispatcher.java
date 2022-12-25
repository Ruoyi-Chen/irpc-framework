package org.idea.irpc.framework.core.dispatcher;

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
                            try {
                                // 2. 取出rpcProtocol，并反序列化成RpcInvocation，并放入过滤链过滤
                                RpcProtocol rpcProtocol = serverChannelReadData.getRpcProtocol();
                                RpcInvocation rpcInvocation = SERVER_SERIALIZE_FACTORY.deserialize(rpcProtocol.getContent(), RpcInvocation.class);
                                SERVER_FILTER_CHAIN.doFilter(rpcInvocation);
                                // 3. 获取该任务需要的服务，执行其需要的方法
                                Object aimObject = PROVIDER_CLASS_MAP.get(rpcInvocation.getTargetServiceName());
                                Method[] methods = aimObject.getClass().getDeclaredMethods();
                                Object result = null;
                                for (Method method : methods) {
                                    if (method.getName().equals(rpcInvocation.getTargetMethod())) {
                                        if (method.getReturnType().equals(Void.TYPE)) {
                                            method.invoke(aimObject, rpcInvocation.getArgs());
                                        } else {
                                            result = method.invoke(aimObject, rpcInvocation.getArgs());
                                        }
                                        break;
                                    }
                                }
                                // 4. 将结果写入rpcInvocation的响应中
                                rpcInvocation.setResponse(result);
                                // 5. 将包含响应结果的数据序列化成RpcProtocol，并发回去
                                RpcProtocol respRpcProtocol = new RpcProtocol(SERVER_SERIALIZE_FACTORY.serialize(rpcInvocation));
                                serverChannelReadData.getChannelHandlerContext().writeAndFlush(respRpcProtocol);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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
