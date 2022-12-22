package org.idea.irpc.framework.core.common.event;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/22 17:55
 */
public class IRpcDestroyEvent implements IRpcEvent {

    private Object data;

    public IRpcDestroyEvent(Object data) {
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
