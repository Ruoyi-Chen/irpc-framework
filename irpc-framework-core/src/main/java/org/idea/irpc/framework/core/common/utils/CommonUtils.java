package org.idea.irpc.framework.core.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.ChannelFutureWrapper;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/17 14:20
 */
@Slf4j
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

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static String getIpAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (networkInterfaces.hasMoreElements()){
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()){
                    continue;
                } else {
                    Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        if (ip != null && ip instanceof Inet4Address) {
                            return ip.getHostAddress();
                        }
                    }
                }
            }

        } catch (SocketException e) {
            log.info("IP地址获取失败" + e.toString());
        }
        return "";
    }
}
