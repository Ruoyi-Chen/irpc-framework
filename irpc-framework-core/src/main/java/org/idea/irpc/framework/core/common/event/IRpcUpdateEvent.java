package org.idea.irpc.framework.core.common.event;

/**
 * 定义一个节点更新事件
 *
 * 当zookeeper的某个节点发生数据变动的时候，
 * 就会发送一个变更事件，然后由对应的监听器去捕获这些数据并做处理。
 *
 * @Author : Ruoyi Chen
 * @create 2022/12/17 14:09
 */
public class IRpcUpdateEvent implements IRpcEvent{

    private Object data;

    public IRpcUpdateEvent(Object data) {
        this.data = data;
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public IRpcEvent setData(Object data) {
        this.data = data;
        return this;
    }
}
