package org.idea.irpc.framework.core.serialize.fastjson;

import com.alibaba.fastjson.JSON;
import org.idea.irpc.framework.core.serialize.SerializeFactory;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/22 16:55
 */
public class FastJsonSerializeFactory implements SerializeFactory {
    @Override
    public <T> byte[] serialize(T t) {
        String jsonString = JSON.toJSONString(t);
        return jsonString.getBytes();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        return JSON.parseObject(new String(data), clazz);
    }
}
