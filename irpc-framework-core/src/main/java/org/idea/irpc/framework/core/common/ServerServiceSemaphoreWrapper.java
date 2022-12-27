package org.idea.irpc.framework.core.common;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.Semaphore;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/27 22:54
 */
@Data
public class ServerServiceSemaphoreWrapper {
    private Semaphore semaphore;
    private int maxNums;
    public ServerServiceSemaphoreWrapper(int maxNums) {
        this.maxNums = maxNums;
        this.semaphore = new Semaphore(maxNums);
    }
}
