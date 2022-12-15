package common.cache;

import common.protocol.RpcInvocation;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/15 17:02
 */
public class CommonClientCache {
    public static BlockingQueue<RpcInvocation> SEND_QUEUE = new ArrayBlockingQueue<RpcInvocation>(100);

    public static Map<String, Object> RESP_MAP = new ConcurrentHashMap<String, Object>();

}
