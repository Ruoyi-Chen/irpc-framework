package org.idea.irpc.framework.core.common.event;

import org.idea.irpc.framework.core.common.event.listener.IRpcListener;
import org.idea.irpc.framework.core.common.event.listener.ProviderNodeDataChangeListener;
import org.idea.irpc.framework.core.common.event.listener.ServiceDestroyListener;
import org.idea.irpc.framework.core.common.event.listener.ServiceUpdateListener;
import org.idea.irpc.framework.core.common.utils.CommonUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 发送事件
 *
 * @Author : Ruoyi Chen
 * @create 2022/12/17 14:12
 */
public class IRpcListenerLoader {
    private static List<IRpcListener> iRpcListenerList = new ArrayList<>();

    private static ExecutorService eventThreadPool = Executors.newFixedThreadPool(2);

    public static void registerListener(IRpcListener iRpcListener) {
        iRpcListenerList.add(iRpcListener);
    }

    public static void sendSyncEvent(IRpcEvent iRpcEvent) {
        System.out.println(iRpcListenerList);
        if (CommonUtils.isEmptyList(iRpcListenerList)) {
            return;
        }
        for (IRpcListener iRpcListener : iRpcListenerList) {
            Class<?> type = getInterfaceT(iRpcListener);
            if (type.equals(iRpcEvent.getClass())) {
                try {
                    iRpcListener.callback(iRpcEvent.getData());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void init() {
        registerListener(new ServiceUpdateListener());
        registerListener(new ServiceDestroyListener());
        registerListener(new ProviderNodeDataChangeListener());
    }

    /**
     * 获取接口上的泛型T
     * @param o     接口
     * @return
     */
    public static Class<?> getInterfaceT(Object o) {
        Type[] types = o.getClass().getGenericInterfaces();
        ParameterizedType parameterizedType = (ParameterizedType) types[0];
        Type type = parameterizedType.getActualTypeArguments()[0];
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        return null;
    }

    /**
     * 发送事件
     *
     * 如果事件的泛型符合监听器x的泛型，开启一个线程去调用该监听器的回调方法
     * @param iRpcEvent
     */
    public static void sendEvent(IRpcEvent iRpcEvent) {
        if (CommonUtils.isEmptyList(iRpcListenerList)) {
            return;
        }
        for (IRpcListener<?> iRpcListener : iRpcListenerList) {
            Class<?> type = getInterfaceT(iRpcListener);
            if (type.equals(iRpcEvent.getClass())) {
                eventThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            iRpcListener.callback(iRpcEvent.getData());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }
}
