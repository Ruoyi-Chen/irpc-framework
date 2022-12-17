package org.idea.irpc.framework.core.common.event;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/17 14:13
 */
public interface IRpcListener<T> {
    void callback(Object t);
}
