package org.idea.irpc.framework.provider.springboot.service.impl;

import org.idea.irpc.framework.interfaces.OrderService;
import org.idea.irpc.framework.spring.starter.common.IRpcService;

import java.util.Arrays;
import java.util.List;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/28 18:06
 */
@IRpcService(serviceToken = "order-token", group = "order-group", limit = 2)
public class OrderServiceImpl implements OrderService {
    @Override
    public List<String> getOrderNoList() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Arrays.asList("item1", "item2");
    }

    // 测试大数据包传输是否有异常
    @Override
    public String testMaxData(int i) {
        StringBuffer buffer = new StringBuffer();
        for (int j = 0; j < i; j++) {
            buffer.append("1");
        }
        return buffer.toString();
    }
}
