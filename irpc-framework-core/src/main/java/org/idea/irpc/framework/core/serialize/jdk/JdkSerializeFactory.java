package org.idea.irpc.framework.core.serialize.jdk;

import org.idea.irpc.framework.core.serialize.SerializeFactory;

import java.io.*;

/**
 * Jdk的序列化技术是一项早期就带有的序列化技术，可以将对象转换成字节数组流，供网络之间发送数据使用，
 * 但是这类序列化技术对于跨语言的兼容性并不足够友好。
 * 而且在生成的字节码流方面也并不是那么精简。
 *
 * 在早期市面上没有那么多序列化框架出现的时候会比较受众，
 * 但是随着序列化技术的日益丰富，开发人员们也开始在逐渐替换这项技术。
 *
 * @Author : Ruoyi Chen
 * @create 2022/12/22 16:36
 */
public class JdkSerializeFactory implements SerializeFactory {
    @Override
    public <T> byte[] serialize(T t) {
        byte[] data = null;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(os);
            output.writeObject(t);
            output.flush();
            output.close();
            data = os.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        ByteArrayInputStream is = new ByteArrayInputStream(data);
        try {
            ObjectInputStream input = new ObjectInputStream(is);
            Object result = input.readObject();
            return (T) result;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
