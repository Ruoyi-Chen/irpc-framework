package proxy;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/15 16:47
 */
public interface ProxyFactory {
    <T> T getProxy(final Class clazz) throws Throwable;
}
