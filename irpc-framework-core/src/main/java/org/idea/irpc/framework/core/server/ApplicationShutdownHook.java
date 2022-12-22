package org.idea.irpc.framework.core.server;

import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.event.IRpcDestroyEvent;
import org.idea.irpc.framework.core.common.event.IRpcListenerLoader;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/22 17:52
 */
@Slf4j
public class ApplicationShutdownHook {
    public static void registryShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("[registryShutdownHook] ==== ");
                IRpcListenerLoader.sendSyncEvent(new IRpcDestroyEvent("destroy"));
                System.out.println("destroy");
            }
        }));
    }
}
