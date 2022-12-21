package org.idea.irpc.framework.core.common.event.listener;

import org.idea.irpc.framework.core.common.ChannelFutureWrapper;
import org.idea.irpc.framework.core.common.event.IRpcListener;
import org.idea.irpc.framework.core.common.event.IrpcNodeChangeEvent;
import org.idea.irpc.framework.core.registry.URL;
import org.idea.irpc.framework.core.registry.zookeeper.ProviderNodeInfo;

import java.util.List;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.CONNECT_MAP;
import static org.idea.irpc.framework.core.common.cache.CommonClientCache.IROUTER;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/21 16:47
 */
public class ProviderNodeDataChangeListener implements IRpcListener<IrpcNodeChangeEvent> {
    @Override
    public void callback(Object t) {
        ProviderNodeInfo providerNodeInfo = (ProviderNodeInfo) t;
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(providerNodeInfo.getServiceName());
        for (ChannelFutureWrapper channelFutureWrapper : channelFutureWrappers){
            String address = channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort();
            if (address.equals(providerNodeInfo.getAddress())) {
                // 修改权重
                channelFutureWrapper.setWeight(providerNodeInfo.getWeight());
                URL url = new URL();
                url.setServiceName(providerNodeInfo.getServiceName());
                // 更新权重 这里对应了RandomRouterImpl类
                IROUTER.updateWeight(url);
                break;
            }
        }
    }
}
