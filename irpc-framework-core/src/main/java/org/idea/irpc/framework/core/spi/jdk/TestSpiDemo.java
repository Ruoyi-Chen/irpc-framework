package org.idea.irpc.framework.core.spi.jdk;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/24 17:12
 */
public class TestSpiDemo {
    public static void doTest(ISpiTest iSpiTest) {
        System.out.println("begin");
        iSpiTest.doTest();
        System.out.println("end");
    }

    /**
     * JDK内置提供的ServiceLoader会自动帮助我们去加载/META-INF/services/目录下边的文件，并且将其转换为具体实现类。
     * org.idea.irpc.framework.core.spi.jdk.ISpiTest
     *
     *  * JDK内部的ServiceLoader加载流程大致为：
     *  * 调用load函数
     *  * ->再调用到reload方法，
     *  *      并且在reload方法里面触发一个叫做LazyIterator的类，
     *  *      这个类实现了迭代器的Iterator接口。
     *
     *      public void reload() {
     *         providers.clear();
     *         lookupIterator = new LazyIterator(service, loader);
     *      }
     * @param args
     */
    public static void main(String[] args) {
        ServiceLoader<ISpiTest> serviceLoader = ServiceLoader.load(ISpiTest.class);
        Iterator<ISpiTest> iSpiTestIterator = serviceLoader.iterator();
        while (iSpiTestIterator.hasNext()) {
            ISpiTest iSpiTest = iSpiTestIterator.next();
            TestSpiDemo.doTest(iSpiTest);
        }
    }
}
