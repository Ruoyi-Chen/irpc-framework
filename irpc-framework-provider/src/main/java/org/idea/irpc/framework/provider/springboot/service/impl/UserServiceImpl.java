package org.idea.irpc.framework.provider.springboot.service.impl;

import org.idea.irpc.framework.interfaces.UserService;
import org.idea.irpc.framework.spring.starter.common.IRpcService;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/28 18:05
 */
@IRpcService
public class UserServiceImpl implements UserService {
    public void test() {
        System.out.println("test");
    }
}
