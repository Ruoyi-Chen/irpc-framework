package org.idea.irpc.framework.core.serialize;

/**
 * 在框架层面可以尝试抽象出一个层将各种不同的序列化技术进行整合归纳。
 *
 * @Author : Ruoyi Chen
 * @create 2022/12/22 16:34
 */
public interface SerializeFactory {
    /**
     * 序列化
     * @param t
     * @return
     * @param <T>
     */
    <T> byte[] serialize(T t);

    /**
     * 反序列化
     * @param data
     * @param clazz
     * @return
     * @param <T>
     */
    <T> T deserialize(byte[] data, Class<T> clazz);
}
