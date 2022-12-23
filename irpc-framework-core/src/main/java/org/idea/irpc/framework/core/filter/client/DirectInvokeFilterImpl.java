package org.idea.irpc.framework.core.filter.client;

import org.idea.irpc.framework.core.common.ChannelFutureWrapper;
import org.idea.irpc.framework.core.common.protocol.RpcInvocation;
import org.idea.irpc.framework.core.common.utils.CommonUtils;
import org.idea.irpc.framework.core.filter.IClientFilter;

import java.util.Iterator;
import java.util.List;

/**
 * ip直连过滤器
 * @Author : Ruoyi Chen
 * @create 2022/12/23 16:26
 */
public class DirectInvokeFilterImpl implements IClientFilter {
    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        String url = (String) rpcInvocation.getAttachments().get("url");
        if (CommonUtils.isEmpty(url)) {
            return;
        }
        Iterator<ChannelFutureWrapper> channelFutureWrapperIterator = src.iterator();
        while (channelFutureWrapperIterator.hasNext()) {
            ChannelFutureWrapper channelFutureWrapper = channelFutureWrapperIterator.next();
            String addr = channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort();
            if (!addr.equals(url)) {
                channelFutureWrapperIterator.remove();
            }
        }
        if (CommonUtils.isEmptyList(src)) {
            throw new RuntimeException("no match provider url for [" + url + "]");
        }
    }
}
