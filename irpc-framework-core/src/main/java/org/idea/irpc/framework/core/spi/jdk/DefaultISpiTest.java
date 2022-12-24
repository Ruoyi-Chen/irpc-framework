package org.idea.irpc.framework.core.spi.jdk;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/24 17:09
 */
public class DefaultISpiTest implements ISpiTest{
    @Override
    public void doTest() {
        System.out.println("执行测试方法");
    }
}
