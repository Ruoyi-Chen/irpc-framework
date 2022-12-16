package org.idea.irpc.framework.core.common.protocol;

import java.io.Serializable;
import java.util.Arrays;

import static org.idea.irpc.framework.core.common.constants.RpcConstants.MAGIC_NUMBER;

public class RpcProtocol implements Serializable {
    /**
     * serialVersionUID作用是序列化时保持版本的兼容性，即在版本升级时反序列化仍保持对象的唯一性。
     * serialVersionUID适用于Java的序列化机制。
     * 简单来说，Java的序列化机制是通过判断类的serialVersionUID来验证版本一致性的。
     * 在进行反序列化时，JVM会把传来的字节流中的serialVersionUID与本地相应实体类的serialVersionUID进行比较，
     * 如果相同就认为是一致的，可以进行反序列化，
     * 否则就会出现序列化版本不一致的异常，即是InvalidCastException。
     * https://blog.51cto.com/u_12302929/2971467
     */
    private static final long serialVersionUID = 5359096060555795690L;

    /**
     * 魔法数，主要是在做服务通讯的时候定义的一个安全检测，确认当前请求的协议是否合法。
     */
    private short magicNumber = MAGIC_NUMBER;
    /**
     * 协议传输核心数据的长度。
     * 这里将长度单独拎出来设置有个好处，当服务端的接收能力有限，可以对该字段进行赋值。
     * 当读取到的网络数据包中的contentLength字段已经超过预期值的话，就不会去读取content字段。
     */
    private int contentLength;
    /**
     * 核心的传输数据，这里核心的传输数据主要是[请求的服务名称，请求服务的方法名称，请求参数内容]。
     * 为了方便后期扩展，这些核心的请求数据都统一封装到了RpcInvocation对象当中。
     */
    private byte[] content;

    public RpcProtocol(byte[] content) {
        this.contentLength = content.length;
        this.content = content;
    }

    public short getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(short magicNumber) {
        this.magicNumber = magicNumber;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "RpcProtocol{" +
                "contentLength=" + contentLength +
                ", content=" + Arrays.toString(content) +
                '}';
    }
}
