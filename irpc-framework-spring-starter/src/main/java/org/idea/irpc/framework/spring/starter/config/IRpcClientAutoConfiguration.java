package org.idea.irpc.framework.spring.starter.config;

import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.client.Client;
import org.idea.irpc.framework.core.client.ConnectionHandler;
import org.idea.irpc.framework.core.client.RpcReference;
import org.idea.irpc.framework.core.client.RpcReferenceWrapper;
import org.idea.irpc.framework.spring.starter.common.IRpcReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/28 16:55
 */
@Slf4j
public class IRpcClientAutoConfiguration implements BeanPostProcessor, ApplicationListener<ApplicationReadyEvent> {
    private static RpcReference rpcReference = null;
    private static Client client = null;
    private volatile boolean needInitClient = false;
    private volatile boolean hasInitClientConfig = false;

    /**
     * postProcessAfterInitialization 方法会在Bean的初始化方法（init-method）被容器调用之后执行。
     *
     * postProcessAfterInitialization 方法的返回值会被Spring容器作为处理后的Bean注册到容器中。
     * 如果你在postProcessAfterInitialization 方法中重新构造了一个Bean进行返回，
     * 而不是返回参数中的bean；
     * 那么你返回的Bean将会被注册到Spring容器中。
     * 而原来在Spring中配置的Bean（被Spring实例化的Bean）将会被覆盖。
     *
     * https://www.jianshu.com/p/dcc990d47df1
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(IRpcReference.class)) {
                if (!hasInitClientConfig) {
                    client = new Client();
                    try {
                        rpcReference = client.initClientApplication();
                    } catch (Exception e) {
                        log.info("[IRpcClientAutoConfiguration] postProcessAfterInitialization has error ",e);
                        throw new RuntimeException(e);
                    }
                    hasInitClientConfig = true;
                }
                needInitClient = true;
                IRpcReference iRpcReference = field.getAnnotation(IRpcReference.class);
                try{
                    field.setAccessible(true);
                    Object refObj = field.get(bean);

                    RpcReferenceWrapper rpcReferenceWrapper = new RpcReferenceWrapper();
                    rpcReferenceWrapper.setAimClass(field.getType());
                    rpcReferenceWrapper.setGroup(iRpcReference.group());
                    rpcReferenceWrapper.setServiceToken(iRpcReference.serviceToken());
                    rpcReferenceWrapper.setUrl(iRpcReference.url());
                    rpcReferenceWrapper.setTimeOut(iRpcReference.timeOut());

                    // 失败重试次数
                    rpcReferenceWrapper.setRetry(iRpcReference.retry());
                    rpcReferenceWrapper.setAsync(iRpcReference.async());

                    refObj = rpcReference.get(rpcReferenceWrapper);
                    field.set(bean, refObj);
                    client.doSubscribeService(field.getType());
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        if (needInitClient && client != null) {
            log.info(" ================== [{}] started success ================== ",client.getClientConfig().getApplicationName());
            ConnectionHandler.setBootstrap(client.getBootstrap());
            client.doConnectServer();
            client.startClient();
        }
    }
}
