package org.idea.irpc.framework.core.common.event;

/**
 * 定义一个抽象的事件，该事件会用于装载需要传递的数据信息
 *
 * @Author : Ruoyi Chen
 * @create 2022/12/17 14:09
 */
public interface IRpcEvent {
    Object getData();
    IRpcEvent setData(Object data);
}
