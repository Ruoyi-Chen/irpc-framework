package org.idea.irpc.framework.core.router;

import org.idea.irpc.framework.core.common.ChannelFutureWrapper;
import org.idea.irpc.framework.core.registry.URL;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.*;

/**
 * 随机路由
 * @Author : Ruoyi Chen
 * @create 2022/12/21 15:58
 */
public class RandomRouterImpl implements IRouter{
    @Override
    public void refreshRouterArr(Selector selector) {
        // 获取服务提供者的数目
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(selector.getProviderServiceName());
        ChannelFutureWrapper[] arr = new ChannelFutureWrapper[channelFutureWrappers.size()];
        // 提前生成调用先后顺序的随机数组
        int[] result = createRandomIndex(arr.length);
        // 生成对应服务集群的每台机器的调用顺序
        for (int i = 0; i < result.length; i++) {
            arr[i] = channelFutureWrappers.get(result[i]);
        }
        SERVICE_ROUTER_MAP.put(selector.getProviderServiceName(), arr);

        URL url = new URL();
        url.setServiceName(selector.getProviderServiceName());
        // 更新权重
        IROUTER.updateWeight(url);
    }

    private int[] createRandomIndex(int length) {
        int[] arrInt = new int[length];
        Random random = new Random();
        Arrays.fill(arrInt, -1);

        int idx = 0;
        while (idx < arrInt.length) {
            int num = random.nextInt(length);
            // 如果数组中不包含这个元素则赋值给数组
            if (!contains(arrInt, num)) {
                arrInt[idx++] = num;
            }
        }
        return arrInt;
    }

    private boolean contains(int[] arrInt, int key) {
        for (int i = 0; i < arrInt.length; i++) {
            if (arrInt[i] == key) return true;
        }
        return false;
    }

    /**
     * 随机路由层内部对外暴露的核心方法是select函数，
     * 每次外界调用服务的时候都是通过这个函数去获取下一次调用的provider信息。
     * @param selector
     * @return
     */
    @Override
    public ChannelFutureWrapper select(Selector selector) {
        /**
         * 在服务的初始化阶段，程序内部自动将下一次需要调用的服务信息存放在了一个Cache集合中
         */
        return CHANNEL_FUTURE_POLLING_REF.getChannelFutureWrapper(selector.getChannelFutureWrappers());
    }

    /**
     * 关于权重更新的设计思路其实也比较简单。每个服务提供者在注册的时候都是默认权重为100，然后将权重值写入到注册中心上。
     * 当zk上的权重数值发生变化的时候，则会通知客户端进行更新。而这部分的更新机制也是通过前边章节所讲解的事件思路来进行设计
     * @param url
     */
    @Override
    public void updateWeight(URL url) {
        // 服务节点的权重
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(url.getServiceName());
        Integer[] weightArr = createWeightArr(channelFutureWrappers);
        Integer[] finalArr = createRandomArr(weightArr);
        ChannelFutureWrapper[] finalChannelFutureWrappers = new ChannelFutureWrapper[finalArr.length];
        for (int j = 0; j < finalArr.length; j++) {
            finalChannelFutureWrappers[j] = channelFutureWrappers.get(finalArr[j]);
        }
        SERVICE_ROUTER_MAP.put(url.getServiceName(), finalChannelFutureWrappers);
    }

    /**
     * 打乱数组顺序
     * @param arr
     * @return
     */
    private Integer[] createRandomArr(Integer[] arr) {
        int total = arr.length;
        Random random = new Random();
        for (int i = 0; i < total; i++) {
            int j = random.nextInt(total);
            if (i == j) continue;
            // swap
            int tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
        return arr;
    }

    /**
     *
     * @param channelFutureWrappers
     * @return
     */
    private static Integer[] createWeightArr(List<ChannelFutureWrapper> channelFutureWrappers) {
        ArrayList<Integer> weightArr = new ArrayList<>();
        for (int k = 0; k < channelFutureWrappers.size(); k++) {
            Integer weight = channelFutureWrappers.get(k).getWeight();
            int c = weight / 100; // 权重/100是多少就调用多少次
            for (int i = 0; i < c; i++) {
                weightArr.add(k);
            }
        }
        Integer[] arr = new Integer[weightArr.size()];
        return weightArr.toArray(arr);
    }

    public static void main(String[] args) {
        List<ChannelFutureWrapper> channelFutureWrappers = new ArrayList<>();
        channelFutureWrappers.add(new ChannelFutureWrapper(null, null, 100));
        channelFutureWrappers.add(new ChannelFutureWrapper(null, null, 200));
        channelFutureWrappers.add(new ChannelFutureWrapper(null, null, 9300));
        channelFutureWrappers.add(new ChannelFutureWrapper(null, null, 400));
        Integer[] r = createWeightArr(channelFutureWrappers);
        System.out.println(r);
    }
}
