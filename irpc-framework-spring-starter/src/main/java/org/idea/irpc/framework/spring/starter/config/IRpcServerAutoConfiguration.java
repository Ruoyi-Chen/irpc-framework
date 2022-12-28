package org.idea.irpc.framework.spring.starter.config;

import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.event.IRpcListenerLoader;
import org.idea.irpc.framework.core.common.event.listener.IRpcListener;
import org.idea.irpc.framework.core.server.ApplicationShutdownHook;
import org.idea.irpc.framework.core.server.Server;
import org.idea.irpc.framework.core.server.ServiceWrapper;
import org.idea.irpc.framework.spring.starter.common.IRpcReference;
import org.idea.irpc.framework.spring.starter.common.IRpcService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.annotation.*;
import java.util.Map;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/28 16:55
 */
@Slf4j
public class IRpcServerAutoConfiguration implements InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        Server server = null;
        // 获取容器中有IRpcReference注解的bean
        Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(IRpcReference.class);
        if (beanMap.size() == 0) {
            // 说明当前应用内部不需要对外暴露服务
            return;
        }
        printBanner();
        long begin = System.currentTimeMillis();
        server = new Server();
        server.initServerConfig();
        // 初始化监听器
        IRpcListenerLoader iRpcListenerLoader = new IRpcListenerLoader();
        iRpcListenerLoader.init();
        // 对每一个bean
        for (String beanName : beanMap.keySet()) {
            Object bean = beanMap.get(beanName);
            IRpcService iRpcService = bean.getClass().getAnnotation(IRpcService.class);
            ServiceWrapper serviceWrapper = new ServiceWrapper(bean, iRpcService.group());
            serviceWrapper.setServiceToken(iRpcService.serviceToken());
            serviceWrapper.setLimit(iRpcService.limit());
            server.exportService(serviceWrapper);
            log.info(">>>>>>>>>>>>>>> [irpc] {} export success! >>>>>>>>>>>>>>> ",beanName);
        }
        long end = System.currentTimeMillis();
        ApplicationShutdownHook.registryShutdownHook();
        server.startApplication();
        log.info(" ================== [{}] started success in {}s ================== ",server.getServerConfig().getApplicationName(),((double)end-(double)begin)/1000);
    }

    private void printBanner() {
        System.out.println();
        System.out.println("====================================================");
        System.out.println("|||----------CHERRY IRpc Starting Now! ----------|||");
        System.out.println("====================================================");
        System.out.println("源代码地址: https://github.com/Ruoyi-Chen/irpc-framework.git");
        System.out.println("version: 1.0.0");
        System.out.println();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


}
