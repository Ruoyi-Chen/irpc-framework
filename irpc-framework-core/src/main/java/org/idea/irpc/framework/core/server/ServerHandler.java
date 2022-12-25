package org.idea.irpc.framework.core.server;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandler;
import org.idea.irpc.framework.core.common.protocol.RpcInvocation;
import org.idea.irpc.framework.core.common.protocol.RpcProtocol;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Method;

import static org.idea.irpc.framework.core.common.cache.CommonServerCache.*;

/**
 * 服务处理
 *  非共享模式，不存在线程安全问题
 *
 * @Author : Ruoyi Chen
 * @create 2022/12/15 16:25
 */
@ChannelHandler.Sharable
public class ServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ServerChannelReadData serverChannelReadData = new ServerChannelReadData();
        serverChannelReadData.setRpcProtocol((RpcProtocol) msg);
        serverChannelReadData.setChannelHandlerContext(ctx);
        // 放入channel分发器
        SERVER_CHANNEL_DISPATCHER.add(serverChannelReadData);
    }
    //    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        // 服务端接收数据的时候统一以RpcProtocol协议的格式接收
//        // 当数据抵达这个位置的时候，已经是以RpcProtocol的格式展现了。因此可以直接强转
//        // 传来的content是byte[]格式，将其转成json字符串，然后转成RpcInvocation对象
//        RpcProtocol rpcProtocol = (RpcProtocol) msg;
//        String json = new String(rpcProtocol.getContent(), 0, rpcProtocol.getContentLength());
//        RpcInvocation rpcInvocation = JSON.parseObject(json, RpcInvocation.class);
//
//        // 执行过滤链路
//        // 在 ChannelInboundHandlerAdapter 内部加入过滤链说明此事请求数据已经落入到了server端的业务线程池中，
//        // 接下来需要通过责任链的每一个环节进行校对，最终确认是否可以执行目标函数。
//        SERVER_FILTER_CHAIN.doFilter(rpcInvocation);
//
//        //这里的PROVIDER_CLASS_MAP就是一开始预先在启动时候存储的Bean集合
//        // 1. 根据传来的service name，获取目标服务接口类对象
//        Object aimService = PROVIDER_CLASS_MAP.get(rpcInvocation.getTargetServiceName());
//        // 2. 利用反射，获取到这个服务类的所有public方法
//        Method[] methods = aimService.getClass().getDeclaredMethods();
//        Object result = null;
//        for (Method method : methods) {
//            if (method.getName().equals(rpcInvocation.getTargetMethod())){
//                // 通过反射找到目标方法对象，然后执行目标方法并返回对应值
//                if (method.getReturnType().equals(Void.TYPE)) {
//                    method.invoke(aimService, rpcInvocation.getArgs());
//                } else {
//                    result = method.invoke(aimService, rpcInvocation.getArgs());
//                }
//                // 找到了要实现的方法，就可以结束了
//                break;
//            }
//        }
//        rpcInvocation.setResponse(result);
//        RpcProtocol respRpcProtocol = new RpcProtocol(SERVER_SERIALIZE_FACTORY.serialize(rpcInvocation));
//        ctx.writeAndFlush(respRpcProtocol);
//    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        Channel channel = ctx.channel();
        if (channel.isActive()) {
            ctx.close();
        }
    }
}
