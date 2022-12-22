package org.idea.irpc.framework.core.serialize.hessian;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import org.idea.irpc.framework.core.serialize.SerializeFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * hessian还曾经被著名的Rpc框架Dubbo入选为默认的序列化技术，但是目前从Dubbo的官网来看，
 * 似乎出现了比它更加优秀的序列化技术框架，例如接下来介绍的Kryo。
 *
 * @Author : Ruoyi Chen
 * @create 2022/12/22 16:40
 */
public class HessianSerializeFactory implements SerializeFactory {
    @Override
    public <T> byte[] serialize(T t) {
        byte[] data = null;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Hessian2Output output = new Hessian2Output(os);
            output.writeObject(t);
            output.getBytesOutputStream().flush();
            output.completeMessage();
            output.close();
            data = os.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        if (data == null) {
            return null;
        }
        Object result = null;

        try {
            ByteArrayInputStream is = new ByteArrayInputStream(data);
            Hessian2Input input = new Hessian2Input(is);
            result = input.readObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return (T) result;
    }
}
