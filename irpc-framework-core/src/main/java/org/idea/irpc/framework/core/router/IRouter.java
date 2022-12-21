package org.idea.irpc.framework.core.router;

import org.idea.irpc.framework.core.common.ChannelFutureWrapper;
import org.idea.irpc.framework.core.registry.URL;


/**
 * @Author : Ruoyi Chen
 * @create 2022/12/21 15:53
 */
public interface IRouter {
    /**
     * 刷新路由数组
     * @param selector
     */
    void refreshRouterArr(Selector selector);

    /**
     * 获取到请求的连接通道
     * @param selector
     * @return
     */
    ChannelFutureWrapper select(Selector selector);

    /**
     * 更新权重信息
     * @param url
     */
    void updateWeight(URL url);
}
