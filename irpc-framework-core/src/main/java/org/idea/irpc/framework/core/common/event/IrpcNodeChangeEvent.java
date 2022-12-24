package org.idea.irpc.framework.core.common.event;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/21 16:43
 */
public class IrpcNodeChangeEvent implements IRpcEvent {
    private Object data;
    public IrpcNodeChangeEvent(Object data) {
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
