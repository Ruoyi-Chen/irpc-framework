package org.idea.irpc.framework.core.common.utils;

import org.idea.irpc.framework.core.common.ChannelFutureWrapper;

import java.util.List;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/17 14:20
 */
public class CommonUtils {

    public static boolean isEmptyList(List list) {
        if (list == null || list.size() == 0) {
            return true;
        }
        return false;
    }

    public static boolean isNotEmptyList(List list) {
        return !isEmptyList(list);
    }
}
