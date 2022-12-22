package org.idea.irpc.framework.core.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.idea.irpc.framework.core.serialize.SerializeFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Kryo是一种非常成熟的序列化实现，
 * 已经在Twitter、Groupon、Yahoo以及多个著名开源项目（如Hive、Storm）中广泛的使用，
 * 其性能要比Hessian更加优秀。
 * 如果是在选择序列化技术方面，比较推荐使用Kryo技术。
 *
 * @Author : Ruoyi Chen
 * @create 2022/12/22 16:48
 */
public class KryoSerializeFactory implements SerializeFactory {

    private static final ThreadLocal<Kryo> kryos = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            return kryo;
        }
    };

    @Override
    public <T> byte[] serialize(T t) {
        Output output = null;
        try {
            Kryo kryo = kryos.get();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            output = new Output(byteArrayOutputStream);
            kryo.writeClassAndObject(output, t);
            return output.toBytes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        Input input = null;
        try {
            Kryo kryo = kryos.get();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            input = new Input(byteArrayInputStream);
            return (T) kryo.readClassAndObject(input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }
}
